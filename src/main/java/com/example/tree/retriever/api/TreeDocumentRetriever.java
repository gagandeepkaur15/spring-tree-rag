package com.example.tree.retriever.api;

import com.example.tree.retriever.core.DocumentTreeNode;
import com.example.tree.retriever.core.TreeIndex;
import com.example.tree.retriever.store.TreeIndexStore;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.retrieval.search.DocumentRetriever;

import java.util.List;
import java.util.Optional;

public class TreeDocumentRetriever implements DocumentRetriever {

    private final TreeIndexStore treeIndexStore;
    private final TraversalEngine traversalEngine;
    private final int maxResults;

    public TreeDocumentRetriever(TreeIndexStore treeIndexStore, TraversalEngine traversalEngine, int maxResults) {
        this.treeIndexStore = treeIndexStore;
        this.traversalEngine = traversalEngine;
        this.maxResults = maxResults;
    }

    @Override
    public List<Document> retrieve(Query query) {
        Optional<TreeIndex> maybeIndex = treeIndexStore.current();
        if (maybeIndex.isEmpty()) {
            return List.of();
        }
        String queryText = queryText(query);
        List<DocumentTreeNode> selectedNodes = traversalEngine.traverse(maybeIndex.get(), queryText, maxResults).selectedNodes();
        return selectedNodes.stream()
                .map(node -> new Document(node.content(), node.metadata()))
                .toList();
    }

    private String queryText(Query query) {
        if (query == null) {
            return "";
        }
        try {
            return (String) Query.class.getMethod("text").invoke(query);
        } catch (Exception ignored) {
            return query.toString();
        }
    }
}
