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

package com.worksap.nlp.sudachi.dictionary;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A user dictionary building tool. This class provide the converter
 * from the source file in the CSV format to the binary format.
 */
public class UserDictionaryBuilder extends DictionaryBuilder {

    Grammar grammar;

    UserDictionaryBuilder(Grammar grammar) {
        super();
        this.grammar = grammar;
    }

    void build(FileInputStream lexiconInput, FileOutputStream output)
        throws IOException {
        buildLexicon(lexiconInput);
        lexiconInput.close();

        FileChannel outputChannel = output.getChannel();
        writeLexicon(outputChannel);
        outputChannel.close();
    }

    @Override
    short getPosId(String... posStrings) {
        return grammar.getPartOfSpeechId(Arrays.asList(posStrings));
    }

    /**
     * Builds the user dictionary.
     *
     * This tool requires three arguments.
     * <ol start="0">
     * <li>the path of the system dictionary</li>
     * <li>the path of the source file in the CSV format</li>
     * <li>the path of the output file</li>
     * </ol>
     * @param args the input filename, the connection matrix file,
     * and the output filename
     * @throws IOException if IO or parsing is failed
     */
    public static void main(String[] args) throws IOException {
        if (args.length != 3) {
            System.err.println("usage: UserDictionaryBuilder system.dic input.csv output.dic");
            return;
        }

        Grammar grammar;
        try (FileInputStream istream = new FileInputStream(args[0]);
             FileChannel inputFile = istream.getChannel()) {
            ByteBuffer bytes;
            bytes = inputFile.map(FileChannel.MapMode.READ_ONLY, 0,
                                  inputFile.size());
            bytes.order(ByteOrder.LITTLE_ENDIAN);
            grammar = new GrammarImpl(bytes, 0);
        }

        try (FileInputStream lexiconInput = new FileInputStream(args[1]);
             FileOutputStream output = new FileOutputStream(args[2])) {
            UserDictionaryBuilder builder = new UserDictionaryBuilder(grammar);
            builder.build(lexiconInput, output);
        }
    }
}
