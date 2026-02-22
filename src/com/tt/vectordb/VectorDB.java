package com.tt.vectordb;

import com.tt.vectordb.config.Configuration;
import com.tt.vectordb.model.*;
import com.tt.vectordb.service.DBService;

import java.util.Properties;
import java.util.Random;

/**
 * Class representing one instance of VectorDB.
 *
 * @author Tuomas Tikka
 */
public class VectorDB {

    /**
     * Initialize an instance of VectorDB.
     *
     * @param properties The configuration properties object
     * @throws Exception If the instance cannot be initialized
     */
    public VectorDB(Properties properties) throws Exception {
        Configuration.getConfiguration(properties);
    }

    /**
     * Create an index.
     *
     * @param index The index object
     * @return The index object (id populated)
     * @throws Exception If the index cannot be created
     */
    public Index createIndex(Index index) throws Exception {
        index.setId(System.currentTimeMillis());
        DBService.getService().createIndex(index);
        return (index);
    }

    /**
     * Return information for an index.
     *
     * @param id The index id
     * @return The index object
     * @throws Exception If the index cannot be returned
     */
    public Index getIndex(long id) throws Exception {
        return (DBService.getService().getIndex(id));
    }

    /**
     * Delete an index.
     *
     * @param id The index id
     * @throws Exception If the index cannot be deleted
     */
    public void deleteIndex(long id) throws Exception {
        DBService.getService().deleteIndex(id);
    }

    /**
     * Create an entry into an index.
     *
     * @param id The index id
     * @param entry The entry object
     * @return The entry object
     * @throws Exception If the entry cannot be created
     */
    public Entry createEntry(long id, Entry entry) throws Exception {
        DBService.getService().createEntry(id, entry);
        return (entry);
    }

    /**
     * Search for entries in an index.
     *
     * @param id The index id
     * @param search The search criteria object
     * @return A search results object
     * @throws Exception If the search cannot be performed
     */
    public SearchResult searchEntries(long id, Search search) throws Exception {
        return (DBService.getService().searchEntries(id, search));
    }

    /**
     * Close the instance.
     *
     * @throws Exception If the instance cannot be closed
     */
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
