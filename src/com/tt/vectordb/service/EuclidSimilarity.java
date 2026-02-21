package com.tt.vectordb.service;

import com.tt.vectordb.model.Search;
import com.tt.vectordb.storage.VectorStoreItem;

public class EuclidSimilarity implements Similarity {

    @Override
    public double compare(Search search, VectorStoreItem item) {
        return (euclidDistance(search.getEmbedding(), item.getEmbedding()));
    }

    private static double euclidDistance(float[] a, float[] b) {
        double d = 0;
        for (int i = 0; i < a.length; i++) {
            d += Math.pow(a[i] - b[i], 2);
        }
        return (Math.sqrt(d));
    }

}
