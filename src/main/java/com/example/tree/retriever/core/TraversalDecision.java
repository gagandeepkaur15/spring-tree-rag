package com.example.tree.retriever.core;

/**
 * Traversal instruction produced for a single node evaluation.
 */
public record TraversalDecision(
        String nodeId,
        boolean includeNode,
        boolean expandChildren,
        double relevanceScore,
        String rationale
) {
}
