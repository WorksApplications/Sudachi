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
import com.worksap.nlp.sudachi.dictionary.Grammar;

/**
 * A plugin that rewrites the characters of input texts.
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
 *     "class" : "com.worksap.nlp.sudachi.InputTextPlugin",
 *     "example" : "example setting"
 *   }
 * }
 * </pre>
 */
public abstract class InputTextPlugin extends Plugin {

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
     * Rewrite the input text.
     *
     * To rewrite the input text, you can use {@link InputTextBuilder#replace}.
     *
     * @param builder
     *            the input text
     */
    public abstract void rewrite(InputTextBuilder builder);
}
