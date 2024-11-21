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
import com.worksap.nlp.sudachi.dictionary.WordParameters;

import java.util.List;
import java.util.Objects;

public class LatticeNodeImpl implements LatticeNode {
    // index of this node in the InuptText.bytes.
    int begin;
    int end;

    int wordId;

    // word param/info that corresponds to wordId or that manually set
    // (special/OOV).
    short leftId;
    short rightId;
    short cost;
    WordInfo wordInfo;

    // for lattice construction
    int totalCost;
    LatticeNodeImpl bestPreviousNode;

    // either Lexicon or StringsCache object
    Object lexiconOrStrings;

    // Empty wordInfo for special words.
    static final WordInfo UNDEFINED_WORDINFO = new WordInfo((short) 0, (short) -1);

    LatticeNodeImpl(Lexicon lexicon, long params, int wordId) {
        this.lexiconOrStrings = lexicon;
        this.leftId = WordParameters.leftId(params);
        this.rightId = WordParameters.rightId(params);
        this.cost = WordParameters.cost(params);
        this.wordId = wordId;
    }

    /** Create empty node. Caller must fill fields. */
    LatticeNodeImpl() {
    }

    /**
     * Create special node with given wordid.
     * 
     * WordParameters should be set by the caller if needed.
     */
    static LatticeNodeImpl makeSpecial(int specialWordId) {
        assert WordId.isSpecial(specialWordId);
        LatticeNodeImpl node = new LatticeNodeImpl();
        node.wordId = specialWordId;
        node.setWordInfo(UNDEFINED_WORDINFO);
        return node;
    }

    /**
     * Create OOV node.
     * 
     * WordParameters should be set by the caller if needed.
     */
    static LatticeNodeImpl makeOov(int begin, int end, short posId, String surface, String normalizedForm,
            String dictionaryForm, String readingForm) {
        LatticeNodeImpl node = new LatticeNodeImpl();
        node.wordId = WordId.makeOov(posId);
        node.wordInfo = new WordInfo((short) (end - begin), posId);
        node.lexiconOrStrings = new StringsCache(surface, readingForm, normalizedForm, dictionaryForm);
        node.begin = begin;
        node.end = end;
        return node;
    }

    @Override
    public void setParameter(short leftId, short rightId, short cost) {
        this.leftId = leftId;
        this.rightId = rightId;
        this.cost = cost;
    }

    /**
     * Set the parameters of connection.
     *
     * @param params
     *            packed parameters
     * 
     * @see com.worksap.nlp.sudachi.dictionary.WordParameters
     */
    public void setParameter(long params) {
        this.leftId = WordParameters.leftId(params);
        this.rightId = WordParameters.rightId(params);
        this.cost = WordParameters.cost(params);
    }

    private Lexicon lexicon() {
        if (lexiconOrStrings instanceof Lexicon) {
            return (Lexicon) lexiconOrStrings;
        } else if (lexiconOrStrings instanceof StringsCache) {
            return ((StringsCache) lexiconOrStrings).getLexicon();
        } else {
            throw new IllegalStateException("lexicon was null probably");
        }
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
        return WordId.isOov(wordId);
    }

    @Override
    public void setOOV() {
        this.wordId = WordId.ID_OOV_NOPOS;
    }

    /** @return if this node is a special node. */
    public boolean isSpecial() {
        return WordId.isSpecial(wordId);
    }

    /** @return if this node comes from a dictionary. */
    public boolean isDefined() {
        return !isOOV() && !isSpecial();
    }

    @Override
    public WordInfo getWordInfo() {
        if (wordInfo != null) {
            return wordInfo;
        }
        wordInfo = lexicon().getWordInfo(wordId);
        return wordInfo;
    }

    @Override
    public void setWordInfo(WordInfo wordInfo) {
        this.wordInfo = wordInfo;
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
        if (isDefined()) {
            return WordId.dic(wordId);
        }
        return -1;
    }

    public boolean isConnectedToBOS() {
        return bestPreviousNode != null;
    }

    @Override
    public String toString() {
        String surface = getSurface();
        short pos = getWordInfo().getPOSId();

        return String.format("%d %d %s(%d) %d %d %d %d", getBegin(), getEnd(), surface, wordId, pos, leftId, rightId,
                cost);
    }

    @Override
    public StringsCache getStrings() {
        Object l = lexiconOrStrings;
        if (l instanceof Lexicon) {
            StringsCache c = new StringsCache((Lexicon) l, wordId);
            lexiconOrStrings = c;
            return c;
        } else if (l instanceof StringsCache) {
            return (StringsCache) l;
        } else {
            throw new IllegalStateException("lexicon is not valid, was " + l);
        }
    }

    /* internal */ void appendSplitsTo(List<LatticeNodeImpl> result, Tokenizer.SplitMode mode) {
        if (mode == Tokenizer.SplitMode.A) {
            appendSplitsTo(result, getWordInfo().getAunitSplit());
        } else if (mode == Tokenizer.SplitMode.B) {
            appendSplitsTo(result, getWordInfo().getBunitSplit());
        } else if (mode == Tokenizer.SplitMode.C) {
            appendSplitsTo(result, getWordInfo().getCunitSplit());
        } else {
            result.add(this);
        }
    }

    private void appendSplitsTo(List<LatticeNodeImpl> result, int[] splitsId) {
        if (splitsId.length == 0) {
            result.add(this);
            return;
        } else if (splitsId.length == 1) {
            int wid = splitsId[0];
            if (wid == getWordId()) {
                result.add(this);
            } else {
                LatticeNodeImpl node = new LatticeNodeImpl(lexicon(), 0L, wid);
                node.begin = begin;
                node.end = end;
                node.totalCost = totalCost;
                result.add(node);
            }
            return;
        }

        int offset = getBegin();
        Lexicon lex = lexicon();
        for (int wid : splitsId) {
            LatticeNodeImpl n = new LatticeNodeImpl(lex, 0L, wid);
            n.begin = offset;
            offset += n.getWordInfo().getLength();
            n.end = offset;
            result.add(n);
        }
    }

    /** Alias for {@link OOVFactory} constructor. */
    public static OOVFactory oovFactory(short leftId, short rightId, short cost, short posId) {
        return new OOVFactory(leftId, rightId, cost, posId);
    }

    /**
     * Factory class for creating OOV LatticeNodeImpl with fixed word paramters and
     * pos.
     */
    public static final class OOVFactory {
        private final short leftId;
        private final short rightId;
        private final short cost;
        private final short posId;

        private OOVFactory(short leftId, short rightId, short cost, short posId) {
            this.rightId = rightId;
            this.cost = cost;
            this.leftId = leftId;
            this.posId = posId;
        }

        /**
         * Create OOV LatticeNode at the given position of the input.
         * 
         * The begin/end must be an index in InputText.bytes.
         */
        public LatticeNodeImpl make(int begin, int end, InputText input) {
            String s = input.getSubstring(begin, end);
            return make(begin, end, s);
        }

        /**
         * Create OOV LatticeNode at the given position and surface.
         * 
         * The begin/end must be an index in InputText.bytes.
         */
        public LatticeNodeImpl make(int begin, int end, String text) {
            LatticeNodeImpl i = makeOov(begin, end, posId, text, text, text, text);
            i.setParameter(leftId, rightId, cost);
            return i;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            OOVFactory that = (OOVFactory) o;
            return leftId == that.leftId && rightId == that.rightId && cost == that.cost && posId == that.posId;
        }

        @Override
        public int hashCode() {
            return Objects.hash(leftId, rightId, cost, posId);
        }
    }
}
