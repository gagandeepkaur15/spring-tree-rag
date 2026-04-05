package com.example.tree.retriever.indexing.summarization;

import java.util.List;

/**
 * Immutable summary output for a content segment.
 */
public record SummarizationResult(
        String summary,
        List<String> keyPoints,
        boolean fallbackUsed
) {
    public SummarizationResult {
        if (summary == null || summary.isBlank()) {
            throw new IllegalArgumentException("summary must not be blank");
        }
        keyPoints = keyPoints == null ? List.of() : List.copyOf(keyPoints);
    }
}
