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

import java.io.IOException;
import java.util.List;

import com.worksap.nlp.sudachi.dictionary.Grammar;

/**
 * A plugin that provides the nodes of out-of-vocabulary morphemes.
 *
 * <p>{@link Dictionary} initialize this plugin with {@link Settings}.
 * It can be referred as {@link Plugin#settings}.
 *
 * <p>The following is an example of settings.
 * <pre>{@code
 *   {
 *     "class" : "com.worksap.nlp.sudachi.OovProviderPlugin",
 *     "example" : "example setting"
 *   }
 * }</pre>
 */
public abstract class OovProviderPlugin extends Plugin {

    /**
     * Set up the plugin.
     *
     * {@link Tokenizer} calls this method for setting up this plugin.
     *
     * @param grammar the grammar of the system dictionary
     * @throws IOException if reading something is failed
     */
    public void setUp(Grammar grammar) throws IOException {}

    /**
     * Provides the nodes of OOV morphemes.
     *
     * To create the new node you can use {@link #createNode}.
     *
     * @param inputText the input text
     * @param offset the index of insertion
     * @param hasOtherWords if {@code true}, the lattice has other words
     *        beginning at {@code offset}.
     * @return the nodes of OOV morphemes
     */
    public abstract List<LatticeNode> provideOOV(InputText inputText,
                                                 int offset,
                                                 boolean hasOtherWords);

    List<LatticeNode> getOOV(UTF8InputText inputText, int offset,
                             boolean hasOtherWords) {
        List<LatticeNode> nodes = provideOOV(inputText, offset, hasOtherWords);
        for (LatticeNode node : nodes) {
            LatticeNodeImpl n = (LatticeNodeImpl)node;
            n.begin = offset;
            n.end = offset + node.getWordInfo().getLength();
        }
        return nodes;
    }

    /**
     * Returns a new node of OOV.
     *
     * @return a node of OOV
     */
    public LatticeNode createNode() {
        LatticeNode node = new LatticeNodeImpl();
        node.setOOV();
        return node;
    }
}
