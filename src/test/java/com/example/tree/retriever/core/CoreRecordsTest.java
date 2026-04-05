package com.example.tree.retriever.core;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class CoreRecordsTest {

    @Test
    void documentTreeNodeAndTreeIndexExposeAssignedValues() {
        DocumentTreeNode child = new DocumentTreeNode("child-1", "root-1", "child-content", Map.of("level", 1), List.of());
        DocumentTreeNode root = new DocumentTreeNode("root-1", null, "root-content", Map.of("level", 0), List.of("child-1"));

        TreeIndex treeIndex = new TreeIndex(
                "idx-1",
                "root-1",
                Map.of("root-1", root, "child-1", child),
                Instant.parse("2026-01-01T00:00:00Z")
        );

        assertThat(treeIndex.indexId()).isEqualTo("idx-1");
        assertThat(treeIndex.rootNodeId()).isEqualTo("root-1");
        assertThat(treeIndex.nodesById()).containsEntry("root-1", root);
        assertThat(treeIndex.nodesById().get("child-1").parentNodeId()).isEqualTo("root-1");
    }

    @Test
    void traversalDecisionAndResultExposeAssignedValues() {
        DocumentTreeNode selectedNode = new DocumentTreeNode("n-1", null, "selected", Map.of(), List.of());

        TraversalDecision decision = new TraversalDecision("n-1", true, false, 0.85d, "high relevance");
        TraversalResult result = new TraversalResult("what is selected", List.of("n-1", "n-2"), List.of(selectedNode));

        assertThat(decision.nodeId()).isEqualTo("n-1");
        assertThat(decision.includeNode()).isTrue();
        assertThat(decision.expandChildren()).isFalse();
        assertThat(decision.relevanceScore()).isEqualTo(0.85d);
        assertThat(decision.rationale()).isEqualTo("high relevance");

        assertThat(result.query()).isEqualTo("what is selected");
        assertThat(result.visitedNodeIds()).containsExactly("n-1", "n-2");
        assertThat(result.selectedNodes()).containsExactly(selectedNode);
    }
}
