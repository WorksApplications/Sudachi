package com.worksap.nlp.sudachi.dictionary;

import java.nio.ByteBuffer;

class GrammarImpl implements Grammar {

    private final int POS_DEPTH = 6;
    private final short[] BOS_PARAMETER = new short[] { 0, 0, 0 }; 
    private final short[] EOS_PARAMETER = new short[] { 0, 0, 0 }; 

    private ByteBuffer bytes;
    private String[][] posList;
    private int connectTableOffset;
    private short leftIdSize;
    private short rightIdSize;

    private int storageSize;

    GrammarImpl(ByteBuffer bytes, int offset) {
        int originalOffset = offset;
        this.bytes = bytes;
        short posSize = bytes.getShort(offset);
        offset += 2;
        posList = new String[posSize][];
        for (int i = 0; i < posSize; i++) {
            posList[i] = new String[POS_DEPTH];
            for (int j = 0; j < POS_DEPTH; j++) {
                posList[i][j] = bufferToString(offset);
                offset += 2 + 2 * posList[i][j].length();
            }
        }
        leftIdSize = bytes.getShort(offset);
        offset += 2;
        rightIdSize = bytes.getShort(offset);
        offset += 2;
        connectTableOffset = offset;

        storageSize = (offset - originalOffset) + 2 * leftIdSize * rightIdSize;
    }

    int storageSize() {
        return storageSize;
    }

    public String[] getPartOfSpeechString(short posId) {
        return posList[posId];
    }

    public short getConnectCost(int leftId, int rightId) {
        return bytes.getShort(connectTableOffset
                              + leftId * 2 + 2 * leftIdSize * rightId);
    }

    public short[] getBOSParameter() { return BOS_PARAMETER; }

    public short[] getEOSParameter() { return EOS_PARAMETER; }

    private String bufferToString(int offset) {
        short length = bytes.getShort(offset);
        char[] str = new char[length];
        for (int i = 0; i < length; i++) {
            str[i] = bytes.getChar(offset + 2 + 2 * i);
        }
        return new String(str);
    }
}
