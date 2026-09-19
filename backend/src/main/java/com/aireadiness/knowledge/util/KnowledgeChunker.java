package com.aireadiness.knowledge.util;

import com.aireadiness.knowledge.model.KnowledgeChunk;
import com.aireadiness.knowledge.model.KnowledgeDocument;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Component
public class KnowledgeChunker {

    public static final int DEFAULT_TARGET_CHUNK_SIZE = 1800;
    public static final int DEFAULT_OVERLAP_SIZE = 200;
    public static final int DEFAULT_MAX_CHUNK_SIZE = 2500;

    private final int targetChunkSize;
    private final int overlapSize;
    private final int maxChunkSize;

    public KnowledgeChunker() {
        this(DEFAULT_TARGET_CHUNK_SIZE, DEFAULT_OVERLAP_SIZE, DEFAULT_MAX_CHUNK_SIZE);
    }

    public KnowledgeChunker(int targetChunkSize, int overlapSize, int maxChunkSize) {
        this.targetChunkSize = Math.max(200, targetChunkSize);
        this.overlapSize = Math.max(0, Math.min(overlapSize, targetChunkSize / 2));
        this.maxChunkSize = Math.max(this.targetChunkSize, maxChunkSize);
    }

    public List<KnowledgeChunk> chunk(KnowledgeDocument doc) {
        if (doc == null || doc.getContent() == null || doc.getContent().trim().isEmpty()) {
            return new ArrayList<>();
        }

        String text = doc.getContent().trim();
        List<String> rawChunks = splitTextIntoChunks(text);

        List<KnowledgeChunk> chunks = new ArrayList<>();
        for (int i = 0; i < rawChunks.size(); i++) {
            KnowledgeChunk chunk = new KnowledgeChunk();
            chunk.setDocumentId(doc.getId());
            chunk.setChunkIndex(i);
            chunk.setText(rawChunks.get(i));

            // Inherit metadata from parent document
            chunk.setTitle(doc.getTitle());
            chunk.setSource(doc.getSource());
            chunk.setSourceType(doc.getSourceType());
            chunk.setCategory(doc.getCategory());
            chunk.setTechnology(doc.getTechnology());
            chunk.setVersion(doc.getVersion());
            chunk.setCreatedAt(Instant.now());

            chunks.add(chunk);
        }

        return chunks;
    }

    private List<String> splitTextIntoChunks(String text) {
        List<String> chunks = new ArrayList<>();

        if (text.length() <= maxChunkSize) {
            chunks.add(text);
            return chunks;
        }

        // Split text by paragraph boundaries (\n\n)
        String[] paragraphs = text.split("\n\n");
        StringBuilder currentChunk = new StringBuilder();

        for (String para : paragraphs) {
            String paragraph = para.trim();
            if (paragraph.isEmpty()) continue;

            // If a single paragraph itself exceeds maxChunkSize, split it by word boundaries
            if (paragraph.length() > maxChunkSize) {
                if (currentChunk.length() > 0) {
                    chunks.add(currentChunk.toString().trim());
                    currentChunk.setLength(0);
                }
                List<String> subChunks = splitLongParagraph(paragraph);
                chunks.addAll(subChunks);
                continue;
            }

            if (currentChunk.length() == 0) {
                currentChunk.append(paragraph);
            } else if (currentChunk.length() + 2 + paragraph.length() <= targetChunkSize) {
                currentChunk.append("\n\n").append(paragraph);
            } else {
                // Finalize current chunk
                String completedChunk = currentChunk.toString().trim();
                chunks.add(completedChunk);

                // Build overlap from end of completed chunk
                String overlap = getOverlapText(completedChunk);
                currentChunk.setLength(0);
                if (!overlap.isEmpty()) {
                    currentChunk.append(overlap).append("\n\n");
                }
                currentChunk.append(paragraph);
            }
        }

        if (currentChunk.length() > 0) {
            String lastChunkText = currentChunk.toString().trim();
            if (!lastChunkText.isEmpty()) {
                chunks.add(lastChunkText);
            }
        }

        return chunks;
    }

    private List<String> splitLongParagraph(String para) {
        List<String> result = new ArrayList<>();
        int start = 0;
        int len = para.length();

        while (start < len) {
            int end = Math.min(start + targetChunkSize, len);
            if (end < len) {
                // Find nearest word boundary (space or newline)
                int spacePos = para.lastIndexOf(' ', end);
                int newlinePos = para.lastIndexOf('\n', end);
                int bestBreak = Math.max(spacePos, newlinePos);

                if (bestBreak > start + (targetChunkSize / 2)) {
                    end = bestBreak;
                }
            }

            String chunkText = para.substring(start, end).trim();
            if (!chunkText.isEmpty()) {
                result.add(chunkText);
            }

            if (end >= len) {
                break;
            }

            // Calculate start of next chunk with overlap
            start = Math.max(start + 1, end - overlapSize);
            // Move to start of next word if in middle of word
            while (start < len && start > 0 && Character.isLetterOrDigit(para.charAt(start - 1)) && Character.isLetterOrDigit(para.charAt(start))) {
                start++;
            }
        }

        return result;
    }

    private String getOverlapText(String chunkText) {
        if (overlapSize <= 0 || chunkText.length() <= overlapSize) {
            return "";
        }
        int startIndex = chunkText.length() - overlapSize;
        // Find nearest space or newline to avoid cutting word
        while (startIndex < chunkText.length() && Character.isLetterOrDigit(chunkText.charAt(startIndex))) {
            startIndex++;
        }
        return chunkText.substring(startIndex).trim();
    }

    public int getTargetChunkSize() {
        return targetChunkSize;
    }

    public int getOverlapSize() {
        return overlapSize;
    }

    public int getMaxChunkSize() {
        return maxChunkSize;
    }
}
