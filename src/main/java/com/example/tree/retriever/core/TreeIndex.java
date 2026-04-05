package com.example.tree.retriever.core;

import java.time.Instant;
import java.util.Map;

/**
 * Immutable in-memory tree index contract for retrieval operations.
 */
public record TreeIndex(
        String indexId,
        String rootNodeId,
        Map<String, DocumentTreeNode> nodesById,
        Instant createdAt
) {
}
