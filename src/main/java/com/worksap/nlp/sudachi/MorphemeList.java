package com.worksap.nlp.sudachi;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.worksap.nlp.sudachi.dictionary.Grammar;
import com.worksap.nlp.sudachi.dictionary.Lexicon;
import com.worksap.nlp.sudachi.dictionary.WordInfo;

class MorphemeList extends AbstractList<Morpheme> {

    final InputText<byte[]> inputText;
    final Grammar grammar;
    final Lexicon lexicon;
    final List<LatticeNodeImpl> path;

    MorphemeList(InputText<byte[]> input,
                 Grammar grammar,
                 Lexicon lexicon,
                 List<LatticeNodeImpl> path) {
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
        return inputText.getOriginalOffset(path.get(index).getBegin());
    }

    int getEnd(int index) {
        return inputText.getOriginalOffset(path.get(index).getEnd());
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

        List<LatticeNodeImpl> nodes = new ArrayList<>(wordIds.length);
        for (int wid : wordIds) {
            nodes.add(new LatticeNodeImpl(lexicon,
                                          (short)0, (short)0, (short)0, wid));
        }

        int offset = path.get(index).getBegin();
        for (LatticeNodeImpl n : nodes) {
            n.begin = offset;
            offset += n.getWordInfo().getLength();
            n.end = offset;
        }

        return new MorphemeList(inputText, grammar, lexicon, nodes);
    }

    boolean isOOV(int index) {
        return path.get(index).isOOV();
    }
}
