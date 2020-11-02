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

package com.worksap.nlp.sudachi.dictionary;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;

import com.worksap.nlp.sudachi.MMap;

public class BinaryDictionary implements Closeable {

    private ByteBuffer bytes;
    private DictionaryHeader header;
    private GrammarImpl grammar;
    private DoubleArrayLexicon lexicon;

    BinaryDictionary(String fileName) throws IOException {
        bytes = MMap.map(fileName);
        int offset = 0;

        header = new DictionaryHeader(bytes, offset);
        offset += header.storageSize();

        long version = header.getVersion();
        if (header.isSystemDictionary() || version == DictionaryVersion.USER_DICT_VERSION_2) {
            grammar = new GrammarImpl(bytes, offset);
            offset += grammar.storageSize();
        } else if (version == DictionaryVersion.USER_DICT_VERSION_1) {
            grammar = new GrammarImpl();
        } else {
            MMap.unmap(bytes);
            throw new IOException("invalid dictionary");
        }

        lexicon = new DoubleArrayLexicon(bytes, offset, version == DictionaryVersion.SYSTEM_DICT_VERSION_2);
    }

    public static BinaryDictionary readSystemDictionary(String fileName) throws IOException {
        BinaryDictionary dict = new BinaryDictionary(fileName);
        if (!dict.getDictionaryHeader().isSystemDictionary()) {
            dict.close();
            throw new IOException("invalid system dictionary");
        }
        return dict;
    }

    public static BinaryDictionary readUserDictionary(String fileName) throws IOException {
        BinaryDictionary dict = new BinaryDictionary(fileName);
        if (!dict.getDictionaryHeader().isUserDictionary()) {
            dict.close();
            throw new IOException("invalid user dictionary");
        }
        return dict;
    }

    @Override
    public void close() throws IOException {
        MMap.unmap(bytes);
    }

    public DictionaryHeader getDictionaryHeader() {
        return header;
    }

    public GrammarImpl getGrammar() {
        return grammar;
    }

    public DoubleArrayLexicon getLexicon() {
        return lexicon;
    }
}