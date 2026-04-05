package com.example.tree.retriever.core;

import java.util.Optional;

/**
 * Thread-safe contract for mutable stores that publish immutable snapshots.
 */
public interface SnapshotStore<K, V> {

    boolean put(K key, V value);

    Optional<V> get(K key);

    boolean remove(K key);

    StoreSnapshot<K, V> snapshot();

    StoreStats stats();
}
