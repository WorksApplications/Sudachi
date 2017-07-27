package com.worksap.nlp.sudachi;

import com.worksap.nlp.sudachi.dictionary.WordInfo;

public interface LatticeNode {

    public void setParameter(short leftId, short rightId, short cost);
    public int getBegin();
    public int getEnd();
    public boolean isOOV();
    public void setOOV();
    public WordInfo getWordInfo();
    public void setWordInfo(WordInfo wordInfo);
    public int getPathCost();
}
