package com.example.tree.retriever.store;

import com.example.tree.retriever.core.StoreStats;
import com.example.tree.retriever.core.TreeIndex;

import java.util.Objects;
import java.util.Optional;

public class InMemoryTreeIndexStore implements TreeIndexStore {

    private static final String ACTIVE_INDEX_KEY = "active-index";

    private final InMemorySnapshotStore<String, TreeIndex> delegate = new InMemorySnapshotStore<>();

    @Override
    public void publish(TreeIndex index) {
        Objects.requireNonNull(index, "index must not be null");
        delegate.put(ACTIVE_INDEX_KEY, index);
    }

    @Override
    public Optional<TreeIndex> current() {
        return delegate.get(ACTIVE_INDEX_KEY);
    }

    @Override
    public StoreStats stats() {
        return delegate.stats();
    }
}
