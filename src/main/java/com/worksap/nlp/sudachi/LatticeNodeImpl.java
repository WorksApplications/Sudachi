package com.worksap.nlp.sudachi;

import com.worksap.nlp.sudachi.dictionary.Lexicon;
import com.worksap.nlp.sudachi.dictionary.WordInfo;

public class LatticeNodeImpl implements LatticeNode {

    int begin;
    int end;

    short leftId;
    short rightId;
    short cost;

    int wordId;

    int totalCost;
    LatticeNodeImpl bestPreviousNode;

    boolean isOOV;
    WordInfo extraWordInfo;

    Lexicon lexicon;

    LatticeNodeImpl(Lexicon lexicon,
                    short leftId, short rightId, short cost, int wordId) {
        this.lexicon = lexicon;
        this.leftId = leftId;
        this.rightId = rightId;
        this.cost = cost;
        this.wordId = wordId;
    }

    LatticeNodeImpl() {
        wordId = -1;
    }

    @Override
    public void setParameter(short leftId, short rightId, short cost) {
        this.leftId = leftId;
        this.rightId = rightId;
        this.cost = cost;
    }

    @Override
    public int getBegin() { return begin; }

    @Override
    public int getEnd() { return end; }

    @Override
    public boolean isOOV() { return isOOV; }

    @Override
    public void setOOV() { isOOV = true; }

    @Override
    public WordInfo getWordInfo() {
        if (wordId >= 0) {
            return lexicon.getWordInfo(wordId);
        } else if (extraWordInfo != null) {
            return extraWordInfo;
        }
        throw new RuntimeException("this node has no WordInfo");
    }

    @Override
    public void setWordInfo(WordInfo wordInfo) {
        extraWordInfo = wordInfo;
        wordId = -1;
    }

    @Override
    public String toString() {
        String surface;
        if (wordId < 0 && extraWordInfo == null) {
            surface = "(null)";
        } else {
            surface = getWordInfo().getSurface();
        }

        return String.format("%d %d %s(%d) %d %d %d",
                             getBegin(), getEnd(), surface, wordId,
                             leftId, rightId, cost);
    }
}
