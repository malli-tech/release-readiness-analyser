package com.aireadiness.knowledge.service;

import java.util.List;

public interface EmbeddingService {

    /**
     * Generates a vector embedding for a single text input.
     *
     * @param text Text content to embed.
     * @return List of Double representing the embedding vector.
     */
    List<Double> embed(String text);

    /**
     * Generates vector embeddings for a batch of text inputs.
     *
     * @param texts List of text contents to embed.
     * @return List of embedding vectors corresponding to the input order.
     */
    List<List<Double>> embedBatch(List<String> texts);

    /**
     * Returns the expected dimension of the embedding vectors produced by this service.
     *
     * @return Vector dimension (e.g., 1536 for text-embedding-3-small).
     */
    int getDimension();
}
