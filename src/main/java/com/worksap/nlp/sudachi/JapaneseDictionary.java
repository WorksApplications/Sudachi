/*
 * Copyright (c) 2017-2022 Works Applications Co., Ltd.
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
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
            EditConnectionCostPlugin instance = p.instantiate();
            instance.setUp(grammar);
            instance.edit(grammar);
        }
        setupCharacterDefinition(config);
        inputTextPlugins = new ArrayList<>();
        for (Config.PluginConf<InputTextPlugin> p : config.getInputTextPlugins()) {
            InputTextPlugin instance = p.instantiate();
            instance.setUp(grammar);
            inputTextPlugins.add(instance);
        }
        oovProviderPlugins = new ArrayList<>();
        for (Config.PluginConf<OovProviderPlugin> p : config.getOovProviderPlugins()) {
            OovProviderPlugin instance = p.instantiate();
            instance.setUp(grammar);
            oovProviderPlugins.add(instance);
        }
        if (oovProviderPlugins.isEmpty()) {
            throw new IllegalArgumentException("there must be at least one OOV provider plugin");
        }
        pathRewritePlugins = new ArrayList<>();
        for (Config.PluginConf<PathRewritePlugin> p : config.getPathRewritePlugins()) {
            PathRewritePlugin instance = p.instantiate();
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
        lexicon = new LexiconSet(dictionary.getLexicon());
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
        userLexicon.calculateCost(tokenizer);

        lexicon.add(userLexicon, (short) grammar.getPartOfSpeechSize());
        grammar.addPosList(dictionary.getGrammar());
    }

    void setupCharacterDefinition(Config config) throws IOException {
        if (grammar == null) {
            return;
        }
        Config.Resource<CharacterCategory> resource = config.getCharacterDefinition();
        if (resource == null) {
            resource = SettingsAnchor.classpath().resource("char.def");
        }
        CharacterCategory category = CharacterCategory.load(resource);
        grammar.setCharacterCategory(category);
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

    public GrammarImpl getGrammar() {
        return grammar;
    }

    public LexiconSet getLexicon() {
        return lexicon;
    }

}
