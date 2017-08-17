package com.worksap.nlp.sudachi;

import java.util.List;

import com.worksap.nlp.sudachi.dictionary.WordInfo;

class MorphemeImpl implements Morpheme {

    final MorphemeList list;
    final int index;
    WordInfo wordInfo;

    MorphemeImpl(MorphemeList list, int index) {
        this.list = list;
        this.index = index;
    }

    @Override
    public int begin() { return list.getBegin(index); }

    @Override
    public int end() { return list.getEnd(index); }

    @Override
    public String surface() {
        return list.getSurface(index);
    }

    @Override
    public List<String> partOfSpeech() {
        WordInfo wi = getWordInfo();
        return list.grammar.getPartOfSpeechString(wi.getPOSId());
    }

    @Override
    public String dictionaryForm() {
        WordInfo wi = getWordInfo();
        return wi.getDictionaryForm();
    }

    @Override
    public String normalizedForm() {
        WordInfo wi = getWordInfo();
        return wi.getNormalizedForm();
    }

    @Override
    public String readingForm() {
        WordInfo wi = getWordInfo();
        return wi.getReadingForm();
    }

    public List<Morpheme> split(Tokenizer.SplitMode mode) {
        WordInfo wi = getWordInfo();
        return list.split(mode, index, wi);
    }

    public boolean isOOV() {
        return list.isOOV(index);
    }
    
    WordInfo getWordInfo() {
        if (wordInfo == null)
            wordInfo = list.getWordInfo(index);
        return wordInfo;
    }

}
