package com.example.tree.retriever.indexing;

import java.util.Map;

/**
 * Parsed document representation used by chunking pipelines.
 *
 * @param text normalized body text
 * @param metadata immutable metadata attached to the source document
 */
public record ParsedDocument(String text, Map<String, Object> metadata) {

    public ParsedDocument {
        text = text == null ? "" : text;
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }
}
