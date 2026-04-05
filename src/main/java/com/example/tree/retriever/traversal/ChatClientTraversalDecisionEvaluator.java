package com.example.tree.retriever.traversal;

import com.example.tree.retriever.api.TraversalDecisionEvaluator;
import com.example.tree.retriever.core.DocumentTreeNode;
import com.example.tree.retriever.core.TraversalDecision;
import org.springframework.ai.chat.client.ChatClient;

public class ChatClientTraversalDecisionEvaluator implements TraversalDecisionEvaluator {

    private static final String SYSTEM_PROMPT = """
            You are a retrieval traversal planner.
            Return strict JSON:
            {"includeNode":true|false,"expandChildren":true|false,"relevanceScore":0.0-1.0,"rationale":"..."}
            """;

    private final ChatClient chatClient;
    private final TraversalDecisionTextParser parser;

    public ChatClientTraversalDecisionEvaluator(ChatClient chatClient, TraversalDecisionTextParser parser) {
        this.chatClient = chatClient;
        this.parser = parser;
    }

    @Override
    public TraversalDecision evaluate(String query, DocumentTreeNode node) {
        String response = chatClient.prompt()
                .system(SYSTEM_PROMPT)
                .user("""
                        Query:
                        %s

                        NodeId: %s
                        Content:
                        %s
                        """.formatted(query, node.nodeId(), node.content()))
                .call()
                .content();
        return parser.parse(node.nodeId(), response);
    }
}
