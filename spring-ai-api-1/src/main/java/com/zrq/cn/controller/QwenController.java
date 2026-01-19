package com.zrq.cn.controller;

import cn.hutool.core.util.HashUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.github.benmanes.caffeine.cache.Cache;
import com.zrq.cn.entity.Order;
import com.zrq.cn.enums.RagStatusEnum;
import com.zrq.cn.utils.AIResponseFormatUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.converter.StructuredOutputConverter;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.time.LocalDateTime;
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

    private final Cache<Long, Object> ragStatusCache;

    @GetMapping("/cache")
    public String testCache() {
        return ((String) ragStatusCache.get(1L, key -> RagStatusEnum.PARSE.getValue()));
    }

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
    public Flux<ChatResponse> chatStream(@RequestParam("conversationId") String conversationId,
                                         @RequestParam("userMsg") String userMsg,
                                         @RequestParam("category") String category) {
        return qwenPlusChatClient.prompt()
                .user(u -> u.text(userMsg))
                .advisors(advisorSpec -> advisorSpec
                        .param(AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY, conversationId)
                        .param(AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY, 15)
                )
                .advisors(QuestionAnswerAdvisor.builder(postgreVectorStore)
                        .searchRequest(SearchRequest.builder()
                                .query(userMsg)
                                .topK(6)
                                .filterExpression("category == '" + category + "'")
                                .build()).build())
                .stream()
                .chatResponse();
    }

    @PostMapping("/upload")
    public String uploadRag(@RequestParam("ragFile") MultipartFile ragFile, @RequestParam("category") String category) throws IOException {
        Long hash = hash(category);
        ragStatusCache.put(hash, RagStatusEnum.PARSE);
        TikaDocumentReader textReader = new TikaDocumentReader(new ByteArrayResource(ragFile.getBytes()));
        List<Document> documents = textReader.get();
        //这里打标签，下面切片以后自动继承标签
        ragStatusCache.put(hash, RagStatusEnum.SEGMENTATION);
        documents.forEach(document -> {
            document.getMetadata().put("source", ragFile.getOriginalFilename());
            document.getMetadata().put("update_time", LocalDateTime.now());
            document.getMetadata().put("category", category);
        });
        TokenTextSplitter splitter = new TokenTextSplitter();
        //用打了标签的数据来分割
        List<Document> split = splitter.split(documents);
        ragStatusCache.put(hash, RagStatusEnum.STORAGE);
        postgreVectorStore.add(split);
        ragStatusCache.put(hash, RagStatusEnum.FINISH);
        return "成功加载文档片段数量: " + documents.size();
    }

    @GetMapping("/queryRagStatus")
    public String queryRag(@RequestParam("category") String category) {
        Long hash = hash(category);
        RagStatusEnum statusEnum = (RagStatusEnum) ragStatusCache.get(hash, key -> RagStatusEnum.NOTFOUND);
        return statusEnum.getValue();
    }

    @GetMapping("/getDoc")
    public String getDoc(@RequestParam("category") String category, @RequestParam("userMsg") String userMsg) {
        Long hash = hash(category);
        List<Document> documents = postgreVectorStore.similaritySearch(SearchRequest.builder()
                .filterExpression("category == '" + category + "'")
                .topK(50)
                .query(userMsg)
                .build());
        return JSON.toJSONString(documents);
    }


    private Long hash(String category) {
        return HashUtil.cityHash64(category.getBytes());
    }

}
