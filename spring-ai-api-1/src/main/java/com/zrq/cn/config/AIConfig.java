package com.zrq.cn.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.zhipuai.ZhiPuAiChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author zrq
 * 2026/1/15 15:38
 */
@Configuration
public class AIConfig {

    @Bean("killerChatClient")
    public ChatClient killerChatClient(OpenAiChatModel glmChatModel) {
        return ChatClient.builder(glmChatModel)
                .build();
    }

    @Bean("warmsChatClient")
    public ChatClient warmsChatClient(ZhiPuAiChatModel zhiPuAiChatModel) {
        return ChatClient.builder(zhiPuAiChatModel)
                .build();
    }
}
