package com.tt.vectordb.storage;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tt.vectordb.config.Configuration;
import com.tt.vectordb.model.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IndexStore {

    private static final Logger L = LoggerFactory.getLogger(IndexStore.class);

    private static String FILENAME = "indexes.json";

    private ObjectMapper mapper = new ObjectMapper();

    private Map<Long, Index> indexes = new HashMap<>();

    public IndexStore() throws Exception {
        File dir = new File(Configuration.getConfiguration().get("data.directory", "data"));
        if (!dir.exists()) {
            if (dir.mkdirs()) {
                L.info(String.format("created data directory '%s'", dir.getAbsolutePath()));
            } else {
                throw new Exception(String.format("error creating data directory '%s'", dir.getAbsolutePath()));
            }
        }
        File file = new File(dir, FILENAME);
        if (file.exists()) {
            List<Index> list = mapper.readValue(file, new TypeReference<List<Index>>() {});
            for (Index index : list) {
                indexes.put(index.getId(), index);
            }
        }
    }

    public Index read(long id) {
        return (indexes.get(id));
    }

    public List<Index> list() {
        return (indexes.values().stream().toList());
    }

    public void write(Index index) {
        indexes.put(index.getId(), index);
    }

    public synchronized void delete(long id) {
        indexes.remove(id);
    }

    public void close() throws Exception {
        File file = new File(Configuration.getConfiguration().get("data.directory", "data"), FILENAME);
        mapper.writeValue(file, list());
        L.info(String.format("wrote %d indexes to file '%s'", indexes.size(), file.getAbsolutePath()));
    }

}
