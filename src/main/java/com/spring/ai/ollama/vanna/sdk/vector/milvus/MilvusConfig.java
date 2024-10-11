package com.spring.ai.ollama.vanna.sdk.vector.milvus;

import org.springframework.context.annotation.Configuration;

/**
 * Milvus向量数据库
 *
 * @author : gao
 * @date 2024年10月09日
 * -------------------------------------------<br>
 * <br>
 * <br>
 */
@Configuration
public class MilvusConfig {
    //
    //@Bean
    //public VectorStore vectorStore(MilvusServiceClient milvusClient, EmbeddingModel embeddingModel) {
    //    MilvusVectorStore.MilvusVectorStoreConfig config = MilvusVectorStore.MilvusVectorStoreConfig.builder()
    //            .withCollectionName("test_milvus_vector_store")
    //            .withDatabaseName("default")
    //            .withIndexType(IndexType.IVF_FLAT)
    //            .withMetricType(MetricType.COSINE)
    //            .build();
    //    return new MilvusVectorStore(milvusClient, embeddingModel,config, true, null);
    //}
    //
    //@Bean
    //public MilvusServiceClient milvusClient() {
    //    return new MilvusServiceClient(ConnectParam.newBuilder()
    //            .withAuthorization("minioadmin", "minioadmin")
    //            .withUri(milvusContainer.getEndpoint())
    //            .build());
    //}
}
