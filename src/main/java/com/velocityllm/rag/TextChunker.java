package com.velocityllm.rag;

import java.util.ArrayList;
import java.util.List;

public final class TextChunker {

    private TextChunker() {
    }

    public static List<DocumentChunk> chunk(String sourceFile, String text, int chunkSize, int overlap) {
        List<DocumentChunk> chunks = new ArrayList<>();
        if (text == null || text.isBlank()) {
            return chunks;
        }

        String normalized = text.replace("\r\n", "\n").trim();
        String[] paragraphs = normalized.split("\n\\s*\n");

        StringBuilder buffer = new StringBuilder();
        for (String paragraph : paragraphs) {
            String trimmed = paragraph.trim();
            if (trimmed.isEmpty()) {
                continue;
            }

            if (buffer.length() + trimmed.length() + 1 <= chunkSize) {
                if (buffer.length() > 0) {
                    buffer.append("\n\n");
                }
                buffer.append(trimmed);
                continue;
            }

            if (buffer.length() > 0) {
                addChunk(chunks, sourceFile, buffer.toString());
                buffer.setLength(0);
            }

            if (trimmed.length() <= chunkSize) {
                buffer.append(trimmed);
            } else {
                splitLongParagraph(chunks, sourceFile, trimmed, chunkSize, overlap);
            }
        }

        if (buffer.length() > 0) {
            addChunk(chunks, sourceFile, buffer.toString());
        }

        return chunks;
    }

    private static void splitLongParagraph(List<DocumentChunk> chunks, String sourceFile, String text, int chunkSize, int overlap) {
        int start = 0;
        while (start < text.length()) {
            int end = Math.min(start + chunkSize, text.length());
            String piece = text.substring(start, end).trim();
            if (!piece.isEmpty()) {
                addChunk(chunks, sourceFile, piece);
            }
            if (end >= text.length()) {
                break;
            }
            start = Math.max(end - overlap, start + 1);
        }
    }

    private static void addChunk(List<DocumentChunk> chunks, String sourceFile, String content) {
        chunks.add(new DocumentChunk(sourceFile, content));
    }
}
