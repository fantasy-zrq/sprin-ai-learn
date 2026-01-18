package com.zrq.cn.controller;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.zrq.cn.entity.Order;
import com.zrq.cn.utils.AIResponseFormatUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.converter.StructuredOutputConverter;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author zrq
 * 2026/1/15 15:33
 */
@RestController
@RequestMapping("/ai/api/qwen")
@RequiredArgsConstructor
public class QwenController {

    @Qualifier("glmChatClient")
    private final ChatClient glmChatClient;

    @Qualifier("qwenFlashChatClient")
    private final ChatClient qwenFlashChatClient;

    @Qualifier("qwenPlusChatClient")
    private final ChatClient qwenPlusChatClient;

    @Qualifier("qwenMultiClient")
    private final ChatClient qwenMultiClient;

    @Qualifier("simpleVectorStore")
    private final VectorStore simpleVectorStore;

    @Qualifier("postgreVectorStore")
    private final VectorStore postgreVectorStore;

    @GetMapping("/chat")
    public String chat(@RequestParam("userMsg") String userMsg) {
        Map<String, Order> map = qwenPlusChatClient
                .prompt()
                .advisors(new SimpleLoggerAdvisor())
                .user(userMsg)
                .call()
                .entity(new StructuredOutputConverter<Map<String, Order>>() {
                    //如何获取一个结构
                    @Override
                    public String getFormat() {
                        return AIResponseFormatUtil.getFormat();
                    }

                    //结构如何转成map
                    @Override
                    public Map<String, Order> convert(String source) {
                        System.out.println("source = " + source);
                        String json = source.trim();

                        // 剥离 ```json ... ``` 或 ``` ... ``` 代码块
                        if (json.startsWith("```")) {
                            int firstNewline = json.indexOf('\n');
                            int lastFence = json.lastIndexOf("```");
                            if (firstNewline > 0 && lastFence > firstNewline) {
                                json = json.substring(firstNewline + 1, lastFence).trim();
                            }
                        }

                        return JSON.parseObject(
                                json,
                                new TypeReference<Map<String, Order>>() {
                                }
                        );
                    }
                });
        return JSON.toJSONString(map);
    }

    @GetMapping(value = "/stream", headers = "Accept=text/event-stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ChatResponse> chatStream(@RequestParam("conversationId") String conversationId, @RequestParam("userMsg") String userMsg) throws IOException {
        return qwenPlusChatClient.prompt()
                .user(u -> u.text(userMsg)
                )
                .advisors(advisorSpec -> advisorSpec
                        .param(AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY, conversationId)
                        .param(AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .stream()
                .chatResponse();
    }

    @PostMapping("/upload")
    public String uploadRag(@RequestParam("ragFile") MultipartFile ragFile) throws IOException {
        TextReader textReader = new TextReader(new ByteArrayResource(ragFile.getBytes()));
        List<Document> documents = textReader.get();
        documents.forEach(document -> document.getMetadata().put("source", ragFile.getOriginalFilename()));

        TokenTextSplitter splitter = new TokenTextSplitter();
        List<Document> split = splitter.split(textReader.get());
        postgreVectorStore.add(split);
        return "成功加载文档片段数量: " + documents.size();
    }
}
