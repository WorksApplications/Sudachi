/*
 * Copyright (c) 2021 Works Applications Co., Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.worksap.nlp.sudachi;

import com.worksap.nlp.sudachi.dictionary.Lexicon;
import com.worksap.nlp.sudachi.dictionary.WordInfo;

import java.util.List;

public class LatticeNodeImpl implements LatticeNode {

    int begin;
    int end;

    short leftId;
    short rightId;
    short cost;

    int wordId;

    int totalCost;
    LatticeNodeImpl bestPreviousNode;
    boolean isConnectedToBOS;

    boolean isDefined;
    boolean isOOV;
    WordInfo extraWordInfo;

    Lexicon lexicon;

    static final String NULL_SURFACE = "(null)";
    private static final short ZERO = (short) 0;
    static final WordInfo UNDEFINED_WORDINFO = new WordInfo(NULL_SURFACE, ZERO, (short) -1, NULL_SURFACE, NULL_SURFACE,
            NULL_SURFACE);

    LatticeNodeImpl(Lexicon lexicon, short leftId, short rightId, short cost, int wordId) {
        this.lexicon = lexicon;
        this.leftId = leftId;
        this.rightId = rightId;
        this.cost = cost;
        this.wordId = wordId;
        this.isDefined = true;
    }

    LatticeNodeImpl() {
        isDefined = false;
    }

    @Override
    public void setParameter(short leftId, short rightId, short cost) {
        this.leftId = leftId;
        this.rightId = rightId;
        this.cost = cost;
    }

    @Override
    public int getBegin() {
        return begin;
    }

    @Override
    public int getEnd() {
        return end;
    }

    @Override
    public void setRange(int begin, int end) {
        this.begin = begin;
        this.end = end;
    }

    @Override
    public boolean isOOV() {
        return isOOV;
    }

    @Override
    public void setOOV() {
        isOOV = true;
    }

    @Override
    public WordInfo getWordInfo() {
        if (!isDefined) {
            return UNDEFINED_WORDINFO;
        }
        if (extraWordInfo != null) {
            return extraWordInfo;
        }
        return lexicon.getWordInfo(wordId);
    }

    @Override
    public void setWordInfo(WordInfo wordInfo) {
        extraWordInfo = wordInfo;
        isDefined = true;
    }

    @Override
    public int getPathCost() {
        return cost;
    }

    @Override
    public int getWordId() {
        return wordId;
    }

    @Override
    public int getDictionaryId() {
        if (!isDefined || extraWordInfo != null) {
            return -1;
        }
        return WordId.dic(wordId);
    }

    @Override
    public String toString() {
        WordInfo wi = getWordInfo();
        String surface = wi.getSurface();
        short pos = wi.getPOSId();

        return String.format("%d %d %s(%d) %d %d %d %d", getBegin(), getEnd(), surface, wordId, pos, leftId, rightId,
                cost);
    }

    /* internal */ void appendSplitsTo(List<LatticeNode> result, Tokenizer.SplitMode mode) {
        if (mode == Tokenizer.SplitMode.A) {
            appendSplitsTo(result, getWordInfo().getAunitSplit());
        } else if (mode == Tokenizer.SplitMode.B) {
            appendSplitsTo(result, getWordInfo().getBunitSplit());
        } else {
            result.add(this);
        }
    }

    private void appendSplitsTo(List<LatticeNode> result, int[] splitsId) {
        if (splitsId.length == 0) {
            result.add(this);
            return;
        } else if (splitsId.length == 1) {
            int wid = splitsId[0];
            if (wid == getWordId()) {
                result.add(this);
            } else {
                LatticeNodeImpl node = new LatticeNodeImpl(lexicon, ZERO, ZERO, ZERO, wid);
                node.begin = begin;
                node.end = end;
                node.totalCost = totalCost;
                result.add(node);
            }
            return;
        }

        int offset = getBegin();
        for (int wid : splitsId) {
            LatticeNodeImpl n = new LatticeNodeImpl(lexicon, ZERO, ZERO, ZERO, wid);
            n.begin = offset;
            offset += n.getWordInfo().getLength();
            n.end = offset;
            result.add(n);
        }
    }
}
