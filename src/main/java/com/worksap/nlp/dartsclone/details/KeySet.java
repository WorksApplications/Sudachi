package com.worksap.nlp.dartsclone.details;

public class KeySet {

    private byte[][] keys;
    private int[] values;

    public KeySet(byte[][] keys, int[] values) {
        this.keys = keys;
        this.values = values;
    }

    int size() { return keys.length; }
    
    byte[] getKey(int id) { return keys[id]; }
    
    byte getKeyByte(int keyId, int byteId) {
        if (byteId >= keys[keyId].length)
            return 0;
        return keys[keyId][byteId];
    }

    boolean hasValues() { return values != null; }

    int getValue(int id) {
        return (hasValues()) ? values[id] : id;
    }
}
