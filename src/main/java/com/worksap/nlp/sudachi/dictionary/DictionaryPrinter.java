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

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class DictionaryPrinter {

    private DictionaryPrinter() {
    }

    static void printDictionary(String filename, BinaryDictionary systemDict, PrintStream output) throws IOException {
        GrammarImpl grammar = null;

        try (BinaryDictionary dictionary = new BinaryDictionary(filename)) {
            if (dictionary.getDictionaryHeader().getVersion() == DictionaryVersion.SYSTEM_DICT_VERSION) {
                grammar = dictionary.getGrammar();
            } else if (systemDict == null) {
                throw new IllegalArgumentException("the system dictionary is not specified");
            } else {
                grammar = systemDict.getGrammar();
                if (dictionary.getDictionaryHeader().getVersion() == DictionaryVersion.USER_DICT_VERSION_2) {
                    grammar.addPosList(dictionary.getGrammar());
                }
            }

            List<String> posStrings = new ArrayList<>();
            for (short pid = 0; pid < grammar.getPartOfSpeechSize(); pid++) {
                posStrings.add(String.join(",", grammar.getPartOfSpeechString(pid)));
            }

            Lexicon lexicon = dictionary.getLexicon();
            for (int wordId = 0; wordId < lexicon.size(); wordId++) {
                short leftId = lexicon.getLeftId(wordId);
                short rightId = lexicon.getRightId(wordId);
                short cost = lexicon.getCost(wordId);
                WordInfo wordInfo = lexicon.getWordInfo(wordId);

                char unitType = getUnitType(wordInfo);

                output.println(String.format("%s,%d,%d,%d,%s,%s,%s,%s,%s,%c,%s,%s,%s", wordInfo.getSurface(), leftId,
                        rightId, cost, wordInfo.getSurface(), posStrings.get(wordInfo.getPOSId()),
                        wordInfo.getReadingForm(), wordInfo.getNormalizedForm(),
                        wordIdToString(wordInfo.getDictionaryFormWordId()), unitType,
                        splitToString(wordInfo.getAunitSplit()), splitToString(wordInfo.getBunitSplit()),
                        splitToString(wordInfo.getWordStructure())));
            }
        }
    }

    static String wordIdToString(int wid) {
        return (wid < 0) ? "*" : Integer.toString(wid);
    }

    static char getUnitType(WordInfo info) {
        if (info.getAunitSplit().length == 0) {
            return 'A';
        } else if (info.getBunitSplit().length == 0) {
            return 'B';
        } else {
            return 'C';
        }
    }

    static String splitToString(int[] split) {
        if (split.length == 0) {
            return "*";
        } else {
            return Arrays.stream(split)
                    .mapToObj(i -> (i >> 28 != 0) ? "U" + Integer.toString(i & ((1 << 28) - 1)) : Integer.toString(i))
                    .collect(Collectors.joining("/"));
        }
    }

    /**
     * Prints the contents of dictionary.
     *
     * <p>
     * Usage: {@code PrintDictionary [-s file] file}
     * <p>
     * The following are the options.
     * <dl>
     * <dt>{@code -s file}</dt>
     * <dd>the system dictionary file</dd>
     * </dl>
     * <p>
     * This tool requires the system dictionary when it dumps an user dictionary.
     * 
     * @param args
     *            the option and the input filename
     * @throws IOException
     *             if IO
     */
    public static void main(String[] args) throws IOException {
        BinaryDictionary systemDict = null;

        try {
            int i = 0;
            for (i = 0; i < args.length; i++) {
                if (args[i].equals("-s") && i + 1 < args.length) {
                    systemDict = BinaryDictionary.readSystemDictionary(args[++i]);
                } else if (args[i].equals("-h")) {
                    System.err.println("usage: PrintDictionary [-s file] file");
                    System.err.println("\t-s file\tsystem dictionary");
                    return;
                } else {
                    break;
                }
            }

            if (i < args.length) {
                printDictionary(args[i], systemDict, System.out);
            }
        } finally {
            if (systemDict != null) {
                systemDict.close();
            }
        }
    }
}
