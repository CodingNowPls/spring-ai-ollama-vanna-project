package com.spring.ai.ollama.vanna.sdk.api.ollama.chat.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * @author : gao
 * @date 2024年09月30日
 * -------------------------------------------<br>
 * <br>
 * <br>
 */
@Data
@Builder
public class Message implements Serializable {

    @JsonProperty("role")
    private String role;

    @JsonProperty("content")
    private String content;

}
