package com.worksap.nlp.sudachi.dictionary;

import java.util.List;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import com.worksap.nlp.dartsclone.DoubleArray;

public class DoubleArrayLexicon implements Lexicon {

    private WordParameterList wordParams;
    private WordInfoList wordInfos;
    private DoubleArray trie;

    public DoubleArrayLexicon(ByteBuffer bytes, int offset) {
        trie = new DoubleArray();
        int size = bytes.getInt(offset);
        offset += 4;
        bytes.position(offset);
        IntBuffer array = bytes.asIntBuffer();
        trie.setArray(array, size);
        offset += trie.totalSize();

        wordParams = new WordParameterList(bytes, offset);
        offset += wordParams.storageSize();

        wordInfos = new WordInfoList(bytes, offset, wordParams.size());
    }

    @Override
    public List<int[]> lookup(byte[] text, int offset) {
        return trie.commonPrefixSearch(text, offset, Integer.MAX_VALUE);
    }

    @Override
    public short getLeftId(int wordId) {
        return wordParams.getLeftId(wordId);
    }

    @Override
    public short getRightId(int wordId) {
        return wordParams.getRightId(wordId);
    }

    @Override
    public short getCost(int wordId) {
        return wordParams.getCost(wordId);
    }

    @Override
    public WordInfo getWordInfo(int wordId) {
        return wordInfos.getWordInfo(wordId);
    }
}
