package com.example.tree.retriever.core;

import java.util.List;

/**
 * Immutable result of one traversal execution.
 */
public record TraversalResult(
        String query,
        List<String> visitedNodeIds,
        List<DocumentTreeNode> selectedNodes
) {
}
