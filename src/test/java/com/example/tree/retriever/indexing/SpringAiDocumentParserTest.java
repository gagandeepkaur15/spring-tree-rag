package com.example.tree.retriever.indexing;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.springframework.ai.document.Document;

class SpringAiDocumentParserTest {

    private final SpringAiDocumentParser parser = new SpringAiDocumentParser();

    @Test
    void parseNullSourceReturnsEmptyTextWithFormatMetadata() {
        ParsedDocument parsed = parser.parse(null);

        assertThat(parsed.text()).isEmpty();
        assertThat(parsed.metadata()).containsEntry("format", "spring-ai-document");
    }

    @Test
    void parseAddsFormatWhenSourceMetadataDoesNotContainIt() {
        Document source = mock(Document.class);
        when(source.getText()).thenReturn("hello");
        when(source.getMetadata()).thenReturn(new LinkedHashMap<>(Map.of("source", "test")));

        ParsedDocument parsed = parser.parse(source);

        assertThat(parsed.text()).isEqualTo("hello");
        assertThat(parsed.metadata())
                .containsEntry("source", "test")
                .containsEntry("format", "spring-ai-document");
    }

    @Test
    void parseKeepsExistingFormatMetadata() {
        Document source = mock(Document.class);
        when(source.getText()).thenReturn("hello");
        when(source.getMetadata()).thenReturn(new LinkedHashMap<>(Map.of("format", "custom-format")));

        ParsedDocument parsed = parser.parse(source);

        assertThat(parsed.metadata()).containsEntry("format", "custom-format");
    }
}
