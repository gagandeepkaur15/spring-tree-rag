package com.example.tree.retriever.indexing;

import java.util.List;

/**
 * Stateless parser + chunker orchestration.
 *
 * @param <T> source type
 */
public final class DocumentProcessingPipeline<T> {

    private final DocumentParser<T> parser;
    private final DocumentChunker chunker;

    public DocumentProcessingPipeline(DocumentParser<T> parser, DocumentChunker chunker) {
        this.parser = parser;
        this.chunker = chunker;
    }

    public List<ParsedChunk> process(T source, SplitterConfig config) {
        final ParsedDocument parsedDocument = parser.parse(source);
        return chunker.chunk(parsedDocument, config);
    }
}
