package com.tt.vectordb.storage;

import com.tt.vectordb.config.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class VectorStore {

    private static final Logger L = LoggerFactory.getLogger(VectorStore.class);

    private long id;

    private int dimensions;

    private RandomAccessFile raf;

    private FileChannel channel;

    private int recordLength;

    private int slot;

    private int maxSlots;

    private MappedByteBuffer buffer;

    public VectorStore(long id, int dimensions) throws Exception {
        this.id = id;
        this.dimensions = dimensions;
        recordLength = Long.BYTES + dimensions * Float.BYTES;
        File file = new File(Configuration.getConfiguration().get("data.directory", "data"), String.format("%d.bin", id));
        if (!file.exists()) {
            raf = new RandomAccessFile(file, "rw");
            maxSlots = Configuration.getConfiguration().get("data.max_vectors_per_index ", 65536);
            raf.setLength(Integer.BYTES + Integer.BYTES + (long) recordLength * maxSlots);
            slot = 0;
            L.info(String.format("created new data file '%s' with length %d and slot %d/%d", file.getAbsolutePath(), raf.length(), slot, maxSlots));
        } else {
            raf = new RandomAccessFile(file, "rw");
            slot = raf.readInt();
            maxSlots = raf.readInt();
            L.info(String.format("using existing data file '%s' with length %d and slot %d/%d", file.getAbsolutePath(), file.length(), slot, maxSlots));
        }
        channel = raf.getChannel();
        buffer = channel.map(FileChannel.MapMode.READ_WRITE, 0, raf.length());
    }

    public synchronized void write(VectorStoreItem item) {
        buffer.position(Integer.BYTES + Integer.BYTES + slot * recordLength);
        buffer.putLong(item.getId());
        for (float f : item.getEmbedding()) {
            buffer.putFloat(f);
        }
        slot++;
    }

    public VectorStoreItem read(int slot) {
        if (slot > this.slot - 1) {
            return (null);
        }
        buffer.position(Integer.BYTES + Integer.BYTES + slot * recordLength);
        long id = buffer.getLong();
        float[] f = new float[dimensions];
        for (int i = 0; i < dimensions; i++) {
            f[i] = buffer.getFloat();
        }
        VectorStoreItem item = new VectorStoreItem();
        item.setId(id);
        item.setEmbedding(f);
        return (item);
    }

    public long getId() {
        return (id);
    }

    public synchronized int getSlot() {
        return (slot);
    }

    public int getMaxSlots() {
        return (maxSlots);
    }

    public long getSizeOnDisk() {
        File file = new File(Configuration.getConfiguration().get("data.directory", "data"), String.format("%d.bin", id));
        return (file.length());
    }

    public synchronized void delete() throws Exception {
        channel.close();
        raf.close();
        File file = new File(Configuration.getConfiguration().get("data.directory", "data"), String.format("%d.bin", id));
        if (file.delete()) {
            L.info(String.format("deleted file '%s'", file.getAbsolutePath()));
        } else {
            L.error(String.format("error deleting file '%s'", file.getAbsolutePath()));
        }
    }

    public void close() throws Exception {
        buffer.position(0);
        buffer.putInt(slot);
        buffer.putInt(maxSlots);
        channel.close();
        raf.close();
        L.info(String.format("wrote slot %d/%d to vector store for index '%d'", slot, maxSlots, id));
    }

}
