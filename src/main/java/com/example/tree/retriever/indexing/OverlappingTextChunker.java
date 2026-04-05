package com.example.tree.retriever.indexing;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Stateless overlapping character-based chunker.
 */
public final class OverlappingTextChunker implements DocumentChunker {

    @Override
    public List<ParsedChunk> chunk(ParsedDocument document, SplitterConfig config) {
        if (document == null || document.text().isBlank()) {
            return List.of();
        }

        final int chunkSize = config.chunkSize();
        final int step = chunkSize - config.chunkOverlap();
        final String text = document.text();
        final List<ParsedChunk> result = new ArrayList<>();

        int index = 0;
        for (int start = 0; start < text.length(); start += step) {
            final int end = Math.min(start + chunkSize, text.length());
            if (end <= start) {
                break;
            }

            final String chunkText = text.substring(start, end);
            final Map<String, Object> metadata = new LinkedHashMap<>(document.metadata());
            metadata.put("chunk_index", index);
            metadata.put("start_offset", start);
            metadata.put("end_offset", end);
            metadata.put("chunk_size", chunkText.length());

            result.add(new ParsedChunk(chunkText, metadata));
            index++;

            if (end >= text.length()) {
                break;
            }
        }

        return List.copyOf(result);
    }
}
