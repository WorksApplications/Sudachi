package com.worksap.nlp.sudachi.dictionary;

public interface Grammar {
    public int getPartOfSpeechSize();
    public String[] getPartOfSpeechString(short posId);
    public short getPartOfSpeechId(String... pos);
    public short getConnectCost(short leftId, short rightId);
    public void setConnectCost(short leftId, short rightId, short cost);
    public short[] getBOSParameter();
    public short[] getEOSParameter();
    public CharacterCategory getCharacterCategory();
    public void setCharacterCategory(CharacterCategory charCategory);
}
