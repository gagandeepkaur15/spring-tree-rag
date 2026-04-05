package com.example.tree.retriever.indexing;

import java.util.Map;

/**
 * Chunked text with inherited metadata.
 *
 * @param text chunk body text
 * @param metadata immutable chunk metadata
 */
public record ParsedChunk(String text, Map<String, Object> metadata) {

    public ParsedChunk {
        text = text == null ? "" : text;
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }
}
