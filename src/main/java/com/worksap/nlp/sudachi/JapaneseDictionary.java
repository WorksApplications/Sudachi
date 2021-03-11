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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.worksap.nlp.sudachi.dictionary.BinaryDictionary;
import com.worksap.nlp.sudachi.dictionary.CharacterCategory;
import com.worksap.nlp.sudachi.dictionary.DoubleArrayLexicon;
import com.worksap.nlp.sudachi.dictionary.GrammarImpl;
import com.worksap.nlp.sudachi.dictionary.LexiconSet;

class JapaneseDictionary implements Dictionary {

    GrammarImpl grammar;
    LexiconSet lexicon;
    List<InputTextPlugin> inputTextPlugins;
    List<OovProviderPlugin> oovProviderPlugins;
    List<PathRewritePlugin> pathRewritePlugins;
    List<BinaryDictionary> dictionaries;
    boolean allowEmptyMorpheme;

    JapaneseDictionary() throws IOException {
        this(null, null, false);
    }

    JapaneseDictionary(String jsonString) throws IOException {
        this(null, jsonString, false);
    }

    JapaneseDictionary(String path, String jsonString, boolean mergeSettings) throws IOException {
        Settings settings = buildSettings(path, jsonString, mergeSettings);

        dictionaries = new ArrayList<>();

        readSystemDictionary(settings.getPath("systemDict"));
        for (EditConnectionCostPlugin p : settings
                .<EditConnectionCostPlugin>getPluginList("editConnectionCostPlugin")) {
            p.setUp(grammar);
            p.edit(grammar);
        }

        readCharacterDefinition(settings.getPath("characterDefinitionFile"));

        inputTextPlugins = settings.getPluginList("inputTextPlugin");
        for (InputTextPlugin p : inputTextPlugins) {
            p.setUp(grammar);
        }
        oovProviderPlugins = settings.getPluginList("oovProviderPlugin");
        if (oovProviderPlugins.isEmpty()) {
            throw new IllegalArgumentException("no OOV provider");
        }
        for (OovProviderPlugin p : oovProviderPlugins) {
            p.setUp(grammar);
        }
        pathRewritePlugins = settings.getPluginList("pathRewritePlugin");
        for (PathRewritePlugin p : pathRewritePlugins) {
            p.setUp(grammar);
        }

        for (String filename : settings.getPathList("userDict")) {
            readUserDictionary(filename);
        }

        allowEmptyMorpheme = settings.getBoolean("allowEmptyMorpheme", true);
    }

    static Settings buildSettings(String path, String jsonString, boolean mergeSettings) throws IOException {
        Settings defaultSettings;
        try (InputStream input = SudachiCommandLine.class.getResourceAsStream("/sudachi.json")) {
            defaultSettings = Settings.parseSettings(path, readAll(input));
        }
        if (jsonString == null) {
            return defaultSettings;
        } else if (mergeSettings) {
            defaultSettings.merge(Settings.parseSettings(path, jsonString));
            return defaultSettings;
        } else {
            return Settings.parseSettings(path, jsonString);
        }
    }

    void readSystemDictionary(String filename) throws IOException {
        if (filename == null) {
            throw new IllegalArgumentException("system dictionary is not specified");
        }

        BinaryDictionary dictionary = BinaryDictionary.readSystemDictionary(filename);
        dictionaries.add(dictionary);
        grammar = dictionary.getGrammar();
        lexicon = new LexiconSet(dictionary.getLexicon());
    }

    void readUserDictionary(String filename) throws IOException {
        if (lexicon.isFull()) {
            throw new IllegalArgumentException("too many dictionaries");
        }

        BinaryDictionary dictionary = BinaryDictionary.readUserDictionary(filename);
        dictionaries.add(dictionary);

        DoubleArrayLexicon userLexicon = dictionary.getLexicon();
        Tokenizer tokenizer = new JapaneseTokenizer(grammar, lexicon, inputTextPlugins, oovProviderPlugins,
                Collections.emptyList());
        userLexicon.calculateCost(tokenizer);

        lexicon.add(userLexicon, (short) grammar.getPartOfSpeechSize());
        grammar.addPosList(dictionary.getGrammar());
    }

    void readCharacterDefinition(String filename) throws IOException {
        if (grammar == null) {
            return;
        }
        CharacterCategory charCategory = new CharacterCategory();
        charCategory.readCharacterDefinition(filename);
        grammar.setCharacterCategory(charCategory);
    }

    @Override
    public void close() throws IOException {
        grammar = null;
        lexicon = null;
        for (BinaryDictionary dictionary : dictionaries) {
            dictionary.close();
        }
    }

    @Override
    public Tokenizer create() {
        JapaneseTokenizer tokenizer = new JapaneseTokenizer(grammar, lexicon, inputTextPlugins, oovProviderPlugins,
                pathRewritePlugins);
        if (!allowEmptyMorpheme) {
            tokenizer.disableEmptyMorpheme();
        }
        return tokenizer;
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

}
