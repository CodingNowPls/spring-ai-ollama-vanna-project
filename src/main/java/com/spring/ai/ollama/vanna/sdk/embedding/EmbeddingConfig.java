package com.spring.ai.ollama.vanna.sdk.embedding;

import com.spring.ai.ollama.vanna.sdk.api.ollama.OllamaApi;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.ollama.OllamaEmbeddingModel;
import org.springframework.ai.ollama.api.OllamaOptions;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author : gao
 * @date 2024年10月09日
 * -------------------------------------------<br>
 * <br>
 * <br>
 */
@Configuration
public class EmbeddingConfig {

    @Bean
    public EmbeddingModel embeddingModel(@Autowired OllamaApi ollamaApi
            , @Value("${ollama.embeddingModelName}") String embeddingModelName) {
        var embeddingModel = new OllamaEmbeddingModel(ollamaApi,
                OllamaOptions.create()
                        .withModel(embeddingModelName));
        return embeddingModel;
    }

}
