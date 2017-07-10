package jp.co.worksap.nlp.sudachi;

public interface Dictionary {
    public void close();
    public Tokenizer create();
}
