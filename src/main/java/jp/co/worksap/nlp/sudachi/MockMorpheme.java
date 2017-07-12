package jp.co.worksap.nlp.sudachi;

import java.util.List;

class MockMorpheme implements Morpheme {
    final MockMorphemeArray array;
    final int index;
    
    MockMorpheme(MockMorphemeArray array, int index) {
        this.array = array;
        this.index = index;
    }

    @Override
    public int begin() { return array.getBegin(index); }

    @Override
    public int end() { return array.getEnd(index); }

    @Override
    public String surface() { return array.getSurface(index); } 

    @Override
    public String[] partOfSpeech() { return array.getPartOfSpeech(index); }

    @Override
    public String dictionaryForm() { return array.getDictionaryForm(index); }

    @Override
    public String normalizedForm() { return array.getNormalizedForm(index); }

    @Override
    public String reading() { return array.getReading(index); }

    @Override
    public List<Morpheme> split(Tokenizer.SplitMode mode) {
        return array.split(index, mode);
    }

    @Override
    public boolean isOOV() { return array.isOOV(index); }
}
