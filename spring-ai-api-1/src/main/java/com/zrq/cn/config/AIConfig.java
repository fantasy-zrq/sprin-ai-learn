package com.zrq.cn.config;

import com.zrq.cn.mcp.DateTools;
import org.springframework.ai.autoconfigure.openai.OpenAiConnectionProperties;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.document.MetadataMode;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.openai.OpenAiEmbeddingOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.ai.zhipuai.ZhiPuAiChatModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

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

    @Bean
    @Primary
    public EmbeddingModel qwenEmbeddingModel(OpenAiApi baseOpenAiApi) {
        OpenAiEmbeddingOptions embeddingOptions = OpenAiEmbeddingOptions.builder()
                .model("text-embedding-v4")
                .dimensions(1024)
                .build();
        return new OpenAiEmbeddingModel(baseOpenAiApi, MetadataMode.INFERENCE, embeddingOptions);
    }

    @Bean("simpleVectorStore")
    public VectorStore simpleVectorStore(@Qualifier("qwenEmbeddingModel") EmbeddingModel qwenEmbeddingModel) {
        return SimpleVectorStore.builder(qwenEmbeddingModel).build();
    }

    @Bean("postgreVectorStore")
    public VectorStore postgreVectorStore(JdbcTemplate jdbcTemplate, @Qualifier("qwenEmbeddingModel") EmbeddingModel qwenEmbeddingModel) {
        return PgVectorStore.builder(jdbcTemplate, qwenEmbeddingModel)
                .dimensions(1024)
                .indexType(PgVectorStore.PgIndexType.HNSW)
                .distanceType(PgVectorStore.PgDistanceType.COSINE_DISTANCE)
                .maxDocumentBatchSize(1000)
                .build();
    }

    @Bean
    public ChatMemory inChatMemory() {
        return new InMemoryChatMemory();
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
    public ChatClient qwenPlusChatClient(OpenAiApi baseOpenAiApi,
                                         @Qualifier("simpleVectorStore") VectorStore simpleVectorStore,
                                         @Qualifier("inChatMemory") ChatMemory inChatMemory,
                                         @Qualifier("postgreVectorStore") VectorStore postgreVectorStore) {
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
                .defaultAdvisors(QuestionAnswerAdvisor.builder(postgreVectorStore).build())
                .defaultAdvisors(new MessageChatMemoryAdvisor(inChatMemory))
                .defaultTools(new DateTools())
                .build();
    }

    //qwen-omni-turbo
    @Bean("qwenMultiClient")
    public ChatClient qwenMultiClient(OpenAiApi baseOpenAiApi) {
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
