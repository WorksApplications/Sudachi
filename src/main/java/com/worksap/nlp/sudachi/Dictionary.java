package com.worksap.nlp.sudachi;

/**
 * A lexicon and a grammar for morphological analysis.
 *
 * This class requires a lot of memory.
 * When using multiple analyzers, it is recommended to generate only one
 * instance of this class, and generate multiple tokenizers.
 *
 * @see DictionaryFactory
 * @see Tokenizer
 * @see AutoCloseable
 */
public interface Dictionary extends AutoCloseable {

    /**
     * Creates a tokenizer instance.
     *
     * @return a tokenizer
     */
    public Tokenizer create();
}
