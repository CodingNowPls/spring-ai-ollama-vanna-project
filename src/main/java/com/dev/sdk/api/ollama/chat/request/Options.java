
package com.dev.sdk.api.ollama.chat.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;


@Data
public class Options implements Serializable {

    @JsonProperty("num_keep")
    private int numKeep;

    @JsonProperty("seed")
    private int seed;

    @JsonProperty("num_predict")
    private int numPredict;

    @JsonProperty("top_k")
    private int topK;

    @JsonProperty("top_p")
    private double topP;

    @JsonProperty("min_p")
    private int minP;

    @JsonProperty("tfs_z")
    private double tfsZ;

    @JsonProperty("typical_p")
    private double typicalP;

    @JsonProperty("repeat_last_n")
    private int repeatLastN;

    @JsonProperty("temperature")
    private double temperature;

    @JsonProperty("repeat_penalty")
    private double repeatPenalty;

    @JsonProperty("presence_penalty")
    private double presencePenalty;

    @JsonProperty("frequency_penalty")
    private int frequencyPenalty;

    @JsonProperty("mirostat")
    private int miroStat;

    @JsonProperty("mirostat_tau")
    private double mirostatTau;

    @JsonProperty("mirostat_eta")
    private double mirostatEta;

    @JsonProperty("penalize_newline")
    private boolean penalizeNewline;

    @JsonProperty("stop")
    private List<String> stop;

    @JsonProperty("numa")
    private boolean numa;

    @JsonProperty("num_ctx")
    private int numCtx;

    @JsonProperty("num_batch")
    private int numBatch;

    @JsonProperty("num_gpu")
    private int numGpu;

    @JsonProperty("main_gpu")
    private int mainGpu;

    @JsonProperty("low_vram")
    private boolean lowVram;

    @JsonProperty("f16_kv")
    private boolean f16Kv;

    @JsonProperty("vocab_only")
    private boolean vocabOnly;

    @JsonProperty("use_mmap")
    private boolean useMmap;

    @JsonProperty("use_mlock")
    private boolean useMlock;

    @JsonProperty("num_thread")
    private int numThread;


}