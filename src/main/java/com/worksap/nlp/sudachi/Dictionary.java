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

/**
 * A lexicon and a grammar for morphological analysis.
 *
 * This class requires a lot of memory.
 * When using multiple analyzers, it is recommended to generate only one
 * instance of this class, and generate multiple tokenizers.
 *
 * @see DictionaryFactory
 * @see Tokenizer
 * @see AutoCloseable
 */
public interface Dictionary extends AutoCloseable {

    /**
     * Creates a tokenizer instance.
     *
     * @return a tokenizer
     */
    public Tokenizer create();

    @Override
    public void close();
}
