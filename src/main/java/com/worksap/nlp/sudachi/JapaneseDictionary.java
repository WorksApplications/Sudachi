/*
 * Copyright (c) 2017-2024 Works Applications Co., Ltd.
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

import com.worksap.nlp.sudachi.dictionary.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class JapaneseDictionary implements Dictionary, DictionaryAccess {

    GrammarImpl grammar;
    LexiconSet lexicon;
    List<InputTextPlugin> inputTextPlugins;
    List<OovProviderPlugin> oovProviderPlugins;
    List<PathRewritePlugin> pathRewritePlugins;
    List<BinaryDictionary> dictionaries;
    boolean allowEmptyMorpheme;

    JapaneseDictionary(Config config) throws IOException {
        dictionaries = new ArrayList<>();
        setupSystemDictionary(config);
        for (Config.PluginConf<EditConnectionCostPlugin> p : config.getEditConnectionCostPlugins()) {
            EditConnectionCostPlugin instance = p.instantiate(config.getAnchor());
            instance.setUp(grammar);
            instance.edit(grammar);
        }
        setupCharacterDefinition(config);
        inputTextPlugins = new ArrayList<>();
        for (Config.PluginConf<InputTextPlugin> p : config.getInputTextPlugins()) {
            InputTextPlugin instance = p.instantiate(config.getAnchor());
            instance.setUp(grammar);
            inputTextPlugins.add(instance);
        }
        oovProviderPlugins = new ArrayList<>();
        for (Config.PluginConf<OovProviderPlugin> p : config.getOovProviderPlugins()) {
            OovProviderPlugin instance = p.instantiate(config.getAnchor());
            instance.setUp(grammar);
            oovProviderPlugins.add(instance);
        }
        if (oovProviderPlugins.isEmpty()) {
            throw new IllegalArgumentException("there must be at least one OOV provider plugin");
        }
        pathRewritePlugins = new ArrayList<>();
        for (Config.PluginConf<PathRewritePlugin> p : config.getPathRewritePlugins()) {
            PathRewritePlugin instance = p.instantiate(config.getAnchor());
            instance.setUp(grammar);
            pathRewritePlugins.add(instance);
        }
        setupUserDictionaries(config);

        allowEmptyMorpheme = config.isAllowEmptyMorpheme();
    }

    void setupSystemDictionary(Config config) throws IOException {
        BinaryDictionary dictionary = BinaryDictionary.loadSystem(config.getSystemDictionary());
        dictionaries.add(dictionary);
        grammar = dictionary.getGrammar();
        lexicon = new LexiconSet(dictionary.getLexicon(), grammar.getSystemPartOfSpeechSize());
    }

    void setupUserDictionaries(Config config) throws IOException {
        for (Config.Resource<BinaryDictionary> userDic : config.getUserDictionaries()) {
            BinaryDictionary instance = BinaryDictionary.loadUser(userDic);
            addUserDictionary(instance);
        }
    }

    void addUserDictionary(BinaryDictionary dictionary) {
        if (lexicon.isFull()) {
            throw new IllegalArgumentException("too many dictionaries");
        }

        dictionaries.add(dictionary);

        DoubleArrayLexicon userLexicon = dictionary.getLexicon();
        Tokenizer tokenizer = new JapaneseTokenizer(grammar, lexicon, inputTextPlugins, oovProviderPlugins,
                Collections.emptyList());
        userLexicon.calculateDynamicCosts(tokenizer);

        lexicon.add(userLexicon, (short) grammar.getPartOfSpeechSize());
        grammar.addPosList(dictionary.getGrammar());
    }

    void setupCharacterDefinition(Config config) throws IOException {
        if (grammar == null) {
            return;
        }
        Config.Resource<CharacterCategory> resource = config.getCharacterDefinition();
        if (resource == null) {
            resource = PathAnchor.classpath().resource("char.def");
        }
        CharacterCategory category = CharacterCategory.load(resource);
        grammar.setCharacterCategory(category);
    }

    @Override
    public void close() throws IOException {
        grammar.invalidate();
        grammar = null;
        lexicon.invalidate();
        lexicon = null;
        for (BinaryDictionary dictionary : dictionaries) {
            dictionary.close();
        }
    }

    /**
     * Iterator of morphemes in the dictionary.
     */
    private class EntryItr implements Iterator<Morpheme> {
        private final GrammarImpl grammar;
        private final LexiconSet lexicon;
        private Iterator<Integer> wordIdItr;

        EntryItr() {
            this.grammar = getGrammar();
            this.lexicon = getLexicon();
            this.wordIdItr = this.lexicon.wordIds();
        }

        @Override
        public boolean hasNext() {
            return wordIdItr.hasNext();
        }

        @Override
        public Morpheme next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            return new SingleMorphemeImpl(this.grammar, this.lexicon, wordIdItr.next());
        }
    }

    @Override
    public Stream<Morpheme> entries() {
        Iterator<Morpheme> iterator = new EntryItr();
        int size = getLexicon().size();
        int characteristics = Spliterator.DISTINCT | Spliterator.IMMUTABLE | Spliterator.NONNULL | Spliterator.SIZED;
        boolean parallel = true;

        Spliterator<Morpheme> spliterator = Spliterators.spliterator(iterator, size, characteristics);
        return StreamSupport.stream(spliterator, parallel);
    }

    @Override
    public List<Morpheme> lookup(CharSequence surface) {
        UTF8InputTextBuilder builder = new UTF8InputTextBuilder(surface, grammar);
        for (InputTextPlugin plugin : inputTextPlugins) {
            plugin.rewrite(builder);
        }
        UTF8InputText input = builder.build();
        byte[] bytes = input.getByteText();

        List<Morpheme> morphemes = new ArrayList<>();
        WordLookup wordLookup = lexicon.makeLookup();
        wordLookup.reset(bytes, 0, bytes.length);
        while (wordLookup.next()) {
            int end = wordLookup.getEndOffset();
            if (end != bytes.length) {
                continue;
            }
            int numWords = wordLookup.getNumWords();
            int[] wordIds = wordLookup.getWordsIds();
            for (int word = 0; word < numWords; ++word) {
                int wordId = wordIds[word];
                Morpheme morpheme = new SingleMorphemeImpl(getGrammar(), getLexicon(), wordId);
                morphemes.add(morpheme);
            }
        }
        return morphemes;
    }

    @Override
    public Morpheme oovMorpheme(short posId, String surface, String reading, String normalizedForm,
            String dictionaryForm) {
        return new SingleMorphemeImpl(getGrammar(), posId, surface, reading, normalizedForm, dictionaryForm);
    }

    @Override
    public Tokenizer tokenizer() {
        if (grammar == null || lexicon == null) {
            throw new IllegalStateException("trying to use closed dictionary");
        }
        JapaneseTokenizer tokenizer = new JapaneseTokenizer(grammar, lexicon, inputTextPlugins, oovProviderPlugins,
                pathRewritePlugins);
        if (!allowEmptyMorpheme) {
            tokenizer.disableEmptyMorpheme();
        }
        return tokenizer;
    }

    @Override
    public Tokenizer create() {
        return tokenizer();
    }

    @Override
    public int getPartOfSpeechSize() {
        return grammar.getPartOfSpeechSize();
    }

    @Override
    public List<String> getPartOfSpeechString(short posId) {
        return grammar.getPartOfSpeechString(posId);
    }

    static String readAll(InputStream input) throws IOException {
        try (InputStreamReader isReader = new InputStreamReader(input, StandardCharsets.UTF_8);
                BufferedReader reader = new BufferedReader(isReader)) {
            StringBuilder sb = new StringBuilder();
            while (true) {
                String line = reader.readLine();
                if (line == null) {
                    break;
                }
                sb.append(line);
            }
            return sb.toString();
        }
    }

    @Override
    public GrammarImpl getGrammar() {
        return grammar;
    }

    @Override
    public LexiconSet getLexicon() {
        return lexicon;
    }

    @Override
    public PosMatcher posMatcher(Predicate<POS> predicate) {
        GrammarImpl grammar = getGrammar();
        int numPos = grammar.getPartOfSpeechSize();
        int[] ids = IntStream.range(0, numPos).filter(id -> predicate.test(grammar.getPartOfSpeechString((short) id)))
                .toArray();
        return new PosMatcher(ids, this);
    }
}
