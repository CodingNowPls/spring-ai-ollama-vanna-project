package com.spring.ai.ollama.vanna.biz.config;

import com.spring.ai.ollama.vanna.sdk.api.ollama.OllamaApi;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.beans.factory.annotation.Autowired;
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
public class ClientConfig {

    @Bean
    public OllamaChatModel chatModel(@Autowired OllamaApi ollamaApi) {
        return new OllamaChatModel(ollamaApi);
    }


}
