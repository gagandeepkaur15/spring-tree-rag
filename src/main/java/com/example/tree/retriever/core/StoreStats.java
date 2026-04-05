package com.example.tree.retriever.core;

public record StoreStats(
        long reads,
        long readHits,
        long readMisses,
        long writeAttempts,
        long successfulWrites,
        long removeAttempts,
        long successfulRemoves,
        long snapshotPublications,
        long currentSize
) {
}
