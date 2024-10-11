package com.dev.biz.config;

import com.dev.sdk.api.ollama.OllamaApi;
import lombok.AllArgsConstructor;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

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
