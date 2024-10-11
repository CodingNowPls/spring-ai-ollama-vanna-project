package com.dev.sdk.vector.chroma;

import com.dev.sdk.api.ollama.OllamaApiClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import java.util.function.Consumer;

/**
 * @author : gao
 * @date 2024年10月10日
 * -------------------------------------------<br>
 * <br>
 * <br>
 */
@Slf4j
public class ChromaApi extends org.springframework.ai.chroma.ChromaApi {

    private RestClient restClient;
    private final ObjectMapper objectMapper;
    private String keyToken;

    public ChromaApi(String baseUrl, RestClient.Builder restClientBuilder, ObjectMapper objectMapper) {
        super(baseUrl, restClientBuilder, objectMapper);
        Consumer<HttpHeaders> defaultHeaders = headers -> {
            headers.setContentType(MediaType.APPLICATION_JSON);
        };
        this.restClient = restClientBuilder.baseUrl(baseUrl).defaultHeaders(defaultHeaders).build();
        this.objectMapper = objectMapper;
    }

    private void httpHeaders(HttpHeaders headers) {
        if (StringUtils.hasText(this.keyToken)) {
            headers.setBearerAuth(this.keyToken);
        }
    }

    public org.springframework.ai.chroma.ChromaApi withKeyToken(String keyToken) {
        this.keyToken = keyToken;
        return this;
    }

    public QueryResponse queryCollection(String collectionId, QueryRequest queryRequest) {

        return this.restClient.post()
                .uri("/api/v1/collections/{collection_id}/query", collectionId)
                .headers(this::httpHeaders)
                .body(queryRequest)
                .retrieve()
                .toEntity(QueryResponse.class)
                .getBody();
    }

    public void upsertEmbeddings(String collectionId, AddEmbeddingsRequest embedding) {

        this.restClient.post()
                .uri("/api/v1/collections/{collection_id}/upsert", collectionId)
                .headers(this::httpHeaders)
                .body(embedding)
                .retrieve()
                .toBodilessEntity();
    }
}
