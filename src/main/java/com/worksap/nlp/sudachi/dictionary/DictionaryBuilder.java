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
import java.util.TreeMap;
import java.util.List;
import java.util.SortedMap;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.worksap.nlp.dartsclone.DoubleArray;

/**
 * A dictionary building tool. This class provide the converter
 * from the source file in the CSV format to the binary format.
 */
public class DictionaryBuilder {

    static final int MAX_LENGTH = 255;
    static final int NUMBER_OF_COLUMNS = 18;
    static final int BUFFER_SIZE = 1024 * 1024;

    static class POSTable {
        private List<String> table = new ArrayList<>();

        short getId(String s) {
            int id = table.indexOf(s);
            if (id < 0) {
                id = table.size();
                table.add(s);
            }
            return (short)id;
        }

        List<String> getList() { return table; };
    }

    POSTable posTable = new POSTable();
    SortedMap<byte[], List<Integer>> trieKeys
        = new TreeMap<>(new Comparator<byte[]>() {
                @Override
                public int compare(byte[] l, byte[] r) {
                    int llen = l.length;
                    int rlen = r.length;
                    for (int i = 0; i < Math.min(llen, rlen); i++) {
                        if (l[i] != r[i]) {
                            return (l[i] & 0xff) - (r[i] & 0xff);
                        }
                    }
                    return l.length - r.length;
                }
            });
    List<Short[]> params = new ArrayList<>();
    List<WordInfo> wordInfos = new ArrayList<>();

    ByteBuffer buffer;
    int wordSize;

    DictionaryBuilder() {
        buffer = ByteBuffer.allocate(BUFFER_SIZE);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
    }

    void build(FileInputStream lexiconInput, FileInputStream matrixInput,
               FileOutputStream output) throws IOException {
        buildLexicon(lexiconInput);
        lexiconInput.close();
        
        FileChannel outputChannel = output.getChannel();
        writeGrammar(matrixInput, outputChannel);
        writeLexicon(outputChannel);
        outputChannel.close();
    }

    void buildLexicon(FileInputStream lexiconInput) throws IOException {
        int lineno = -1;
        try (InputStreamReader isr = new InputStreamReader(lexiconInput);
             LineNumberReader reader = new LineNumberReader(isr)) {

            System.err.print("reading the source file...");
            int wordId = 0;
            for (; ; wordId++) {
                String line = reader.readLine();
                if (line == null) {
                    break;
                }
                lineno = reader.getLineNumber();

                String[] cols = line.split(",");
                if (cols.length != NUMBER_OF_COLUMNS) {
                    System.err.println("Error: invalid format at line " + lineno);
                    continue;
                }
                for (int i = 0; i < cols.length; i++) {
                    cols[i] = decode(cols[i]);
                }

                if (cols[0].length() > MAX_LENGTH
                    || cols[4].length() > MAX_LENGTH
                    || cols[11].length() > MAX_LENGTH
                    || cols[12].length() > MAX_LENGTH) {
                    System.err.println("Error: string is too long at line" + lineno);
                }

                if (cols[0].length() == 0) {
                    System.err.println("Error: headword is empty at line " + lineno);
                    continue;
                }
                if (!cols[1].equals("-1")) {
                    // headword
                    byte[] key = cols[0].getBytes(StandardCharsets.UTF_8);
                    if (!trieKeys.containsKey(key)) {
                        trieKeys.put(key, new ArrayList<Integer>());
                    }
                    trieKeys.get(key).add(wordId);
                    // left-id, right-id, cost
                }
                params.add(new Short[] { Short.parseShort(cols[1]),
                                         Short.parseShort(cols[2]),
                                         Short.parseShort(cols[3]) });

                short posId = getPosId(cols[5], cols[6], cols[7],
                                       cols[8], cols[9], cols[10]);
                if (posId < 0) {
                    System.err.println("Error: Part of speech is wrong at line  "
                                       + lineno);
                }

                WordInfo info
                    = new WordInfo(cols[4], // headword
                                   (short)cols[0].getBytes(StandardCharsets.UTF_8).length,
                                   posId,
                                   cols[12], // normalizedForm
                                   (cols[13].equals("*") ? -1 :Integer.parseInt(cols[13])), // dictionaryFormWordId
                                   "", // dummy
                                   cols[11], // readingForm
                                   parseSplitInfo(cols[15]), // aUnitSplit
                                   parseSplitInfo(cols[16]), // bUnitSplit
                                   parseSplitInfo(cols[17]) // wordStructure
                                   );
                wordInfos.add(info);
            }
            wordSize = wordId;
        } catch (Exception e) {
            if (lineno > 0) {
                System.err.println("Error: at line " + lineno);
            }
            throw e;
        }
        System.err.println(String.format(" %,d words", wordSize));
    }

    short getPosId(String... posStrings) {
        return posTable.getId(String.join(",", posStrings));
    }

    void writeGrammar(FileInputStream matrixInput,
                      FileChannel output) throws IOException {

        System.err.print("writing the POS table...");

        List<String> posList = posTable.getList();
        buffer.putShort((short)posList.size());

        for (String pos : posList) {
            for (String text : pos.split(",")) {
                writeString(text);
            }
        }
        buffer.flip();
        output.write(buffer);
        System.err.println(String.format(" %,d bytes", buffer.limit()));
        buffer.clear();

        LineNumberReader reader
            = new LineNumberReader(new InputStreamReader(matrixInput));
        String header = reader.readLine();
        if (header == null) {
            throw new RuntimeException("invalid format at line " + reader.getLineNumber());
        }

        System.err.print("writing the connection matrix...");

        String[] lr = header.split("\\s+");
        short leftSize = Short.parseShort(lr[0]);
        short rightSize = Short.parseShort(lr[1]);

        buffer.putShort(leftSize);
        buffer.putShort(rightSize);
        buffer.flip();
        output.write(buffer);
        buffer.clear();

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
                System.err.println("invalid format at line "
                                   + reader.getLineNumber());
                continue;
            }
            short left = Short.parseShort(cols[0]);
            short right = Short.parseShort(cols[1]);
            short cost = Short.parseShort(cols[2]);
            matrix.putShort(2 * (left + leftSize * right), cost);
        }
        output.write(matrix);
        System.err.println(String.format(" %,d bytes", matrix.limit() + 4));
        matrix = null;
    }

    void writeLexicon(FileChannel output) throws IOException {
        DoubleArray trie = new DoubleArray();

        int size = trieKeys.size();

        byte[][] keys = new byte[size][];
        int[] values = new int[size];
        ByteBuffer wordIdTable = ByteBuffer.allocate(wordSize * (4 + 2));
        wordIdTable.order(ByteOrder.LITTLE_ENDIAN);

        int i = 0;
        for (byte[] key : trieKeys.keySet()) {
            keys[i] = key;
            values[i] = wordIdTable.position();
            i++;
            List<Integer> wordIds = trieKeys.get(key);
            wordIdTable.put((byte)wordIds.size());
            for (int wordId : wordIds) {
                wordIdTable.putInt(wordId);
            }
        }

        System.err.print("building the trie");
        trie.build(keys, values,
                   (n, s) -> { if (n % ((s / 10) + 1) == 0) System.err.print(".");});
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
        buffer.putInt(params.size());
        for(Short[] param : params) {
            buffer.putShort(param[0]);
            buffer.putShort(param[1]);
            buffer.putShort(param[2]);
            buffer.flip();
            output.write(buffer);
            buffer.clear();
        }
        System.err.println(String.format(" %,d bytes", params.size() * 6 + 4));

        writeWordInfo(output);
    }

    void writeWordInfo(FileChannel output) throws IOException {
        long mark = output.position();
        output.position(mark + 4 * wordInfos.size());
        
        ByteBuffer offsets = ByteBuffer.allocate(4 * wordInfos.size());
        offsets.order(ByteOrder.LITTLE_ENDIAN);

        System.err.print("writing the wordInfos...");
        long base = output.position();
        for (WordInfo wi : wordInfos) {
            offsets.putInt((int)output.position());

            writeString(wi.getSurface());
            buffer.put((byte)wi.getLength());
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
            writeIntArray(wi.getAunitSplit());
            writeIntArray(wi.getBunitSplit());
            writeIntArray(wi.getWordStructure());
            buffer.flip();
            output.write(buffer);
            buffer.clear();
        }
        System.err.println(String.format(" %,d bytes", output.position() - base));

        System.err.print("writing wordInfo offsets...");
        output.position(mark);
        offsets.flip();
        output.write(offsets);
        System.err.println(String.format(" %,d bytes", offsets.position() + 4));
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

    static int[] parseSplitInfo(String info) {
        if (info.equals("*")) {
            return new int[0];
        }
        String[] ids = info.split("/");
        if (ids.length > MAX_LENGTH) {
            throw new IllegalArgumentException("too many units");
        }
        int[] ret = new int[ids.length];
        for (int i = 0; i < ids.length; i++) {
            ret[i] = Integer.parseInt(ids[i]);
        }
        return ret;
    }

    void writeString(String text) {
        buffer.put((byte)text.length());
        for (int i = 0; i < text.length(); i++) {
            buffer.putChar(text.charAt(i));
        }
    }

    void writeIntArray(int[] array) {
        buffer.put((byte)array.length);
        for (int i : array) {
            buffer.putInt(i);
        }
    }

    /**
     * Builds the system dictionary.
     *
     * This tool requires three arguments.
     * <ol start="0">
     * <li>the path of the source file in the CSV format</li>
     * <li>the path of the connection matrix file
     *     in MeCab's matrix.def format</li>
     * <li>the path of the output file</li>
     * </ol>
     * @param args the input filename, the connection matrix file,
     * and the output filename
     * @throws IOException if IO or parsing is failed
     */
    public static void main(String[] args) throws IOException {
        if (args.length != 3) {
            System.err.println("usage: DictionaryBuilder input.csv matrix.def output.dic");
            return;
        }

        try (FileInputStream lexiconInput = new FileInputStream(args[0]);
             FileInputStream matrixInput = new FileInputStream(args[1]);
             FileOutputStream output = new FileOutputStream(args[2])) {

            DictionaryBuilder builder = new DictionaryBuilder();
            builder.build(lexiconInput, matrixInput, output);
        }
    }
}
