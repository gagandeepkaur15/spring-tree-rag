package com.example.tree.retriever.indexing;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class BaselinePipelinesTest {

    @Test
    void textPipelineParsesAndChunks() {
        final List<ParsedChunk> chunks = BaselinePipelines.text().process("hello world", new SplitterConfig(5, 1));

        assertThat(chunks).hasSize(3);
        assertThat(chunks.get(0).text()).isEqualTo("hello");
        assertThat(chunks.get(0).metadata()).containsEntry("format", "text");
    }

    @Test
    void markdownPipelineParsesAndChunks() {
        final List<ParsedChunk> chunks = BaselinePipelines.markdown().process("# Title", new SplitterConfig(10, 2));

        assertThat(chunks).hasSize(1);
        assertThat(chunks.get(0).text()).isEqualTo("# Title");
        assertThat(chunks.get(0).metadata()).containsEntry("format", "markdown");
    }
}
