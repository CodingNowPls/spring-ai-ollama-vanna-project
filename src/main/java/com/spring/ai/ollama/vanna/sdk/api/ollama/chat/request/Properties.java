package com.spring.ai.ollama.vanna.sdk.api.ollama.chat.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class Properties implements Serializable {

    @JsonProperty("location")
    private Location location;

    @JsonProperty("format")
    private Format format;


}