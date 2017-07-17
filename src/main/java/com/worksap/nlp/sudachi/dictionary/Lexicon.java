package com.worksap.nlp.sudachi.dictionary;

import java.util.List;

public interface Lexicon {

    List<int[]> lookup(byte[] text, int offset);
    short getLeftId(int wordId);
    short getRightId(int wordId);
    short getCost(int wordId);
    WordInfo getWordInfo(int wordId);
}
