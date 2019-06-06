/*
 * Copyright (c) 2019 Works Applications Co., Ltd.
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

import com.worksap.nlp.sudachi.dictionary.Grammar;

/**
 * A plugin for editing the connection costs.
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
 *     "class" : "com.worksap.nlp.sudachi.SampleEditConnectionPlugin",
 *     "example" : "example setting"
 *   }
 * }
 * </pre>
 */
public abstract class EditConnectionCostPlugin extends Plugin {

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
     * Edit the connection costs.
     *
     * To edit connection costs, you can use {@link Grammar#getConnectCost},
     * {@link Grammar#setConnectCost}, and {@link inhibitConnection}.
     *
     * @param grammar
     *            the grammar of the system dictionary
     */
    public abstract void edit(Grammar grammar);

    /**
     * Inhibit a connection.
     *
     * @param grammar
     *            the grammar of the system dictionary
     * @param leftId
     *            the left-ID of the connection
     * @param rightId
     *            the right-ID of the connection
     */
    public void inhibitConnection(Grammar grammar, short leftId, short rightId) {
        grammar.setConnectCost(leftId, rightId, Grammar.INHIBITED_CONNECTION);
    }
}
