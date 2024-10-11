package com.spring.ai.ollama.vanna.sdk.api.ollama.chat.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Properties;

@Data
public class Parameters implements Serializable {

    @JsonProperty("type")
    private String type;

    @JsonProperty("properties")
    private Properties properties;

    @JsonProperty("required")
    private List<String> required;


}