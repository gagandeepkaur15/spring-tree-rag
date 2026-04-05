package com.example.tree.retriever.spring;

import com.example.tree.retriever.api.TraversalDecisionEvaluator;
import com.example.tree.retriever.api.TraversalEngine;
import com.example.tree.retriever.api.TreeDocumentRetriever;
import com.example.tree.retriever.api.TreeIndexBuilder;
import com.example.tree.retriever.indexing.DefaultTreeIndexBuilder;
import com.example.tree.retriever.indexing.summarization.ChatClientSummarizationEngine;
import com.example.tree.retriever.indexing.summarization.SummarizationEngine;
import com.example.tree.retriever.store.InMemoryTreeIndexStore;
import com.example.tree.retriever.store.TreeIndexStore;
import com.example.tree.retriever.traversal.ChatClientTraversalDecisionEvaluator;
import com.example.tree.retriever.traversal.IterativeDfsTraversalEngine;
import com.example.tree.retriever.traversal.TraversalDecisionTextParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.rag.retrieval.search.DocumentRetriever;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.time.Clock;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@AutoConfiguration
@EnableConfigurationProperties(TreeRetrieverProperties.class)
@ConditionalOnClass(ChatClient.class)
public class TreeRetrieverAutoConfiguration {

    @Bean(destroyMethod = "shutdown")
    @ConditionalOnMissingBean
    public ExecutorService treeTraversalExecutor() {
        return Executors.newFixedThreadPool(4);
    }

    @Bean
    @ConditionalOnBean(ChatClient.class)
    @ConditionalOnMissingBean
    public SummarizationEngine summarizationEngine(ChatClient chatClient, ObjectMapper objectMapper, TreeRetrieverProperties properties) {
        return new ChatClientSummarizationEngine(chatClient, objectMapper, properties.getLlmTimeout(), properties.getLlmRetries());
    }

    @Bean
    @ConditionalOnMissingBean
    public TreeIndexBuilder treeIndexBuilder(SummarizationEngine summarizationEngine, TreeRetrieverProperties properties) {
        return new DefaultTreeIndexBuilder(summarizationEngine, properties.getBranchFactor());
    }

    @Bean
    @ConditionalOnMissingBean
    public TraversalDecisionTextParser traversalDecisionTextParser(ObjectMapper objectMapper) {
        return new TraversalDecisionTextParser(objectMapper);
    }

    @Bean
    @ConditionalOnBean(ChatClient.class)
    @ConditionalOnMissingBean
    public TraversalDecisionEvaluator traversalDecisionEvaluator(ChatClient chatClient, TraversalDecisionTextParser parser) {
        return new ChatClientTraversalDecisionEvaluator(chatClient, parser);
    }

    @Bean
    @ConditionalOnBean(TraversalDecisionEvaluator.class)
    @ConditionalOnMissingBean
    public TraversalEngine traversalEngine(
            TraversalDecisionEvaluator decisionEvaluator,
            ExecutorService treeTraversalExecutor,
            TreeRetrieverProperties properties
    ) {
        return new IterativeDfsTraversalEngine(decisionEvaluator, treeTraversalExecutor, properties.getTraversalDeadline(), Clock.systemUTC());
    }

    @Bean
    @ConditionalOnMissingBean
    public TreeIndexStore treeIndexStore() {
        return new InMemoryTreeIndexStore();
    }

    @Bean
    @ConditionalOnBean(TraversalEngine.class)
    @ConditionalOnMissingBean
    public DocumentRetriever documentRetriever(TreeIndexStore treeIndexStore, TraversalEngine traversalEngine, TreeRetrieverProperties properties) {
        return new TreeDocumentRetriever(treeIndexStore, traversalEngine, properties.getMaxResults());
    }

    @Bean
    @ConditionalOnClass(Endpoint.class)
    @ConditionalOnMissingBean
    public TreeRetrieverEndpoint treeRetrieverEndpoint(TreeIndexStore treeIndexStore) {
        return new TreeRetrieverEndpoint(treeIndexStore);
    }

    @Endpoint(id = "treeRetriever")
    public static class TreeRetrieverEndpoint {
        private final TreeIndexStore treeIndexStore;

        public TreeRetrieverEndpoint(TreeIndexStore treeIndexStore) {
            this.treeIndexStore = treeIndexStore;
        }

        @ReadOperation
        public Map<String, Object> stats() {
            return Map.of(
                    "hasIndex", treeIndexStore.current().isPresent(),
                    "storeStats", treeIndexStore.stats()
            );
        }
    }
}
