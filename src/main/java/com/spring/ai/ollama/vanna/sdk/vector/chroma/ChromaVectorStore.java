package com.spring.ai.ollama.vanna.sdk.vector.chroma;

import cn.hutool.extra.spring.SpringUtil;
import com.spring.ai.ollama.vanna.sdk.vector.VectorIdGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.observation.ObservationRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.BatchingStrategy;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingOptionsBuilder;

import org.springframework.ai.vectorstore.ChromaFilterExpressionConverter;
import org.springframework.ai.vectorstore.JsonUtils;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.filter.FilterExpressionConverter;
import org.springframework.ai.vectorstore.observation.VectorStoreObservationConvention;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.Assert;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestClient;


import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * @author : gao
 * @date 2024年10月10日
 * -------------------------------------------<br>
 * <br>
 * <br>
 */
@Slf4j
public class ChromaVectorStore extends org.springframework.ai.vectorstore.ChromaVectorStore {
    private final EmbeddingModel embeddingModel;
    private final ChromaApi chromaApi;
    private final String collectionName;
    private FilterExpressionConverter filterExpressionConverter;
    private String collectionId;
    private final boolean initializeSchema;

    private final BatchingStrategy batchingStrategy;

    private RestClient restClient;

    public ChromaVectorStore(EmbeddingModel embeddingModel
            , ChromaApi chromaApi
            , @Value("${chroma.collectionName}") String collectionName
            , @Value("${chroma.initializeSchema}") boolean initializeSchema
            , ObservationRegistry observationRegistry,
                             VectorStoreObservationConvention customObservationConvention
            , BatchingStrategy batchingStrategy, RestClient restClient) {
        super(embeddingModel, chromaApi, collectionName, initializeSchema, observationRegistry, customObservationConvention, batchingStrategy);
        this.embeddingModel = embeddingModel;
        this.chromaApi = chromaApi;
        this.collectionName = collectionName;
        this.initializeSchema = initializeSchema;
        this.filterExpressionConverter = new ChromaFilterExpressionConverter();
        this.batchingStrategy = batchingStrategy;
        this.restClient = restClient;
    }


    private ChromaResponseErrorHandler errorHandler = new ChromaResponseErrorHandler();

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        this.collectionId = super.getCollectionId();
    }


    public String addSqlDocument(String question, String sql) throws NoSuchAlgorithmException, JsonProcessingException {
        // 校验输入
        Assert.notNull(question, "question 不能为空");
        Assert.notNull(sql, "sql 不能为空");
        // 生成唯一 ID
        String uuid = VectorIdGenerator.deterministicUUID(question);

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("question", question);
        metadata.put("sql", sql);
        ObjectMapper objectMapper = SpringUtil.getBean(ObjectMapper.class);
        String jsonContent = objectMapper.writeValueAsString(metadata);
        // 创建 Document 对象
        Document document = new Document(uuid, jsonContent, metadata);

        List<String> ids = new ArrayList<>();
        List<Map<String, Object>> metadatas = new ArrayList<>();
        List<String> contents = new ArrayList<>();
        List<float[]> embeddings = new ArrayList<>();
        List<Document> documentList = Arrays.asList(document);
        this.embeddingModel.embed(documentList, EmbeddingOptionsBuilder.builder().build(), this.batchingStrategy);

        for (Document doc : documentList) {
            ids.add(doc.getId());
            metadatas.add(doc.getMetadata());
            contents.add(doc.getContent());
            document.setEmbedding(doc.getEmbedding());
            embeddings.add(doc.getEmbedding());
        }

        ChromaApi.AddEmbeddingsRequest request = new ChromaApi.AddEmbeddingsRequest(ids, embeddings, metadatas, contents);

        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("ids", ids);
        paramMap.put("embeddings", embeddings);
        paramMap.put("metadatas", metadatas);
        paramMap.put("documents", contents);

        //this.chromaApi.upsertEmbeddings(this.collectionId,
        //        new ChromaApi.AddEmbeddingsRequest(ids, embeddings, metadatas, contents));
        ////this.restClient.post()
        ////        .uri("/api/v1/collections/{collection_id}/upsert", this.collectionId)
        ////        .body(paramMap)
        ////        .retrieve()
        ////        .onStatus(this.errorHandler)
        ////        .body(OllamaApiClient.TagResponse.class);
        //super.add();
        return uuid;
    }

    @Override
    public List<Document> doSimilaritySearch(SearchRequest request) {

        String nativeFilterExpression = (request.getFilterExpression() != null)
                ? this.filterExpressionConverter.convertExpression(request.getFilterExpression()) : "";

        String query = request.getQuery();
        Assert.notNull(query, "Query string must not be null");

        float[] embedding = this.embeddingModel.embed(query);
        Map<String, Object> where = (StringUtils.hasText(nativeFilterExpression))
                ? JsonUtils.jsonToMap(nativeFilterExpression) : Map.of();
        var queryRequest = new ChromaApi.QueryRequest(embedding, request.getTopK(), where);


        var queryResponse = this.chromaApi.queryCollection(this.collectionId, queryRequest);
        var embeddings = this.chromaApi.toEmbeddingResponseList(queryResponse);

        List<Document> responseDocuments = new ArrayList<>();

        for (ChromaApi.Embedding chromaEmbedding : embeddings) {
            float distance = chromaEmbedding.distances().floatValue();
            if ((1 - distance) >= request.getSimilarityThreshold()) {
                String id = chromaEmbedding.id();
                String content = chromaEmbedding.document();
                Map<String, Object> metadata = chromaEmbedding.metadata();
                if (metadata == null) {
                    metadata = new HashMap<>();
                }
                metadata.put(DISTANCE_FIELD_NAME, distance);
                Document document = new Document(id, content, metadata);
                document.setEmbedding(chromaEmbedding.embedding());
                responseDocuments.add(document);
            }
        }

        return responseDocuments;
    }

    private static class ChromaResponseErrorHandler implements ResponseErrorHandler {

        @Override
        public boolean hasError(ClientHttpResponse response) throws IOException {
            return response.getStatusCode().isError();
        }

        @Override
        public void handleError(ClientHttpResponse response) throws IOException {
            if (response.getStatusCode().isError()) {
                int statusCode = response.getStatusCode().value();
                String statusText = response.getStatusText();
                String message = StreamUtils.copyToString(response.getBody(), java.nio.charset.StandardCharsets.UTF_8);
                log.warn(String.format("[%s] %s - %s", statusCode, statusText, message));
                throw new RuntimeException(String.format("[%s] %s - %s", statusCode, statusText, message));
            }
        }
    }

    public void deleteByQuestion(String question) throws NoSuchAlgorithmException {
        this.delete(Arrays.asList(VectorIdGenerator.deterministicUUID(question)));
    }
}
