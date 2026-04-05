package com.example.tree.retriever.indexing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class OverlappingTextChunkerTest {

    @Test
    void splitterConfigValidatesBounds() {
        assertThatThrownBy(() -> new SplitterConfig(0, 0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new SplitterConfig(10, -1))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new SplitterConfig(10, 10))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void chunkRetainsMetadataAndAppliesOverlap() {
        final ParsedDocument parsedDocument = new ParsedDocument(
                "abcdefghij",
                Map.of("source", "unit-test", "format", "text")
        );

        final OverlappingTextChunker chunker = new OverlappingTextChunker();
        final List<ParsedChunk> chunks = chunker.chunk(parsedDocument, new SplitterConfig(4, 1));

        assertThat(chunks).hasSize(3);
        assertThat(chunks.get(0).text()).isEqualTo("abcd");
        assertThat(chunks.get(1).text()).isEqualTo("defg");
        assertThat(chunks.get(2).text()).isEqualTo("ghij");

        assertThat(chunks.get(0).metadata())
                .containsEntry("source", "unit-test")
                .containsEntry("chunk_index", 0)
                .containsEntry("start_offset", 0)
                .containsEntry("end_offset", 4);

        assertThat(chunks.get(1).metadata())
                .containsEntry("chunk_index", 1)
                .containsEntry("start_offset", 3)
                .containsEntry("end_offset", 7);
    }
}
