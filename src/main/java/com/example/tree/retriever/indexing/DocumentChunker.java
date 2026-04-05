package com.example.tree.retriever.indexing;

import java.util.List;

/**
 * Splits a parsed document into chunks using deterministic rules.
 */
@FunctionalInterface
public interface DocumentChunker {

    List<ParsedChunk> chunk(ParsedDocument document, SplitterConfig config);
}
