package com.worksap.nlp.sudachi;

import java.io.IOException;

/**
 * Build a {@link Dictionary} instance from a dictionary file.
 */
public class DictionaryFactory {

    /**
     * Creates <tt>Dictionary</tt> by read a dictionary file.
     *
     * @param settings settings in JSON string
     * @return {@link Dictionary}
     * @throws IOException if reading a file is failed
     */
    public Dictionary create(String settings) throws IOException {
        return new JapaneseDictionary(settings);
    }
}
