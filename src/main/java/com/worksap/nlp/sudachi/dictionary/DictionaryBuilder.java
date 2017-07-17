package com.worksap.nlp.sudachi.dictionary;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.worksap.nlp.dartsclone.DoubleArray;

public class DictionaryBuilder {

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
    List<String> trieKeys = new ArrayList<>();
    List<Short[]> params = new ArrayList<>();
    List<WordInfo> wordInfos = new ArrayList<>();

    ByteBuffer buffer;

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
        BufferedReader reader
            = new BufferedReader(new InputStreamReader(lexiconInput));

        for (int wordId = 0; ; wordId++) {
            String line = reader.readLine();
            if (line == null) {
                break;
            }

            String[] cols = line.split("\\s*,\\s*");
            if (cols.length != NUMBER_OF_COLUMNS) {
                System.err.println("Error: " + line);
                continue;
            }
            for (int i = 0; i < cols.length; i++) {
                cols[i] = decode(cols[i]);
            }

            // headword
            trieKeys.add(cols[0]);
            // left-id, right-id, cost
            params.add(new Short[] { Short.parseShort(cols[1]),
                                     Short.parseShort(cols[2]),
                                     Short.parseShort(cols[3]) });

            short posId = posTable.getId(String.join(",",
                                                     cols[5], cols[6], cols[7],
                                                     cols[8], cols[9], cols[10]));

            WordInfo info
                = new WordInfo(cols[4], // headword
                               posId,
                               cols[12], // normalizedForm
                               (cols[13].equals("*") ? -1 :Integer.parseInt(cols[13])), // dictionaryFormWordId
                               "", // dummy
                               cols[11], // reading
                               parseSplitInfo(cols[15]), // aUnitSplit
                               parseSplitInfo(cols[16]), // bUnitSplit
                               parseSplitInfo(cols[17]) // wordStructure
                               );
        }

    }

    void writeGrammar(FileInputStream matrixInput,
                      FileChannel output) throws IOException {
        List<String> posList = posTable.getList();
        buffer.putShort((short)posList.size());

        for (String pos : posList) {
            for (String text : pos.split(",")) {
                writeString(text);
            }
        }
        buffer.flip();
        output.write(buffer);
        buffer.clear();


        BufferedReader reader
            = new BufferedReader(new InputStreamReader(matrixInput));

        String[] lr = reader.readLine().split("\\s+");
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
            String[] cols = line.split("\\s+");
            short left = Short.parseShort(cols[0]);
            short right = Short.parseShort(cols[1]);
            short cost = Short.parseShort(cols[2]);
            matrix.putShort(2 * (left + leftSize * right), cost);
        }
        output.write(matrix);
        matrix = null;
    }

    void writeLexicon(FileChannel output) throws IOException {
        DoubleArray trie = new DoubleArray();
        byte[][] keys = new byte[trieKeys.size()][];
        int[] values = new int[trieKeys.size()];
        for (int i = 0; i < trieKeys.size(); i++) {
            keys[i] = trieKeys.get(i).getBytes(StandardCharsets.UTF_8);
            values[i] = i;
        }
        trie.build(keys, values, null);

        buffer.clear();
        buffer.putInt(trie.size());
        buffer.flip();
        output.write(buffer);
        buffer.clear();

        output.write(trie.byteArray());
        trie = null;


        for(Short[] param : params) {
            buffer.putShort(param[0]);
            buffer.putShort(param[1]);
            buffer.putShort(param[2]);
            buffer.flip();
            output.write(buffer);
            buffer.clear();
        }


        writeWordInfo(output);
    }

    void writeWordInfo(FileChannel output) throws IOException {
        long mark = output.position();
        output.position(mark + 4 * wordInfos.size());
        
        ByteBuffer offsets = ByteBuffer.allocate(4 * wordInfos.size());
        offsets.order(ByteOrder.LITTLE_ENDIAN);

        for (WordInfo wi : wordInfos) {
            offsets.putInt((int)output.position());

            writeString(wi.getSurface());
            buffer.putShort(wi.getPOSId());
            writeString(wi.getNormalizedForm());
            buffer.putInt(wi.getDictionaryFormWordId());
            writeString(wi.getReading());
            writeIntArray(wi.getAunitSplit());
            writeIntArray(wi.getBunitSplit());
            writeIntArray(wi.getWordStructure());
            buffer.flip();
            output.write(buffer);
            buffer.clear();
        }

        output.position(mark);
        offsets.flip();
        output.write(offsets);
    }

    static final Pattern unicodeLiteral = Pattern.compile("\\\\u([0-9a-fA-F]+)");
    static String decode(String text) {
        Matcher m = unicodeLiteral.matcher(text);
        if (!m.find()) {
            return text;
        }

        StringBuffer sb = new StringBuffer();
        m.reset();
        while (m.find()) {
            m.appendReplacement(sb, new String(Character.toChars(Integer.parseInt(m.group(1), 16))));
        }
        m.appendTail(sb);
        return sb.toString();
    }

    static int[] parseSplitInfo(String info) {
        if (info.equals("*")) {
            return new int[0];
        }
        String[] ids = info.split("/");
        int[] ret = new int[ids.length];
        for (int i = 0; i < ids.length; i++) {
            ret[i] = Integer.parseInt(ids[i]);
        }
        return ret;
    }

    void writeString(String text) {
        buffer.putShort((short)text.length());
        for (int i = 0; i < text.length(); i++) {
            buffer.putChar(text.charAt(i));
        }
    }

    void writeIntArray(int[] array) {
        buffer.putShort((short)array.length);
        for (int i : array) {
            buffer.putInt(i);
        }
    }

    public static void main(String[] args) throws IOException {
        FileInputStream lexiconInput = new FileInputStream(args[0]);
        FileInputStream matrixInput = new FileInputStream(args[1]);
        FileOutputStream output = new FileOutputStream(args[2]);

        DictionaryBuilder builder = new DictionaryBuilder();
        builder.build(lexiconInput, matrixInput, output);
    }
}
