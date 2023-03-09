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

import java.io.IOException;
import java.io.PrintStream;
import java.io.Reader;
import java.io.StringWriter;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.json.JsonWriter;

import com.worksap.nlp.sudachi.dictionary.*;
import com.worksap.nlp.sudachi.sentdetect.SentenceDetector;

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
    public MorphemeList tokenize(Tokenizer.SplitMode mode, String text) {
        if (text.isEmpty()) {
            return MorphemeList.EMPTY;
        }
        UTF8InputText input = buildInputText(text);
        return tokenizeSentence(mode, input);
    }

    @Override
    public Iterable<MorphemeList> tokenizeSentences(SplitMode mode, String text) {
        if (text.isEmpty()) {
            return Collections.emptyList();
        }

        SentenceSplittingAnalysis analysis = new SentenceSplittingAnalysis(mode, this);
        int length = analysis.tokenizeBuffer(text);
        ArrayList<MorphemeList> result = analysis.result;
        int bos = analysis.bos;
        if (length < 0) {
            // treat remaining thing as a single sentence
            int eos = analysis.input.getText().length();
            if (bos != eos) {
                UTF8InputText slice = analysis.input;
                if (bos != 0) {
                    slice = slice.slice(bos, eos);
                }
                result.add(tokenizeSentence(mode, slice));
            }
        }
        return result;
    }

    @Override
    public Iterable<MorphemeList> tokenizeSentences(SplitMode mode, Reader reader) throws IOException {
        CharBuffer buffer = CharBuffer.allocate(SentenceDetector.DEFAULT_LIMIT);
        SentenceSplittingAnalysis analysis = new SentenceSplittingAnalysis(mode, this);

        while (IOTools.readAsMuchAsCan(reader, buffer) > 0) {
            buffer.flip();
            int length = analysis.tokenizeBuffer(buffer);
            if (length < 0) {
                buffer.position(analysis.bosPosition());
                buffer.compact();
            }
        }
        buffer.flip();
        ArrayList<MorphemeList> sentences = analysis.result;

        if (buffer.hasRemaining()) {
            sentences.add(tokenizeSentence(mode, buildInputText(buffer)));
        }

        return sentences;
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

    MorphemeList tokenizeSentence(Tokenizer.SplitMode mode, UTF8InputText input) {
        buildLattice(input);

        if (dumpOutput != null) {
            dumpOutput.println("=== Lattice dump:");
            lattice.dump(dumpOutput);
        }
        if (jsonBuilder != null) {
            jsonBuilder.add("lattice", lattice.toJson());
        }

        List<LatticeNode> path = lattice.getBestPath();

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

        if (mode != Tokenizer.SplitMode.C) {
            path = splitPath(path, mode);
        }

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
        ArrayList<LatticeNodeImpl> unkNodes = new ArrayList<>(64);
        WordLookup wordLookup = lexicon.makeLookup();
        for (int byteBoundary = 0; byteBoundary < bytes.length; byteBoundary++) {
            if (!input.canBow(byteBoundary) || !lattice.hasPreviousNode(byteBoundary)) {
                continue;
            }
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
                    LatticeNodeImpl n = new LatticeNodeImpl(lexicon, lexicon.getLeftId(wordId),
                            lexicon.getRightId(wordId), lexicon.getCost(wordId), wordId);
                    lattice.insert(byteBoundary, end, n);
                    unkNodes.add(n);
                    wordMask = WordMask.addNth(wordMask, end - byteBoundary);
                }
            }
            long wordMaskWithOov = wordMask;

            // OOV
            if (!input.getCharCategoryTypes(byteBoundary).contains(CategoryType.NOOOVBOW)) {
                for (OovProviderPlugin plugin : oovProviderPlugins) {
                    wordMaskWithOov = provideOovs(plugin, input, unkNodes, byteBoundary, wordMaskWithOov);
                }
            }
            if (wordMaskWithOov == 0 && defaultOovProvider != null) {
                wordMaskWithOov = provideOovs(defaultOovProvider, input, unkNodes, byteBoundary, wordMaskWithOov);
            }
            if (wordMaskWithOov == 0) {
                throw new IllegalStateException("failed to found any morpheme candidate at boundary " + byteBoundary);
            }
        }
        lattice.connectEosNode();

        return lattice;
    }

    private long provideOovs(OovProviderPlugin plugin, UTF8InputText input, ArrayList<LatticeNodeImpl> unkNodes,
            int boundary, long wordMask) {
        int initialSize = unkNodes.size();
        int created = plugin.getOOV(input, boundary, wordMask, unkNodes);
        if (created == 0) {
            return wordMask;
        }
        for (int i = initialSize; i < initialSize + created; ++i) {
            LatticeNodeImpl node = unkNodes.get(i);
            lattice.insert(node.getBegin(), node.getEnd(), node);
            wordMask = WordMask.addNth(wordMask, node.getEnd() - node.getBegin());
        }
        return wordMask;
    }

    private List<LatticeNode> splitPath(List<LatticeNode> path, SplitMode mode) {
        List<LatticeNode> newPath = new ArrayList<>();
        for (LatticeNode node : path) {
            LatticeNodeImpl nodeImpl = (LatticeNodeImpl) node;
            nodeImpl.appendSplitsTo(newPath, mode);
        }
        return newPath;
    }

    void dumpPath(List<LatticeNode> path) {
        int i = 0;
        for (LatticeNode node : path) {
            dumpOutput.printf("%d: %s\n", i, node.toString());
            i++;
        }
    }

    JsonArrayBuilder pathToJson(List<LatticeNode> path, LatticeImpl lattice) {
        JsonArrayBuilder builder = Json.createArrayBuilder();
        for (LatticeNode node : path) {
            builder.add(lattice.nodeToJson((LatticeNodeImpl) node));
        }
        return builder;
    }

    void disableEmptyMorpheme() {
        allowEmptyMorpheme = false;
    }
}
