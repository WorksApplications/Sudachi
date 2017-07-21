package com.worksap.nlp.sudachi.dictionary;

import java.nio.ByteBuffer;

class WordInfoList {

    private final ByteBuffer bytes;
    private final int offset;

    WordInfoList(ByteBuffer bytes, int offset, int wordSize) {
        this.bytes = bytes;
        this.offset = offset;
    }

    WordInfo getWordInfo(int wordId) {
        int index = wordIdToOffset(wordId);
        
        String surface = bufferToString(index);
        index += 2 + 2 * surface.length();
        short posId = bytes.getShort(index);
        index += 2;
        String normalizedForm = bufferToString(index);
        index += 2 + 2 * normalizedForm.length();
        int dictionaryFormWordId = bytes.getInt(index);
        index += 4;
        String reading = bufferToString(index);
        index += 2 + 2 * reading.length();
        int[] aUnitSplit = bufferToIntArray(index);
        index += 2 + 4 * aUnitSplit.length;
        int[] bUnitSplit = bufferToIntArray(index);
        index += 2 + 4 * bUnitSplit.length;
        int[] wordStructure = bufferToIntArray(index);

        String dictionaryForm = surface;
        if (dictionaryFormWordId >= 0 && dictionaryFormWordId != wordId) {
            WordInfo wi = getWordInfo(dictionaryFormWordId);
            dictionaryForm = wi.getSurface();
        }

        return new WordInfo(surface, posId, normalizedForm,
                            dictionaryFormWordId, dictionaryForm, reading,
                            aUnitSplit, bUnitSplit, wordStructure);
    }

    private int wordIdToOffset(int wordId) {
        return bytes.getInt(offset + 4 * wordId);
    }

    private String bufferToString(int offset) {
        short length = bytes.getShort(offset);
        char[] str = new char[length];
        for (int i = 0; i < length; i++) {
            str[i] = bytes.getChar(offset + 2 + 2 * i);
        }
        return new String(str);
    }

    private int[] bufferToIntArray(int offset) {
        short length = bytes.getShort(offset);
        int[] array = new int[length];
        for (int i = 0; i < length; i++) {
            array[i] = bytes.getInt(offset + 2 + 4 * i);
        }
        return array;
    }
}
