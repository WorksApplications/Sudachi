package com.worksap.nlp.sudachi;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import com.worksap.nlp.sudachi.dictionary.Grammar;
import com.worksap.nlp.sudachi.dictionary.Lexicon;

public class JapaneseTokenizer implements Tokenizer {

    Grammar grammar;
    Lexicon lexicon;
    List<InputTextPlugin> inputTextPlugins;
    List<OovProviderPlugin> oovProviderPlugins;
    List<PathRewritePlugin> pathRewritePlugins;
    PrintStream dumpOutput;

    JapaneseTokenizer(Grammar grammar, Lexicon lexicon,
                      List<InputTextPlugin> inputTextPlugins,
                      List<OovProviderPlugin> oovProviderPlugins,
                      List<PathRewritePlugin> pathRewritePlugins) {

        this.grammar = grammar;
        this.lexicon = lexicon;
        this.inputTextPlugins = inputTextPlugins;
        this.oovProviderPlugins = oovProviderPlugins;
        this.pathRewritePlugins = pathRewritePlugins;
    }

    @Override
    public List<Morpheme> tokenize(Tokenizer.SplitMode mode, String text) {
        if (text.isEmpty()) {
            return Collections.emptyList();
        }

        UTF8InputText input = new UTF8InputText(text, grammar);
        for (InputTextPlugin plugin : inputTextPlugins) {
            plugin.rewrite(input);
        }
        byte[] bytes = input.getByteText();

        LatticeImpl lattice = new LatticeImpl(bytes.length, grammar);
        for (int i = 0; i < bytes.length; i++) {
            if (!input.isCharAlignment(i) || !lattice.hasPreviousNode(i)) {
                continue;
            }
            Iterator<int[]> iterator = lexicon.lookup(bytes, i);
            boolean hasWords = iterator.hasNext();
            while (iterator.hasNext()) {
                int[] r = iterator.next();
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
            for (OovProviderPlugin plugin : oovProviderPlugins) {
                for (LatticeNode node : plugin.getOOV(input, i, hasWords)) {
                    hasWords = true;
                    lattice.insert(node.getBegin(), node.getEnd(), node);
                }
            }
            if (!hasWords) {
                throw new IllegalStateException("there is no morpheme at " + i);
            }
        }

        List<LatticeNode> path = lattice.getBestPath();
        path.remove(path.size() - 1); // remove EOS
        for (PathRewritePlugin plugin : pathRewritePlugins) {
            plugin.rewrite(path, lattice);
        }

        if (dumpOutput != null) {
            lattice.dump(dumpOutput);
        }

        if (mode != Tokenizer.SplitMode.C) {
            List<LatticeNode> newPath = new ArrayList<>();
            for (LatticeNode node : path) {
                int[] wids;
                if (mode == Tokenizer.SplitMode.A) {
                    wids = node.getWordInfo().getAunitSplit();
                } else {        // Tokenizer.SplitMode.B
                    wids = node.getWordInfo().getBunitSplit();
                }
                if (wids.length == 0) {
                    newPath.add(node);
                } else {
                    int offset = node.getBegin();
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

    public void setDumpOutput(PrintStream output) {
        dumpOutput = output;
    }
}
