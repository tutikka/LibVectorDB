package com.tt.vectordb.model;

import java.util.ArrayList;
import java.util.List;

public class SearchResult {

    private List<Match> matches = new ArrayList<>();

    private long duration;

    private long scanned;

    private long total;

    private String similarity;

    public SearchResult() {
    }

    public List<Match> getMatches() {
        return matches;
    }

    public void setMatches(List<Match> matches) {
        this.matches = matches;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public long getScanned() {
        return scanned;
    }

    public void setScanned(long scanned) {
        this.scanned = scanned;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public String getSimilarity() {
        return similarity;
    }

    public void setSimilarity(String similarity) {
        this.similarity = similarity;
    }

}
