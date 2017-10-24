/*
 * Copyright (c) 2017 Works Applications Co., Ltd.
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

    final InputText<byte[]> inputText;
    final Grammar grammar;
    final Lexicon lexicon;
    final List<LatticeNode> path;

    MorphemeList(InputText<byte[]> input,
                 Grammar grammar,
                 Lexicon lexicon,
                 List<LatticeNode> path) {
        this.inputText = input;
        this.grammar = grammar;
        this.lexicon = lexicon;
        this.path = path;
    }

    @Override
    public Morpheme get(int index) {
        return new MorphemeImpl(this, index);
    }

    @Override
    public int size() { return path.size(); }

    int getBegin(int index) {
        return inputText.getOriginalIndex(path.get(index).getBegin());
    }

    int getEnd(int index) {
        return inputText.getOriginalIndex(path.get(index).getEnd());
    }

    String getSurface(int index) {
        int begin = getBegin(index);
        int end = getEnd(index);
        return inputText.getOriginalText().substring(begin, end);
    }

    WordInfo getWordInfo(int index) {
        return path.get(index).getWordInfo();
    }
    
    List<Morpheme> split(Tokenizer.SplitMode mode, int index, WordInfo wi) {
        int[] wordIds;
        switch (mode) {
        case A:
            wordIds = wi.getAunitSplit();
            break;
        case B:
            wordIds = wi.getBunitSplit();
            break;
        default:
            return Collections.singletonList(get(index));
        }
        if (wordIds.length == 0 || wordIds.length == 1) {
            return Collections.singletonList(get(index));
        }

        int offset = path.get(index).getBegin();
        List<LatticeNode> nodes = new ArrayList<>(wordIds.length);
        for (int wid : wordIds) {
            LatticeNodeImpl n
                = new LatticeNodeImpl(lexicon, (short)0, (short)0, (short)0, wid);
            n.begin = offset;
            offset += n.getWordInfo().getLength();
            n.end = offset;
            nodes.add(n);
        }

        return new MorphemeList(inputText, grammar, lexicon, nodes);
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
