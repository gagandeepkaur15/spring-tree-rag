package com.example.tree.retriever.indexing.summarization;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ChatClientSummarizationEngineTest {

    @Test
    void summarizesWithStructuredJsonResponse() {
        final ChatClient chatClient = mock(ChatClient.class, RETURNS_DEEP_STUBS);
        when(chatClient.prompt().system(anyString()).user(anyString()).call().content())
                .thenReturn("{\"summary\":\"Concise summary\",\"keyPoints\":[\"A\",\"B\"]}");

        final ChatClientSummarizationEngine engine = new ChatClientSummarizationEngine(
                chatClient,
                new ObjectMapper(),
                Duration.ofSeconds(1),
                1
        );

        final SummarizationResult result = engine.summarize("Some long content");

        assertThat(result.summary()).isEqualTo("Concise summary");
        assertThat(result.keyPoints()).containsExactly("A", "B");
        assertThat(result.fallbackUsed()).isFalse();
    }

    @Test
    void retriesOnceWhenFirstResponseIsMalformed() {
        final ChatClient chatClient = mock(ChatClient.class, RETURNS_DEEP_STUBS);
        when(chatClient.prompt().system(anyString()).user(anyString()).call().content())
                .thenReturn("not-json")
                .thenReturn("{\"summary\":\"Recovered summary\",\"keyPoints\":[\"Recovered\"]}");

        final ChatClientSummarizationEngine engine = new ChatClientSummarizationEngine(
                chatClient,
                new ObjectMapper(),
                Duration.ofSeconds(1),
                1
        );

        final SummarizationResult result = engine.summarize("Input content");

        assertThat(result.summary()).isEqualTo("Recovered summary");
        assertThat(result.keyPoints()).containsExactly("Recovered");
        assertThat(result.fallbackUsed()).isFalse();
    }

    @Test
    void fallsBackWhenModelCallTimesOut() {
        final ChatClient chatClient = mock(ChatClient.class, RETURNS_DEEP_STUBS);
        when(chatClient.prompt().system(anyString()).user(anyString()).call().content())
                .thenAnswer(invocation -> {
                    Thread.sleep(120);
                    return "{\"summary\":\"Late summary\",\"keyPoints\":[]}";
                });

        final ChatClientSummarizationEngine engine = new ChatClientSummarizationEngine(
                chatClient,
                new ObjectMapper(),
                Duration.ofMillis(20),
                0
        );

        final SummarizationResult result = engine.summarize("This content should trigger fallback because timeout.");

        assertThat(result.fallbackUsed()).isTrue();
        assertThat(result.summary()).contains("This content should trigger fallback");
        assertThat(result.keyPoints()).isEmpty();
    }
}
