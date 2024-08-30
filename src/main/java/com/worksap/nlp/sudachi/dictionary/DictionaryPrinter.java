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
import com.worksap.nlp.sudachi.dictionary.build.RawLexiconReader;
import com.worksap.nlp.sudachi.dictionary.build.WordRef;
import com.worksap.nlp.sudachi.dictionary.build.RawLexiconReader.Column;

import java.io.Console;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DictionaryPrinter {
    private final PrintStream output;
    private Progress progress = Progress.syserr(20);

    private final GrammarImpl grammar;
    private final LexiconSet lex;
    // sorted raw word ids taken from the target dict.
    private final Ints wordIds;

    private POSMode posMode = POSMode.DEFAULT;
    private WordRefMode wordRefMode = WordRefMode.DEFAULT;

    public enum POSMode {
        PARTS, ID, BOTH;

        public static POSMode DEFAULT = PARTS;
    }

    public enum WordRefMode {
        TRIPLE_PARTS, TRIPLE_ID;

        public static WordRefMode DEFAULT = TRIPLE_PARTS;
    }

    DictionaryPrinter(PrintStream output, BinaryDictionary dic, BinaryDictionary base) {
        if (dic.getDictionaryHeader().isUserDictionary() && base == null) {
            throw new IllegalArgumentException("System dictionary is required to print user dictionary");
        }

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

    DictionaryPrinter(PrintStream output, BinaryDictionary dic, BinaryDictionary base, POSMode posMode,
            WordRefMode wordRefMode) {
        this(output, dic, base);
        this.posMode = posMode;
        this.wordRefMode = wordRefMode;
    }

    void setProgress(Progress progress) {
        this.progress = progress;
    }

    static void printUsage() {
        Console console = System.console();
        console.printf("usage: PrintDictionary [-s file] [--posMode mode] [--wordRefMode mode] file\n");
        console.printf("\t-s file\tsystem dictionary. required to print user dictionary.\n");
        console.printf("\t--posMode [PARTS, ID, BOTH]\tprint specified POS column (default PARTS).\n");
        console.printf(
                "\t--wordRefMode [TRIPLE_PARTS, TRIPLE_ID]\tprint word-reference in specified format (default TRIPLE_PARTS).\n");
    }

    void printDictionary() {
        printHeader();
        printEntries();
    }

    void printHeader() {
        List<Column> posColumns;
        if (posMode == POSMode.PARTS) {
            posColumns = Arrays.asList(Column.POS1, Column.POS2, Column.POS3, Column.POS4, Column.POS5, Column.POS6);
        } else if (posMode == POSMode.ID) {
            posColumns = Arrays.asList(Column.POS_ID);
        } else { // BOTH
            posColumns = Arrays.asList(Column.POS_ID, Column.POS1, Column.POS2, Column.POS3, Column.POS4, Column.POS5,
                    Column.POS6);
        }

        List<Column> headerColumns = Stream
                .of(Arrays.asList(Column.SURFACE, Column.LEFT_ID, Column.RIGHT_ID, Column.COST), posColumns,
                        Arrays.asList(Column.READING_FORM, Column.NORMALIZED_FORM, Column.DICTIONARY_FORM,
                                Column.SPLIT_A, Column.SPLIT_B, Column.SPLIT_C, Column.WORD_STRUCTURE,
                                Column.SYNONYM_GROUPS, Column.USER_DATA))
                .flatMap(l -> l.stream()).collect(Collectors.toList());

        printColumnHeaders(headerColumns);
    }

    void printColumnHeaders(List<Column> headers) {
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
        for (int i = 0; i < size; ++i) {
            printEntry(wordIds.get(i));
            progress.progress(i, size);
        }
        progress.endBlock(size, System.nanoTime());
    }

    void printEntry(int wordId) {
        int dic = WordId.dic(wordId);
        WordInfo info = lex.getWordInfo(wordId);
        short posId = info.getPOSId();
        POS pos = grammar.getPartOfSpeechString(posId);
        long params = lex.parameters(wordId);
        short leftId = WordParameters.leftId(params);
        short rightId = WordParameters.rightId(params);
        short cost = WordParameters.cost(params);
        field(lex.string(dic, info.getSurface()));
        field(leftId);
        field(rightId);
        field(cost);
        if (posMode == POSMode.ID || posMode == POSMode.BOTH) {
            field(posId);
        }
        if (posMode == POSMode.PARTS || posMode == POSMode.BOTH) {
            field(pos.get(0));
            field(pos.get(1));
            field(pos.get(2));
            field(pos.get(3));
            field(pos.get(4));
            field(pos.get(5));
        }
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
        int dic = WordId.dic(wordId);
        String surface = lex.string(dic, info.getSurface());
        short posId = info.getPOSId();
        String reading = lex.string(dic, info.getReadingForm());

        List<String> parts;
        if (wordRefMode == WordRefMode.TRIPLE_ID) {
            parts = Arrays.asList(surface, String.valueOf(posId), reading);
        } else {
            POS pos = grammar.getPartOfSpeechString(posId);
            parts = new ArrayList<>(1 + POS.DEPTH + 1);
            parts.add(surface);
            parts.addAll(pos);
            parts.add(reading);
        }

        return String.join(String.valueOf(WordRef.Parser.WORDREF_DELIMITER),
                parts.stream().map(this::maybeEscapeRefPart).collect(Collectors.toList()));
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
        return String.join(String.valueOf(RawLexiconReader.LIST_DELIMITER),
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
        boolean hasDelimiter = hasCh(value, RawLexiconReader.LIST_DELIMITER);
        boolean hasJoiner = hasCh(value, WordRef.Parser.WORDREF_DELIMITER);
        if (!hasDelimiter && !hasJoiner) {
            return value;
        }
        return unicodeEscape(value, Arrays.asList(RawLexiconReader.LIST_DELIMITER, WordRef.Parser.WORDREF_DELIMITER));
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
     *             the option and the input filename
     * @throws IOException
     *                     if IO
     */
    public static void main(String[] args) throws IOException {
        String systemDictPath = null;
        POSMode posMode = POSMode.PARTS;
        WordRefMode wordRefMode = WordRefMode.TRIPLE_PARTS;

        int i = 0;
        for (i = 0; i < args.length; i++) {
            if (args[i].equals("-h")) {
                printUsage();
                return;
            } else if (args[i].equals("-s") && i + 1 < args.length) {
                systemDictPath = args[++i];
            } else if (args[i].equals("--posMode") && i + 1 < args.length) {
                posMode = POSMode.valueOf(args[++i]);
            } else if (args[i].equals("--wordRefMode") && i + 1 < args.length) {
                wordRefMode = WordRefMode.valueOf(args[++i]);
            } else {
                break;
            }
        }
        if (i >= args.length) {
            System.console().printf("target dictionary file is missing");
            return;
        }

        String dictPath = args[i];
        BinaryDictionary systemDict = null;
        try (BinaryDictionary dict = new BinaryDictionary(dictPath)) {
            if (systemDictPath != null) {
                systemDict = BinaryDictionary.loadSystem(systemDictPath);
            }

            DictionaryPrinter printer = new DictionaryPrinter(System.out, dict, systemDict, posMode, wordRefMode);
            printer.printDictionary();
        } finally {
            if (systemDict != null) {
                systemDict.close();
            }
        }
    }
}
