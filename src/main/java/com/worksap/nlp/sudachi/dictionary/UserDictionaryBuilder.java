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
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.time.Instant;
import java.util.Arrays;

import com.worksap.nlp.sudachi.MMap;

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
     * <li>(optional) the description which is embedded in the dictionary</li>
     * </ol>
     * @param args the input filename, the connection matrix file,
     * and the output filename
     * @throws IOException if IO or parsing is failed
     */
    public static void main(String[] args) throws IOException {
        if (args.length < 3) {
            System.err.println("usage: UserDictionaryBuilder system.dic input.csv output.dic [description]");
            return;
        }

        ByteBuffer bytes = MMap.map(args[0]);
        DictionaryHeader systemHeader = new DictionaryHeader(bytes, 0);
        if (systemHeader.getVersion() != DictionaryVersion.SYSTEM_DICT_VERSION) {
            System.err.println("Error: invalid system dictionary: " + args[0]);
            return;
        }
        Grammar grammar = new GrammarImpl(bytes, systemHeader.storageSize());

        String description = (args.length >= 4) ? args[3] : "";
        DictionaryHeader header
            = new DictionaryHeader(DictionaryVersion.USER_DICT_VERSION,
                                   Instant.now().getEpochSecond(),
                                   description);

        try (FileInputStream lexiconInput = new FileInputStream(args[1]);
             FileOutputStream output = new FileOutputStream(args[2])) {
            output.write(header.toByte());

            UserDictionaryBuilder builder = new UserDictionaryBuilder(grammar);
            builder.build(lexiconInput, output);
        }
    }
}
