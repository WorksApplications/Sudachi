package com.worksap.nlp.sudachi.dictionary;

import java.nio.charset.StandardCharsets;

public class WordInfo {

    private final String surface;
    private final short headwordLength;
    private final short posId;
    private final String normalizedForm;
    private final int dictionaryFormWordId;
    private final String dictionaryForm;
    private final String reading;
    private final int[] aUnitSplit;
    private final int[] bUnitSplit;
    private final int[] wordStructure;

    WordInfo(String surface,
             short headwordLength,
             short posId,
             String normalizedForm,
             int dictionaryFormWordId,
             String dictionaryForm,
             String reading,
             int[] aUnitSplit,
             int[] bUnitSplit,
             int[] wordStructure) {
        this.surface = surface;
        this.headwordLength = headwordLength;
        this.posId = posId;
        this.normalizedForm = normalizedForm;
        this.dictionaryFormWordId = dictionaryFormWordId;
        this.dictionaryForm = dictionaryForm;
        this.reading = reading;
        this.aUnitSplit = aUnitSplit;
        this.bUnitSplit = bUnitSplit;
        this.wordStructure = wordStructure;
    }

    public WordInfo(String surface,
             short posId,
             String normalizedForm,
             String dictionaryForm,
             String reading) {
        this.surface = surface;
        this.headwordLength = (short)surface.getBytes(StandardCharsets.UTF_8).length;
        this.posId = posId;
        this.normalizedForm = normalizedForm;
        this.dictionaryFormWordId = -1;
        this.dictionaryForm = dictionaryForm;
        this.reading = reading;
        this.aUnitSplit = new int[0];
        this.bUnitSplit = new int[0];
        this.wordStructure = new int[0];
    }

    public String getSurface() {
        return surface;
    }

    public short getLength() {
        return headwordLength;
    }

    public short getPOSId() {
        return posId;
    }
    
    public String getNormalizedForm() {
        return normalizedForm;
    }

    public int getDictionaryFormWordId() {
        return dictionaryFormWordId;
    }

    public String getDictionaryForm() {
        return dictionaryForm;
    }

    public String getReading() {
        return reading;
    }

    public int[] getAunitSplit() {
        return aUnitSplit;
    }

    public int[] getBunitSplit() {
        return bUnitSplit;
    }

    public int[] getWordStructure() {
        return wordStructure;
    }
}
