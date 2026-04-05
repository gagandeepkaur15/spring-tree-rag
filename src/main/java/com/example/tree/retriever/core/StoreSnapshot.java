package com.example.tree.retriever.core;

import java.time.Instant;
import java.util.Map;

public record StoreSnapshot<K, V>(
        long version,
        Instant publishedAt,
        Map<K, V> entries
) {

    public StoreSnapshot {
        publishedAt = publishedAt == null ? Instant.EPOCH : publishedAt;
        entries = entries == null ? Map.of() : Map.copyOf(entries);
    }
}
