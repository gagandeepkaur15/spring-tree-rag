# Complete User Flow: End-to-End

This document provides a **complete walkthrough** of how a user sets up and uses the `spring-ai-tree-retriever` library, with inputs, configuration, and expected outputs.

---

## Phase 1: Setup & Configuration

### Step 1.1: Create a Spring Boot Application

User creates a new Spring Boot application (consumer app):

```bash
mvn archetype:generate \
  -DgroupId=com.example \
  -DartifactId=my-rag-app \
  -DarchetypeArtifactId=maven-archetype-quickstart
```

### Step 1.2: Add Dependencies

**File: `pom.xml`**

```xml
<dependencies>
    <!-- This library -->
    <dependency>
        <groupId>com.example</groupId>
        <artifactId>spring-ai-tree-retriever</artifactId>
        <version>0.0.1-SNAPSHOT</version>
    </dependency>

    <!-- Spring Boot Base -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
        <version>3.3.5</version>
    </dependency>

    <!-- Choose ONE LLM Provider (example: OpenAI) -->
    <dependency>
        <groupId>org.springframework.ai</groupId>
        <artifactId>spring-ai-openai-spring-boot-starter</artifactId>
        <version>1.0.0</version>
    </dependency>

    <!-- Optional: for actuator endpoints -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-actuator</artifactId>
        <version>3.3.5</version>
    </dependency>
</dependencies>
```

### Step 1.3: Configure the Application

**File: `application.yaml`**

```yaml
spring:
  application:
    name: my-rag-app

  # LLM Provider Configuration (OpenAI example)
  ai:
    openai:
      api-key: ${OPENAI_API_KEY}  # Set environment variable
      model: gpt-4
    
    # Tree Retriever Configuration
    tree-retriever:
      max-results: 5                # Return top 5 documents
      traversal-deadline: 5s         # Max 5 seconds for entire traversal
      llm-timeout: 3s               # Max 3 seconds per LLM call
      llm-retries: 1                # Retry once if LLM response is malformed
      branch-factor: 4              # Group max 4 chunks per parent node

# Optional: Enable actuator endpoints for monitoring
management:
  endpoints:
    web:
      exposure:
        include: treeRetriever
```

### Step 1.4: Set Environment Variables

```bash
export OPENAI_API_KEY="sk-..."
export SPRING_PROFILE_ACTIVE=prod
```

---

## Phase 2: Index Building (Document Preparation)

### Step 2.1: Prepare Documents

User has documents they want to search through:

**Input Documents:**
```
documents/
├── billing-guide.md
├── api-reference.md
└── troubleshooting.txt
```

### Step 2.2: Create Index Building Service

**File: `src/main/java/com/example/IndexBuildingService.java`**

```java
@Service
public class IndexBuildingService {
    
    private final TreeIndexBuilder treeIndexBuilder;
    private final TreeIndexStore treeIndexStore;

    public IndexBuildingService(TreeIndexBuilder treeIndexBuilder, TreeIndexStore treeIndexStore) {
        this.treeIndexBuilder = treeIndexBuilder;
        this.treeIndexStore = treeIndexStore;
    }

    public void buildIndexFromMarkdown() {
        // Step 1: Parse documents
        List<ParsedDocument> parsedDocs = new ArrayList<>();
        try {
            String billingContent = Files.readString(Paths.get("documents/billing-guide.md"));
            String apiContent = Files.readString(Paths.get("documents/api-reference.md"));
            
            DocumentProcessingPipeline<String> pipeline = BaselinePipelines.markdown();
            parsedDocs.addAll(pipeline.process(billingContent));
            parsedDocs.addAll(pipeline.process(apiContent));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Step 2: Build tree (LLM summarization happens here)
        TreeIndex treeIndex = treeIndexBuilder.build(parsedDocs);

        // Step 3: Store in index store
        treeIndexStore.publish(treeIndex);

        System.out.println("✅ Index built with " + countLeaves(treeIndex.root()) + " document chunks");
    }

    private int countLeaves(DocumentTreeNode node) {
        return node.children().isEmpty() ? 1 : node.children().stream()
                .mapToInt(this::countLeaves).sum();
    }
}
```

### Step 2.3: Trigger Index Building

**File: `src/main/java/com/example/Application.java`**

```java
@SpringBootApplication
public class Application implements CommandLineRunner {

    @Autowired
    private IndexBuildingService indexBuildingService;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Override
    public void run(String... args) {
        System.out.println("🚀 Building document tree index...");
        indexBuildingService.buildIndexFromMarkdown();
    }
}
```

### Step 2.4: What Happens During Index Building

**Input → Processing → Output**

```
📄 Input Document:
"Billing is managed through our portal. Users pay monthly. 
 We accept credit cards and bank transfers. Invoices are 
 sent via email on the 1st of each month. For disputes, 
 contact billing@company.com"

    ↓ (DocumentChunker)
    
📦 Chunks Created (overlapping):
1. "Billing is managed through our portal. Users pay monthly."
2. "Users pay monthly. We accept credit cards and bank transfers."
3. "We accept credit cards and bank transfers. Invoices are sent via email..."
... (more overlapping chunks)

    ↓ (ChatClientSummarizationEngine - LLM Call #1)
    
📝 Leaf Node Summaries:
Chunk1 Summary: "Billing system and payment frequency"
Chunk2 Summary: "Payment methods accepted"
Chunk3 Summary: "Invoice delivery and billing contact"

    ↓ (Group & Summarize - LLM Call #2)
    
🌳 Parent Node Summary:
"Billing portal manages monthly payments via credit cards/transfers. 
 Invoices emailed on 1st. Contact billing@company.com for disputes."

    ↓ (Repeat for all documents)
    
🎯 Final Tree Index Created:
Root Node (summary of all documents)
├── Billing Section (parent)
│   ├── Chunk 1
│   ├── Chunk 2
│   └── Chunk 3
├── API Section (parent)
│   ├── Chunk 4
│   ├── Chunk 5
│   └── Chunk 6
└── Troubleshooting Section (parent)
    ├── Chunk 7
    └── Chunk 8
```

**Result:** Tree index stored in memory, ready for queries

---

## Phase 3: Query Time (User Asking Questions)

### Step 3.1: Create Query Service

**File: `src/main/java/com/example/QueryService.java`**

```java
@Service
public class QueryService {
    
    private final DocumentRetriever documentRetriever;

    public QueryService(DocumentRetriever documentRetriever) {
        this.documentRetriever = documentRetriever;
    }

    public List<Document> answerQuestion(String userQuestion) {
        return documentRetriever.retrieve(new Query(userQuestion));
    }
}
```

### Step 3.2: Create REST API Endpoint

**File: `src/main/java/com/example/RagController.java`**

```java
@RestController
@RequestMapping("/api/rag")
public class RagController {
    
    @Autowired
    private QueryService queryService;

    @PostMapping("/ask")
    public ResponseEntity<?> askQuestion(@RequestBody QuestionRequest request) {
        List<Document> results = queryService.answerQuestion(request.getQuestion());
        
        return ResponseEntity.ok(new QuestionResponse(
            request.getQuestion(),
            results.stream()
                .map(doc -> new DocumentResponse(
                    doc.getText(),
                    doc.getMetadata()
                ))
                .collect(Collectors.toList())
        ));
    }
}

record QuestionRequest(String question) {}
record DocumentResponse(String content, Map<String, Object> metadata) {}
record QuestionResponse(String question, List<DocumentResponse> relevantDocuments) {}
```

### Step 3.3: User Asks a Question

**User Input (HTTP Request):**

```bash
curl -X POST http://localhost:8080/api/rag/ask \
  -H "Content-Type: application/json" \
  -d '{
    "question": "What payment methods do you accept?"
  }'
```

### Step 3.4: What Happens During Query (LLM-Guided Tree Traversal)

**Step-by-Step Execution:**

```
🔍 USER QUESTION:
"What payment methods do you accept?"

    ↓ Query passed to TreeDocumentRetriever
    
🌳 TRAVERSAL PHASE:
Start at Root Node
├─ LLM Call #1: "Is root node relevant to payment methods?" 
│  System: "You are a retrieval traversal planner..."
│  Response: {"includeNode": true, "expandChildren": true, "relevanceScore": 0.95}
│
├─ ✅ Include Root, expand children
│
├─ LLM Call #2: "Evaluate Billing Section node"
│  User Prompt: "Query: What payment methods do you accept?
│                NodeId: section_billing
│                Content: Billing portal manages monthly payments via credit cards/transfers..."
│  Response: {"includeNode": true, "expandChildren": true, "relevanceScore": 0.98}
│
├─ ✅ Include Billing Section, expand its children
│
├─ LLM Call #3: "Evaluate Chunk 2 (Payment Methods)"
│  User Prompt: "Query: What payment methods do you accept?
│                NodeId: chunk_2
│                Content: We accept credit cards and bank transfers. Invoices are sent..."
│  Response: {"includeNode": true, "expandChildren": false, "relevanceScore": 0.99}
│
├─ ✅ SELECTED! This chunk is highly relevant
│
├─ LLM Call #4: "Evaluate API Section node"
│  Response: {"includeNode": false, "expandChildren": false, "relevanceScore": 0.15}
│
└─ ❌ SKIP: Not relevant to payment methods

TRAVERSAL COMPLETE (took 2.3 seconds)
```

### Step 3.5: User Receives Results

**API Response (JSON):**

```json
{
  "question": "What payment methods do you accept?",
  "relevantDocuments": [
    {
      "content": "We accept credit cards and bank transfers. Invoices are sent via email on the 1st of each month. For disputes, contact billing@company.com",
      "metadata": {
        "source": "billing-guide.md",
        "format": "markdown",
        "nodeId": "chunk_2",
        "relevanceScore": 0.99
      }
    },
    {
      "content": "Billing portal manages monthly payments via credit cards/transfers. Invoices emailed on 1st. Contact billing@company.com for disputes.",
      "metadata": {
        "source": "billing-guide.md",
        "format": "markdown",
        "nodeId": "section_billing",
        "relevanceScore": 0.98
      }
    }
  ]
}
```

---

## Phase 4: Monitoring & Troubleshooting

### Step 4.1: Check Index Status

**Actuator Endpoint:**

```bash
curl http://localhost:8080/actuator/treeRetriever
```

**Response:**

```json
{
  "hasIndex": true,
  "storeStats": {
    "totalNodes": 42,
    "totalLeafNodes": 12,
    "treeLevels": 4,
    "lastPublished": "2026-04-05T11:35:59Z"
  }
}
```

### Step 4.2: Monitor Performance

**What Gets Logged:**

```
INFO  IndexBuildingService - ✅ Index built with 12 document chunks
DEBUG ChatClientSummarizationEngine - Summarizing chunk (attempt 1/2)
DEBUG ChatClientTraversalDecisionEvaluator - Evaluating node section_1 for query: "What payment methods..."
WARN  ChatClientSummarizationEngine - LLM timeout on chunk_5, using fallback summary
INFO  TreeDocumentRetriever - Retrieved 2 documents in 2.3s
```

---

## Complete Data Flow Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                         SETUP PHASE                             │
├─────────────────────────────────────────────────────────────────┤
│ 1. User adds pom.xml dependencies                               │
│ 2. User configures application.yaml (LLM + tree settings)       │
│ 3. User sets OPENAI_API_KEY environment variable               │
│ 4. Spring auto-creates all necessary beans                      │
└─────────────────────────────┬───────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│                   INDEX BUILDING PHASE (One-time)               │
├─────────────────────────────────────────────────────────────────┤
│ INPUT: Raw documents (markdown, text files, PDFs)               │
│                              ↓                                  │
│ DocumentParser: Extract text & metadata                         │
│                              ↓                                  │
│ DocumentChunker: Split into overlapping chunks                  │
│                              ↓                                  │
│ TreeIndexBuilder with ChatClientSummarizationEngine:            │
│   - Group chunks by branch-factor (4)                           │
│   - LLM summarizes each group → Parent node                     │
│   - Repeat until single root node                               │
│                              ↓                                  │
│ OUTPUT: In-memory tree index (published to TreeIndexStore)      │
└─────────────────────────────┬───────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│                    QUERY TIME PHASE (Per query)                 │
├─────────────────────────────────────────────────────────────────┤
│ INPUT: User question (text string)                              │
│          ↓                                                      │
│ Query wrapped in Spring AI Query object                         │
│          ↓                                                      │
│ TreeDocumentRetriever.retrieve(query)                           │
│          ↓                                                      │
│ Load current TreeIndex from TreeIndexStore                      │
│          ↓                                                      │
│ IterativeDfsTraversalEngine starts at root                      │
│   For each node:                                                │
│     - ChatClientTraversalDecisionEvaluator evaluates            │
│       (sends query + node summary to LLM)                       │
│     - LLM returns: {"includeNode": bool, "expandChildren": bool}│
│     - Parser extracts decision                                  │
│     - Add included nodes to results                             │
│     - Continue DFS based on expandChildren                      │
│          ↓                                                      │
│ If LLM timeout or error:                                        │
│   - Use fallback heuristic scoring                              │
│          ↓                                                      │
│ Traverse for max traversal-deadline (5 seconds)                │
│          ↓                                                      │
│ Convert selected DocumentTreeNodes to Spring AI Documents       │
│          ↓                                                      │
│ OUTPUT: Top N documents (max-results: 5) sorted by relevance    │
└─────────────────────────────┬───────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│                         API RESPONSE PHASE                      │
├─────────────────────────────────────────────────────────────────┤
│ Return List<Document> with:                                     │
│   - Document text (original chunk content)                      │
│   - Metadata (source file, format, relevance score, etc)        │
│                                                                 │
│ User receives JSON with relevant documents                      │
└─────────────────────────────────────────────────────────────────┘
```

---

## Configuration Parameters Reference

| Parameter | Type | Default | Purpose |
|-----------|------|---------|---------|
| `max-results` | int | 5 | Maximum documents returned per query |
| `traversal-deadline` | Duration | 5s | Max time spent traversing the tree |
| `llm-timeout` | Duration | 3s | Timeout per individual LLM call |
| `llm-retries` | int | 1 | Retries if LLM returns malformed JSON |
| `branch-factor` | int | 4 | Max children per parent during indexing |

---

## Example Outputs at Each Stage

### Index Building Output
```
✅ Index built with 12 document chunks
📊 Tree Structure:
   - 1 Root node
   - 3 Section nodes (level 1)
   - 8 Subsection nodes (level 2)
   - 12 Leaf chunks (level 3)
   - Total LLM calls: ~15 (for summarization)
```

### Query Response Output
```json
{
  "question": "What payment methods do you accept?",
  "executionTimeMs": 2341,
  "relevantDocuments": [
    {
      "score": 0.99,
      "content": "We accept credit cards and bank transfers...",
      "source": "billing-guide.md"
    }
  ],
  "stats": {
    "nodesEvaluated": 8,
    "llmCallsMade": 4,
    "fallbacksUsed": 0
  }
}
```

---

## Summary

**User Journey:**
1. ✅ **Setup**: Add dependency, configure application.yaml, set API keys
2. ✅ **Index**: Call service to parse documents → LLM summarizes → Tree stored
3. ✅ **Query**: Send question → LLM guides traversal → Receive relevant documents
4. ✅ **Monitor**: Check actuator endpoint for stats and performance
