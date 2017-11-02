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

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.worksap.nlp.sudachi.dictionary.CategoryType;
import com.worksap.nlp.sudachi.dictionary.Grammar;
import com.worksap.nlp.sudachi.dictionary.Lexicon;

class JapaneseTokenizer implements Tokenizer {

    Grammar grammar;
    Lexicon lexicon;
    List<InputTextPlugin> inputTextPlugins;
    List<OovProviderPlugin> oovProviderPlugins;
    List<PathRewritePlugin> pathRewritePlugins;
    OovProviderPlugin defaultOovProvider;
    PrintStream dumpOutput;

    LatticeImpl lattice;

    JapaneseTokenizer(Grammar grammar, Lexicon lexicon,
                      List<InputTextPlugin> inputTextPlugins,
                      List<OovProviderPlugin> oovProviderPlugins,
                      List<PathRewritePlugin> pathRewritePlugins) {

        this.grammar = grammar;
        this.lexicon = lexicon;
        this.inputTextPlugins = inputTextPlugins;
        this.oovProviderPlugins = oovProviderPlugins;
        this.pathRewritePlugins = pathRewritePlugins;
        this.lattice = new LatticeImpl(grammar);

        if (!oovProviderPlugins.isEmpty()) {
            defaultOovProvider = oovProviderPlugins.get(oovProviderPlugins.size() - 1);
        }
    }

    @Override
    public List<Morpheme> tokenize(Tokenizer.SplitMode mode, String text) {
        if (text.isEmpty()) {
            return Collections.emptyList();
        }

        UTF8InputTextBuilder builder = new UTF8InputTextBuilder(text, grammar);
        for (InputTextPlugin plugin : inputTextPlugins) {
            plugin.rewrite(builder);
        }
        UTF8InputText input = builder.build();
        if (dumpOutput != null) {
            dumpOutput.println("=== Input dump:");
            dumpOutput.println(input.getText());
        }

        byte[] bytes = input.getByteText();
        lattice.resize(bytes.length);
        for (int i = 0; i < bytes.length; i++) {
            if (!input.canBow(i) || !lattice.hasPreviousNode(i)) {
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
            if (!input.getCharCategoryTypes(i).contains(CategoryType.NOOOVBOW)) {
                for (OovProviderPlugin plugin : oovProviderPlugins) {
                    for (LatticeNode node : plugin.getOOV(input, i, hasWords)) {
                        hasWords = true;
                        lattice.insert(node.getBegin(), node.getEnd(), node);
                    }
                }
            }
            if (!hasWords && defaultOovProvider != null) {
                for (LatticeNode node : defaultOovProvider.getOOV(input, i, hasWords)) {
                    hasWords = true;
                    lattice.insert(node.getBegin(), node.getEnd(), node);
                }
            }
            if (!hasWords) {
                throw new IllegalStateException("there is no morpheme at " + i);
            }
        }

        List<LatticeNode> path = lattice.getBestPath();
        if (dumpOutput != null) {
            dumpOutput.println("=== Lattice dump:");
            lattice.dump(dumpOutput);
        }
        lattice.clear();

        path.remove(path.size() - 1); // remove EOS
        if (dumpOutput != null) {
            dumpOutput.println("=== Before rewriting:");
            dumpPath(path);
        }
        for (PathRewritePlugin plugin : pathRewritePlugins) {
            plugin.rewrite(input, path, lattice);
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
                if (wids.length == 0 || wids.length == 1) {
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

        if (dumpOutput != null) {
            dumpOutput.println("=== After rewriting:");
            dumpPath(path);
            dumpOutput.println("===");
        }

        return new MorphemeList(input, grammar, lexicon, path);
    }

    @Override
    public void setDumpOutput(PrintStream output) {
        dumpOutput = output;
    }

    void dumpPath(List<LatticeNode> path) {
        int i = 0;
        for (LatticeNode node : path) {
            dumpOutput.println(String.format("%d: %s", i, node.toString()));
            i++;
        }
    }
}
