# spring-ai-tree-retriever

`spring-ai-tree-retriever` is a Spring Boot starter that implements **vectorless RAG** using a tree index and LLM-guided traversal.

This project is intentionally structured so junior developers can learn:
- Java records and interfaces,
- Spring Boot auto-configuration,
- Spring AI integration (`ChatClient` + `DocumentRetriever`),
- resilient retrieval design (timeouts, fallback, immutable snapshots).

## What this project solves

Most RAG systems require embeddings + vector databases. This project explores a different approach:
- build a **tree** from documents,
- summarize bottom-up,
- let an LLM guide traversal decisions at query time,
- return the best matching leaves.

No vector DB is required.

## How it works (high level)

1. Documents are parsed and chunked.
2. Chunks become leaf nodes.
3. Leaves are grouped and summarized into parent nodes until one root remains.
4. During retrieval, the traversal engine walks the tree using iterative DFS.
5. LLM decisions guide node inclusion/expansion.
6. If LLM output fails or times out, deterministic fallback scoring keeps retrieval safe.

For full architecture details, see [docs/architecture.md](docs/architecture.md).

## Project structure

- `src/main/java/com/example/tree/retriever/core`  
  Immutable domain records (`DocumentTreeNode`, `TreeIndex`, traversal/store records).
- `src/main/java/com/example/tree/retriever/indexing`  
  Parsing, chunking, and tree index construction.
- `src/main/java/com/example/tree/retriever/indexing/summarization`  
  `ChatClient` summarization + strict JSON parsing.
- `src/main/java/com/example/tree/retriever/traversal`  
  Iterative DFS traversal and decision parsing.
- `src/main/java/com/example/tree/retriever/store`  
  Snapshot-based in-memory storage and stats.
- `src/main/java/com/example/tree/retriever/spring`  
  Auto-configuration, properties, and actuator endpoint.
- `src/test/java/com/example/tree/retriever`  
  Unit and integration-boundary tests.

For a file-by-file guide, see [docs/code-tour.md](docs/code-tour.md).

## Requirements

- Java 21
- Maven 3.9+ (or Maven wrapper if added later)
- A Spring AI-compatible `ChatClient` provider in the consuming app

## Dependency

```xml
<dependency>
    <groupId>com.example</groupId>
    <artifactId>spring-ai-tree-retriever</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

## Auto-configuration behavior

`TreeRetrieverAutoConfiguration` wires beans when dependencies exist:
- Always: `TreeRetrieverProperties`, `TreeIndexStore`
- When `ChatClient` is present: summarization + decision evaluator
- When evaluator exists: traversal engine
- When traversal exists: `DocumentRetriever`
- When Actuator is present: `treeRetriever` endpoint

This ensures safe startup in different environments.

## Configuration reference

All properties live in `spring.ai.tree-retriever`:

```yaml
spring:
  ai:
    tree-retriever:
      max-results: 5
      traversal-deadline: 5s
      llm-timeout: 3s
      llm-retries: 1
      branch-factor: 4
```

- `max-results`: max documents returned
- `traversal-deadline`: max time for a traversal call
- `llm-timeout`: timeout per LLM call
- `llm-retries`: retry count for malformed LLM output
- `branch-factor`: max children grouped when building upper tree layers

## Minimal usage

Inject Spring AI `DocumentRetriever`:

```java
List<Document> docs = documentRetriever.retrieve(new Query("How does billing work?"));
```

Internally this routes through `TreeDocumentRetriever`.

## Build and test

- Compile: `mvn -DskipTests compile`
- Unit tests: `mvn test`
- Integration-boundary tests: `mvn verify` (runs `*IT.java` via Failsafe)
- Optional live integration toggle in this project:
  - `mvn -Drun.integration.tests=true verify`

More operational help is in [docs/run-test-troubleshoot.md](docs/run-test-troubleshoot.md).

## Learning path for juniors

1. Start with [docs/learning-path.md](docs/learning-path.md)
2. Read architecture: [docs/architecture.md](docs/architecture.md)
3. Follow file walkthrough: [docs/code-tour.md](docs/code-tour.md)
4. Use API/config details: [docs/reference.md](docs/reference.md)

## Design principles used

- Immutable data models for thread safety
- Interface-first contracts for extension
- Constructor injection (no field injection)
- Defensive fallback behavior on LLM failures
- Spring abstractions over provider-specific APIs
