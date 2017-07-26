package com.worksap.nlp.sudachi;

public interface Dictionary extends AutoCloseable {
    public Tokenizer create();
}
