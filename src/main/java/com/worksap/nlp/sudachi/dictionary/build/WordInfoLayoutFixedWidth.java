package com.worksap.nlp.sudachi.dictionary.build;

import com.worksap.nlp.sudachi.dictionary.Ints;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class WordInfoLayoutFixedWidth {
    private final WordIdResolver resolver;
    private final ByteBuffer buffer = ByteBuffer.allocate(4 * 1024 * 1024);
    private int position;
    private Ints aSplits = new Ints(16);
    private Ints bSplits = new Ints(16);
    private Ints cSplits = new Ints(16);
    private Ints wordStructure = new Ints(16);
    private Ints wordOffsets = new Ints(0);


    public WordInfoLayoutFixedWidth(WordIdResolver resolver) {
        this.resolver = resolver;
        buffer.order(ByteOrder.LITTLE_ENDIAN);
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
        buffer.putInt(0); // surfacePtr
        buffer.putInt(0); // readingPtr
        buffer.putInt(entryPtr); // write normalized entry pointer in second pass
        buffer.putInt(entryPtr); // write dictionary form entry pointer in second pass
        // 8 + 16 = 24 bytes

        byte aSplitLen = resolver.parseList(entry.aUnitSplitString, aSplits);
        byte bSplitLen = resolver.parseList(entry.bUnitSplitString, bSplits);
        byte cSplitLen = resolver.parseList(entry.cUnitSplitString, cSplits);
        byte wordStructureLen = resolver.parseList(entry.wordStructureString, wordStructure);
        byte synonymLen = (byte) entry.wordInfo.getSynonymGoupIds().length;

        buffer.putShort(entry.surfaceUtf8Length);
        buffer.put(entry.userData.length() != 0 ? (byte)0 : (byte)1);
        buffer.put(synonymLen);
        buffer.put(cSplitLen);
        buffer.put(bSplitLen);
        buffer.put(aSplitLen);
        buffer.put(wordStructureLen);
        // 24 + 8 = 32 bytes

        // align to 8 boundary
        int currentPosition = buffer.position();
        if ((currentPosition & 0x7) != 0) {
            buffer.position((currentPosition & 0xffff_fff8) + 8);
        }

        return entryPtr;
    }

    public <T> T consume(IOConsumer<T> consumer) throws IOException {
        position += buffer.position();
        buffer.flip();
        T result = consumer.accept(buffer);
        buffer.clear();
        return result;
    }
}
