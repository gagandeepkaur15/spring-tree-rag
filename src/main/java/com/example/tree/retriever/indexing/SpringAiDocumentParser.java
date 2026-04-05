package com.example.tree.retriever.indexing;

import java.util.Map;

import org.springframework.ai.document.Document;

/**
 * Parser that uses Spring AI's Document model directly.
 */
public final class SpringAiDocumentParser implements DocumentParser<Document> {

    @Override
    public ParsedDocument parse(Document source) {
        if (source == null) {
            return new ParsedDocument("", Map.of("format", "spring-ai-document"));
        }

        final Map<String, Object> metadata = source.getMetadata() == null
                ? Map.of("format", "spring-ai-document")
                : appendFormat(source.getMetadata());

        return new ParsedDocument(source.getText(), metadata);
    }

    private Map<String, Object> appendFormat(Map<String, Object> sourceMetadata) {
        final Map<String, Object> copy = new java.util.LinkedHashMap<>(sourceMetadata);
        copy.putIfAbsent("format", "spring-ai-document");
        return copy;
    }
}
