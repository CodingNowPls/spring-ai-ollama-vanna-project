package com.spring.ai.ollama.vanna.sdk.api.ollama.chat.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * api返回对象
 *
 * @author : gao
 * @date 2024年09月30日
 * -------------------------------------------<br>
 * <br>
 * <br>
 */
@Data
@Builder
public class ChatResponse implements Serializable {

    @JsonProperty("model")
    private String model;

    @JsonProperty("created_at")
    private String createdAt;

    @JsonProperty("message")
    private Message message;

    @JsonProperty("done_reason")
    private String doneReason;


    @JsonProperty("done")
    private Boolean done;

    @JsonProperty("total_duration")
    private Long totalDuration;

    @JsonProperty("load_duration")
    private Long loadDuration;

    @JsonProperty("prompt_eval_count")
    private Integer promptEvalCount;

    @JsonProperty("prompt_eval_duration")
    private Long promptEvalDuration;

    @JsonProperty("eval_count")
    private Integer evalCount;

    @JsonProperty("eval_duration")
    private Long evalDuration;
}
