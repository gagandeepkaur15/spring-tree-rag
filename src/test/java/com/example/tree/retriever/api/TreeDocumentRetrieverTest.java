package com.example.tree.retriever.api;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.Query;

import com.example.tree.retriever.core.DocumentTreeNode;
import com.example.tree.retriever.core.StoreStats;
import com.example.tree.retriever.core.TraversalResult;
import com.example.tree.retriever.core.TreeIndex;
import com.example.tree.retriever.store.TreeIndexStore;

class TreeDocumentRetrieverTest {

    @Test
    void returnsEmptyWhenNoIndexAvailable() {
        TreeIndexStore treeIndexStore = new StubStore(Optional.empty());
        TraversalEngine traversalEngine = mock(TraversalEngine.class);
        TreeDocumentRetriever retriever = new TreeDocumentRetriever(treeIndexStore, traversalEngine, 3);

        List<Document> documents = retriever.retrieve(new Query("pricing"));

        assertThat(documents).isEmpty();
    }

    @Test
    void mapsSelectedNodesToSpringAiDocuments() {
        DocumentTreeNode root = new DocumentTreeNode("r", null, "root", Map.of(), List.of("n1"));
        DocumentTreeNode n1 = new DocumentTreeNode("n1", "r", "node-content", Map.of("source", "unit"), List.of());
        TreeIndex treeIndex = new TreeIndex("idx", "r", Map.of("r", root, "n1", n1), Instant.now());

        TreeIndexStore treeIndexStore = new StubStore(Optional.of(treeIndex));
        TraversalEngine traversalEngine = mock(TraversalEngine.class);
        when(traversalEngine.traverse(treeIndex, "pricing", 2))
                .thenReturn(new TraversalResult("pricing", List.of("r", "n1"), List.of(n1)));
        TreeDocumentRetriever retriever = new TreeDocumentRetriever(treeIndexStore, traversalEngine, 2);

        List<Document> documents = retriever.retrieve(new Query("pricing"));

        assertThat(documents).hasSize(1);
        assertThat(documents.get(0).getText()).isEqualTo("node-content");
    }

    private static class StubStore implements TreeIndexStore {
        private final Optional<TreeIndex> index;

        private StubStore(Optional<TreeIndex> index) {
            this.index = index;
        }

        @Override
        public void publish(TreeIndex index) {
        }

        @Override
        public Optional<TreeIndex> current() {
            return index;
        }

        @Override
        public StoreStats stats() {
            return new StoreStats(0, 0, 0, 0, 0, 0, 0, 0, 0);
        }
    }
}
