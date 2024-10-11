package com.spring.ai.ollama.vanna.sdk.config;

import com.spring.ai.ollama.vanna.sdk.vector.simple.SimpleVectorStore;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * Chroma 向量数据库
 *
 * @author : gao
 * @date 2024年10月09日
 * -------------------------------------------<br>
 * <br>
 * <br>
 */
@Configuration
public class VectorStoreConfig {

    //@Bean
    //public ChromaApi chromaApi(@Value("${chroma.apiUrl}") String chromaUrl, @Autowired ObjectMapper objectMapper) {
    //    RestClient.Builder restClientBuilder = RestClient.builder();
    //    ChromaApi chromaApi = new ChromaApi(chromaUrl, restClientBuilder, objectMapper);
    //    return chromaApi;
    //}
    //
    //@Bean
    //public ChromaVectorStore chromaVectorStore(@Value("${chroma.apiUrl}") String chromaUrl, EmbeddingModel embeddingModel, ChromaApi chromaApi
    //        , @Value("${chroma.collectionName}") String collectionName
    //        , @Value("${chroma.initializeSchema}") Boolean initializeSchema) {
    //    //先判断是否存在，不存在的话需要先创建
    //    ChromaApi.Collection collection = chromaApi.getCollection(collectionName);
    //    if (Objects.isNull(collection)) {
    //        ChromaApi.CreateCollectionRequest createCollectionRequest = new ChromaApi.CreateCollectionRequest(collectionName);
    //        chromaApi.createCollection(createCollectionRequest);
    //    }
    //    RestClient.Builder restClientBuilder = RestClient.builder();
    //
    //    Consumer<HttpHeaders> defaultHeaders = headers -> {
    //        headers.setContentType(MediaType.APPLICATION_JSON);
    //    };
    //    RestClient restClient = restClientBuilder.baseUrl(chromaUrl).defaultHeaders(defaultHeaders).build();
    //    return new ChromaVectorStore(embeddingModel, chromaApi, collectionName, initializeSchema, ObservationRegistry.NOOP, null,
    //            new TokenCountBatchingStrategy(), restClient);
    //}
    //
    //@Bean
    //public VectorStore vectorStore(@Value("${chroma.apiUrl}") String chromaUrl, EmbeddingModel embeddingModel, ChromaApi chromaApi
    //        , @Value("${chroma.collectionName}") String collectionName
    //        , @Value("${chroma.initializeSchema}") Boolean initializeSchema) {
    //    return chromaVectorStore(chromaUrl, embeddingModel, chromaApi, collectionName, initializeSchema);
    //}

    @Bean
    public VectorStore vectorStore(EmbeddingModel embeddingModel) {
        return simpleVectorStore(embeddingModel);
    }

    @Bean
    public SimpleVectorStore simpleVectorStore(EmbeddingModel embeddingModel) {
        return new SimpleVectorStore(embeddingModel);
    }

}
