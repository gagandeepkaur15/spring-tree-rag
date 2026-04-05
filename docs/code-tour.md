# Code Tour (File-by-File)

Use this guide when reading the code for the first time.

## Main source files

| File | What it does | Why it exists | Read first |
|---|---|---|---|
| `src/main/java/com/example/tree/retriever/core/DocumentTreeNode.java` | Tree node record (content, metadata, children) | Basic unit of index + traversal | Record fields |
| `src/main/java/com/example/tree/retriever/core/TreeIndex.java` | Immutable full index snapshot | Retrieval reads one stable view | `rootNodeId`, `nodesById` |
| `src/main/java/com/example/tree/retriever/api/TreeDocumentRetriever.java` | Spring AI `DocumentRetriever` adapter | Public retrieval entrypoint | `retrieve()` |
| `src/main/java/com/example/tree/retriever/indexing/DefaultTreeIndexBuilder.java` | Builds tree bottom-up | Converts leaves into summarized hierarchy | `build()` |
| `src/main/java/com/example/tree/retriever/indexing/SpringAiDocumentParser.java` | Converts Spring AI `Document` to normalized parsed form | Standardizes source format | `parse()` |
| `src/main/java/com/example/tree/retriever/indexing/OverlappingTextChunker.java` | Splits text into overlapping chunks | Balances context retention and size | `chunk()` |
| `src/main/java/com/example/tree/retriever/indexing/summarization/ChatClientSummarizationEngine.java` | Calls LLM to summarize grouped node content | Creates parent-node summaries | `summarize()` |
| `src/main/java/com/example/tree/retriever/traversal/IterativeDfsTraversalEngine.java` | Deadline-aware iterative DFS | Performs robust query traversal | `traverse()` |
| `src/main/java/com/example/tree/retriever/traversal/ChatClientTraversalDecisionEvaluator.java` | Produces include/expand decisions from LLM output | Guides traversal path | `evaluate()` |
| `src/main/java/com/example/tree/retriever/store/InMemorySnapshotStore.java` | Concurrent map + snapshot publisher | Thread-safe mutable state with immutable reads | `put()`, `snapshot()` |
| `src/main/java/com/example/tree/retriever/spring/TreeRetrieverAutoConfiguration.java` | Registers starter beans conditionally | Plug-and-play Spring Boot behavior | Bean methods |
| `src/main/java/com/example/tree/retriever/spring/TreeRetrieverProperties.java` | Externalized configuration | Tunable runtime behavior | Property defaults |

## Representative test files

| File | What it validates | Why it matters |
|---|---|---|
| `src/test/java/com/example/tree/retriever/api/TreeDocumentRetrieverTest.java` | Retriever behavior with/without index | Public adapter correctness |
| `src/test/java/com/example/tree/retriever/traversal/IterativeDfsTraversalEngineTest.java` | DFS order, fallback behavior | Safety under failure/timeouts |
| `src/test/java/com/example/tree/retriever/indexing/summarization/ChatClientSummarizationEngineTest.java` | JSON parsing/retry/fallback | LLM-output robustness |
| `src/test/java/com/example/tree/retriever/store/InMemorySnapshotStoreTest.java` | Snapshot publish semantics + stats | Concurrency-safe state handling |
| `src/test/java/com/example/tree/retriever/spring/TreeRetrieverAutoConfigurationTest.java` | Bean wiring and conditions | Starter usability |

## Common junior pitfalls

- Forgetting null/empty checks in records and parser paths.
- Treating LLM output as trusted text instead of strict JSON.
- Using recursion for traversal instead of iterative stack-based logic.
- Mutating shared state directly instead of publishing snapshots.
- Adding Spring field injection instead of constructor-based wiring.

## Suggested read order

1. `core` records
2. `TreeDocumentRetriever`
3. `DefaultTreeIndexBuilder`
4. `ChatClientSummarizationEngine`
5. `IterativeDfsTraversalEngine`
6. `TreeRetrieverAutoConfiguration`
7. test classes listed above
