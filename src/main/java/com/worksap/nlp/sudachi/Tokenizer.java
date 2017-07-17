package com.worksap.nlp.sudachi;

import java.util.List;
public interface Tokenizer {
    public List<Morpheme> tokenize(SplitMode mode, String text);

    public default List<Morpheme> tokenize(String text) {
        return tokenize(SplitMode.C, text);
    }

    public static enum SplitMode { A, B, C; }
}
