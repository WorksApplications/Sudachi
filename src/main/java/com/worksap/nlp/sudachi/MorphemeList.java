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

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.worksap.nlp.sudachi.dictionary.Grammar;
import com.worksap.nlp.sudachi.dictionary.Lexicon;
import com.worksap.nlp.sudachi.dictionary.WordInfo;

public class MorphemeList extends AbstractList<Morpheme> {
    final InputText inputText;
    final Grammar grammar;
    final Lexicon lexicon;
    final List<LatticeNode> path;
    final boolean allowEmptyMorpheme;

    final Tokenizer.SplitMode mode;

    public final static MorphemeList EMPTY = new MorphemeList(null, null, null, Collections.emptyList(), true,
            Tokenizer.SplitMode.C);

    MorphemeList(InputText input, Grammar grammar, Lexicon lexicon, List<LatticeNode> path, boolean allowEmptyMorpheme,
            Tokenizer.SplitMode mode) {
        this.inputText = input;
        this.grammar = grammar;
        this.lexicon = lexicon;
        this.path = path;
        this.allowEmptyMorpheme = allowEmptyMorpheme;
        this.mode = mode;
    }

    @Override
    public Morpheme get(int index) {
        return new MorphemeImpl(this, index);
    }

    @Override
    public int size() {
        return path.size();
    }

    int getBegin(int index) {
        int begin = inputText.getOriginalIndex(path.get(index).getBegin());
        if (!allowEmptyMorpheme) {
            int end = inputText.getOriginalIndex(path.get(index).getEnd());
            if (begin == end && index != 0) {
                return getBegin(index - 1);
            }
        }
        return begin;
    }

    int getEnd(int index) {
        int end = inputText.getOriginalIndex(path.get(index).getEnd());
        if (!allowEmptyMorpheme) {
            int begin = inputText.getOriginalIndex(path.get(index).getBegin());
            if (begin == end && index != 0) {
                return getEnd(index - 1);
            }
        }
        return end;
    }

    String getSurface(int index) {
        int begin = getBegin(index);
        int end = getEnd(index);
        return inputText.getOriginalText().substring(begin, end);
    }

    WordInfo getWordInfo(int index) {
        return path.get(index).getWordInfo();
    }

    List<Morpheme> split(Tokenizer.SplitMode mode, int index) {
        List<LatticeNode> nodes = new ArrayList<>();
        LatticeNodeImpl node = (LatticeNodeImpl) path.get(index);
        node.appendSplitsTo(nodes, mode);
        return new MorphemeList(inputText, grammar, lexicon, nodes, allowEmptyMorpheme, mode);
    }

    /**
     * Produce a copy of this list in a different split mode. If the mode is coarser
     * than the current split mode, returns the current list. The current list is
     * not modified.
     *
     * @param mode
     *            requested split mode
     * @return current list or a new list in the requested split mode.
     */
    public MorphemeList split(Tokenizer.SplitMode mode) {
        if (mode.compareTo(this.mode) >= 0) {
            return this;
        }

        List<LatticeNode> nodes = new ArrayList<>();

        for (LatticeNode node : path) {
            LatticeNodeImpl nodeImpl = (LatticeNodeImpl) node;
            nodeImpl.appendSplitsTo(nodes, mode);
        }

        return new MorphemeList(inputText, grammar, lexicon, nodes, allowEmptyMorpheme, mode);
    }

    boolean isOOV(int index) {
        return path.get(index).isOOV();
    }

    int getWordId(int index) {
        return path.get(index).getWordId();
    }

    int getDictionaryId(int index) {
        return path.get(index).getDictionaryId();
    }

    public int getInternalCost() {
        return path.get(path.size() - 1).getPathCost() - path.get(0).getPathCost();
    }
}
