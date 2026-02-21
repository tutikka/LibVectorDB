package com.tt.vectordb.service;

import com.tt.vectordb.config.Configuration;
import com.tt.vectordb.model.Match;
import com.tt.vectordb.model.*;
import com.tt.vectordb.storage.IndexStore;
import com.tt.vectordb.storage.VectorStore;
import com.tt.vectordb.storage.VectorStoreItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class DBService {

    private static final Logger L = LoggerFactory.getLogger(DBService.class);

    private IndexStore indexStore;

    private Map<Long, VectorStore> vectorStores;

    private static DBService service;

    private DBService() {
        try {
            indexStore = new IndexStore();
            L.info("initialized index store");
        } catch (Exception e) {
            L.error(String.format("error initializing index store: %s", e.getMessage()));
            return;
        }
        vectorStores = new HashMap<>();
        for (Index index : indexStore.list()) {
            try {
                vectorStores.put(index.getId(), new VectorStore(index.getId(), index.getDimensions()));
                L.info(String.format("initialized vector store for index '%d'", index.getId()));
            } catch (Exception e) {
                L.error(String.format("error initializing vector store for index '%d': %s", index.getId(), e.getMessage()));
            }
        }
    }

    public static DBService getService() {
        if (service == null) {
            service = new DBService();
        }
        return (service);
    }

    public void createIndex(Index index) throws Exception {
        if (index == null) {
            throw new IllegalArgumentException("unrecognized request");
        }
        if (index.getDimensions() < 1 || index.getDimensions() > 2048) {
            throw new IllegalArgumentException("index dimensions must be in range 1-2048");
        }
        if (index.getName() == null || index.getName().isEmpty()) {
            throw new IllegalArgumentException("index name must not be empty");
        }
        if (index.getSimilarity() == null || !Arrays.stream((new String[]{"cosine", "euclid", "manhattan"})).toList().contains(index.getSimilarity())) {
            throw new IllegalArgumentException("acceptable values for index similarity are ['cosine', 'euclid', 'manhattan']");
        }
        if (index.getOptimization() == null || !index.getOptimization().equals("none")) {
            throw new IllegalArgumentException("acceptable values for index optimization are ['none']");
        }
        if (indexStore.read(index.getId()) != null) {
            throw new IllegalArgumentException(String.format("index '%s' already exists", index.getId()));
        }
        VectorStore store = new VectorStore(index.getId(), index.getDimensions());
        vectorStores.put(index.getId(), store);
        indexStore.write(index);
    }

    /**
     * Get index by identifier
     *
     * @param id The identifier
     * @return The index or NULL if not found
     */
    public Index getIndex(long id) {
        return (indexStore.read(id));
    }

    public Map<String, Object> getIndexExtras(long id) {
        VectorStore store = vectorStores.get(id);
        if (store != null) {
            return (Map.of(
                    "_num_vectors", store.getSlot(),
                    "_max_vectors", store.getMaxSlots(),
                    "_size_on_disk", store.getSizeOnDisk()
            ));
        } else {
            return (null);
        }
    }

    public void deleteIndex(long id) throws Exception {
        indexStore.delete(id);
        try {
            vectorStores.get(id).delete();
            vectorStores.remove(id);
        } catch (Exception e) {
            e.printStackTrace(System.err);
            throw new Exception(String.format("error deleting index '%d': %s", id, e.getMessage()));
        }
        L.info(String.format("deleted index '%d'", id));
    }

    public List<Index> listIndexes() {
        return (indexStore.list());
    }

    public void createEntry(long id, Entry entry) throws Exception {
        Index index = getIndex(id);
        if (index == null) {
            throw new IllegalArgumentException(String.format("index '%d' not found", id));
        }
        if (entry == null) {
            throw new IllegalArgumentException("unrecognized request");
        }
        if (entry.getEmbedding() == null) {
            throw new IllegalArgumentException("entry embedding must not be empty");
        }
        if (entry.getEmbedding().length != index.getDimensions()) {
            throw new IllegalArgumentException(String.format("length of entry embedding (%d) must be equal to index dimension (%d)", entry.getEmbedding().length, index.getDimensions()));
        }
        if (index.getSimilarity().equals(Index.SIMILARITY_COSINE_DISTANCE)) {
            entry.setEmbedding(CosineSimilarity.normalize(entry.getEmbedding()));
        }
        VectorStore store = vectorStores.get(id);
        if (store == null) {
            throw new Exception(String.format("vector store not initialized for index '%d'", index.getId()));
        }
        if (store.getSlot() >= Configuration.getConfiguration().get("data.max_vectors_per_index", 65536)) {
            throw new Exception(String.format("max vectors per index exceeded (%d)", store.getSlot()));
        }
        store.write(new VectorStoreItem(entry.getId(), entry.getEmbedding()));
    }

    public List<Entry> listEntries(long id, int offset, int limit) throws Exception {
        Index index = getIndex(id);
        if (index == null) {
            throw new IllegalArgumentException(String.format("index '%d' not found", id));
        }
        VectorStore store = vectorStores.get(id);
        if (store == null) {
            throw new Exception(String.format("vector store not initialized for index '%d'", index.getId()));
        }
        if (offset < 0) {
            throw new IllegalArgumentException("offset must be >= 0");
        }
        if (offset > store.getSlot()) {
            throw new IllegalArgumentException(String.format("offset (%d) must be < number of items in index (%d)", offset, store.getSlot() + 1));
        }
        if (limit < 1) {
            throw new IllegalArgumentException(String.format("limit (%d) must be > 0", limit));
        }
        List<Entry> entries = new ArrayList<>();
        for (int i = offset; i < offset + limit; i++) {
            VectorStoreItem item = store.read(i);
            if (item != null) {
                Entry entry = new Entry();
                entry.setId(item.getId());
                entry.setEmbedding(item.getEmbedding());
                entries.add(entry);
            }
        }
        return (entries);
    }

    public SearchResult searchEntries(long id, Search search) throws Exception {
        Index index = getIndex(id);
        if (index == null) {
            throw new IllegalArgumentException(String.format("index '%d' not found", id));
        }
        if (search == null) {
            throw new IllegalArgumentException("unrecognized request");
        }
        if (search.getEmbedding() == null) {
            throw new IllegalArgumentException("search embedding cannot be empty");
        }
        if (search.getEmbedding().length != index.getDimensions()) {
            throw new IllegalArgumentException(String.format("length of search embedding (%d) must be equal to index dimension (%d)", search.getEmbedding().length, index.getDimensions()));
        }
        if (search.getTop() < 1 || search.getTop() > 128) {
            throw new IllegalArgumentException("top must be in range 1-128");
        }
        VectorStore store = vectorStores.get(id);
        if (store == null) {
            throw new Exception(String.format("vector store not initialized for index '%d'", index.getId()));
        }
        long start = System.currentTimeMillis();
        PriorityQueue<VectorStoreItem> queue = new PriorityQueue<>(
                Comparator.comparingDouble(item -> -item.getDistance())
        );
        long scanned = 0;
        long total = store.getSlot();
        for (int i = 0; i < store.getSlot(); i++) {
            VectorStoreItem item = store.read(i);
            switch (index.getSimilarity()) {
                case Index.SIMILARITY_COSINE_DISTANCE -> item.setDistance(new CosineSimilarity().compare(search, item));
                case Index.SIMILARITY_EUCLID_DISTANCE -> item.setDistance(new EuclidSimilarity().compare(search, item));
                case Index.SIMILARITY_MANHATTAN_DISTANCE -> item.setDistance(new ManhattanSimilarity().compare(search, item));
            }
            queue.offer(item);
            if (queue.size() > search.getTop()) {
                queue.poll();
            }
            scanned++;
        }
        long end = System.currentTimeMillis();
        SearchResult result = new SearchResult();
        result.setDuration(end - start);
        result.setScanned(scanned);
        result.setTotal(total);
        result.setSimilarity(index.getSimilarity());
        for (VectorStoreItem item : queue) {
            Match match = new Match();
            match.setId(item.getId());
            match.setDistance(item.getDistance());
            result.getMatches().add(match);
        }
        result.getMatches().sort((a, b) -> Double.compare(a.getDistance(), b.getDistance()));
        return (result);
    }

    public void close() {
        for (VectorStore store : vectorStores.values()) {
            try {
                store.close();
                L.info(String.format("closed vector store for index '%d'", store.getId()));
            } catch (Exception e) {
                L.error(String.format("error closing vector store for index '%d': %s", store.getId(), e.getMessage()));
                e.printStackTrace(System.err);
            }
        }
        try {
            indexStore.close();
            L.info("closed index store");
        } catch (Exception e) {
            L.error(String.format("error closing index store: %s", e.getMessage()));
            e.printStackTrace(System.err);
        }
    }

}
