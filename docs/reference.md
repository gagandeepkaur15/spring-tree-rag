# API and Configuration Reference

## Public contracts

### `TraversalEngine`
Path: `src/main/java/com/example/tree/retriever/api/TraversalEngine.java`

Purpose:
- Defines query traversal over a `TreeIndex`.

Method:
- `TraversalResult traverse(TreeIndex treeIndex, String query, int maxResults)`

### `TraversalDecisionEvaluator`
Path: `src/main/java/com/example/tree/retriever/api/TraversalDecisionEvaluator.java`

Purpose:
- Produces a node-level decision for inclusion and expansion.

### `TreeIndexBuilder`
Path: `src/main/java/com/example/tree/retriever/api/TreeIndexBuilder.java`

Purpose:
- Builds immutable tree index snapshots from node lists.

### `TreeDocumentRetriever`
Path: `src/main/java/com/example/tree/retriever/api/TreeDocumentRetriever.java`

Purpose:
- Adapter that implements Spring AI `DocumentRetriever`.

## Core records

- `DocumentTreeNode`: node id, content, metadata, children.
- `TreeIndex`: immutable index snapshot (root + node map + timestamp).
- `TraversalDecision`: include/expand/score/rationale.
- `TraversalResult`: visited nodes + selected nodes.
- `StoreSnapshot`: immutable store publication.
- `StoreStats`: counters for store behavior.

## Configuration properties

Source: `src/main/java/com/example/tree/retriever/spring/TreeRetrieverProperties.java`

Prefix: `spring.ai.tree-retriever`

| Property | Default | Effect |
|---|---|---|
| `max-results` | `5` | Max documents returned by retriever |
| `traversal-deadline` | `5s` | Total traversal time budget |
| `llm-timeout` | `3s` | Timeout per LLM call |
| `llm-retries` | `1` | Retry count for malformed model output |
| `branch-factor` | `4` | Group size while building parent layers |

## Auto-configured beans

Source: `src/main/java/com/example/tree/retriever/spring/TreeRetrieverAutoConfiguration.java`

Primary beans:
- `ExecutorService treeTraversalExecutor()`
- `SummarizationEngine summarizationEngine(...)`
- `TreeIndexBuilder treeIndexBuilder(...)`
- `TraversalDecisionTextParser traversalDecisionTextParser(...)`
- `TraversalDecisionEvaluator traversalDecisionEvaluator(...)`
- `TraversalEngine traversalEngine(...)`
- `TreeIndexStore treeIndexStore()`
- `DocumentRetriever documentRetriever(...)`
- `TreeRetrieverEndpoint treeRetrieverEndpoint(...)`

Condition notes:
- many beans use `@ConditionalOnMissingBean` for override safety.
- LLM-dependent beans require `ChatClient` presence.

## Actuator endpoint

Endpoint id: `treeRetriever`

Operation:
- `@ReadOperation stats()`

Returns:
- `hasIndex`: whether an index is published
- `storeStats`: snapshot store counters and size

## Extension points

You can customize behavior by providing your own beans:

- Custom summarizer:
  - provide a `SummarizationEngine` bean
- Custom traversal evaluator:
  - provide a `TraversalDecisionEvaluator` bean
- Custom traversal strategy:
  - provide a `TraversalEngine` bean
- Custom index store:
  - provide a `TreeIndexStore` bean
- Custom index builder:
  - provide a `TreeIndexBuilder` bean

Because auto-config uses `@ConditionalOnMissingBean`, your bean takes priority.
