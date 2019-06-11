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

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.worksap.nlp.dartsclone.DoubleArray;

/**
 * A dictionary building tool. This class provide the converter from the source
 * file in the CSV format to the binary format.
 */
public class DictionaryBuilder {

    static final int STRING_MAX_LENGTH = Short.MAX_VALUE;
    static final int ARRAY_MAX_LENGTH = Byte.MAX_VALUE;
    static final int NUMBER_OF_COLUMNS = 18;
    static final int BUFFER_SIZE = 1024 * 1024;

    static class WordEntry {
        String headword;
        short[] parameters;
        WordInfo wordInfo;
        String aUnitSplitString;
        String bUnitSplitString;
        String wordStructureString;
    }

    static class POSTable {
        private List<String> table = new ArrayList<>();

        short getId(String s) {
            int id = table.indexOf(s);
            if (id < 0) {
                id = table.size();
                table.add(s);
            }
            return (short) id;
        }

        List<String> getList() {
            return table;
        }
    }

    POSTable posTable = new POSTable();
    SortedMap<byte[], List<Integer>> trieKeys = new TreeMap<>((byte[] l, byte[] r) -> {
        int llen = l.length;
        int rlen = r.length;
        for (int i = 0; i < Math.min(llen, rlen); i++) {
            if (l[i] != r[i]) {
                return (l[i] & 0xff) - (r[i] & 0xff);
            }
        }
        return l.length - r.length;
    });
    List<WordEntry> entries = new ArrayList<>();

    boolean isUserDictionary = false;

    ByteBuffer buffer;

    DictionaryBuilder() {
        buffer = ByteBuffer.allocate(BUFFER_SIZE);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
    }

    void build(List<String> lexiconPaths, FileInputStream matrixInput, FileOutputStream output) throws IOException {
        System.err.print("reading the source file...");
        for (String path : lexiconPaths) {
            try (FileInputStream lexiconInput = new FileInputStream(path)) {
                buildLexicon(path, lexiconInput);
            }
        }
        System.err.println(String.format(" %,d words", entries.size()));

        FileChannel outputChannel = output.getChannel();
        writeGrammar(matrixInput, outputChannel);
        writeLexicon(outputChannel);
        outputChannel.close();
    }

    void buildLexicon(String filename, FileInputStream lexiconInput) throws IOException {
        int lineno = -1;
        try (InputStreamReader isr = new InputStreamReader(lexiconInput);
                LineNumberReader reader = new LineNumberReader(isr);
                CSVParser parser = new CSVParser(reader)) {
            for (List<String> columns = parser.getNextRecord(); columns != null; columns = parser.getNextRecord()) {
                lineno = reader.getLineNumber();
                WordEntry entry = parseLine(columns.toArray(new String[columns.size()]));
                if (entry.headword != null) {
                    addToTrie(entry.headword, entries.size());
                }
                entries.add(entry);
            }
        } catch (Exception e) {
            if (lineno > 0) {
                System.err.println("Error: " + e.getMessage() + " at line " + lineno + " in " + filename);
            }
            throw e;
        }
    }

    WordEntry parseLine(String[] cols) {
        if (cols.length != NUMBER_OF_COLUMNS) {
            throw new IllegalArgumentException("invalid format");
        }
        for (int i = 0; i < 15; i++) {
            cols[i] = decode(cols[i]);
        }

        if (cols[0].getBytes(StandardCharsets.UTF_8).length > STRING_MAX_LENGTH || !isValidLength(cols[4])
                || !isValidLength(cols[11]) || !isValidLength(cols[12])) {
            throw new IllegalArgumentException("string is too long");
        }

        if (cols[0].isEmpty()) {
            throw new IllegalArgumentException("headword is empty");
        }

        WordEntry entry = new WordEntry();

        // headword for trie
        if (!cols[1].equals("-1")) {
            entry.headword = cols[0];
        }

        // left-id, right-id, cost
        entry.parameters = new short[] { Short.parseShort(cols[1]), Short.parseShort(cols[2]),
                Short.parseShort(cols[3]) };

        // part of speech
        short posId = getPosId(cols[5], cols[6], cols[7], cols[8], cols[9], cols[10]);
        if (posId < 0) {
            throw new IllegalArgumentException("invalid part of speech");
        }

        entry.aUnitSplitString = cols[15];
        entry.bUnitSplitString = cols[16];
        entry.wordStructureString = cols[17];
        checkSplitInfoFormat(entry.aUnitSplitString);
        checkSplitInfoFormat(entry.bUnitSplitString);
        checkSplitInfoFormat(entry.wordStructureString);
        if (cols[14].equals("A") && (!entry.aUnitSplitString.equals("*") || !entry.bUnitSplitString.equals("*"))) {
            throw new IllegalArgumentException("invalid splitting");
        }

        entry.wordInfo = new WordInfo(cols[4], // headword
                (short) cols[0].getBytes(StandardCharsets.UTF_8).length, posId, cols[12], // normalizedForm
                (cols[13].equals("*") ? -1 : Integer.parseInt(cols[13])), // dictionaryFormWordId
                "", // dummy
                cols[11], // readingForm
                null, null, null);

        return entry;
    }

    void addToTrie(String headword, int wordId) {
        byte[] key = headword.getBytes(StandardCharsets.UTF_8);
        trieKeys.computeIfAbsent(key, k -> new ArrayList<>()).add(wordId);
    }

    short getPosId(String... posStrings) {
        return posTable.getId(String.join(",", posStrings));
    }

    void writeGrammar(FileInputStream matrixInput, FileChannel output) throws IOException {

        System.err.print("writing the POS table...");
        convertPOSTable(posTable.getList());
        buffer.flip();
        output.write(buffer);
        System.err.println(String.format(" %,d bytes", buffer.limit()));
        buffer.clear();

        System.err.print("writing the connection matrix...");
        ByteBuffer matrix = convertMatrix(matrixInput);
        buffer.flip();
        output.write(buffer);
        buffer.clear();
        output.write(matrix);
        System.err.println(String.format(" %,d bytes", matrix.limit() + 4));
        matrix = null;
    }

    void convertPOSTable(List<String> posList) {
        buffer.putShort((short) posList.size());

        for (String pos : posList) {
            for (String text : pos.split(",")) {
                writeString(text);
            }
        }
    }

    ByteBuffer convertMatrix(InputStream matrixInput) throws IOException {
        LineNumberReader reader = new LineNumberReader(new InputStreamReader(matrixInput));
        String header = reader.readLine();
        if (header == null) {
            throw new IllegalArgumentException("invalid format at line " + reader.getLineNumber());
        }

        String[] lr = header.split("\\s+");
        short leftSize = Short.parseShort(lr[0]);
        short rightSize = Short.parseShort(lr[1]);
        buffer.putShort(leftSize);
        buffer.putShort(rightSize);

        ByteBuffer matrix = ByteBuffer.allocate(2 * leftSize * rightSize);
        matrix.order(ByteOrder.LITTLE_ENDIAN);

        while (true) {
            String line = reader.readLine();
            if (line == null) {
                break;
            }
            if (line.matches("\\s*")) {
                continue;
            }
            String[] cols = line.split("\\s+");
            if (cols.length < 3) {
                System.err.println("invalid format at line " + reader.getLineNumber());
                continue;
            }
            short left = Short.parseShort(cols[0]);
            short right = Short.parseShort(cols[1]);
            short cost = Short.parseShort(cols[2]);
            matrix.putShort(2 * (left + leftSize * right), cost);
        }
        return matrix;
    }

    void writeLexicon(FileChannel output) throws IOException {
        DoubleArray trie = new DoubleArray();

        int size = trieKeys.size();

        byte[][] keys = new byte[size][];
        int[] values = new int[size];
        ByteBuffer wordIdTable = ByteBuffer.allocate(entries.size() * (4 + 2));
        wordIdTable.order(ByteOrder.LITTLE_ENDIAN);

        int i = 0;
        for (Entry<byte[], List<Integer>> entry : trieKeys.entrySet()) {
            keys[i] = entry.getKey();
            values[i] = wordIdTable.position();
            i++;
            List<Integer> wordIds = entry.getValue();
            wordIdTable.put((byte) wordIds.size());
            for (int wid : wordIds) {
                wordIdTable.putInt(wid);
            }
        }

        System.err.print("building the trie");
        trie.build(keys, values, (n, s) -> {
            if (n % ((s / 10) + 1) == 0) {
                System.err.print(".");
            }
        });
        System.err.println("done");

        System.err.print("writing the trie...");
        buffer.clear();
        buffer.putInt(trie.size());
        buffer.flip();
        output.write(buffer);
        buffer.clear();

        output.write(trie.byteArray());
        System.err.println(String.format(" %,d bytes", trie.size() * 4 + 4));
        trie = null;

        System.err.print("writing the word-ID table...");
        buffer.putInt(wordIdTable.position());
        buffer.flip();
        output.write(buffer);
        buffer.clear();

        wordIdTable.flip();
        output.write(wordIdTable);
        System.err.println(String.format(" %,d bytes", wordIdTable.position() + 4));
        wordIdTable = null;

        System.err.print("writing the word parameters...");
        buffer.putInt(entries.size());
        for (WordEntry entry : entries) {
            buffer.putShort(entry.parameters[0]);
            buffer.putShort(entry.parameters[1]);
            buffer.putShort(entry.parameters[2]);
            buffer.flip();
            output.write(buffer);
            buffer.clear();
        }
        System.err.println(String.format(" %,d bytes", entries.size() * 6 + 4));

        writeWordInfo(output);
    }

    void writeWordInfo(FileChannel output) throws IOException {
        long mark = output.position();
        output.position(mark + 4 * entries.size());

        ByteBuffer offsets = ByteBuffer.allocate(4 * entries.size());
        offsets.order(ByteOrder.LITTLE_ENDIAN);

        System.err.print("writing the wordInfos...");
        long base = output.position();
        for (WordEntry entry : entries) {
            WordInfo wi = entry.wordInfo;
            offsets.putInt((int) output.position());

            writeString(wi.getSurface());
            writeStringLength(wi.getLength());
            buffer.putShort(wi.getPOSId());
            if (wi.getNormalizedForm().equals(wi.getSurface())) {
                writeString("");
            } else {
                writeString(wi.getNormalizedForm());
            }
            buffer.putInt(wi.getDictionaryFormWordId());
            if (wi.getReadingForm().equals(wi.getSurface())) {
                writeString("");
            } else {
                writeString(wi.getReadingForm());
            }
            writeIntArray(parseSplitInfo(entry.aUnitSplitString));
            writeIntArray(parseSplitInfo(entry.bUnitSplitString));
            writeIntArray(parseSplitInfo(entry.wordStructureString));
            buffer.flip();
            output.write(buffer);
            buffer.clear();
        }
        System.err.println(String.format(" %,d bytes", output.position() - base));

        System.err.print("writing wordInfo offsets...");
        output.position(mark);
        offsets.flip();
        output.write(offsets);
        System.err.println(String.format(" %,d bytes", offsets.position()));
    }

    static boolean isValidLength(String text) {
        return text.length() <= STRING_MAX_LENGTH;
    }

    static final Pattern unicodeLiteral = Pattern.compile("\\\\u([0-9a-fA-F]{4}|\\{[0-9a-fA-F]+\\})");

    static String decode(String text) {
        Matcher m = unicodeLiteral.matcher(text);
        if (!m.find()) {
            return text;
        }

        StringBuffer sb = new StringBuffer();
        m.reset();
        while (m.find()) {
            String u = m.group(1);
            if (u.startsWith("{")) {
                u = u.substring(1, u.length() - 1);
            }
            m.appendReplacement(sb, new String(Character.toChars(Integer.parseInt(u, 16))));
        }
        m.appendTail(sb);
        return sb.toString();
    }

    void checkSplitInfoFormat(String info) {
        if (info.chars().filter(i -> i == '/').count() + 1 > ARRAY_MAX_LENGTH) {
            throw new IllegalArgumentException("too many units");
        }
    }

    int[] parseSplitInfo(String info) {
        if (info.equals("*")) {
            return new int[0];
        }
        String[] words = info.split("/");
        if (words.length > ARRAY_MAX_LENGTH) {
            throw new IllegalArgumentException("too many units");
        }
        int[] ret = new int[words.length];
        for (int i = 0; i < words.length; i++) {
            if (isId(words[i])) {
                ret[i] = parseId(words[i]);
            } else {
                ret[i] = wordToId(words[i]);
            }
        }
        return ret;
    }

    boolean isId(String text) {
        return text.matches("U?\\d+");
    }

    int parseId(String text) {
        int id = 0;
        if (text.startsWith("U")) {
            id = Integer.parseInt(text.substring(1));
            if (isUserDictionary) {
                id |= (1 << 28);
            }
        } else {
            id = Integer.parseInt(text);
            if (id < 0) {
                throw new IllegalArgumentException("not found such a word");
            }
        }
        return id;
    }

    int wordToId(String text) {
        String[] cols = text.split(",");
        if (cols.length < 8) {
            throw new IllegalArgumentException("too few columns");
        }
        String headword = decode(cols[0]);
        short posId = getPosId(cols[1], cols[2], cols[3], cols[4], cols[5], cols[6]);
        if (posId < 0) {
            throw new IllegalArgumentException("invalid part of speech");
        }
        String reading = decode(cols[7]);
        return getWordId(headword, posId, reading);
    }

    int getWordId(String headword, short posId, String readingForm) {
        for (int wid = 0; wid < entries.size(); wid++) {
            WordInfo info = entries.get(wid).wordInfo;
            if (info.getSurface().equals(headword) && info.getPOSId() == posId
                    && info.getReadingForm().equals(readingForm)) {
                return wid;
            }
        }
        return -1;
    }

    void writeString(String text) {
        writeStringLength((short) text.length());
        for (int i = 0; i < text.length(); i++) {
            buffer.putChar(text.charAt(i));
        }
    }

    void writeStringLength(short length) {
        if (length <= Byte.MAX_VALUE) {
            buffer.put((byte) length);
        } else {
            buffer.put((byte) ((length >> 8) | 0x80));
            buffer.put((byte) (length & 0xFF));
        }
    }

    void writeIntArray(int[] array) {
        buffer.put((byte) array.length);
        for (int i : array) {
            buffer.putInt(i);
        }
    }

    static void printUsage() {
        System.err.println("usage: DictionaryBuilder -o file -m file [-d description] files...");
        System.err.println("\t-o file\toutput to file");
        System.err.println("\t-m file\tmatrix file");
        System.err.println("\t-d description\tcomment");
    }

    /**
     * Builds the system dictionary.
     *
     * This tool requires three arguments.
     * <ol start="0">
     * <li>{@code -o file} the path of the output file</li>
     * <li>{@code -m file} the path of the connection matrix file in MeCab's
     * matrix.def format</li>
     * <li>{@code -d string} (optional) the description which is embedded in the
     * dictionary</li>
     * <li>the paths of the source files in the CSV format</li>
     * </ol>
     * 
     * @param args
     *            the options and input filenames
     * @throws IOException
     *             if IO or parsing is failed
     */
    public static void main(String[] args) throws IOException {
        String description = "";
        String outputPath = null;
        String matrixPath = null;

        int i = 0;
        for (i = 0; i < args.length; i++) {
            if (args[i].equals("-o") && i + 1 < args.length) {
                outputPath = args[++i];
            } else if (args[i].equals("-m") && i + 1 < args.length) {
                matrixPath = args[++i];
            } else if (args[i].equals("-d") && i + 1 < args.length) {
                description = args[++i];
            } else if (args[i].equals("-h")) {
                printUsage();
                return;
            } else {
                break;
            }
        }

        if (args.length <= i || outputPath == null || matrixPath == null) {
            printUsage();
            return;
        }

        List<String> lexiconPaths = Arrays.asList(args).subList(i, args.length);

        DictionaryHeader header = new DictionaryHeader(DictionaryVersion.SYSTEM_DICT_VERSION,
                Instant.now().getEpochSecond(), description);

        try (FileInputStream matrixInput = new FileInputStream(matrixPath);
                FileOutputStream output = new FileOutputStream(outputPath)) {

            output.write(header.toByte());

            DictionaryBuilder builder = new DictionaryBuilder();
            builder.build(lexiconPaths, matrixInput, output);
        }
    }
}
