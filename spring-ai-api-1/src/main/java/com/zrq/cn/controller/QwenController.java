package com.zrq.cn.controller;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.zrq.cn.entity.Order;
import com.zrq.cn.utils.AIResponseFormatUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.converter.StructuredOutputConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import java.io.IOException;
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

    @Qualifier("qwenMultimodalModel")
    private final ChatClient qwenMultimodalModel;

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
    public Flux<ChatResponse> chatStream(@RequestParam("userMsg") String userMsg, @RequestParam("file") MultipartFile file) throws IOException {
        ByteArrayResource resource = new ByteArrayResource(file.getBytes());
        return qwenMultimodalModel.prompt()
                .user(u -> u.text(userMsg)
                        .media(MimeTypeUtils.IMAGE_JPEG, resource)
                )
                .advisors(new SimpleLoggerAdvisor())
                .stream().chatResponse();
    }
}
