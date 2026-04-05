package com.example.tree.retriever.indexing;

import com.example.tree.retriever.api.TreeIndexBuilder;
import com.example.tree.retriever.core.DocumentTreeNode;
import com.example.tree.retriever.core.TreeIndex;
import com.example.tree.retriever.indexing.summarization.SummarizationEngine;
import com.example.tree.retriever.indexing.summarization.SummarizationResult;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class DefaultTreeIndexBuilder implements TreeIndexBuilder {

    private final SummarizationEngine summarizationEngine;
    private final int branchFactor;

    public DefaultTreeIndexBuilder(SummarizationEngine summarizationEngine, int branchFactor) {
        this.summarizationEngine = summarizationEngine;
        this.branchFactor = Math.max(2, branchFactor);
    }

    @Override
    public TreeIndex build(String indexId, List<DocumentTreeNode> nodes) {
        List<DocumentTreeNode> currentLayer = new ArrayList<>(nodes == null ? List.of() : nodes);
        if (currentLayer.isEmpty()) {
            String emptyId = "node-" + UUID.randomUUID();
            currentLayer = List.of(new DocumentTreeNode(emptyId, null, "Empty index", Map.of(), List.of()));
        }

        Map<String, DocumentTreeNode> allNodes = new HashMap<>();
        currentLayer.forEach(node -> allNodes.put(node.nodeId(), node));

        while (currentLayer.size() > 1) {
            List<DocumentTreeNode> nextLayer = new ArrayList<>();
            for (int i = 0; i < currentLayer.size(); i += branchFactor) {
                List<DocumentTreeNode> group = currentLayer.subList(i, Math.min(i + branchFactor, currentLayer.size()));
                String parentId = "node-" + UUID.randomUUID();
                String groupedContent = group.stream().map(DocumentTreeNode::content).reduce("", (a, b) -> a + "\n" + b);
                SummarizationResult summary = summarizationEngine.summarize(groupedContent);
                List<String> childIds = group.stream().map(DocumentTreeNode::nodeId).toList();
                DocumentTreeNode parent = new DocumentTreeNode(
                        parentId,
                        null,
                        summary.summary(),
                        Map.of("fallbackUsed", summary.fallbackUsed(), "keyPoints", summary.keyPoints()),
                        childIds
                );
                nextLayer.add(parent);
                allNodes.put(parentId, parent);
            }
            currentLayer = nextLayer;
        }

        DocumentTreeNode root = currentLayer.get(0);
        return new TreeIndex(indexId, root.nodeId(), Map.copyOf(allNodes), Instant.now());
    }
}
