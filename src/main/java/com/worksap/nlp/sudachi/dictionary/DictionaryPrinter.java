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

import com.worksap.nlp.sudachi.WordId;
import com.worksap.nlp.sudachi.dictionary.build.Progress;
import com.worksap.nlp.sudachi.dictionary.build.RawLexiconReader.Column;

import java.io.Console;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class DictionaryPrinter {
    public static final char wordRefDelimiter = '/';
    public static final String wordRefDelimiterStr = String.valueOf(wordRefDelimiter);
    public static final char wordRefJoiner = ',';
    public static final String wordRefJoinerStr = String.valueOf(wordRefJoiner);

    private final PrintStream output;
    private final Progress progress = Progress.syserr(20);

    private final GrammarImpl grammar;
    private final LexiconSet lex;
    // sorted raw word ids taken from the target dict.
    private final Ints wordIds;

    private DictionaryPrinter(PrintStream output, BinaryDictionary dic, BinaryDictionary base) {
        this.output = output;

        if (base == null) {
            grammar = dic.getGrammar();
            lex = new LexiconSet(dic.getLexicon(), grammar.getSystemPartOfSpeechSize());
        } else {
            grammar = base.getGrammar();
            lex = new LexiconSet(base.getLexicon(), grammar.getSystemPartOfSpeechSize());

            lex.add(dic.getLexicon(), (short) grammar.getPartOfSpeechSize());
            grammar.addPosList(dic.getGrammar());
        }

        // in order to output dictionary entries in in-dictionary order we need to sort
        // them. iterator over them will get them not in the sorted order, but grouped
        // by surface (and sorted in groups).
        DoubleArrayLexicon targetLex = dic.getLexicon();
        Ints allIds = new Ints(targetLex.size());
        Iterator<Ints> ids = targetLex.wordIds(0);
        while (ids.hasNext()) {
            allIds.appendAll(ids.next());
        }
        allIds.sort();
        wordIds = allIds;
    }

    static void printUsage() {
        Console console = System.console();
        console.printf("usage: PrintDictionary [-s file] file\n");
        console.printf("\t-s file\tsystem dictionary\n");
    }

    void printHeader() {
        // @formatter:off
        printColumnHeaders(Column.SURFACE, Column.LEFT_ID, Column.RIGHT_ID, Column.COST, Column.POS1, Column.POS2,
                Column.POS3, Column.POS4, Column.POS5, Column.POS6, Column.READING_FORM, Column.NORMALIZED_FORM,
                Column.DICTIONARY_FORM, Column.SPLIT_A, Column.SPLIT_B, Column.SPLIT_C, Column.WORD_STRUCTURE,
                Column.SYNONYM_GROUPS, Column.USER_DATA);
        // @formatter:on
    }

    void printColumnHeaders(Column... headers) {
        boolean isFirst = true;
        for (Column c : headers) {
            if (isFirst) {
                isFirst = false;
            } else {
                output.print(',');
            }
            output.print(c.name());
        }
        output.println();
    }

    private void printEntries() {
        progress.startBlock("Entries", System.nanoTime(), Progress.Kind.ENTRY);
        long size = wordIds.length();
        for (int i = 0; i < wordIds.length(); ++i) {
            printEntry(wordIds.get(i));
            progress.progress(i, size);
        }
        progress.endBlock(size, System.nanoTime());
    }

    void printEntry(int wordId) {
        int dic = WordId.dic(wordId);
        WordInfo info = lex.getWordInfo(wordId);
        POS pos = grammar.getPartOfSpeechString(info.getPOSId());
        long params = lex.parameters(wordId);
        short leftId = WordParameters.leftId(params);
        short rightId = WordParameters.rightId(params);
        short cost = WordParameters.cost(params);
        field(lex.string(dic, info.getSurface()));
        field(leftId);
        field(rightId);
        field(cost);
        field(pos.get(0));
        field(pos.get(1));
        field(pos.get(2));
        field(pos.get(3));
        field(pos.get(4));
        field(pos.get(5));
        field(lex.string(dic, info.getReadingForm()));
        field(wordRefHeadword(info.getNormalizedForm(), wordId));
        field(wordRef(info.getDictionaryForm(), wordId));
        field(wordRefList(info.getAunitSplit()));
        field(wordRefList(info.getBunitSplit()));
        field(wordRefList(info.getCunitSplit()));
        field(wordRefList(info.getWordStructure()));
        field(intList(info.getSynonymGroupIds()));
        lastField(info.getUserData());
        output.print("\n");
    }

    void field(short value) {
        output.print(value);
        output.print(',');
    }

    void field(String value) {
        output.print(maybeEscapeString(value));
        output.print(',');
    }

    void lastField(String value) {
        output.print(maybeEscapeString(value));
    }

    /**
     * encode word entry pointed by the wordId as WordRef.Triple. If it points to
     * self, return empty string.
     */
    String wordRef(int wordId, int reference) {
        if (wordId == reference) {
            return "";
        }
        return wordRef(wordId);
    }

    /** encode word entry pointed by the wordId as WordRef.Triple. */
    String wordRef(int wordId) {
        WordInfo info = lex.getWordInfo(wordId);
        POS pos = grammar.getPartOfSpeechString(info.getPOSId());
        int dic = WordId.dic(wordId);
        String surface = lex.string(dic, info.getSurface());
        String reading = lex.string(dic, info.getReadingForm());

        List<String> parts = new ArrayList<>(1 + POS.DEPTH + 1);
        parts.add(surface);
        parts.addAll(pos);
        parts.add(reading);

        return String.join(wordRefJoinerStr, parts.stream().map(this::maybeEscapeRefPart).collect(Collectors.toList()));
    }

    /** encode word entry pointed by the wordId as WordRef.Headword. */
    String wordRefHeadword(int wordId, int reference) {
        if (wordId == reference) {
            return "";
        }
        int dic = WordId.dic(wordId);
        WordInfo info = lex.getWordInfo(wordId);
        return lex.string(dic, info.getSurface());
    }

    String wordRefList(int[] wordIds) {
        return String.join(wordRefDelimiterStr,
                Arrays.stream(wordIds).boxed().map(this::wordRef).collect(Collectors.toList()));
    }

    String intList(int[] ints) {
        return String.join("/", Arrays.stream(ints).boxed().map(Object::toString).collect(Collectors.toList()));
    }

    private static boolean hasCh(String value, int ch) {
        return value.indexOf(ch) != -1;
    }

    /** escape string field of csv. */
    private String maybeEscapeString(String value) {
        boolean hasCommas = hasCh(value, ',');
        boolean hasQuotes = hasCh(value, '"');
        if (!hasCommas && !hasQuotes) {
            return value;
        }
        if (hasQuotes) {
            return "\"" + unicodeEscape(value, Arrays.asList('"')) + "\"";
        }
        return "\"" + value + "\"";
    }

    /** escape WordRef.Triple part. */
    private String maybeEscapeRefPart(String value) {
        boolean hasDelimiter = hasCh(value, wordRefDelimiter);
        boolean hasJoiner = hasCh(value, wordRefJoiner);
        if (!hasDelimiter && !hasJoiner) {
            return value;
        }
        return unicodeEscape(value, Arrays.asList(wordRefDelimiter, wordRefJoiner));
    }

    /** escape specified chars as unicode codepoint */
    private String unicodeEscape(String value, List<Character> targetChars) {
        StringBuilder sb = new StringBuilder(value.length() + 10);
        int len = value.length();
        for (int i = 0; i < len; ++i) {
            char c = value.charAt(i);
            if (targetChars.contains(c)) {
                sb.append("\\u{").append(Integer.toHexString(c)).append('}');
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
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

    static void printDictionary(String filename, BinaryDictionary systemDict, PrintStream output) throws IOException {
        try (BinaryDictionary dictionary = new BinaryDictionary(filename)) {
            DictionaryPrinter dp;
            if (dictionary.getDictionaryHeader().isUserDictionary()) {
                if (systemDict == null) {
                    throw new IllegalArgumentException(
                            "System dictionary (`-s` option) is required to print user dictionary: " + filename);
                }
                dp = new DictionaryPrinter(output, dictionary, systemDict);
            } else if (dictionary.getDictionaryHeader().isSystemDictionary()) {
                dp = new DictionaryPrinter(output, dictionary, null);
            } else {
                // should not happen
                throw new IllegalStateException("Invalid dictionary");
            }

            dp.printHeader();
            dp.printEntries();
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
                    systemDict = BinaryDictionary.loadSystem(args[++i]);
                } else if (args[i].equals("-h")) {
                    printUsage();
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
