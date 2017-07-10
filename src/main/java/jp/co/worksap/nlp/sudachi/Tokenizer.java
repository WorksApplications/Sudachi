package jp.co.worksap.nlp.sudachi;

import java.util.List;
public interface Tokenizer {
    public List<Morpheme> tokenize(SplitMode mode, String text);
    public List<Morpheme> tokenize(String text);

    public static enum SplitMode { A, B, C; }
}
