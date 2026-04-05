# Learning Path for Junior Developers

This path is designed to help you learn Java + Spring + AI integration by reading this project in a practical order.

## Stage 1: Core Java records and interfaces

Read:
- `src/main/java/com/example/tree/retriever/core/DocumentTreeNode.java`
- `src/main/java/com/example/tree/retriever/core/TreeIndex.java`
- `src/main/java/com/example/tree/retriever/api/TraversalEngine.java`
- `src/main/java/com/example/tree/retriever/api/TreeIndexBuilder.java`

Learn:
- How immutable records model domain state.
- How interfaces separate contract from implementation.

Exercise:
- Add a new metadata key to `DocumentTreeNode` and trace where it appears later in retrieval output.

## Stage 2: Parsing and chunking

Read:
- `src/main/java/com/example/tree/retriever/indexing/SpringAiDocumentParser.java`
- `src/main/java/com/example/tree/retriever/indexing/OverlappingTextChunker.java`
- `src/main/java/com/example/tree/retriever/indexing/SplitterConfig.java`

Learn:
- How raw text becomes normalized chunks.
- Why overlap helps preserve context between adjacent chunks.

Exercise:
- Change chunk size/overlap and reason about how many chunks a sample paragraph produces.

## Stage 3: Summarization and decision parsing

Read:
- `src/main/java/com/example/tree/retriever/indexing/summarization/ChatClientSummarizationEngine.java`
- `src/main/java/com/example/tree/retriever/traversal/TraversalDecisionTextParser.java`

Learn:
- Strict JSON parsing for LLM outputs.
- Retry and fallback patterns for resilience.

Exercise:
- Simulate malformed JSON in a test and verify fallback behavior is used.

## Stage 4: Traversal engine

Read:
- `src/main/java/com/example/tree/retriever/traversal/IterativeDfsTraversalEngine.java`
- `src/main/java/com/example/tree/retriever/traversal/ChatClientTraversalDecisionEvaluator.java`

Learn:
- Iterative DFS with deadline budget.
- Why heuristic fallback protects retrieval reliability.

Exercise:
- Add a test case where evaluator times out for some nodes and verify best-so-far still returns results.

## Stage 5: Spring Boot wiring

Read:
- `src/main/java/com/example/tree/retriever/spring/TreeRetrieverAutoConfiguration.java`
- `src/main/java/com/example/tree/retriever/spring/TreeRetrieverProperties.java`

Learn:
- Conditional bean creation.
- Property-driven configuration.
- Starter-style extension points (`@ConditionalOnMissingBean`).

Exercise:
- Override one bean in a test app context and confirm auto-config backs off correctly.

## Stage 6: Testing and safe extension

Read:
- `src/test/java/com/example/tree/retriever/api/TreeDocumentRetrieverTest.java`
- `src/test/java/com/example/tree/retriever/traversal/IterativeDfsTraversalEngineTest.java`
- `src/test/java/com/example/tree/retriever/indexing/summarization/ChatClientSummarizationEngineTest.java`

Learn:
- Unit testing without Spring context.
- How to mock collaborators (`ChatClient`, evaluator interfaces).
- How to guard regressions before extending behavior.

Exercise:
- Add a new property (for example, min relevance threshold), wire it into traversal, and write tests before implementation.
