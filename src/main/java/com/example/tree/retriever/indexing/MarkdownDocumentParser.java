package com.example.tree.retriever.indexing;

import java.util.Map;

/**
 * Baseline parser for markdown documents.
 */
public final class MarkdownDocumentParser implements DocumentParser<String> {

    @Override
    public ParsedDocument parse(String source) {
        return new ParsedDocument(source == null ? "" : source, Map.of("format", "markdown"));
    }
}
