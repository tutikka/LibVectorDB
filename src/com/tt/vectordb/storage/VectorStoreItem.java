package com.tt.vectordb.storage;

public class VectorStoreItem {

    private long id;

    private float[] embedding;

    private double distance;

    public VectorStoreItem() {
    }

    public VectorStoreItem(long id, float[] embedding) {
        this.id = id;
        this.embedding = embedding;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public float[] getEmbedding() {
        return embedding;
    }

    public void setEmbedding(float[] embedding) {
        this.embedding = embedding;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

}
