package com.zrq.cn.controller;

import jakarta.annotation.Resource;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author zrq
 * @time 2026/1/15 15:33
 * @description
 */
@RestController
@RequestMapping("/ai/api/qwen")
public class QwenController {

    @Qualifier("warmsChatClient")
    @Resource
    private ChatClient warmsChatClient;

    @Qualifier("killerChatClient")
    @Resource
    private ChatClient killerChatClient;

    @GetMapping("/chat")
    public String chat(@RequestParam("userMsg") String userMsg){
        if(userMsg.contains("红楼梦")){
            return warmsChatClient.prompt().user(userMsg).call().content();
        }else {
            return killerChatClient.prompt().user(userMsg).call().content();
        }
    }
}
