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

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.worksap.nlp.sudachi.dictionary.CharacterCategory;
import com.worksap.nlp.sudachi.dictionary.DictionaryHeader;
import com.worksap.nlp.sudachi.dictionary.DictionaryVersion;
import com.worksap.nlp.sudachi.dictionary.DoubleArrayLexicon;
import com.worksap.nlp.sudachi.dictionary.Grammar;
import com.worksap.nlp.sudachi.dictionary.GrammarImpl;
import com.worksap.nlp.sudachi.dictionary.LexiconSet;

class JapaneseDictionary implements Dictionary {

    Grammar grammar;
    LexiconSet lexicon;
    List<InputTextPlugin> inputTextPlugins;
    List<OovProviderPlugin> oovProviderPlugins;
    List<PathRewritePlugin> pathRewritePlugins;
    List<MappedByteBuffer> buffers;

    JapaneseDictionary() throws IOException {
        this(null, null);
    }

    JapaneseDictionary(String jsonString) throws IOException {
        this(null, jsonString);
    }

    JapaneseDictionary(String path, String jsonString) throws IOException {
        if (jsonString == null) {
            try (InputStream input
                 = SudachiCommandLine.class
                 .getResourceAsStream("/sudachi.json")) {
                jsonString = readAll(input);
            }
        }
        Settings settings = Settings.parseSettings(path, jsonString);

        buffers = new ArrayList<>();

        readSystemDictionary(settings.getPath("systemDict"));
        for (EditConnectionCostPlugin p :
                 settings.<EditConnectionCostPlugin>getPluginList("editConnectionCostPlugin")) {
            p.setUp(grammar);
            p.edit(grammar);
        }

        readCharacterDefinition(settings.getPath("characterDefinitionFile"));

        inputTextPlugins = settings.getPluginList("inputTextPlugin");
        for (InputTextPlugin p : inputTextPlugins) {
            p.setUp();
        }
        oovProviderPlugins = settings.getPluginList("oovProviderPlugin");
        if (oovProviderPlugins.isEmpty()) {
            throw new IllegalArgumentException("no OOV provider");
        }
        for (OovProviderPlugin p : oovProviderPlugins) {
            p.setUp(grammar);
        }
        pathRewritePlugins = settings.getPluginList("pathRewritePlugin");
        for (PathRewritePlugin p : pathRewritePlugins) {
            p.setUp(grammar);
        }

        for (String filename : settings.getPathList("userDict")) {
            readUserDictionary(filename);
        }
    }

    void readSystemDictionary(String filename) throws IOException {
        if (filename == null) {
            throw new IllegalArgumentException("system dictionary is not specified");
        }
        MappedByteBuffer bytes;
        try (FileInputStream istream = new FileInputStream(filename);
             FileChannel inputFile = istream.getChannel()) {
            bytes = inputFile.map(FileChannel.MapMode.READ_ONLY, 0,
                                  inputFile.size());
            bytes.order(ByteOrder.LITTLE_ENDIAN);
        }
        buffers.add(bytes);

        int offset = 0;
        DictionaryHeader header = new DictionaryHeader(bytes, offset);
        if (header.getVersion() != DictionaryVersion.SYSTEM_DICT_VERSION) {
            throw new IllegalArgumentException("invalid system dictionary");
        }
        offset += header.storageSize();

        GrammarImpl grammar = new GrammarImpl(bytes, offset);
        this.grammar = grammar;
        offset += grammar.storageSize();

        lexicon = new LexiconSet(new DoubleArrayLexicon(bytes, offset));
    }

    void readUserDictionary(String filename) throws IOException {
        if (lexicon.isFull()) {
            throw new IllegalArgumentException("too many dictionaries");
        }

        MappedByteBuffer bytes;
        try (FileInputStream input = new FileInputStream(filename);
             FileChannel inputFile = input.getChannel()) {
            bytes = inputFile.map(FileChannel.MapMode.READ_ONLY, 0,
                                  inputFile.size());
            bytes.order(ByteOrder.LITTLE_ENDIAN);
        }
        buffers.add(bytes);

        int offset = 0;
        DictionaryHeader header = new DictionaryHeader(bytes, offset);
        if (header.getVersion() != DictionaryVersion.USER_DICT_VERSION) {
            throw new IllegalArgumentException("invalid user dictionary");
        }
        offset += header.storageSize();

        DoubleArrayLexicon userLexicon
            = new DoubleArrayLexicon(bytes, offset);
        Tokenizer tokenizer
            = new JapaneseTokenizer(grammar, lexicon,
                                    inputTextPlugins, oovProviderPlugins,
                                    Collections.emptyList());

        userLexicon.calculateCost(tokenizer);
        lexicon.add(userLexicon);
    }
    
    void readCharacterDefinition(String filename) throws IOException {
        if (grammar == null) {
            return;
        }
        CharacterCategory charCategory = new CharacterCategory();
        charCategory.readCharacterDefinition(filename);
        grammar.setCharacterCategory(charCategory);
    }

    @Override
    public void close() {
        grammar = null;
        lexicon = null;
        for (MappedByteBuffer buffer : buffers) {
            destroyByteBuffer(buffer);
        }
    }

    @Override
    public Tokenizer create() {
        return new JapaneseTokenizer(grammar, lexicon,
                                     inputTextPlugins, oovProviderPlugins,
                                     pathRewritePlugins);
    }

    @Override
    public int getPartOfSpeechSize() {
        return grammar.getPartOfSpeechSize();
    }

    @Override
    public List<String> getPartOfSpeechString(short posId) {
        return grammar.getPartOfSpeechString(posId);
    }

    static String readAll(InputStream input) throws IOException {
        try (InputStreamReader isReader = new InputStreamReader(input, StandardCharsets.UTF_8);
             BufferedReader reader = new BufferedReader(isReader)) {
            StringBuilder sb = new StringBuilder();
            while (true) {
                String line = reader.readLine();
                if (line == null) {
                    break;
                }
                sb.append(line);
            }
            return sb.toString();
        }
    }

    // reference: https://github.com/apache/lucene-solr/blob/master/lucene/core/src/java/org/apache/lucene/store/MMapDirectory.java

    private static MethodHandle unmapper = null;
    private static Class<?> unmappableBufferClass = null;
    static {
        final MethodHandles.Lookup lookup = MethodHandles.lookup();
        try {
            try {
                // for JDK 9 or later
                final Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
                final MethodHandle cleanerMethod =
                    lookup.findVirtual(unsafeClass, "invokeCleaner",
                                       MethodType.methodType(void.class, ByteBuffer.class));
                final Field f = unsafeClass.getDeclaredField("theUnsafe");
                f.setAccessible(true);
                final Object theUnsafe = f.get(null);
                unmapper = cleanerMethod.bindTo(theUnsafe);
                unmappableBufferClass = ByteBuffer.class;
            } catch (SecurityException se) {
                throw se;
            } catch (ReflectiveOperationException | RuntimeException e) {
                // for JDK 8
                final Class<?> directBufferClass =
                    Class.forName("java.nio.DirectByteBuffer");
                final Method m = directBufferClass.getMethod("cleaner");
                m.setAccessible(true);
                final MethodHandle directBufferCleanerMethod = lookup.unreflect(m);
                final Class<?> cleanerClass
                    = directBufferCleanerMethod.type().returnType();

                final MethodHandle cleanMethod =
                    lookup.findVirtual(cleanerClass, "clean",
                                       MethodType.methodType(void.class));
                final MethodHandle nonNullTest =
                    lookup.findStatic(Object.class, "nonNull",
                                      MethodType.methodType(boolean.class, Object.class))
                    .asType(MethodType.methodType(boolean.class, cleanerClass));
                final MethodHandle noop = MethodHandles.dropArguments(MethodHandles.constant(Void.class, null).asType(MethodType.methodType(void.class)), 0, cleanerClass);
                unmapper =
                    MethodHandles.filterReturnValue(directBufferCleanerMethod,
                                                    MethodHandles.guardWithTest(nonNullTest, cleanMethod, noop))
                    .asType(MethodType.methodType(void.class, ByteBuffer.class));
                unmapper = directBufferCleanerMethod;
                unmappableBufferClass = directBufferClass;
            }
        } catch (SecurityException se) {
            throw se;
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            unmapper = null;
        }
    }

    static void destroyByteBuffer(ByteBuffer buffer) {
        if (!buffer.isDirect()) {
            return;
        }
        if (unmapper != null && unmappableBufferClass.isInstance(buffer)) {
            try {
                unmapper.invokeExact(buffer);
            } catch(Throwable e) {
                throw new RuntimeException("can not destroy direct buffer " + buffer, e);
            }
        }
    }
}
