package com.dev.biz.controller;

import cn.hutool.json.JSONUtil;
import com.dev.biz.config.DatabaseVector;
import com.dev.sdk.vector.VectorIdGenerator;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;

/**
 * @author : gao
 * @date 2024年10月09日
 * -------------------------------------------<br>
 * <br>
 * <br>
 */

@RestController
@RequestMapping("/test")

public class OllamaTestController {

    private final EmbeddingModel embeddingModel;

    private final VectorStore vectorStore;

    private final String dbName;

    public OllamaTestController(@Value("${dbName}") String dbName, EmbeddingModel embeddingModel, VectorStore vectorStore) {
        this.dbName = dbName;
        this.embeddingModel = embeddingModel;
        this.vectorStore = vectorStore;
    }

    @GetMapping("/test")
    public EmbeddingResponse testEmbed() {
        EmbeddingResponse embeddingResponse = embeddingModel
                .embedForResponse(List.of("你好"));
        return embeddingResponse;
    }

    @GetMapping("/removeByKey/{question}")
    public void removeByKey(@PathVariable("question") String question) throws NoSuchAlgorithmException {
        vectorStore.delete(List.of(VectorIdGenerator.deterministicUUID(question)));
    }

    @GetMapping("/testDb")
    public String testDb() throws Exception {
        Map<String, String[]> databaseVector = DatabaseVector.getDatabaseVector(dbName);
        return JSONUtil.toJsonStr(databaseVector);
    }


}
