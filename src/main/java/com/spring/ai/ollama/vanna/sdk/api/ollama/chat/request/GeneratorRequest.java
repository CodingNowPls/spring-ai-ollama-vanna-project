package com.spring.ai.ollama.vanna.sdk.api.ollama.chat.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author : gao
 * @date 2024年10月09日
 * -------------------------------------------<br>
 * <br>
 * <br>
 */
@Data
public class GeneratorRequest implements Serializable {

    @JsonProperty("model")
    private String model;

    @JsonProperty("prompt")
    private String prompt;

    @JsonProperty("suffix")
    private String suffix;

    @JsonProperty("images")
    private String images;

    //高级参数
    @JsonProperty("format")
    private String format;

    @JsonProperty("options")
    private Options options;

    @JsonProperty("system")
    private String system;

    @JsonProperty("template")
    private String template;

    @JsonProperty("context")
    private String context;

    @JsonProperty("stream")
    private Boolean stream;

    @JsonProperty("raw")
    private String raw;

    @JsonProperty("keep_alive")
    private Integer keepAlive;


}
