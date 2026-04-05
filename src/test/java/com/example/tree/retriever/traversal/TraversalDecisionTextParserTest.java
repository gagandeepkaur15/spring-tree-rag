package com.example.tree.retriever.traversal;

import com.example.tree.retriever.core.DocumentTreeNode;
import com.example.tree.retriever.core.TraversalDecision;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class TraversalDecisionTextParserTest {

    @Test
    void parsesValidJsonPayload() {
        TraversalDecisionTextParser parser = new TraversalDecisionTextParser(new ObjectMapper());
        DocumentTreeNode node = new DocumentTreeNode("n1", null, "content", Map.of(), List.of());

        Optional<TraversalDecision> decision = parser.parse("""
                {
                  "includeNode": true,
                  "expandChildren": false,
                  "relevanceScore": 0.82,
                  "rationale": "High topical match"
                }
                """, node);

        assertThat(decision).isPresent();
        assertThat(decision.get().includeNode()).isTrue();
        assertThat(decision.get().expandChildren()).isFalse();
        assertThat(decision.get().relevanceScore()).isEqualTo(0.82d);
        assertThat(decision.get().nodeId()).isEqualTo("n1");
    }

    @Test
    void returnsEmptyForMalformedPayload() {
        TraversalDecisionTextParser parser = new TraversalDecisionTextParser(new ObjectMapper());
        DocumentTreeNode node = new DocumentTreeNode("n1", null, "content", Map.of(), List.of());

        Optional<TraversalDecision> decision = parser.parse("not-json", node);

        assertThat(decision).isEmpty();
    }
}
