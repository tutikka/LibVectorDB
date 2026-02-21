package com.tt.vectordb.model;

import java.util.Map;

public class Index {

    public static final String SIMILARITY_COSINE_DISTANCE = "cosine";

    public static final String SIMILARITY_EUCLID_DISTANCE = "euclid";

    public static final String SIMILARITY_MANHATTAN_DISTANCE = "manhattan";

    public static final String OPTIMIZATION_NONE = "none";

    private long id;

    private String name;

    private int dimensions;

    private String similarity;

    private String optimization;

    public Index() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getDimensions() {
        return dimensions;
    }

    public void setDimensions(int dimensions) {
        this.dimensions = dimensions;
    }

    public String getSimilarity() {
        return similarity;
    }

    public void setSimilarity(String similarity) {
        this.similarity = similarity;
    }

    public String getOptimization() {
        return optimization;
    }

    public void setOptimization(String optimization) {
        this.optimization = optimization;
    }

}
