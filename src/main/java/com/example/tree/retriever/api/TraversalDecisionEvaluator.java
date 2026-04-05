package com.example.tree.retriever.api;

import com.example.tree.retriever.core.DocumentTreeNode;
import com.example.tree.retriever.core.TraversalDecision;

/**
 * Evaluates a single node against a query and returns a traversal decision.
 */
public interface TraversalDecisionEvaluator {

    TraversalDecision evaluate(String query, DocumentTreeNode node);
}
