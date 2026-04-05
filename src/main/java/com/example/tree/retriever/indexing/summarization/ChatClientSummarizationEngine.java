package com.example.tree.retriever.indexing.summarization;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.client.ChatClient;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

/**
 * ChatClient-based structured summarization with retry and safe fallback.
 */
public final class ChatClientSummarizationEngine implements SummarizationEngine {

    private static final String SYSTEM_PROMPT = """
            You are a summarization engine.
            Return STRICT JSON only.
            Output schema:
            {
              "summary": "<concise summary>",
              "keyPoints": ["<point 1>", "<point 2>"]
            }
            Do not wrap in markdown code fences.
            """;

    private final ChatClient chatClient;
    private final ObjectMapper strictObjectMapper;
    private final Duration requestTimeout;
    private final int maxRetries;

    public ChatClientSummarizationEngine(
            ChatClient chatClient,
            ObjectMapper objectMapper,
            Duration requestTimeout,
            int maxRetries
    ) {
        if (requestTimeout == null || requestTimeout.isZero() || requestTimeout.isNegative()) {
            throw new IllegalArgumentException("requestTimeout must be positive");
        }
        if (maxRetries < 0) {
            throw new IllegalArgumentException("maxRetries must be >= 0");
        }
        this.chatClient = chatClient;
        this.strictObjectMapper = objectMapper.copy()
                .enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .enable(DeserializationFeature.FAIL_ON_NULL_CREATOR_PROPERTIES)
                .enable(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES);
        this.requestTimeout = requestTimeout;
        this.maxRetries = maxRetries;
    }

    @Override
    public SummarizationResult summarize(String content) {
        final String normalizedContent = content == null ? "" : content.trim();
        if (normalizedContent.isBlank()) {
            return new SummarizationResult("No content provided.", List.of(), true);
        }

        for (int attempt = 0; attempt <= maxRetries; attempt++) {
            try {
                final String response = requestSummaryJson(normalizedContent);
                final ModelSummary parsed = strictObjectMapper.readValue(response, ModelSummary.class);
                return new SummarizationResult(parsed.summary(), parsed.keyPoints(), false);
            } catch (TimeoutException | ExecutionException ex) {
                if (attempt == maxRetries) {
                    return fallbackSummary(normalizedContent);
                }
            } catch (Exception ex) {
                if (attempt == maxRetries) {
                    return fallbackSummary(normalizedContent);
                }
            }
        }

        return fallbackSummary(normalizedContent);
    }

    private String requestSummaryJson(String content) throws TimeoutException, ExecutionException, InterruptedException {
        final CompletableFuture<String> future = CompletableFuture.supplyAsync(() ->
                chatClient.prompt()
                        .system(SYSTEM_PROMPT)
                        .user(buildUserPrompt(content))
                        .call()
                        .content()
        );
        try {
            return future.get(requestTimeout.toMillis(), java.util.concurrent.TimeUnit.MILLISECONDS);
        } catch (TimeoutException ex) {
            future.cancel(true);
            throw ex;
        }
    }

    private static String buildUserPrompt(String content) {
        return "Summarize the following content:\n\n" + content;
    }

    private static SummarizationResult fallbackSummary(String content) {
        final String condensed = content.replaceAll("\\s+", " ").trim();
        final int maxLength = Math.min(condensed.length(), 220);
        final String summary = condensed.substring(0, maxLength) + (condensed.length() > maxLength ? "..." : "");
        return new SummarizationResult(summary, List.of(), true);
    }

    private record ModelSummary(
            @JsonProperty(value = "summary", required = true) String summary,
            @JsonProperty(value = "keyPoints", required = true) List<String> keyPoints
    ) {
        private ModelSummary {
            if (summary == null || summary.isBlank()) {
                throw new IllegalArgumentException("summary is required");
            }
            keyPoints = keyPoints == null ? List.of() : List.copyOf(keyPoints);
        }
    }
}
