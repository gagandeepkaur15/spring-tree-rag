package com.example.tree.retriever.api;

import com.example.tree.retriever.core.DocumentTreeNode;

import java.util.List;
import java.util.Optional;

/**
 * Abstraction over node persistence and lookup for tree traversal.
 */
public interface TreeStore {

    Optional<DocumentTreeNode> findNodeById(String nodeId);

    Optional<DocumentTreeNode> findRootNode();

    List<DocumentTreeNode> findChildNodes(String parentNodeId);
}
