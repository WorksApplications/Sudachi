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

package com.worksap.nlp.sudachi.dictionary;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.worksap.nlp.sudachi.Config;
import com.worksap.nlp.sudachi.MMap;

public class BinaryDictionary implements Closeable, DictionaryAccess {

    private final ByteBuffer bytes;
    private final Description header;
    private final GrammarImpl grammar;
    private final DoubleArrayLexicon lexicon;

    public BinaryDictionary(String fileName) throws IOException {
        this(Paths.get(fileName));
    }

    public BinaryDictionary(Path filename) throws IOException {
        this(MMap.map(filename));
    }

    public BinaryDictionary(ByteBuffer dictionary) throws IOException {
        bytes = dictionary;

        header = Description.load(dictionary);
        grammar = GrammarImpl.load(bytes, header);
        lexicon = DoubleArrayLexicon.load(bytes, header);
    }

    public static BinaryDictionary loadSystem(String fileName) throws IOException {
        return loadSystem(MMap.map(fileName));
    }

    public static BinaryDictionary loadUser(String fileName) throws IOException {
        return loadUser(MMap.map(fileName));
    }

    public static BinaryDictionary loadSystem(ByteBuffer buffer) throws IOException {
        BinaryDictionary dict = new BinaryDictionary(buffer);
        if (!dict.getDictionaryHeader().isSystemDictionary()) {
            dict.close();
            throw new IOException("invalid system dictionary");
        }
        return dict;
    }

    public static BinaryDictionary loadUser(ByteBuffer buffer) throws IOException {
        BinaryDictionary dict = new BinaryDictionary(buffer);
        if (!dict.getDictionaryHeader().isUserDictionary()) {
            dict.close();
            throw new IOException("invalid user dictionary");
        }
        return dict;
    }

    public static BinaryDictionary loadSystem(Config.Resource<BinaryDictionary> resource) throws IOException {
        return resource.consume(res -> loadSystem(res.asByteBuffer()));
    }

    public static BinaryDictionary loadUser(Config.Resource<BinaryDictionary> resource) throws IOException {
        return resource.consume(res -> loadUser(res.asByteBuffer()));
    }

    @Override
    public void close() throws IOException {
        MMap.unmap(bytes);
    }

    public Description getDictionaryHeader() {
        return header;
    }

    public GrammarImpl getGrammar() {
        return grammar;
    }

    public DoubleArrayLexicon getLexicon() {
        return lexicon;
    }
}