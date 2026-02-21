package com.tt.vectordb.service;

import com.tt.vectordb.model.Search;
import com.tt.vectordb.storage.VectorStoreItem;

public class CosineSimilarity implements Similarity {

    @Override
    public double compare(Search search, VectorStoreItem item) {
        return (1 - cosineSimilarity(search.getEmbedding(), item.getEmbedding()));
    }

    public static float[] normalize(float[] v) {
        float norm = 0.0f;
        for (float x : v) {
            norm += x * x;
        }
        norm = (float) Math.sqrt(norm);
        float[] result = new float[v.length];
        for (int i = 0; i < v.length; i++) {
            result[i] = v[i] / norm;
        }
        return result;
    }

    private static float cosineSimilarity(float[] a, float[] b) {
        float dot = 0, normA = 0, normB = 0;
        for (int i = 0; i < a.length; i++) {
            dot += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }
        return (float)(dot / (Math.sqrt(normA) * Math.sqrt(normB)));
    }

}
