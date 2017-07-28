package com.worksap.nlp.sudachi.dictionary;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class LexiconSet implements Lexicon {

    List<Lexicon> lexicons = new ArrayList<>();;

    public LexiconSet(Lexicon systemLexicon) {
        lexicons.add(systemLexicon);
    }

    public void add(Lexicon lexicon) {
        if (!lexicons.contains(lexicon)) {
            lexicons.add(lexicon);
        }
    }

    public Stream<int[]> lookup(byte[] text, int offset) {
        if (lexicons.size() == 1) {
            return lexicons.get(0).lookup(text, offset);
        }
        Stream<int[]> results = Stream.<int[]>empty();
        for (int i = 1; i < lexicons.size(); i++) {
            Stream<int[]> rs = lexicons.get(i).lookup(text, offset);
            final int dictId = i;
            rs = rs.map(r -> new int[] {buildWordId(dictId, r[0]), r[1]});
            results = Stream.concat(results, rs);
        }
        results = Stream.concat(results, lexicons.get(0).lookup(text, offset));
        return results;
    }

    public short getLeftId(int wordId) {
        return lexicons.get(getDictionaryId(wordId)).getLeftId(getWordId(wordId));
    }

    public short getRightId(int wordId) {
        return lexicons.get(getDictionaryId(wordId)).getRightId(getWordId(wordId));
    }

    public short getCost(int wordId) {
        return lexicons.get(getDictionaryId(wordId)).getCost(getWordId(wordId));
    }

    public WordInfo getWordInfo(int wordId) {
        return lexicons.get(getDictionaryId(wordId)).getWordInfo(getWordId(wordId));
    }

    private int getDictionaryId(int wordId) {
        return wordId >>> 28;
    }

    private int getWordId(int wordId) {
        return 0x0fffffff & wordId;
    }

    private int buildWordId(int dictId, int wordId) {
        if (wordId > 0x0fffffff) {
            throw new RuntimeException("wordId is too large: " + wordId);
        }
        if (dictId > 0xf) {
            throw new RuntimeException("dictionaryId is too large: " + dictId);
        }
        return (dictId << 28) | wordId;
    }
}
