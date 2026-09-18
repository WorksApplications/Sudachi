/*
 * Copyright (c) 2021-2026 Works Applications Co., Ltd.
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
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.json.JsonWriter;

import com.worksap.nlp.sudachi.dictionary.*;

class JapaneseTokenizer implements Tokenizer {

    Grammar grammar;
    LexiconSet lexicon;
    List<InputTextPlugin> inputTextPlugins;
    List<OovProviderPlugin> oovProviderPlugins;
    List<PathRewritePlugin> pathRewritePlugins;
    OovProviderPlugin defaultOovProvider;
    PrintStream dumpOutput;
    JsonObjectBuilder jsonBuilder;
    boolean allowEmptyMorpheme;

    LatticeImpl lattice;

    JapaneseTokenizer(Grammar grammar, Lexicon lexicon, List<InputTextPlugin> inputTextPlugins,
            List<OovProviderPlugin> oovProviderPlugins, List<PathRewritePlugin> pathRewritePlugins) {

        this.grammar = grammar;
        this.lexicon = (LexiconSet) lexicon;
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
            // return MorphemeList instance for the case internalCost is required.
            return MorphemeList.EMPTY;
        }
        UTF8InputText input = buildInputText(text);
        return tokenizeSentence(mode, input);
    }

    @Override
    public Iterable<List<Morpheme>> tokenizeSentences(SplitMode mode, String text) {
        if (text.isEmpty()) {
            return Collections.emptyList();
        }

        StringReader input = new StringReader(text);
        SentenceSplittingLazyAnalysis analysis = new SentenceSplittingLazyAnalysis(mode, this, input);
        List<List<Morpheme>> result = new ArrayList<>();
        analysis.forEachRemaining(result::add);
        return result;
    }

    @Override
    public Iterator<List<Morpheme>> tokenizeSentences(SplitMode mode, Readable input) {
        return new SentenceSplittingLazyAnalysis(mode, this, input);
    }

    @Override
    public Iterator<List<Morpheme>> lazyTokenizeSentences(SplitMode mode, Readable input) {
        return tokenizeSentences(mode, input);
    }

    @Override
    public List<Morpheme> split(List<Morpheme> morphemes, SplitMode mode) {
        if (morphemes instanceof MorphemeList) {
            return ((MorphemeList) morphemes).split(mode);
        }

        List<Morpheme> result = new ArrayList<>();
        for (Morpheme m : morphemes) {
            if (m instanceof SingleMorphemeImpl) {
                ((SingleMorphemeImpl) m).appendSplitsTo(result, mode);
            } else {
                for (Morpheme subsplit : m.split(mode)) {
                    result.add(subsplit);
                }
            }
        }
        return result;
    }

    @Override
    public void setDumpOutput(PrintStream output) {
        dumpOutput = output;
    }

    @Override
    public String dumpInternalStructures(String text) {
        jsonBuilder = Json.createObjectBuilder();
        tokenize(SplitMode.C, text);

        StringWriter stringWriter = new StringWriter();
        try (JsonWriter writer = Json.createWriter(stringWriter)) {
            writer.writeObject(jsonBuilder.build());
        }
        return stringWriter.toString();
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
        if (jsonBuilder != null) {
            jsonBuilder.add("inputText", Json.createObjectBuilder().add("originalText", input.getOriginalText())
                    .add("modifiedText", input.getText()));
        }

        return input;
    }

    List<Morpheme> tokenizeSentence(Tokenizer.SplitMode mode, UTF8InputText input) {
        checkIfAlive();
        buildLattice(input);

        if (dumpOutput != null) {
            dumpOutput.println("=== Lattice dump:");
            lattice.dump(dumpOutput);
        }
        if (jsonBuilder != null) {
            jsonBuilder.add("lattice", lattice.toJson());
        }

        List<LatticeNodeImpl> path = lattice.getBestPath();

        if (dumpOutput != null) {
            dumpOutput.println("=== Before rewriting:");
            dumpPath(path);
        }
        if (jsonBuilder != null) {
            jsonBuilder.add("bestPath", pathToJson(path, lattice));
        }

        for (PathRewritePlugin plugin : pathRewritePlugins) {
            plugin.rewrite(input, path, lattice);
        }
        lattice.clear();

        path = splitPath(path, mode);

        if (dumpOutput != null) {
            dumpOutput.println("=== After rewriting:");
            dumpPath(path);
            dumpOutput.println("===");
        }
        if (jsonBuilder != null) {
            jsonBuilder.add("rewrittenPath", pathToJson(path, lattice));
        }

        return new MorphemeList(input, grammar, lexicon, path, allowEmptyMorpheme, mode);
    }

    LatticeImpl buildLattice(UTF8InputText input) {
        byte[] bytes = input.getByteText();
        lattice.resize(bytes.length);
        ArrayList<LatticeNodeImpl> crrNodes = new ArrayList<>(64);
        WordLookup wordLookup = lexicon.makeLookup();
        for (int byteBoundary = 0; byteBoundary < bytes.length; byteBoundary++) {
            if (!input.canBow(byteBoundary) || !lattice.hasPreviousNode(byteBoundary)) {
                continue;
            }
            crrNodes.clear();
            wordLookup.reset(bytes, byteBoundary, bytes.length);
            long wordMask = 0L;
            while (wordLookup.next()) {
                int end = wordLookup.getEndOffset();
                if (end < bytes.length && !input.canBow(end)) {
                    continue;
                }
                int numWords = wordLookup.getNumWords();
                int[] wordIds = wordLookup.getWordsIds();
                for (int word = 0; word < numWords; ++word) {
                    int wordId = wordIds[word];
                    LatticeNodeImpl n = new LatticeNodeImpl(lexicon, lexicon.parameters(wordId), wordId);
                    lattice.insert(byteBoundary, end, n);
                    crrNodes.add(n);
                    wordMask = WordMask.addNth(wordMask, end - byteBoundary);
                }
            }
            long wordMaskWithOov = wordMask;

            // OOV
            if (input.canOovBow(byteBoundary)) {
                for (OovProviderPlugin plugin : oovProviderPlugins) {
                    wordMaskWithOov = provideOovs(plugin, input, byteBoundary, wordMaskWithOov, crrNodes);
                }
            }
            if (wordMaskWithOov == 0 && defaultOovProvider != null) {
                wordMaskWithOov = provideOovs(defaultOovProvider, input, byteBoundary, wordMaskWithOov, crrNodes);
            }
            if (wordMaskWithOov == 0) {
                throw new IllegalStateException("failed to found any morpheme candidate at boundary " + byteBoundary);
            }
        }
        lattice.connectEosNode();

        return lattice;
    }

    /**
     * Create OOV nodes using plugin at the given position and update crrNodes and
     * wordMask.
     * 
     * @param plugin
     *            OOVProviderPlugin to use
     * @param input
     *            Full inputText
     * @param boundary
     *            Byte index of inputText where OOV nodes should start from
     * @param crrNodes
     *            Nodes already provided by dict or other plugins. Provided nodes
     *            should be appended to this
     * @param wordMask
     *            Word mask based on crrNodes
     * @return wordMask updated based on created OOV nodes.
     */
    private long provideOovs(OovProviderPlugin plugin, UTF8InputText input, int boundary, long wordMask,
            ArrayList<LatticeNodeImpl> crrNodes) {
        int initialSize = crrNodes.size();
        int created = plugin.provideOOV(input, boundary, wordMask, crrNodes);
        if (created == 0) {
            return wordMask;
        }
        for (int i = initialSize; i < initialSize + created; ++i) {
            LatticeNodeImpl node = crrNodes.get(i);
            lattice.insert(node.getBegin(), node.getEnd(), node);
            wordMask = WordMask.addNth(wordMask, node.getEnd() - node.getBegin());
        }
        return wordMask;
    }

    private List<LatticeNodeImpl> splitPath(List<LatticeNodeImpl> path, SplitMode mode) {
        List<LatticeNodeImpl> newPath = new ArrayList<>();
        for (LatticeNodeImpl node : path) {
            node.appendSplitsTo(newPath, mode);
        }
        return newPath;
    }

    void dumpPath(List<? extends LatticeNode> path) {
        int i = 0;
        for (LatticeNode node : path) {
            dumpOutput.printf("%d: %s\n", i, node.toString());
            i++;
        }
    }

    JsonArrayBuilder pathToJson(List<? extends LatticeNode> path, LatticeImpl lattice) {
        JsonArrayBuilder builder = Json.createArrayBuilder();
        for (LatticeNode node : path) {
            builder.add(lattice.nodeToJson((LatticeNodeImpl) node));
        }
        return builder;
    }

    void disableEmptyMorpheme() {
        allowEmptyMorpheme = false;
    }

    void checkIfAlive() {
        if (lexicon.isValid() && grammar.isValid()) {
            return;
        }
        throw new IllegalStateException("dictionary was closed prior to tokenization");
    }
}
