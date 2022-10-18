/*
 * Copyright (c) 2021 Works Applications Co., Ltd.
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

import com.worksap.nlp.sudachi.dictionary.build.RawLexiconReader.Column;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class DictionaryPrinter {

    private final PrintStream output;
    private final BinaryDictionary dic;
    private final BinaryDictionary base;

    private final GrammarImpl grammar;
    private final DoubleArrayLexicon lex;
    private final Ints wordIds;

    private DictionaryPrinter(PrintStream output, BinaryDictionary dic, BinaryDictionary base) {
        this.output = output;
        this.dic = dic;
        this.base = base;

        if (base != null) {
            GrammarImpl grammar = base.getGrammar();
            grammar.addPosList(dic.getGrammar());
            this.grammar = grammar;
        } else {
            grammar = dic.getGrammar();
        }

        lex = dic.getLexicon();

        // in order to output dictionary entries in in-dictionary order we need to sort them
        // iterator over them will get them not in the sorted order, but grouped by surface (and sorted in groups)
        Ints allIds = new Ints(lex.size());
        Iterator<Ints> ids = lex.wordIds();
        while (ids.hasNext()) {
            allIds.appendAll(ids.next());
        }
        allIds.sort();
        wordIds = allIds;
    }

    void printHeader() {
        // @formatter:off
        printColumnHeaders(Column.Surface, Column.LeftId, Column.RightId, Column.Cost, Column.Pos1, Column.Pos2,
                Column.Pos3, Column.Pos4, Column.Pos5, Column.Pos6, Column.ReadingForm, Column.DictionaryForm,
                Column.NormalizedForm, Column.Mode, Column.SplitA, Column.SplitB, Column.SplitC, Column.WordStructure,
                Column.SynonymGroups, Column.UserData);
        // @formatter:on
    }

    void printColumnHeaders(Column... headers) {
        for (Column c : headers) {
            output.print(c.name());
        }
        output.println();
    }

    void printEntry(int wordId) {
        WordInfo info = lex.getWordInfo(wordId);
        POS pos = grammar.getPartOfSpeechString(info.getPOSId());
        long params = lex.parameters(wordId);
        short leftId = WordParameters.leftId(params);
        short rightId = WordParameters.rightId(params);
        short cost = WordParameters.cost(params);
        String surface = lex.string(0, info.getSurface());
        String reading = lex.string(0, info.getReadingForm());
        field(surface);
        field(leftId);
        field(rightId);
        field(cost);
        field(pos.get(0));
        field(pos.get(1));
        field(pos.get(2));
        field(pos.get(3));
        field(pos.get(4));
        field(pos.get(5));
        field(reading);
        entryPtr(info.getNormalizedForm(), ",");
        entryPtr(info.getDictionaryForm(), ",");
        output.print("\n");
    }

    void entryPtr(int wordId, String delimiter) {
        WordInfo info = lex.getWordInfo(wordId);
        POS pos = grammar.getPartOfSpeechString(info.getPOSId());
        String surface = lex.string(0, info.getSurface());
        String reading = lex.string(0, info.getReadingForm());
        ptrPart(surface, "-");
        ptrPart(pos.get(0), "-");
        ptrPart(pos.get(1), "-");
        ptrPart(pos.get(2), "-");
        ptrPart(pos.get(3), "-");
        ptrPart(pos.get(4), "-");
        ptrPart(pos.get(5), "-");
        ptrPart(reading, "");
        output.print(delimiter);
    }

    void ptrPart(String part, String delimiter) {
        output.print(part);
        output.print(delimiter);
    }

    void field(short value) {
        output.print(value);
        output.print(',');
    }

    void field(String value) {
        output.print(maybeQuoteField(value));
        output.print(',');
    }

    private String maybeQuoteField(String value) {
        boolean hasCommas = value.indexOf(',') != -1;
        boolean hasQuotes = value.indexOf('"') != -1;
        if (hasCommas || hasQuotes) {
            return escape(value, hasQuotes);
        }
        return value;
    }

    private String maybeQuoteRefPart(String value) {
        if (value.indexOf(',') != -1 || value.indexOf('"') != -1 || value.indexOf('-') != -1 || value.indexOf(
                '/') != -1) {
            return fullEscape(value);
        }
        return value;
    }

    private String escape(String value, boolean hasQuotes) {
        if (hasQuotes) {
            return fullEscape(value);
        }
        // only commas
        return "\"" + value + "\"";
    }

    private String fullEscape(String value) {
        StringBuilder sb = new StringBuilder(value.length() + 10);
        int len = value.length();
        for (int i = 0; i < len; ++i) {
            char c = value.charAt(i);
            if (c != '"' && c != '-' && c != ',' && c != '/') {
                sb.append(c);
            } else {
                sb.append("\\u{").append(Integer.toHexString(c)).append('}');
            }
        }
        return sb.toString();
    }

    private void printEntries() {
        for (int i = 0; i < wordIds.length(); ++i) {
            printEntry(wordIds.get(i));
        }
    }

    static void printDictionary(String filename, BinaryDictionary systemDict, PrintStream output) throws IOException {
        try (BinaryDictionary dictionary = new BinaryDictionary(filename)) {
            DictionaryPrinter dp = new DictionaryPrinter(output, dictionary, systemDict);
            dp.printHeader();
            dp.printEntries();
        }
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
     *         the option and the input filename
     * @throws IOException
     *         if IO
     */
    public static void main(String[] args) throws IOException {
        BinaryDictionary systemDict = null;

        try {
            int i = 0;
            for (i = 0; i < args.length; i++) {
                if (args[i].equals("-s") && i + 1 < args.length) {
                    systemDict = BinaryDictionary.loadSystem(args[++i]);
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
