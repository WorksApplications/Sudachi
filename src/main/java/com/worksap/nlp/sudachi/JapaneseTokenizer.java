package com.worksap.nlp.sudachi;

import java.util.List;
import java.util.ArrayList;

import com.worksap.nlp.sudachi.dictionary.Grammar;
import com.worksap.nlp.sudachi.dictionary.Lexicon;

public class JapaneseTokenizer implements Tokenizer {

    Grammar grammar;
    Lexicon lexicon;
    List<InputTextPlugin> inputTextPlugins;
    List<WordLookingUpPlugin> wordLookingupPlugins;

    JapaneseTokenizer(Grammar grammar, Lexicon lexicon/*,
                      List<InputTextPlugin> inputTextPlugins,
                      List<WordLookingUpPlugin> wordLookingupPlugins */) {
        this.grammar = grammar;
        this.lexicon = lexicon;
        /*
        this.inputTextPlugins = inputTextPlugins;
        this.wordLookingupPlugins = wordLookingupPlugins;
        */
    }

    @Override
    public List<Morpheme> tokenize(Tokenizer.SplitMode mode, String text) {
        UTF8InputText input = new UTF8InputText(text);
        for (InputTextPlugin plugin : inputTextPlugins) {
            plugin.rewrite(input);
        }
        byte[] bytes = input.getByteText();

        LatticeImpl lattice = new LatticeImpl(bytes.length, grammar);
        for (int i = 0; i < bytes.length; i++) {
            List<int[]> words = lexicon.lookup(bytes, i);
            /*
            if (words.isEmpty()) {
                for (WordLookingUpPlugin plugin : wordLookingupPlugins) {
                    plugin.rewrite(bytes, i);
                }
            }
            */
            for (int[] r : words) {
                int wordId = r[0];
                int end = r[1];

                LatticeNode n = new LatticeNodeImpl(lexicon,
                                                    lexicon.getLeftId(wordId),
                                                    lexicon.getRightId(wordId),
                                                    lexicon.getCost(wordId),
                                                    wordId);
                lattice.insert(i, end, n);
            }

            // OOV
            for (WordLookingUpPlugin plugin : wordLookingupPlugins) {
                for (LatticeNode node : plugin.getOOV(input, i, words)) {
                    lattice.insert(node.getBegin(), node.getEnd(), node);
                }
            }
        }

        List<LatticeNodeImpl> path = lattice.getBestPath();
        path.remove(path.size() - 1); // remove EOS

        if (mode != Tokenizer.SplitMode.C) {
            List<LatticeNodeImpl> newPath = new ArrayList<>();
            for (LatticeNodeImpl node : path) {
                int[] wids;
                if (mode == Tokenizer.SplitMode.A) {
                    wids = node.getWordInfo().getAunitSplit();
                } else {        // Tokenizer.SplitMode.B
                    wids = node.getWordInfo().getBunitSplit();
                }
                if (wids.length == 0) {
                    newPath.add(node);
                } else {
                    int offset = node.begin;
                    for (int wid : wids) {
                        LatticeNodeImpl n
                            = new LatticeNodeImpl(lexicon,
                                                  (short)0, (short)0, (short)0,
                                                  wid);
                        n.begin = offset;
                        offset += n.getWordInfo().getLength();
                        n.end = offset;
                        newPath.add(n);
                    }
                }
            }
            path = newPath;
        }

        return new MorphemeList(input, grammar, lexicon, path);
    }
}
