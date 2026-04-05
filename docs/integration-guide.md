# Integration Guide: Using spring-ai-tree-retriever in Your App

This guide explains **step-by-step** how to integrate the `spring-ai-tree-retriever` library into your existing Spring Boot application.

---

## Prerequisites

Before you start, ensure:
- ✅ `spring-ai-tree-retriever` is built and installed in your local Maven repository
- ✅ You have a Spring Boot 3.3.5+ application
- ✅ Java 21 is installed
- ✅ You have an LLM API key (OpenAI, Claude, etc.)

---

## Step 1: Build and Install the Library Locally

First, build the `spring-ai-tree-retriever` project and install it to your local Maven repository.

**From the library project directory:**

```bash
cd /path/to/spring-tree-rag
mvn clean install -DskipTests
```

**Output:**
```
[INFO] Installing /path/to/spring-tree-rag/target/spring-ai-tree-retriever-0.0.1-SNAPSHOT.jar to ~/.m2/repository/com/example/spring-ai-tree-retriever/0.0.1-SNAPSHOT/...
[INFO] BUILD SUCCESS
```

Now the library is available in your local Maven repository and can be used by other projects.

---

Steps:
1. Go to root folder- cd /Users/yourname/Documents/spring-tree-rag
2. Run maven install- mvn clean install -DskipTests
It creates a JAR and installs it here:
~/.m2/repository/com/example/spring-ai-tree-retriever/0.0.1-SNAPSHOT/
3. Check:
ls ~/.m2/repository/com/example/spring-ai-tree-retriever/
You should see:
0.0.1-SNAPSHOT/
4. Use in consumer app
Now your pom.xml works:
<dependency>
    <groupId>com.example</groupId>
    <artifactId>spring-ai-tree-retriever</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>

## Step 2: Create Your Consumer Application

Create a new Spring Boot application that will use this library:

```bash
mvn archetype:generate \
  -DgroupId=com.mycompany \
  -DartifactId=my-rag-application \
  -DarchetypeArtifactId=maven-archetype-quickstart
cd my-rag-application
```

---

## Step 3: Add Dependencies to Your Consumer App

Edit your `pom.xml` to include the library and required dependencies.

**File: `pom.xml`**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.3.5</version>
        <relativePath/>
    </parent>

    <groupId>com.mycompany</groupId>
    <artifactId>my-rag-application</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <name>My RAG Application</name>

    <properties>
        <java.version>21</java.version>
    </properties>

    <dependencies>
        <!-- 🔴 THE LIBRARY (this is the key dependency) -->
        <dependency>
            <groupId>com.example</groupId>
            <artifactId>spring-ai-tree-retriever</artifactId>
            <version>0.0.1-SNAPSHOT</version>
        </dependency>

        <!-- Spring Boot Web Starter (for REST endpoints) -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <!-- ⭐ Choose ONE LLM Provider -->
        <!-- Option A: OpenAI -->
        <dependency>
            <groupId>org.springframework.ai</groupId>
            <artifactId>spring-ai-openai-spring-boot-starter</artifactId>
            <version>1.0.0</version>
        </dependency>

        <!-- Option B: (Uncomment if using Claude instead)
        <dependency>
            <groupId>org.springframework.ai</groupId>
            <artifactId>spring-ai-anthropic-spring-boot-starter</artifactId>
            <version>1.0.0</version>
        </dependency>
        -->

        <!-- Optional: Actuator (for monitoring endpoints) -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>

        <!-- Testing -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

---

## Step 4: Configure Your Application

Create or update `application.yaml` in `src/main/resources/`.

**File: `src/main/resources/application.yaml`**

```yaml
spring:
  application:
    name: my-rag-application

  # ⭐ LLM Provider Configuration (OpenAI example)
  ai:
    openai:
      api-key: ${OPENAI_API_KEY}      # Set via environment variable
      model: gpt-4                     # Or gpt-3.5-turbo for cheaper option

    # 🌳 Tree Retriever Library Configuration
    tree-retriever:
      max-results: 5                   # Return top 5 documents
      traversal-deadline: 5s           # Max 5 seconds for tree traversal
      llm-timeout: 3s                  # Timeout per LLM call
      llm-retries: 1                   # Retry count for malformed JSON
      branch-factor: 4                 # Max children per tree node

# Optional: Enable actuator endpoints
management:
  endpoints:
    web:
      exposure:
        include: treeRetriever,health,info
  endpoint:
    health:
      show-details: always
```

### Alternative Configurations

**For Claude (Anthropic):**
```yaml
spring:
  ai:
    anthropic:
      api-key: ${ANTHROPIC_API_KEY}
      model: claude-3-sonnet-20240229
```

**For Ollama (local models):**
```yaml
spring:
  ai:
    ollama:
      base-url: http://localhost:11434
      model: llama2
```

---

## Step 5: Set Environment Variables

Before running your app, set the LLM API key:

**On macOS/Linux:**
```bash
export OPENAI_API_KEY="sk-..."
export SPRING_PROFILES_ACTIVE=default
```

**On Windows (PowerShell):**
```powershell
$env:OPENAI_API_KEY="sk-..."
```

---

## Step 6: Create Your Spring Boot Application Class

**File: `src/main/java/com/mycompany/RagApplication.java`**

```java
package com.mycompany;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class RagApplication {

    public static void main(String[] args) {
        SpringApplication.run(RagApplication.class, args);
    }
}
```

**That's it!** All the `spring-ai-tree-retriever` beans are auto-configured by Spring Boot.

---

## Step 7: Use the Auto-Configured Beans

### Option A: Inject DocumentRetriever (Simplest)

The library provides a `DocumentRetriever` bean that you can inject directly:

**File: `src/main/java/com/mycompany/RagService.java`**

```java
package com.mycompany;

import org.springframework.ai.document.Document;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.retrieval.search.DocumentRetriever;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class RagService {

    private final DocumentRetriever documentRetriever;

    public RagService(DocumentRetriever documentRetriever) {
        this.documentRetriever = documentRetriever;
    }

    public List<Document> retrieveRelevantDocuments(String question) {
        // Just call retrieve() with your question
        return documentRetriever.retrieve(new Query(question));
    }
}
```

### Option B: Inject Lower-Level Components (Advanced)

If you need more control, inject the underlying components:

```java
package com.mycompany;

import com.example.tree.retriever.api.TreeIndexBuilder;
import com.example.tree.retriever.store.TreeIndexStore;
import com.example.tree.retriever.core.TreeIndex;
import com.example.tree.retriever.indexing.DocumentProcessingPipeline;
import com.example.tree.retriever.indexing.BaselinePipelines;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class IndexManagementService {

    private final TreeIndexBuilder treeIndexBuilder;
    private final TreeIndexStore treeIndexStore;

    public IndexManagementService(TreeIndexBuilder treeIndexBuilder, 
                                   TreeIndexStore treeIndexStore) {
        this.treeIndexBuilder = treeIndexBuilder;
        this.treeIndexStore = treeIndexStore;
    }

    public void buildIndex(String markdownContent) {
        // Parse and chunk documents
        DocumentProcessingPipeline<String> pipeline = BaselinePipelines.markdown();
        var parsedDocs = pipeline.process(markdownContent);

        // Build tree (with LLM summarization)
        TreeIndex treeIndex = treeIndexBuilder.build(parsedDocs);

        // Store the index
        treeIndexStore.publish(treeIndex);

        System.out.println("✅ Index built and published");
    }

    public void checkIndexStatus() {
        var snapshot = treeIndexStore.current();
        if (snapshot.isPresent()) {
            System.out.println("📊 Index exists with root: " + snapshot.get().root());
        } else {
            System.out.println("⚠️ No index found");
        }
    }
}
```

---

## Step 8: Create REST Endpoints

**File: `src/main/java/com/mycompany/RagController.java`**

```java
package com.mycompany;

import org.springframework.ai.document.Document;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/rag")
public class RagController {

    private final RagService ragService;
    private final IndexManagementService indexService;

    public RagController(RagService ragService, IndexManagementService indexService) {
        this.ragService = ragService;
        this.indexService = indexService;
    }

    @PostMapping("/ask")
    public ResponseEntity<?> askQuestion(@RequestBody AskRequest request) {
        List<Document> results = ragService.retrieveRelevantDocuments(request.question);
        
        List<DocumentDto> docs = results.stream()
            .map(doc -> new DocumentDto(
                doc.getText(),
                doc.getMetadata()
            ))
            .collect(Collectors.toList());

        return ResponseEntity.ok(new AskResponse(
            request.question,
            docs,
            docs.size()
        ));
    }

    @PostMapping("/build-index")
    public ResponseEntity<?> buildIndex(@RequestBody BuildIndexRequest request) {
        try {
            indexService.buildIndex(request.content);
            return ResponseEntity.ok(new Response("Index built successfully", true));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(
                new Response("Failed to build index: " + e.getMessage(), false)
            );
        }
    }

    @GetMapping("/status")
    public ResponseEntity<?> getStatus() {
        indexService.checkIndexStatus();
        return ResponseEntity.ok(new Response("Check console output", true));
    }

    // DTOs
    record AskRequest(String question) {}
    record BuildIndexRequest(String content) {}
    record DocumentDto(String content, java.util.Map<String, Object> metadata) {}
    record AskResponse(String question, List<DocumentDto> documents, int count) {}
    record Response(String message, boolean success) {}
}
```

---

## Step 9: Test the Integration

### Test 1: Build and Run

```bash
# Build the app
mvn clean package -DskipTests

# Run it
mvn spring-boot:run

# Or run the JAR directly
java -jar target/my-rag-application-1.0.0-SNAPSHOT.jar
```

**Expected Output:**
```
2026-04-05 11:40:00.123  INFO 12345 --- [main] o.s.b.w.e.t.TomcatWebServer : Tomcat started on port(s): 8080 (http)
2026-04-05 11:40:00.456  INFO 12345 --- [main] c.m.RagApplication : Started RagApplication in 3.456 seconds
```

### Test 2: Build an Index

```bash
curl -X POST http://localhost:8080/api/rag/build-index \
  -H "Content-Type: application/json" \
  -d '{
    "content": "Our billing system handles monthly payments. We accept credit cards and bank transfers. Contact billing@company.com for support."
  }'
```

**Response:**
```json
{
  "message": "Index built successfully",
  "success": true
}
```

### Test 3: Ask a Question

```bash
curl -X POST http://localhost:8080/api/rag/ask \
  -H "Content-Type: application/json" \
  -d '{
    "question": "What payment methods do you support?"
  }'
```

**Response:**
```json
{
  "question": "What payment methods do you support?",
  "documents": [
    {
      "content": "We accept credit cards and bank transfers...",
      "metadata": {
        "relevanceScore": 0.99
      }
    }
  ],
  "count": 1
}
```

### Test 4: Check Index Status (via Actuator)

```bash
curl http://localhost:8080/actuator/treeRetriever
```

**Response:**
```json
{
  "hasIndex": true,
  "storeStats": {
    "totalNodes": 5,
    "totalLeafNodes": 2,
    "treeLevels": 3
  }
}
```

---

## Common Issues & Solutions

| Issue | Cause | Solution |
|-------|-------|----------|
| **Dependency not found** | Library not installed locally | Run `mvn install` in library project first |
| **ChatClient not autowired** | Missing LLM provider dependency | Add `spring-ai-openai-spring-boot-starter` |
| **API key error** | `OPENAI_API_KEY` not set | Set environment variable before running |
| **LLM timeout** | Network issue or slow LLM | Increase `llm-timeout` in configuration |
| **No index available** | Need to build index first | Call `/api/rag/build-index` endpoint |

---

## Project Structure After Integration

```
my-rag-application/
├── pom.xml                          (includes spring-ai-tree-retriever)
├── src/
│   ├── main/
│   │   ├── java/com/mycompany/
│   │   │   ├── RagApplication.java  (main app)
│   │   │   ├── RagService.java      (uses DocumentRetriever)
│   │   │   ├── RagController.java   (REST endpoints)
│   │   │   └── IndexManagementService.java
│   │   └── resources/
│   │       └── application.yaml     (LLM config)
│   └── test/
│       └── java/...
└── target/
    └── my-rag-application-1.0.0-SNAPSHOT.jar
```

---

## Next Steps

1. ✅ Integrate the library into your app
2. 📄 Load your actual documents (from files, database, etc.)
3. 🌳 Build the tree index with your documents
4. 🔍 Start querying!

For more details, see:
- [User Flow Guide](user-flow.md)
- [Architecture Guide](architecture.md)
- [Configuration Reference](reference.md)
