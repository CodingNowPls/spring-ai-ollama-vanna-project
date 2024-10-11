package com.dev.sdk.vector.simple;


import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;

import java.util.*;

/**
 * @author : gao
 * @date 2024年10月10日
 * -------------------------------------------<br>
 * <br>
 * <br>
 */
@Slf4j
public class SimpleVectorStore extends org.springframework.ai.vectorstore.SimpleVectorStore {
    public SimpleVectorStore(EmbeddingModel embeddingModel) {
        super(embeddingModel);
    }


    public Map<String, Document> getAllData() {
        return super.store;
    }
}
