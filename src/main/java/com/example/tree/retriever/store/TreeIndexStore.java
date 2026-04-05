package com.example.tree.retriever.store;

import com.example.tree.retriever.core.StoreStats;
import com.example.tree.retriever.core.TreeIndex;

import java.util.Optional;

public interface TreeIndexStore {

    void publish(TreeIndex index);

    Optional<TreeIndex> current();

    StoreStats stats();
}
