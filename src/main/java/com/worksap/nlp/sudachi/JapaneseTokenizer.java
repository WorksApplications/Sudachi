package com.worksap.nlp.sudachi;

import java.util.List;

import com.worksap.nlp.sudachi.dictionary.Grammar;
import com.worksap.nlp.sudachi.dictionary.Lexicon;

public class JapaneseTokenizer implements Tokenizer {

    Grammar grammar;
    Lexicon lexicon;

    JapaneseTokenizer(Grammar grammar, Lexicon lexicon) {
        this.grammar = grammar;
        this.lexicon = lexicon;
    }

    @Override
    public List<Morpheme> tokenize(Tokenizer.SplitMode mode, String text) {
        InputText<byte[]> input = new UTF8InputText(text);
        byte[] bytes = input.getText();

        LatticeImpl lattice = new LatticeImpl(bytes.length, grammar);
        for (int i = 0; i < bytes.length; i++) {
            for (int[] r :lexicon.lookup(bytes, i)) {
                int wordId = r[0];
                int length = r[1];


                LatticeNode n = new LatticeNodeImpl(lexicon,
                                                    lexicon.getLeftId(wordId),
                                                    lexicon.getRightId(wordId),
                                                    lexicon.getCost(wordId),
                                                    wordId);

                lattice.insert(i, i + length, n);
            }
        }

        List<LatticeNodeImpl> path = lattice.getBestPath();
        path.remove(path.size() - 1); // remove EOS
        return new MorphemeList(input, grammar, lexicon, path);
    }
}
