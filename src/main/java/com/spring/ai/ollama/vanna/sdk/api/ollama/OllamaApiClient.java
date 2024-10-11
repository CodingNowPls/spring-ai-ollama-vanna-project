package com.spring.ai.ollama.vanna.sdk.api.ollama;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
@AllArgsConstructor
public class OllamaApiClient {


    private final OllamaApi ollamaApi;


    // 定义响应对象，用于接收 /api/tags 请求的返回值
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record TagResponse(
            @JsonProperty("models") List<ModelResponse> models) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record ModelResponse(
            @JsonProperty("name") String name,
            @JsonProperty("modified_at") Instant modifiedAt,
            @JsonProperty("size") Long size,
            @JsonProperty("digest") String digest,
            @JsonProperty("details") ModelDetails details) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record ModelDetails(
            @JsonProperty("format") String format,
            @JsonProperty("family") String family,
            @JsonProperty("families") List<String> families,
            @JsonProperty("parameter_size") String parameterSize,
            @JsonProperty("quantization_level") String quantizationLevel) {
    }

    /**
     * 获取所有模型标签
     *
     * @return TagResponse
     */
    public TagResponse getTags() {
        return ollamaApi.getRestClient().get()
                .uri("/api/tags")
                .retrieve()
                .onStatus(this.ollamaApi.getResponseErrorHandler())
                .body(TagResponse.class);
    }

    // 定义版本响应对象
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record VersionResponse(
            @JsonProperty("version") String version) {
    }

    /**
     * 获取 API 版本信息
     *
     * @return VersionResponse
     */
    public VersionResponse getVersion() {
        return ollamaApi.getRestClient().get()
                .uri("/api/version")
                .retrieve()
                .onStatus(this.ollamaApi.getResponseErrorHandler())
                .body(VersionResponse.class);
    }
}
