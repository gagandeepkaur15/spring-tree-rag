package com.example.tree.retriever.api;

import com.example.tree.retriever.core.TraversalResult;
import com.example.tree.retriever.core.TreeIndex;

/**
 * Contract for executing query-driven traversal over an immutable tree index.
 */
public interface TraversalEngine {

    TraversalResult traverse(TreeIndex treeIndex, String query, int maxResults);
}
