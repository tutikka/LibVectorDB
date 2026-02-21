# LibVectorDB

Core vector database library. This library can be included into projects for embedded use (see example below).

For a runnable web application with examples, see:

https://github.com/tutikka/VectorDB

## Requirements

- Java 21

## Embedded Use

```java
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
```