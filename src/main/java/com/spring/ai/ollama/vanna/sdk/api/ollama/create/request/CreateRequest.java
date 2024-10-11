package com.spring.ai.ollama.vanna.sdk.api.ollama.create.request;

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
public class CreateRequest implements Serializable {

    @JsonProperty("name")
    private String name;

    @JsonProperty("modelfile")
    private String modelFile;


}
