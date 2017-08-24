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
        index += 1 + 2 * surface.length();
        short headwordLength = (short)Byte.toUnsignedInt(bytes.get(index));
        index += 1;
        short posId = bytes.getShort(index);
        index += 2;
        String normalizedForm = bufferToString(index);
        index += 1 + 2 * normalizedForm.length();
        if (normalizedForm.isEmpty()) {
            normalizedForm = surface;
        }
        int dictionaryFormWordId = bytes.getInt(index);
        index += 4;
        String readingForm = bufferToString(index);
        index += 1 + 2 * readingForm.length();
        if (readingForm.isEmpty()) {
            readingForm = surface;
        }
        int[] aUnitSplit = bufferToIntArray(index);
        index += 1 + 4 * aUnitSplit.length;
        int[] bUnitSplit = bufferToIntArray(index);
        index += 1 + 4 * bUnitSplit.length;
        int[] wordStructure = bufferToIntArray(index);

        String dictionaryForm = surface;
        if (dictionaryFormWordId >= 0 && dictionaryFormWordId != wordId) {
            WordInfo wi = getWordInfo(dictionaryFormWordId);
            dictionaryForm = wi.getSurface();
        }

        return new WordInfo(surface, headwordLength, posId, normalizedForm,
                            dictionaryFormWordId, dictionaryForm, readingForm,
                            aUnitSplit, bUnitSplit, wordStructure);
    }

    private int wordIdToOffset(int wordId) {
        return bytes.getInt(offset + 4 * wordId);
    }

    private String bufferToString(int offset) {
        int length = Byte.toUnsignedInt(bytes.get(offset++));
        char[] str = new char[length];
        for (int i = 0; i < length; i++) {
            str[i] = bytes.getChar(offset + 2 * i);
        }
        return new String(str);
    }

    private int[] bufferToIntArray(int offset) {
        int length = Byte.toUnsignedInt(bytes.get(offset++));
        int[] array = new int[length];
        for (int i = 0; i < length; i++) {
            array[i] = bytes.getInt(offset + 4 * i);
        }
        return array;
    }
}
