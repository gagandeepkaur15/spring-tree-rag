# Run, Test, and Troubleshoot

## Prerequisites

- Java 21 (`java -version`)
- Maven 3.9+ (`mvn -version`)

If Maven is missing, install it first or add a Maven wrapper (`mvnw`) to the project.

## Common commands

- Compile only:
  - `mvn -DskipTests compile`
- Run unit tests:
  - `mvn test`
- Run full verify (includes integration phase):
  - `mvn verify`
- Run optional integration behavior in this repo:
  - `mvn -Drun.integration.tests=true verify`

## Unit vs integration test boundary

The `pom.xml` uses:
- **Surefire** for unit tests and excludes `*IT.java`
- **Failsafe** for integration tests and includes `*IT.java`

So:
- `*Test.java` -> fast unit tests
- `*IT.java` -> integration lifecycle (`integration-test`, `verify`)

## Troubleshooting guide

### `mvn: command not found`

Cause: Maven is not installed or not on PATH.

Fix:
1. Install Maven.
2. Reopen terminal.
3. Re-run `mvn -version`.

### Java version mismatch

Cause: Project expects Java 21.

Fix:
1. Install JDK 21.
2. Set `JAVA_HOME`.
3. Verify with `java -version`.

### Retrieval returns empty list

Likely causes:
- No current `TreeIndex` published in `TreeIndexStore`
- Query text extraction fallback did not produce useful text
- Traversal `maxResults` is too low or zero

Check:
- index publish flow in `DefaultTreeIndexBuilder` + `TreeIndexStore`
- `TreeRetrieverProperties.maxResults`

### LLM decision/summarization parse failures

Symptoms:
- Frequent fallback behavior
- Poor relevance quality

Check:
- strict JSON schema expectations in:
  - `ChatClientSummarizationEngine`
  - `TraversalDecisionTextParser`
- response format in prompts
- `llm-timeout` and `llm-retries` values

### Traversal times out too quickly

Cause:
- `traversal-deadline` too short for tree depth and model latency.

Fix:
- increase `spring.ai.tree-retriever.traversal-deadline`
- tune branch factor and model timeout

## Debugging checklist

1. Confirm index exists.
2. Confirm query text is non-empty.
3. Confirm evaluator returns parseable decisions.
4. Check fallback path frequency in logs/metrics.
5. Validate actuator endpoint (if enabled) for store stats.
