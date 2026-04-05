package com.example.tree.retriever.indexing;

/**
 * Configures fixed-size overlapping chunking behavior.
 *
 * @param chunkSize max chunk length in characters
 * @param chunkOverlap overlap between consecutive chunks in characters
 */
public record SplitterConfig(int chunkSize, int chunkOverlap) {

    public SplitterConfig {
        if (chunkSize <= 0) {
            throw new IllegalArgumentException("chunkSize must be > 0");
        }
        if (chunkOverlap < 0) {
            throw new IllegalArgumentException("chunkOverlap must be >= 0");
        }
        if (chunkOverlap >= chunkSize) {
            throw new IllegalArgumentException("chunkOverlap must be < chunkSize");
        }
    }

    public static SplitterConfig defaults() {
        return new SplitterConfig(1000, 200);
    }
}
