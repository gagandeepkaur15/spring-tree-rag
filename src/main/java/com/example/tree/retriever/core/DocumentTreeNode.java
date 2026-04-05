package com.example.tree.retriever.core;

import java.util.List;
import java.util.Map;

/**
 * Immutable node representation used by the retriever tree index.
 */
public record DocumentTreeNode(
        String nodeId,
        String parentNodeId,
        String content,
        Map<String, Object> metadata,
        List<String> childNodeIds
) {
}
