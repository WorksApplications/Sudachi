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
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import com.worksap.nlp.sudachi.dictionary.Grammar;
import com.worksap.nlp.sudachi.dictionary.GrammarImpl;
import com.worksap.nlp.sudachi.dictionary.POS;

/**
 * A plugin that provides the nodes of out-of-vocabulary morphemes.
 *
 * <p>
 * {@link Dictionary} initialize this plugin with {@link Settings}. It can be
 * referred as {@link Plugin#settings}.
 *
 * <p>
 * The following is an example of settings.
 * 
 * <pre>
 * {@code
 *   {
 *     "class" : "com.worksap.nlp.sudachi.OovProviderPlugin",
 *     "example" : "example setting"
 *   }
 * }
 * </pre>
 */
public abstract class OovProviderPlugin extends Plugin {

    /**
     * Set up the plugin.
     *
     * {@link Tokenizer} calls this method for setting up this plugin.
     *
     * @param grammar
     *            the grammar of the system dictionary
     * @throws IOException
     *             if reading something is failed
     */
    public void setUp(Grammar grammar) throws IOException {
    }

    /**
     * Provides the nodes of OOV morphemes.
     *
     * To create the new node you can use {@link #createNode}.
     *
     * @param inputText
     *            the input text
     * @param offset
     *            the index of insertion
     * @param otherWords
     *            bitmask that contains information nodes on which boundaries were
     *            already created. For example, a mask of 0b1001 means that there
     *            were already created two nodes: of length 1 and 4. If the highest
     *            bit is set, it means that a node of length of 64 <b>or greater</b>
     *            was created.
     * @param result
     *            OOV provider plugins need to add nodes here
     * @return the number of created nodes. Values outside that range will be
     *         ignored.
     */
    public abstract int provideOOV(InputText inputText, int offset, long otherWords, List<LatticeNodeImpl> result);

    int getOOV(UTF8InputText inputText, int offset, long otherWords, List<LatticeNodeImpl> result) {
        int oldSize = result.size();
        int numCreated = provideOOV(inputText, offset, otherWords, result);
        for (int i = 0; i < numCreated; i++) {
            LatticeNodeImpl n = result.get(oldSize + i);
            n.begin = offset;
            n.end = offset + n.getWordInfo().getLength();
        }
        return numCreated;
    }

    /**
     * Returns a new node which represents an OOV word.
     *
     * @return a new OOV node
     */
    protected LatticeNodeImpl createNode() {
        LatticeNodeImpl node = new LatticeNodeImpl();
        node.setOOV();
        return node;
    }

    /**
     * Recommended name for user POS flag
     */
    public static final String USER_POS = "userPos";

    public static final String USER_POS_FORBID = "forbid";
    public static final String USER_POS_ALLOW = "allow";

    protected short posIdOf(Grammar grammar, POS pos, String userPosMode) {
        short posIdPresent = grammar.getPartOfSpeechId(pos);
        userPosMode = userPosMode.toLowerCase(Locale.ROOT);

        if (Objects.equals(userPosMode, USER_POS_FORBID)) {
            if (posIdPresent >= 0) {
                return posIdPresent;
            }
            throw new IllegalArgumentException(String.format(
                    "POS %s WAS NOT present in dictionary and OOV Plugin %s is forbidden to add new POS tags", pos,
                    this));
        } else if (!Objects.equals(userPosMode, USER_POS_ALLOW)) {
            throw new IllegalArgumentException(
                    "Unknown user POS mode: " + userPosMode + " allowed values are: forbid, allow");
        }
        GrammarImpl grammarImpl = (GrammarImpl) grammar;
        return grammarImpl.registerPOS(pos);
    }
}
