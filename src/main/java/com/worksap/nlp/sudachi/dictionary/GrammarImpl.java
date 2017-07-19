package com.worksap.nlp.sudachi.dictionary;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class GrammarImpl implements Grammar {

    private final int POS_DEPTH = 6;
    private final short[] BOS_PARAMETER = new short[] { 0, 0, 0 }; 
    private final short[] EOS_PARAMETER = new short[] { 0, 0, 0 }; 

    private final ByteBuffer bytes;
    private List<String[]> posList;
    private final int connectTableOffset;
    private final short leftIdSize;
    private final short rightIdSize;

    private int storageSize;

    public GrammarImpl(ByteBuffer bytes, int offset) {
        int originalOffset = offset;
        this.bytes = bytes;
        short posSize = bytes.getShort(offset);
        offset += 2;
        posList = new ArrayList<String[]>(posSize) {
                @Override public int indexOf(Object o) {
                    if (!(o instanceof String[])) {
                        return -1;
                    }
                    String[] s = (String[])o;
                    search: for (int i = 0; i < this.size(); i++) {
                        if (this.get(i).length != s.length) {
                            continue;
                        }
                        for (int j = 0; j < s.length; j++) {
                            if (!this.get(i)[j].equals(s[j])) {
                                continue search;
                            }
                        }
                        return i;
                    }
                    return -1;
                }
            };
        for (int i = 0; i < posSize; i++) {
            posList.add(new String[POS_DEPTH]);
            for (int j = 0; j < POS_DEPTH; j++) {
                posList.get(i)[j] = bufferToString(offset);
                offset += 2 + 2 * posList.get(i)[j].length();
            }
        }
        leftIdSize = bytes.getShort(offset);
        offset += 2;
        rightIdSize = bytes.getShort(offset);
        offset += 2;
        connectTableOffset = offset;

        storageSize = (offset - originalOffset) + 2 * leftIdSize * rightIdSize;
    }

    public int storageSize() {
        return storageSize;
    }

    @Override
    public int getPartOfSpeechSize() {
        return posList.size();
    }

    @Override
    public String[] getPartOfSpeechString(short posId) {
        return posList.get(posId);
    }

    @Override
    public short getPartOfSpeechId(String[] pos) {
        return (short)posList.indexOf(pos);
    }

    @Override
    public short getConnectCost(int leftId, int rightId) {
        return bytes.getShort(connectTableOffset
                              + leftId * 2 + 2 * leftIdSize * rightId);
    }

    @Override
    public short[] getBOSParameter() { return BOS_PARAMETER; }

    @Override
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
