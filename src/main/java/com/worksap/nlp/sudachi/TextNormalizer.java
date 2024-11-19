/*
 * Copyright (c) 2024 Works Applications Co., Ltd.
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

import java.util.List;
import java.io.IOException;
import java.util.ArrayList;

import com.worksap.nlp.sudachi.dictionary.CharacterCategory;
import com.worksap.nlp.sudachi.dictionary.Grammar;
import com.worksap.nlp.sudachi.dictionary.GrammarImpl;

/**
 * A text normalizer.
 */
public class TextNormalizer {
    private final Grammar grammar;
    private final List<InputTextPlugin> inputTextPlugins;

    /**
     * Create a TextNormalizer from a grammar and input text plugins.
     * 
     * Grammar must have
     * {@link com.worksap.nlp.sudachi.dictionary.CharacterCategory}.
     */
    public TextNormalizer(Grammar grammar, List<InputTextPlugin> inputTextPlugins) {
        if (grammar.getCharacterCategory() == null) {
            throw new IllegalArgumentException("grammar for TextNormalizer must have CharacterCategory.");
        }
        this.grammar = grammar;
        this.inputTextPlugins = inputTextPlugins;
    }

    /**
     * Create a TextNormalizer from a grammar.
     * 
     * Grammar must have a
     * {@link com.worksap.nlp.sudachi.dictionary.CharacterCategory}.
     * {@link DefaultInputTextPlugin} will be used.
     */
    public TextNormalizer(Grammar grammar) throws IOException {
        this(grammar, setupDefaultInputTextPlugins(grammar));
    }

    /**
     * Create a default TextNormalizer that uses default
     * {@link com.worksap.nlp.sudachi.dictionary.CharacterCategory} and
     * {@link DefaultInputTextPlugin}.
     */
    public static TextNormalizer defaultTextNormalizer() throws IOException {
        Grammar grammar = new GrammarImpl();
        grammar.setCharacterCategory(CharacterCategory.loadDefault());
        return new TextNormalizer(grammar);
    }

    /**
     * Create TextNormalizer based on the {@link JapaneseDictionary}.
     */
    public static TextNormalizer fromDictionary(JapaneseDictionary dictionary) {
        return new TextNormalizer(dictionary.getGrammar(), dictionary.inputTextPlugins);
    }

    /**
     * Setup {@link DefaultInputTextPlugin} using a grammar.
     */
    private static List<InputTextPlugin> setupDefaultInputTextPlugins(Grammar grammar) throws IOException {
        PathAnchor anchor = PathAnchor.classpath();
        List<Config.PluginConf<InputTextPlugin>> pconfs = Config.fromJsonString(
                "{\"inputTextPlugin\":[{\"class\":\"com.worksap.nlp.sudachi.DefaultInputTextPlugin\"}]}", anchor)
                .getInputTextPlugins();

        List<InputTextPlugin> plugins = new ArrayList<>();
        for (Config.PluginConf<InputTextPlugin> pconf : pconfs) {
            InputTextPlugin p = pconf.instantiate(anchor);
            p.setUp(grammar);
            plugins.add(p);
        }

        return plugins;
    }

    /** Normalize given text */
    public String normalize(CharSequence text) {
        UTF8InputTextBuilder builder = new UTF8InputTextBuilder(text, grammar);
        for (InputTextPlugin plugin : inputTextPlugins) {
            plugin.rewrite(builder);
        }
        UTF8InputText input = builder.build();
        return input.getText();
    }
}
