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

/**
 * Build a {@link Dictionary} instance from a dictionary file.
 */
public class DictionaryFactory {

    /**
     * Creates <tt>Dictionary</tt> by read a dictionary file.
     *
     * @param settings settings in JSON string
     * @return {@link Dictionary}
     * @throws IOException if reading a file is failed
     */
    public Dictionary create(String settings) throws IOException {
        return new JapaneseDictionary(settings);
    }

    /**
     * Creates <tt>Dictionary</tt> by read a dictionary file.
     *
     * @param path the base path if "path" is undefined in settings
     * @param settings settings in JSON string
     * @return {@link Dictionary}
     * @throws IOException if reading a file is failed
     */
    public Dictionary create(String path, String settings) throws IOException {
        return new JapaneseDictionary(path, settings);
    }
}
