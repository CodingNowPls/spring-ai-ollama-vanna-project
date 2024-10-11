package com.dev.sdk.api.ollama.chat.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class Location implements Serializable {

    @JsonProperty("type")
    private String type;

    @JsonProperty("description")
    private String description;


}