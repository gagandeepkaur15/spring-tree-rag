package com.example.tree.retriever.indexing.summarization;

/**
 * Summarizes document content into a compact representation.
 */
public interface SummarizationEngine {

    SummarizationResult summarize(String content);
}
