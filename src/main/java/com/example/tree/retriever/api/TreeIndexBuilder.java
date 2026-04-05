package com.example.tree.retriever.api;

import com.example.tree.retriever.core.DocumentTreeNode;
import com.example.tree.retriever.core.TreeIndex;

import java.util.List;

/**
 * Contract for building an immutable tree index from source nodes.
 */
public interface TreeIndexBuilder {

    TreeIndex build(String indexId, List<DocumentTreeNode> nodes);
}
