package com.tt.vectordb.service;

import com.tt.vectordb.model.Search;
import com.tt.vectordb.storage.VectorStoreItem;

public interface Similarity {

    double compare(Search search, VectorStoreItem item);

}
