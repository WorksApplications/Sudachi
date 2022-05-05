package com.worksap.nlp.sudachi.dictionary.build;

import com.worksap.nlp.sudachi.dictionary.Ints;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;

public class WordInfoLayoutFixedWidth {
    private final StringIndex index;
    private final WordRef.Parser wordRefParser;
    private final Lookup2 lookup;
    private final ByteBuffer buffer = ByteBuffer.allocate(512 * 1024);
    private int position;
    private final Ints aSplits = new Ints(16);
    private final Ints bSplits = new Ints(16);
    private final Ints cSplits = new Ints(16);
    private final Ints wordStructure = new Ints(16);


    public WordInfoLayoutFixedWidth(Lookup2 resolver, StringIndex index, WordRef.Parser parser) {
        this.lookup = resolver;
        this.index = index;
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        wordRefParser = parser;
    }

    public void process(CsvLexicon.WordEntry entry) {

    }

    public int put(CsvLexicon.WordEntry entry) {
        int position = this.position + buffer.position();
        int entryPtr = position >>> 3;
        buffer.putShort(entry.leftId);
        buffer.putShort(entry.rightId);
        buffer.putShort(entry.cost);
        buffer.putShort(entry.wordInfo.getPOSId());
        // 8 bytes
        buffer.putInt(index.resolve(entry.wordInfo.getSurface()).encode()); // surfacePtr
        buffer.putInt(index.resolve(entry.wordInfo.getReadingForm()).encode()); // readingPtr
        int normFormPtr = wordRefParser.parse(entry.wordInfo.getNormalizedForm()).resolve(lookup);
        int dicFormPtr = wordRefParser.parse(entry.wordInfo.getDictionaryForm()).resolve(lookup);
        buffer.putInt(normFormPtr); // normalized entry
        buffer.putInt(dicFormPtr); // dictionary form
        // 8 + 16 = 24 bytes

        byte aSplitLen = parseList(entry.aUnitSplitString, aSplits);
        byte bSplitLen = parseList(entry.bUnitSplitString, bSplits);
        byte cSplitLen = parseList(entry.cUnitSplitString, cSplits);
        byte wordStructureLen = parseList(entry.wordStructureString, wordStructure);
        byte synonymLen = (byte) entry.wordInfo.getSynonymGroupIds().length;

        buffer.putShort(entry.surfaceUtf8Length);
        buffer.put(cSplitLen);
        buffer.put(bSplitLen);
        buffer.put(aSplitLen);
        buffer.put(wordStructureLen);
        buffer.put(synonymLen);
        int userDataLength = entry.userData.length();
        buffer.put(userDataLength != 0 ? (byte)0 : (byte)1);
        // 24 + 8 = 32 bytes

        putInts(cSplits, cSplitLen);
        putInts(bSplits, bSplitLen);
        putInts(aSplits, aSplitLen);
        putInts(wordStructure, wordStructureLen);
        putInts(Ints.wrap(entry.wordInfo.getSynonymGroupIds()), synonymLen);

        if (userDataLength != 0) {
            buffer.putShort((short) userDataLength);
            String userData = entry.userData;
            for (int i = 0; i < userDataLength; ++i) {
                buffer.putShort((short)userData.charAt(i));
            }
        }

        // align to 8 boundary
        int currentPosition = buffer.position();
        buffer.position(Align.align(currentPosition, 8));

        return entryPtr;
    }

    private void putInts(Ints ints, int len) {
        for (int i = 0; i < len; ++i) {
            buffer.putInt(ints.get(i));
        }
    }

    public void fillPointers(ByteBuffer data, List<CsvLexicon.WordEntry> entries, Lookup2 lookup) {
        for (int i = 0; i < entries.size(); i++) {
            CsvLexicon.WordEntry entry = entries.get(i);
            int offset = entry.pointer << 3;
            data.position(offset + 8);

            data.putInt(index.resolve(entry.wordInfo.getSurface()).encode());
            data.putInt(index.resolve(entry.wordInfo.getReadingForm()).encode());
            data.putInt(entry.wordInfo.getDictionaryFormWordId());
            //data.putInt(entry.)
        }
    }

    public <T> T consume(IOConsumer<T> consumer) throws IOException {
        position += buffer.position();
        buffer.flip();
        T result = consumer.accept(buffer);
        buffer.clear();
        return result;
    }

    byte parseList(String data, Ints result) {
        String[] parts = data.split("/");
        if (parts.length > Byte.MAX_VALUE) {
            throw new IllegalArgumentException("reference list contained more than 127 entries: " + data);
        }
        result.clear();
        for (String part : parts) {
            WordRef ref = wordRefParser.parse(part);
            result.append(ref.resolve(lookup));
        }
        return (byte) parts.length;
    }
}
