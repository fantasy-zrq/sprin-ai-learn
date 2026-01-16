package com.zrq.cn.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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

    @GetMapping("/chat")
    public String chat(@RequestParam("userMsg") String userMsg) {
        if (userMsg.contains("1")) {
            return glmChatClient.prompt().user(userMsg).call().content();
        } else if (userMsg.contains("2")) {
            return qwenFlashChatClient.prompt().user(userMsg).call().content();
        } else {
            return qwenPlusChatClient.prompt().user(userMsg).call().content();
        }
    }
}
