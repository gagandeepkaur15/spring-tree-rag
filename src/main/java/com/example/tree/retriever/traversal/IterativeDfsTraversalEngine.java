package com.example.tree.retriever.traversal;

import com.example.tree.retriever.api.TraversalDecisionEvaluator;
import com.example.tree.retriever.api.TraversalEngine;
import com.example.tree.retriever.core.DocumentTreeNode;
import com.example.tree.retriever.core.TraversalDecision;
import com.example.tree.retriever.core.TraversalResult;
import com.example.tree.retriever.core.TreeIndex;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

/**
 * Deadline-aware iterative DFS traversal engine with resilient fallback scoring.
 */
public final class IterativeDfsTraversalEngine implements TraversalEngine {

    private final TraversalDecisionEvaluator decisionEvaluator;
    private final ExecutorService evaluatorExecutor;
    private final Duration traversalDeadline;
    private final Clock clock;

    public IterativeDfsTraversalEngine(
            TraversalDecisionEvaluator decisionEvaluator,
            ExecutorService evaluatorExecutor,
            Duration traversalDeadline,
            Clock clock
    ) {
        this.decisionEvaluator = Objects.requireNonNull(decisionEvaluator, "decisionEvaluator must not be null");
        this.evaluatorExecutor = Objects.requireNonNull(evaluatorExecutor, "evaluatorExecutor must not be null");
        this.traversalDeadline = Objects.requireNonNull(traversalDeadline, "traversalDeadline must not be null");
        this.clock = Objects.requireNonNull(clock, "clock must not be null");
    }

    @Override
    public TraversalResult traverse(TreeIndex treeIndex, String query, int maxResults) {
        Objects.requireNonNull(treeIndex, "treeIndex must not be null");
        Objects.requireNonNull(query, "query must not be null");
        if (maxResults <= 0) {
            return new TraversalResult(query, List.of(), List.of());
        }

        Map<String, DocumentTreeNode> nodesById = treeIndex.nodesById() == null ? Map.of() : treeIndex.nodesById();
        String rootId = treeIndex.rootNodeId();
        if (rootId == null || !nodesById.containsKey(rootId)) {
            return new TraversalResult(query, List.of(), List.of());
        }

        Instant deadline = clock.instant().plus(traversalDeadline);
        ArrayDeque<String> stack = new ArrayDeque<>();
        stack.push(rootId);

        Set<String> visited = new HashSet<>();
        List<String> visitOrder = new ArrayList<>();
        List<ScoredNode> bestSoFar = new ArrayList<>();

        while (!stack.isEmpty() && !isTimedOut(deadline)) {
            String nodeId = stack.pop();
            if (!visited.add(nodeId)) {
                continue;
            }

            DocumentTreeNode node = nodesById.get(nodeId);
            if (node == null) {
                continue;
            }

            visitOrder.add(nodeId);
            TraversalDecision decision = evaluateWithFallback(query, node, deadline);

            if (decision.includeNode()) {
                bestSoFar.add(new ScoredNode(node, decision.relevanceScore()));
                bestSoFar.sort(Comparator.comparingDouble(ScoredNode::score).reversed());
                if (bestSoFar.size() > maxResults) {
                    bestSoFar = new ArrayList<>(bestSoFar.subList(0, maxResults));
                }
            }

            if (decision.expandChildren()) {
                List<String> childIds = node.childNodeIds() == null ? List.of() : node.childNodeIds();
                for (int i = childIds.size() - 1; i >= 0; i--) {
                    String childId = childIds.get(i);
                    if (childId != null && !visited.contains(childId)) {
                        stack.push(childId);
                    }
                }
            }
        }

        List<DocumentTreeNode> selectedNodes = bestSoFar.stream()
                .map(ScoredNode::node)
                .collect(Collectors.toList());

        return new TraversalResult(query, List.copyOf(visitOrder), selectedNodes);
    }

    private TraversalDecision evaluateWithFallback(String query, DocumentTreeNode node, Instant deadline) {
        long remainingMillis = Math.max(1L, Duration.between(clock.instant(), deadline).toMillis());
        Callable<TraversalDecision> task = () -> decisionEvaluator.evaluate(query, node);
        Future<TraversalDecision> future = evaluatorExecutor.submit(task);

        try {
            TraversalDecision decision = future.get(remainingMillis, TimeUnit.MILLISECONDS);
            return normalizeDecision(decision, node, query);
        } catch (TimeoutException ex) {
            future.cancel(true);
            return heuristicFallback(query, node, "Evaluator timed out");
        } catch (ExecutionException | InterruptedException | RuntimeException ex) {
            if (ex instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            return heuristicFallback(query, node, "Evaluator failed");
        }
    }

    private TraversalDecision normalizeDecision(TraversalDecision decision, DocumentTreeNode node, String query) {
        if (decision == null) {
            return heuristicFallback(query, node, "Null decision");
        }
        String effectiveNodeId = decision.nodeId() == null || decision.nodeId().isBlank() ? node.nodeId() : decision.nodeId();
        double score = Double.isFinite(decision.relevanceScore()) ? decision.relevanceScore() : 0.0d;
        String rationale = decision.rationale() == null ? "No rationale provided" : decision.rationale();
        return new TraversalDecision(effectiveNodeId, decision.includeNode(), decision.expandChildren(), score, rationale);
    }

    private TraversalDecision heuristicFallback(String query, DocumentTreeNode node, String rationalePrefix) {
        double score = heuristicScore(query, node.content());
        boolean includeNode = score > 0.0d;
        return new TraversalDecision(node.nodeId(), includeNode, true, score, rationalePrefix + " - heuristic fallback");
    }

    private double heuristicScore(String query, String content) {
        if (content == null || content.isBlank() || query.isBlank()) {
            return 0.0d;
        }
        Set<String> queryTokens = tokenize(query);
        if (queryTokens.isEmpty()) {
            return 0.0d;
        }
        Set<String> contentTokens = tokenize(content);
        if (contentTokens.isEmpty()) {
            return 0.0d;
        }
        long overlap = queryTokens.stream().filter(contentTokens::contains).count();
        return overlap / (double) queryTokens.size();
    }

    private Set<String> tokenize(String value) {
        return List.of(value.toLowerCase(Locale.ROOT).split("\\W+"))
                .stream()
                .filter(token -> !token.isBlank())
                .collect(Collectors.toSet());
    }

    private boolean isTimedOut(Instant deadline) {
        return !clock.instant().isBefore(deadline);
    }

    private record ScoredNode(DocumentTreeNode node, double score) {
    }
}
