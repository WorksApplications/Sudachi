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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.worksap.nlp.sudachi.Config;
import com.worksap.nlp.sudachi.MMap;

public class BinaryDictionary implements Closeable, DictionaryAccess {
    private final ByteBuffer bytes;
    private final Description header;
    private final GrammarImpl grammar;
    private final DoubleArrayLexicon lexicon;
    private Map<Integer, String> referenceIdMap;

    public BinaryDictionary(String fileName) throws IOException {
        this(Paths.get(fileName));
    }

    public BinaryDictionary(Path filename) throws IOException {
        this(MMap.map(filename));
    }

    public BinaryDictionary(ByteBuffer dictionary) {
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

    /**
     * Build-time helper data for dictionary compilation. Runtime tokenization does
     * not use this map.
     */
    public Map<Integer, String> getReferenceIdMap() {
        if (referenceIdMap != null) {
            return referenceIdMap;
        }

        ByteBuffer slice = header.sliceOrNull(bytes, Block.REFERENCE_ID_TABLE);
        if (slice == null) {
            referenceIdMap = Collections.emptyMap();
            return referenceIdMap;
        }

        BufReader reader = new BufReader(slice);
        int length = reader.readVarint32();
        HashMap<Integer, String> map = new HashMap<>(Math.max(1, length / 10 + 1));
        for (int i = 0; i < length; ++i) {
            map.put(reader.readVarint32(), reader.readUtf8String());
        }
        referenceIdMap = Collections.unmodifiableMap(map);
        return referenceIdMap;
    }

    /**
     * Check if two dictionaries are built on a same system dictionary.
     * 
     * User dictionary stores the signature of the system dictionary which it is
     * built on as Desctiption.reference
     * ({@link com.worksap.nlp.sudachi.dictionary.build.DicBuilder.User#system}).
     * 
     * @param other
     *            dictionary to check with
     * @return true if and only if two dictionaries have matching signature or
     *         reference.
     */
    public boolean isCompatibleWith(BinaryDictionary other) {
        String thisSignature;
        if (this.header.isSystemDictionary()) {
            thisSignature = this.header.getSignature();
        } else if (this.header.isUserDictionary()) {
            thisSignature = this.header.getReference();
        } else {
            throw new IllegalStateException("Invalid dictionary");
        }

        String otherSignature;
        if (other.header.isSystemDictionary()) {
            otherSignature = other.header.getSignature();
        } else if (other.header.isUserDictionary()) {
            otherSignature = other.header.getReference();
        } else {
            throw new IllegalStateException("Invalid dictionary");
        }

        return thisSignature.equals(otherSignature);
    }
}
