package com.example.tree.retriever.indexing;

/**
 * Transforms an input source model into a normalized parsed document.
 *
 * @param <T> source model type
 */
@FunctionalInterface
public interface DocumentParser<T> {

    ParsedDocument parse(T source);
}
