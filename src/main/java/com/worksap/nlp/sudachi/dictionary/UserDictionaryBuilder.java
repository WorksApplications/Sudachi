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

package com.worksap.nlp.sudachi.dictionary;

import java.io.Console;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

/**
 * A user dictionary building tool. This class provide the converter from the
 * source file in the CSV format to the binary format.
 */
public class UserDictionaryBuilder extends DictionaryBuilder {

    Grammar grammar;
    Lexicon systemLexicon;

    UserDictionaryBuilder(Grammar grammar, Lexicon systemLexicon) {
        super();
        isUserDictionary = true;
        this.grammar = grammar;
        this.systemLexicon = systemLexicon;
    }

    void build(List<String> lexiconPaths, FileOutputStream output) throws IOException {
        logger.info("reading the source file...");
        for (String path : lexiconPaths) {
            try (FileInputStream lexiconInput = new FileInputStream(path)) {
                buildLexicon(path, lexiconInput);
            }
        }
        logger.info(() -> String.format(" %,d words%n", entries.size()));

        FileChannel outputChannel = output.getChannel();
        writeLexicon(outputChannel);
        outputChannel.close();
    }

    @Override
    short getPosId(String... posStrings) {
        return grammar.getPartOfSpeechId(Arrays.asList(posStrings));
    }

    @Override
    int getWordId(String headword, short posId, String readingForm) {
        int wid = super.getWordId(headword, posId, readingForm);
        if (wid >= 0) {
            return wid | (1 << 28);
        }
        return systemLexicon.getWordId(headword, posId, readingForm);
    }

    @Override
    void checkWordId(int wordId) {
        if (wordId >= (1 << 28)) {
            super.checkWordId(wordId & ((1 << 28) - 1));
        } else if (wordId < 0 || wordId >= systemLexicon.size()) {
            throw new IllegalArgumentException("invalid word ID");
        }
    }

    static void printUsage() {
        Console console = System.console();
        console.printf("usage: UserDictionaryBuilder -o file -s file [-d description] files...\n");
        console.printf("\t-o file\toutput to file\n");
        console.printf("\t-s file\tsystem dictionary\n");
        console.printf("\t-d description\tcomment\n");
    }

    /**
     * Builds the user dictionary.
     *
     * This tool requires three arguments.
     * <ol start="0">
     * <li>{@code -o file} the path of the output file</li>
     * <li>{@code -s file} the path of the system dictionary</li>
     * <li>{@code -d string} (optional) the description which is embedded in the
     * dictionary</li>
     * <li>the paths of the source file in the CSV format</li>
     * </ol>
     * 
     * @param args
     *            options and input filenames
     * @throws IOException
     *             if IO or parsing is failed
     */
    public static void main(String[] args) throws IOException {
        readLoggerConfig();

        String description = "";
        String outputPath = null;
        String sysDictPath = null;

        int i = 0;
        for (i = 0; i < args.length; i++) {
            if (args[i].equals("-o") && i + 1 < args.length) {
                outputPath = args[++i];
            } else if (args[i].equals("-s") && i + 1 < args.length) {
                sysDictPath = args[++i];
            } else if (args[i].equals("-d") && i + 1 < args.length) {
                description = args[++i];
            } else if (args[i].equals("-h")) {
                printUsage();
                return;
            } else {
                break;
            }
        }

        if (args.length <= i || outputPath == null || sysDictPath == null) {
            printUsage();
            return;
        }

        try (BinaryDictionary systemDict = BinaryDictionary.readSystemDictionary(sysDictPath)) {
            Grammar grammar = systemDict.getGrammar();
            Lexicon systemLexicon = systemDict.getLexicon();

            List<String> lexiconPaths = Arrays.asList(args).subList(i, args.length);

            DictionaryHeader header = new DictionaryHeader(DictionaryVersion.USER_DICT_VERSION,
                    Instant.now().getEpochSecond(), description);

            try (FileOutputStream output = new FileOutputStream(outputPath)) {
                output.write(header.toByte());

                UserDictionaryBuilder builder = new UserDictionaryBuilder(grammar, systemLexicon);
                builder.build(lexiconPaths, output);
            }
        }
    }
}
