package com.spring.ai.ollama.vanna.sdk.api.ollama.chat.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;

/**
 *
 */
@Data
public class Messages implements Serializable {

    @JsonProperty("role")
    private String role;

    @JsonProperty("content")
    private String content;

}