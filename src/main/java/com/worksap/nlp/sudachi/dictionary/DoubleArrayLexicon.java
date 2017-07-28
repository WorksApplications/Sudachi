package com.worksap.nlp.sudachi.dictionary;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.worksap.nlp.dartsclone.DoubleArray;

import com.worksap.nlp.sudachi.MorphemeList;
import com.worksap.nlp.sudachi.Tokenizer;

public class DoubleArrayLexicon implements Lexicon {

    static final int USER_DICT_COST_PAR_MORPH = -20;

    private WordIdTable wordIdTable;
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

        wordIdTable = new WordIdTable(bytes, offset);
        offset += wordIdTable.storageSize();

        wordParams = new WordParameterList(bytes, offset);
        offset += wordParams.storageSize();

        wordInfos = new WordInfoList(bytes, offset, wordParams.size());
    }

    @Override
    public Stream<int[]> lookup(byte[] text, int offset) {
        List<int[]> r
            = trie.commonPrefixSearch(text, offset, Integer.MAX_VALUE);
        if (r.isEmpty()) {
            return r.stream();
        }
        return r.stream()
            .flatMap(p -> Stream.of(wordIdTable.get(p[0]))
                     .map(i -> new int[] {i, p[1]}));
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

    public void calculateCost(Tokenizer tokenizer) {
        for (int wordId = 0; wordId < wordParams.size(); wordId++) {
            if (getCost(wordId) != Short.MIN_VALUE) {
                continue;
            }
            String surface = getWordInfo(wordId).getSurface();
            MorphemeList ms = (MorphemeList)tokenizer.tokenize(surface);
            int cost = ms.getInternalCost()
                + USER_DICT_COST_PAR_MORPH * ms.size();
            if (cost > Short.MAX_VALUE) {
                cost = Short.MAX_VALUE;
            } else if (cost < Short.MIN_VALUE) {
                cost = Short.MIN_VALUE;
            }
            wordParams.setCost(wordId, (short)cost);
        }
    }
}
