package com.example.tree.retriever.spring;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "spring.ai.tree-retriever")
public class TreeRetrieverProperties {

    private int maxResults = 5;
    private Duration traversalDeadline = Duration.ofSeconds(5);
    private Duration llmTimeout = Duration.ofSeconds(3);
    private int llmRetries = 1;
    private int branchFactor = 4;

    public int getMaxResults() {
        return maxResults;
    }

    public void setMaxResults(int maxResults) {
        this.maxResults = maxResults;
    }

    public Duration getTraversalDeadline() {
        return traversalDeadline;
    }

    public void setTraversalDeadline(Duration traversalDeadline) {
        this.traversalDeadline = traversalDeadline;
    }

    public Duration getLlmTimeout() {
        return llmTimeout;
    }

    public void setLlmTimeout(Duration llmTimeout) {
        this.llmTimeout = llmTimeout;
    }

    public int getLlmRetries() {
        return llmRetries;
    }

    public void setLlmRetries(int llmRetries) {
        this.llmRetries = llmRetries;
    }

    public int getBranchFactor() {
        return branchFactor;
    }

    public void setBranchFactor(int branchFactor) {
        this.branchFactor = branchFactor;
    }
}
