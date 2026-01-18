package com.zrq.cn.config;

import org.springframework.ai.autoconfigure.openai.OpenAiConnectionProperties;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.zhipuai.ZhiPuAiChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author zrq
 * 2026/1/15 15:38
 */
@Configuration
public class AIConfig {

    @Bean
    public OpenAiApi baseOpenAiApi(OpenAiConnectionProperties properties) {
        return OpenAiApi.builder()
                .apiKey(properties.getApiKey())
                .baseUrl(properties.getBaseUrl())
                .build();
    }

    @Bean("qwenFlashChatClient")
    public ChatClient qwenFlashChatClient(OpenAiApi baseOpenAiApi) {
        return ChatClient.builder(
                        OpenAiChatModel.builder()
                                .openAiApi(baseOpenAiApi)
                                .build()
                )
                .defaultOptions(
                        ChatOptions.builder()
                                .model("qwen-flash")
                                .temperature(0.5)
                                .build()
                )
                .build();
    }

    @Bean("qwenPlusChatClient")
    public ChatClient qwenPlusChatClient(OpenAiApi baseOpenAiApi) {
        return ChatClient.builder(
                        OpenAiChatModel.builder()
                                .openAiApi(baseOpenAiApi)
                                .build()
                )
                .defaultOptions(
                        ChatOptions.builder()
                                .model("qwen-plus")
                                .temperature(0.7)
                                .build()
                )
                .build();
    }

    //qwen-omni-turbo
    @Bean
    public ChatClient qwenMultimodalModel(OpenAiApi baseOpenAiApi) {
        return ChatClient.builder(
                        OpenAiChatModel.builder()
                                .openAiApi(baseOpenAiApi)
                                .build()
                )
                .defaultOptions(
                        ChatOptions.builder()
                                .model("qwen-omni-turbo")
                                .temperature(0.7)
                                .build()
                )
                .build();
    }

    @Bean("glmChatClient")
    public ChatClient glmChatClient(ZhiPuAiChatModel zhiPuAiChatModel) {
        return ChatClient.builder(zhiPuAiChatModel)
                .defaultAdvisors()
                .build();
    }
}
