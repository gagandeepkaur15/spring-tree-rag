package com.example.tree.retriever.traversal;

import com.example.tree.retriever.core.DocumentTreeNode;
import com.example.tree.retriever.core.TraversalDecision;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Optional;

/**
 * Parses text payloads (typically LLM JSON responses) into traversal decisions.
 */
public final class TraversalDecisionTextParser {

    private final ObjectMapper objectMapper;

    public TraversalDecisionTextParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public Optional<TraversalDecision> parse(String payload, DocumentTreeNode node) {
        if (payload == null || payload.isBlank() || node == null) {
            return Optional.empty();
        }

        try {
            JsonNode root = objectMapper.readTree(payload);
            boolean includeNode = root.path("includeNode").asBoolean(false);
            boolean expandChildren = root.path("expandChildren").asBoolean(true);
            double relevanceScore = root.path("relevanceScore").asDouble(0.0d);
            String rationale = root.path("rationale").asText("Parsed from payload");
            return Optional.of(new TraversalDecision(node.nodeId(), includeNode, expandChildren, relevanceScore, rationale));
        } catch (Exception ignored) {
            return Optional.empty();
        }
    }

    public TraversalDecision parse(String nodeId, String payload) {
        if (payload == null || payload.isBlank()) {
            return new TraversalDecision(nodeId, false, true, 0.0d, "Empty payload");
        }
        try {
            JsonNode root = objectMapper.readTree(payload);
            boolean includeNode = root.path("includeNode").asBoolean(false);
            boolean expandChildren = root.path("expandChildren").asBoolean(true);
            double relevanceScore = root.path("relevanceScore").asDouble(0.0d);
            String rationale = root.path("rationale").asText("Parsed from payload");
            return new TraversalDecision(nodeId, includeNode, expandChildren, relevanceScore, rationale);
        } catch (Exception ignored) {
            return new TraversalDecision(nodeId, false, true, 0.0d, "Malformed payload");
        }
    }
}
