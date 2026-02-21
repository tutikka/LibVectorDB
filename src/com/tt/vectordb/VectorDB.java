package com.tt.vectordb;

import com.tt.vectordb.config.Configuration;
import com.tt.vectordb.model.*;
import com.tt.vectordb.service.DBService;

import java.util.Properties;
import java.util.Random;

public class VectorDB {

    public VectorDB(Properties properties) throws Exception {
        Configuration.getConfiguration(properties);
    }

    public Index createIndex(Index index) throws Exception {
        index.setId(System.currentTimeMillis());
        DBService.getService().createIndex(index);
        return (index);
    }

    public Index getIndex(long id) throws Exception {
        return (DBService.getService().getIndex(id));
    }

    public void deleteIndex(long id) throws Exception {
        DBService.getService().deleteIndex(id);
    }

    public Entry createEntry(long id, Entry entry) throws Exception {
        DBService.getService().createEntry(id, entry);
        return (entry);
    }

    public SearchResult searchEntries(long id, Search search) throws Exception {
        return (DBService.getService().searchEntries(id, search));
    }

    public void close() throws Exception {
        DBService.getService().close();
    }

    public static void main(String[] args) throws Exception {

        Random random = new Random();

        // initialize properties
        Properties properties = new Properties();
        properties.setProperty("data.directory", "data");
        properties.setProperty("data.max_vectors_per_index", "65536");

        // new instance
        VectorDB db = new VectorDB(properties);

        // create index
        Index index = new Index();
        index.setName("test");
        index.setDimensions(3);
        index.setSimilarity(Index.SIMILARITY_COSINE_DISTANCE);
        index.setOptimization(Index.OPTIMIZATION_NONE);
        index = db.createIndex(index);

        // create entries
        for (int i = 0; i < 100; i++) {
            Entry entry = new Entry();
            entry.setId(i + 1);
            entry.setEmbedding(new float[]{random.nextFloat(), random.nextFloat(), random.nextFloat()});
            db.createEntry(index.getId(), entry);
        }

        // search for entries
        Search search = new Search();
        search.setTop(1);
        search.setEmbedding(new float[]{random.nextFloat(), random.nextFloat(), random.nextFloat()});
        SearchResult result = db.searchEntries(index.getId(), search);
        Match match = result.getMatches().get(0);
        System.out.printf("closest entry: id = %d, distance = %f%n", match.getId(), match.getDistance());

        // delete index
        db.deleteIndex(index.getId());

        // clean up
        db.close();

    }

}
