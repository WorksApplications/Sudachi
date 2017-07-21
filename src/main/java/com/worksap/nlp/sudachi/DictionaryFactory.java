package com.worksap.nlp.sudachi;

import java.io.IOException;

public class DictionaryFactory {

    public Dictionary create(String settings) throws IOException {
        return new JapaneseDictionary(settings);
    }
}
