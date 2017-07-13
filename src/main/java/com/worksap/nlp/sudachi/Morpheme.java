package com.worksap.nlp.sudachi;

import java.util.List;

public interface Morpheme {
    public int begin();
    public int end();
    public String surface();
    public String[] partOfSpeech();
    public String dictionaryForm();
    public String normalizedForm();
    public String reading();
    public List<Morpheme> split(Tokenizer.SplitMode mode);
    public boolean isOOV();
}
