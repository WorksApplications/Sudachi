package com.worksap.nlp.sudachi.dictionary;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class WordParameters {
    private final ByteBuffer data;

    private WordParameters(ByteBuffer data) {
        this.data = data;
    }

    public long loadParams(int wordId) {
        int addr = wordId * 8;
        return data.getLong(addr);
    }

    public void setCost(int wordId, short cost) {
        int addr = wordId * 8 + 6;
        data.putShort(addr, cost);
    }

    public static WordParameters readOnly(ByteBuffer full, Description desc) {
        ByteBuffer data = desc.slice(full, Blocks.ENTRIES);
        data.order(ByteOrder.LITTLE_ENDIAN);
        return new WordParameters(data);
    }

    public static WordParameters readWrite(ByteBuffer full, Description desc) {
        WordParameters ro = readOnly(full, desc);
        ByteBuffer roBuf = ro.data;
        int lim = roBuf.limit();
        ByteBuffer buf = ByteBuffer.allocate(lim);
        buf.order(ByteOrder.LITTLE_ENDIAN);
        roBuf.put(buf);
        buf.position(0);
        return new WordParameters(buf);
    }

    public static short leftId(long packed) {
        return (short) (packed & 0xffff);
    }

    public static short rightId(long packed) {
        return (short) ((packed >>> 16) & 0xffff);
    }

    public static short cost(long packed) {
        return (short) ((packed >>> 32) & 0xffff);
    }
}
