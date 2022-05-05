package com.worksap.nlp.sudachi.dictionary.build;

import com.worksap.nlp.sudachi.dictionary.StringPtr;

@FunctionalInterface
public interface StringIndex {
    /**
     * Produces a StringPtr for a String.
     * @param data given String
     * @return StringPtr
     */
    StringPtr resolve(String data);
}
