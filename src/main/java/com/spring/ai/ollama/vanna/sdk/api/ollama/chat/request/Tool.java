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
public class Tool implements Serializable {

    @JsonProperty("type")
    private String type;

    @JsonProperty("function")
    private Function function;

}
