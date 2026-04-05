package com.example.tree.retriever.traversal;

import com.example.tree.retriever.api.TraversalDecisionEvaluator;
import com.example.tree.retriever.core.DocumentTreeNode;
import com.example.tree.retriever.core.TraversalDecision;
import com.example.tree.retriever.core.TraversalResult;
import com.example.tree.retriever.core.TreeIndex;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class IterativeDfsTraversalEngineTest {

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @AfterEach
    void tearDown() {
        executorService.shutdownNow();
    }

    @Test
    void returnsBestSoFarWhenEvaluatorFails() {
        TraversalDecisionEvaluator evaluator = mock(TraversalDecisionEvaluator.class);
        when(evaluator.evaluate(eq("alpha"), any(DocumentTreeNode.class)))
                .thenThrow(new RuntimeException("llm unavailable"));

        IterativeDfsTraversalEngine engine = new IterativeDfsTraversalEngine(
                evaluator,
                executorService,
                Duration.ofMillis(500),
                Clock.fixed(Instant.parse("2025-01-01T00:00:00Z"), ZoneOffset.UTC)
        );

        TreeIndex index = sampleTree();
        TraversalResult result = engine.traverse(index, "alpha", 2);

        assertThat(result.visitedNodeIds()).containsExactly("root", "child-1", "child-2");
        assertThat(result.selectedNodes())
                .extracting(DocumentTreeNode::nodeId)
                .containsExactly("child-1");
    }

    @Test
    void keepsDfsOrderAndSortsResultsByScore() {
        TraversalDecisionEvaluator evaluator = mock(TraversalDecisionEvaluator.class);
        when(evaluator.evaluate(eq("query"), any(DocumentTreeNode.class))).thenAnswer(invocation -> {
            DocumentTreeNode node = invocation.getArgument(1);
            return switch (node.nodeId()) {
                case "root" -> new TraversalDecision("root", true, true, 0.20d, "root");
                case "child-1" -> new TraversalDecision("child-1", true, false, 0.90d, "best");
                case "child-2" -> new TraversalDecision("child-2", true, false, 0.70d, "second");
                default -> new TraversalDecision(node.nodeId(), false, true, 0.0d, "none");
            };
        });

        IterativeDfsTraversalEngine engine = new IterativeDfsTraversalEngine(
                evaluator,
                executorService,
                Duration.ofSeconds(1),
                Clock.systemUTC()
        );

        TraversalResult result = engine.traverse(sampleTree(), "query", 2);

        assertThat(result.visitedNodeIds()).containsExactly("root", "child-1", "child-2");
        assertThat(result.selectedNodes())
                .extracting(DocumentTreeNode::nodeId)
                .containsExactly("child-1", "child-2");
    }

    private TreeIndex sampleTree() {
        DocumentTreeNode root = new DocumentTreeNode("root", null, "root summary", Map.of(), List.of("child-1", "child-2"));
        DocumentTreeNode child1 = new DocumentTreeNode("child-1", "root", "alpha topic details", Map.of(), List.of());
        DocumentTreeNode child2 = new DocumentTreeNode("child-2", "root", "beta topic details", Map.of(), List.of());

        return new TreeIndex(
                "index-1",
                "root",
                Map.of(
                        "root", root,
                        "child-1", child1,
                        "child-2", child2
                ),
                Instant.parse("2025-01-01T00:00:00Z")
        );
    }
}
