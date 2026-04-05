package com.example.tree.retriever.indexing;

import org.springframework.ai.document.Document;

/**
 * Factory methods for baseline indexing pipelines.
 */
public final class BaselinePipelines {

    private BaselinePipelines() {
    }

    public static DocumentProcessingPipeline<String> text() {
        return new DocumentProcessingPipeline<>(new TextDocumentParser(), new OverlappingTextChunker());
    }

    public static DocumentProcessingPipeline<String> markdown() {
        return new DocumentProcessingPipeline<>(new MarkdownDocumentParser(), new OverlappingTextChunker());
    }

    public static DocumentProcessingPipeline<Document> springAiDocument() {
        return new DocumentProcessingPipeline<>(new SpringAiDocumentParser(), new OverlappingTextChunker());
    }
}
