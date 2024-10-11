package com.dev.sdk.api.ollama.config;

import com.dev.sdk.api.ollama.OllamaApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * @author : gao
 * @date 2024年10月10日
 * -------------------------------------------<br>
 * <br>
 * <br>
 */
@Configuration
public class OllamaConfig {


    @Bean
    public OllamaApi ollamaApi(@Value("${ollama.apiUrl}") String baseUrl) {
        RestClient.Builder restBuilder = RestClient.builder();
        WebClient.Builder webClientBuilder = WebClient.builder();
        OllamaApi ollamaApi = new OllamaApi(baseUrl, restBuilder, webClientBuilder);
        return ollamaApi;
    }

}