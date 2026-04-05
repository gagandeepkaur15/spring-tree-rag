# Demo Consumer

```java
@Service
public class RagAnswerService {

    private final DocumentRetriever documentRetriever;

    public RagAnswerService(DocumentRetriever documentRetriever) {
        this.documentRetriever = documentRetriever;
    }

    public List<Document> retrieveContext(String question) {
        return documentRetriever.retrieve(new Query(question));
    }
}
```

Starter beans are auto-configured when `ChatClient` is available in the application context.
