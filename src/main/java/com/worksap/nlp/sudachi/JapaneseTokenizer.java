/*
 * Copyright (c) 2020 Works Applications Co., Ltd.
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

import java.io.IOException;
import java.io.PrintStream;
import java.io.Reader;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.worksap.nlp.sudachi.dictionary.CategoryType;
import com.worksap.nlp.sudachi.dictionary.Grammar;
import com.worksap.nlp.sudachi.dictionary.Lexicon;
import com.worksap.nlp.sudachi.sentdetect.SentenceDetector;

class JapaneseTokenizer implements Tokenizer {

    Grammar grammar;
    Lexicon lexicon;
    List<InputTextPlugin> inputTextPlugins;
    List<OovProviderPlugin> oovProviderPlugins;
    List<PathRewritePlugin> pathRewritePlugins;
    OovProviderPlugin defaultOovProvider;
    PrintStream dumpOutput;
    boolean allowEmptyMorpheme;

    LatticeImpl lattice;

    JapaneseTokenizer(Grammar grammar, Lexicon lexicon, List<InputTextPlugin> inputTextPlugins,
            List<OovProviderPlugin> oovProviderPlugins, List<PathRewritePlugin> pathRewritePlugins) {

        this.grammar = grammar;
        this.lexicon = lexicon;
        this.inputTextPlugins = inputTextPlugins;
        this.oovProviderPlugins = oovProviderPlugins;
        this.pathRewritePlugins = pathRewritePlugins;
        this.lattice = new LatticeImpl(grammar);
        allowEmptyMorpheme = true;

        if (!oovProviderPlugins.isEmpty()) {
            defaultOovProvider = oovProviderPlugins.get(oovProviderPlugins.size() - 1);
        }
    }

    @Override
    public List<Morpheme> tokenize(Tokenizer.SplitMode mode, String text) {
        if (text.isEmpty()) {
            return Collections.emptyList();
        }
        UTF8InputText input = buildInputText(text);
        return tokenizeSentence(mode, input);
    }

    @Override
    public Iterable<List<Morpheme>> tokenizeSentences(SplitMode mode, String text) {
        if (text.isEmpty()) {
            return Collections.emptyList();
        }
        UTF8InputText input = buildInputText(text);
        String normalized = input.getText();

        ArrayList<List<Morpheme>> sentences = new ArrayList<>();
        SentenceDetector detector = new SentenceDetector();
        int bos = 0;
        int length;
        NonBreakChecker checker = new NonBreakChecker(input);
        checker.setBos(bos);
        while ((length = detector.getEos(normalized, checker)) != 0) {
            if (length < 0) {
                length = -length;
            }
            int eos = bos + length;
            if (eos < normalized.length()) {
                eos = input.getNextInOriginal(eos - 1);
                length = eos - bos;
            }
            UTF8InputText sentence = input.slice(bos, eos);
            sentences.add(tokenizeSentence(mode, sentence));
            normalized = normalized.substring(length);
            bos = eos;
            checker.setBos(bos);
        }
        return sentences;
    }

    @Override
    public Iterable<List<Morpheme>> tokenizeSentences(SplitMode mode, Reader reader) throws IOException {
        ArrayList<List<Morpheme>> sentences = new ArrayList<>();
        CharBuffer buffer = CharBuffer.allocate(SentenceDetector.DEFAULT_LIMIT);
        SentenceDetector detector = new SentenceDetector();

        while (reader.read(buffer) > 0) {
            buffer.flip();

            UTF8InputText input = buildInputText(buffer);
            String normalized = input.getText();

            int bos = 0;
            int length;
            NonBreakChecker checker = new NonBreakChecker(input);
            checker.setBos(bos);
            while ((length = detector.getEos(normalized, checker)) > 0) {
                int eos = bos + length;
                if (eos < normalized.length()) {
                    eos = input.getNextInOriginal(eos - 1);
                    length = eos - bos;
                }
                UTF8InputText sentence = input.slice(bos, eos);
                sentences.add(tokenizeSentence(mode, sentence));
                normalized = normalized.substring(length);
                bos = eos;
                checker.setBos(bos);
            }
            if (length < 0) {
                buffer.position(input.textIndexToOriginalTextIndex(bos));
                buffer.compact();
            }
        }
        buffer.flip();
        if (buffer.hasRemaining()) {
            sentences.add(tokenizeSentence(mode, buildInputText(buffer)));
        }

        return sentences;
    }

    @Override
    public void setDumpOutput(PrintStream output) {
        dumpOutput = output;
    }

    UTF8InputText buildInputText(CharSequence text) {
        UTF8InputTextBuilder builder = new UTF8InputTextBuilder(text, grammar);
        for (InputTextPlugin plugin : inputTextPlugins) {
            plugin.rewrite(builder);
        }
        UTF8InputText input = builder.build();
        if (dumpOutput != null) {
            dumpOutput.println("=== Input dump:");
            dumpOutput.println(input.getText());
        }

        return input;
    }

    List<Morpheme> tokenizeSentence(Tokenizer.SplitMode mode, UTF8InputText input) {
        buildLattice(input);

        if (dumpOutput != null) {
            dumpOutput.println("=== Lattice dump:");
            lattice.dump(dumpOutput);
        }

        List<LatticeNode> path = lattice.getBestPath();

        if (dumpOutput != null) {
            dumpOutput.println("=== Before rewriting:");
            dumpPath(path);
        }

        for (PathRewritePlugin plugin : pathRewritePlugins) {
            plugin.rewrite(input, path, lattice);
        }
        lattice.clear();

        if (mode != Tokenizer.SplitMode.C) {
            path = splitPath(path, mode);
        }

        if (dumpOutput != null) {
            dumpOutput.println("=== After rewriting:");
            dumpPath(path);
            dumpOutput.println("===");
        }

        return new MorphemeList(input, grammar, lexicon, path, allowEmptyMorpheme);
    }

    LatticeImpl buildLattice(UTF8InputText input) {
        byte[] bytes = input.getByteText();
        lattice.resize(bytes.length);
        for (int i = 0; i < bytes.length; i++) {
            if (!input.canBow(i) || !lattice.hasPreviousNode(i)) {
                continue;
            }
            Iterator<int[]> iterator = lexicon.lookup(bytes, i);
            boolean hasWords = false;
            while (iterator.hasNext()) {
                int[] r = iterator.next();
                int wordId = r[0];
                int end = r[1];

                if (end < bytes.length && !input.canBow(end)) {
                    continue;
                }
                LatticeNode n = new LatticeNodeImpl(lexicon, lexicon.getLeftId(wordId), lexicon.getRightId(wordId),
                        lexicon.getCost(wordId), wordId);
                lattice.insert(i, end, n);
                hasWords = true;
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
        lattice.connectEosNode();

        return lattice;
    }

    List<LatticeNode> splitPath(List<LatticeNode> path, SplitMode mode) {
        List<LatticeNode> newPath = new ArrayList<>();
        for (LatticeNode node : path) {
            int[] wids;
            if (mode == Tokenizer.SplitMode.A) {
                wids = node.getWordInfo().getAunitSplit();
            } else { // Tokenizer.SplitMode.B
                wids = node.getWordInfo().getBunitSplit();
            }
            if (wids.length == 0 || wids.length == 1) {
                newPath.add(node);
            } else {
                int offset = node.getBegin();
                for (int wid : wids) {
                    LatticeNodeImpl n = new LatticeNodeImpl(lexicon, (short) 0, (short) 0, (short) 0, wid);
                    n.begin = offset;
                    offset += n.getWordInfo().getLength();
                    n.end = offset;
                    newPath.add(n);
                }
            }
        }
        return newPath;
    }

    void dumpPath(List<LatticeNode> path) {
        int i = 0;
        for (LatticeNode node : path) {
            dumpOutput.println(String.format("%d: %s", i, node.toString()));
            i++;
        }
    }

    void disableEmptyMorpheme() {
        allowEmptyMorpheme = false;
    }

    class NonBreakChecker implements SentenceDetector.NonBreakCheker {
        private final UTF8InputText input;
        private int bos;

        NonBreakChecker(UTF8InputText input) {
            this.input = input;
        }

        public void setBos(int bos) {
            this.bos = bos;
        }

        @Override
        public boolean hasNonBreakWord(int length) {
            int byteEOS = input.getCodePointsOffsetLength(0, bos + length);
            byte[] bytes = input.getByteText();
            for (int i = Math.max(0, byteEOS - 64); i < byteEOS; i++) {
                Iterator<int[]> iterator = lexicon.lookup(bytes, i);
                while (iterator.hasNext()) {
                    int[] r = iterator.next();
                    int l = r[1];
                    if (l > byteEOS || (l == byteEOS && bos + length - input.getOffsetTextLength(i) > 1)) {
                        return true;
                    }
                }
            }
            return false;
        }
    }
}
