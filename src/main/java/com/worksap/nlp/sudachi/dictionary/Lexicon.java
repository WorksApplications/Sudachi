package com.worksap.nlp.sudachi.dictionary;

import java.util.stream.Stream;

public interface Lexicon {

    Stream<int[]> lookup(byte[] text, int offset);
    short getLeftId(int wordId);
    short getRightId(int wordId);
    short getCost(int wordId);
    WordInfo getWordInfo(int wordId);
}
