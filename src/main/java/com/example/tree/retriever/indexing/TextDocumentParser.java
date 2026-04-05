package com.example.tree.retriever.indexing;

import java.util.Map;

/**
 * Baseline parser for plain text sources.
 */
public final class TextDocumentParser implements DocumentParser<String> {

    @Override
    public ParsedDocument parse(String source) {
        return new ParsedDocument(source == null ? "" : source, Map.of("format", "text"));
    }
}
