package com.worksap.nlp.sudachi.dictionary;

public interface Grammar {
    public int getPartOfSpeechSize();
    public String[] getPartOfSpeechString(short posId);
    public short getConnectCost(int leftId, int rightId);
    public short[] getBOSParameter();
    public short[] getEOSParameter();
}
