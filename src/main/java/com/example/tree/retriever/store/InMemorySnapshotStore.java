package com.example.tree.retriever.store;

import com.example.tree.retriever.core.SnapshotStore;
import com.example.tree.retriever.core.StoreSnapshot;
import com.example.tree.retriever.core.StoreStats;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.LongAdder;

/**
 * In-memory implementation that publishes immutable snapshots after successful mutations.
 */
public class InMemorySnapshotStore<K, V> implements SnapshotStore<K, V> {

    private final ConcurrentHashMap<K, V> state = new ConcurrentHashMap<>();
    private final AtomicLong version = new AtomicLong();
    private final AtomicReference<StoreSnapshot<K, V>> currentSnapshot = new AtomicReference<>(
            new StoreSnapshot<>(0L, Instant.EPOCH, Map.of())
    );

    private final LongAdder reads = new LongAdder();
    private final LongAdder readHits = new LongAdder();
    private final LongAdder readMisses = new LongAdder();
    private final LongAdder writeAttempts = new LongAdder();
    private final LongAdder successfulWrites = new LongAdder();
    private final LongAdder removeAttempts = new LongAdder();
    private final LongAdder successfulRemoves = new LongAdder();
    private final LongAdder snapshotPublications = new LongAdder();

    @Override
    public boolean put(K key, V value) {
        Objects.requireNonNull(key, "key must not be null");
        Objects.requireNonNull(value, "value must not be null");
        writeAttempts.increment();

        AtomicReference<Boolean> changed = new AtomicReference<>(false);
        state.compute(key, (k, existing) -> {
            if (Objects.equals(existing, value)) {
                return existing;
            }
            changed.set(true);
            return value;
        });

        if (changed.get()) {
            successfulWrites.increment();
            publishSnapshot();
            return true;
        }
        return false;
    }

    @Override
    public Optional<V> get(K key) {
        Objects.requireNonNull(key, "key must not be null");
        reads.increment();

        V value = state.get(key);
        if (value == null) {
            readMisses.increment();
            return Optional.empty();
        }

        readHits.increment();
        return Optional.of(value);
    }

    @Override
    public boolean remove(K key) {
        Objects.requireNonNull(key, "key must not be null");
        removeAttempts.increment();

        V removed = state.remove(key);
        if (removed != null) {
            successfulRemoves.increment();
            publishSnapshot();
            return true;
        }
        return false;
    }

    @Override
    public StoreSnapshot<K, V> snapshot() {
        return currentSnapshot.get();
    }

    @Override
    public StoreStats stats() {
        return new StoreStats(
                reads.sum(),
                readHits.sum(),
                readMisses.sum(),
                writeAttempts.sum(),
                successfulWrites.sum(),
                removeAttempts.sum(),
                successfulRemoves.sum(),
                snapshotPublications.sum(),
                state.size()
        );
    }

    private void publishSnapshot() {
        long nextVersion = version.incrementAndGet();
        StoreSnapshot<K, V> next = new StoreSnapshot<>(nextVersion, Instant.now(), Map.copyOf(state));
        currentSnapshot.set(next);
        snapshotPublications.increment();
    }
}
