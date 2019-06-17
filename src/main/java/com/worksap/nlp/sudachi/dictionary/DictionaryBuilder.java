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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.logging.LogManager;
import java.util.logging.Logger;
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

    ByteBuffer byteBuffer;
    Buffer buffer;

    protected Logger logger;

    DictionaryBuilder() {
        logger = Logger.getLogger(this.getClass().getName());
        byteBuffer = ByteBuffer.allocate(BUFFER_SIZE);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer = byteBuffer; // a kludge for Java 9
    }

    void build(List<String> lexiconPaths, FileInputStream matrixInput, FileOutputStream output) throws IOException {
        logger.info("reading the source file...");
        for (String path : lexiconPaths) {
            try (FileInputStream lexiconInput = new FileInputStream(path)) {
                buildLexicon(path, lexiconInput);
            }
        }
        logger.info(() -> String.format(" %,d words%n", entries.size()));

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
                logger.severe("Error: " + e.getMessage() + " at line " + lineno + " in " + filename + "\n");
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
        logger.info("writing the POS table...");
        convertPOSTable(posTable.getList());
        buffer.flip();
        output.write(byteBuffer);
        printSize(byteBuffer.limit());
        buffer.clear();

        logger.info("writing the connection matrix...");
        if (matrixInput == null) {
            byteBuffer.putShort((short) 0);
            byteBuffer.putShort((short) 0);
            buffer.flip();
            output.write(byteBuffer);
            printSize(byteBuffer.limit());
            buffer.clear();
        } else {
            ByteBuffer matrix = convertMatrix(matrixInput);
            buffer.flip();
            output.write(byteBuffer);
            buffer.clear();
            output.write(matrix);
            printSize(matrix.limit() + 4L);
        }
    }

    void convertPOSTable(List<String> posList) {
        byteBuffer.putShort((short) posList.size());

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
        byteBuffer.putShort(leftSize);
        byteBuffer.putShort(rightSize);

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
                logger.warning("invalid format at line " + reader.getLineNumber());
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

        logger.info("building the trie");
        trie.build(keys, values, (n, s) -> {
            if (n % ((s / 10) + 1) == 0) {
                logger.info(".");
            }
        });
        logger.info("done\n");

        logger.info("writing the trie...");
        buffer.clear();
        byteBuffer.putInt(trie.size());
        buffer.flip();
        output.write(byteBuffer);
        buffer.clear();

        output.write(trie.byteArray());
        printSize(trie.size() * 4 + 4L);
        trie = null;

        logger.info("writing the word-ID table...");
        byteBuffer.putInt(wordIdTable.position());
        buffer.flip();
        output.write(byteBuffer);
        buffer.clear();

        ((Buffer) wordIdTable).flip(); // a kludge for Java 9
        output.write(wordIdTable);
        printSize(wordIdTable.position() + 4L);
        wordIdTable = null;

        logger.info("writing the word parameters...");
        byteBuffer.putInt(entries.size());
        for (WordEntry entry : entries) {
            byteBuffer.putShort(entry.parameters[0]);
            byteBuffer.putShort(entry.parameters[1]);
            byteBuffer.putShort(entry.parameters[2]);
            buffer.flip();
            output.write(byteBuffer);
            buffer.clear();
        }
        printSize(entries.size() * 6 + 4L);

        writeWordInfo(output);
    }

    void writeWordInfo(FileChannel output) throws IOException {
        long mark = output.position();
        output.position(mark + 4 * entries.size());

        ByteBuffer offsets = ByteBuffer.allocate(4 * entries.size());
        offsets.order(ByteOrder.LITTLE_ENDIAN);

        logger.info("writing the wordInfos...");
        long base = output.position();
        for (WordEntry entry : entries) {
            WordInfo wi = entry.wordInfo;
            offsets.putInt((int) output.position());

            writeString(wi.getSurface());
            writeStringLength(wi.getLength());
            byteBuffer.putShort(wi.getPOSId());
            if (wi.getNormalizedForm().equals(wi.getSurface())) {
                writeString("");
            } else {
                writeString(wi.getNormalizedForm());
            }
            byteBuffer.putInt(wi.getDictionaryFormWordId());
            if (wi.getReadingForm().equals(wi.getSurface())) {
                writeString("");
            } else {
                writeString(wi.getReadingForm());
            }
            writeIntArray(parseSplitInfo(entry.aUnitSplitString));
            writeIntArray(parseSplitInfo(entry.bUnitSplitString));
            writeIntArray(parseSplitInfo(entry.wordStructureString));
            buffer.flip();
            output.write(byteBuffer);
            buffer.clear();
        }
        printSize(output.position() - base);

        logger.info("writing wordInfo offsets...");
        output.position(mark);
        ((Buffer) offsets).flip(); // a kludge for Java 9
        output.write(offsets);
        printSize(offsets.position());
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
                if (ret[i] < 0) {
                    throw new IllegalArgumentException("not found such a word");
                }
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
        }
        checkWordId(id);
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

    void checkWordId(int wordId) {
        if (wordId < 0 || wordId >= entries.size()) {
            throw new IllegalArgumentException("invalid word ID");
        }
    }

    void writeString(String text) {
        writeStringLength((short) text.length());
        for (int i = 0; i < text.length(); i++) {
            byteBuffer.putChar(text.charAt(i));
        }
    }

    void writeStringLength(short length) {
        if (length <= Byte.MAX_VALUE) {
            byteBuffer.put((byte) length);
        } else {
            byteBuffer.put((byte) ((length >> 8) | 0x80));
            byteBuffer.put((byte) (length & 0xFF));
        }
    }

    void writeIntArray(int[] array) {
        byteBuffer.put((byte) array.length);
        for (int i : array) {
            byteBuffer.putInt(i);
        }
    }

    void printSize(long size) {
        logger.info(() -> String.format(" %,d bytes%n", size));
    }

    static void printUsage() {
        Console console = System.console();
        console.printf("usage: DictionaryBuilder -o file -m file [-d description] files...\n");
        console.printf("\t-o file\toutput to file\n");
        console.printf("\t-m file\tmatrix file\n");
        console.printf("\t-d description\tcomment\n");
    }

    static void readLoggerConfig() throws IOException {
        InputStream is = DictionaryBuilder.class.getResourceAsStream("/logger.properties");
        if (is != null) {
            LogManager.getLogManager().readConfiguration(is);
        }
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
        readLoggerConfig();

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
