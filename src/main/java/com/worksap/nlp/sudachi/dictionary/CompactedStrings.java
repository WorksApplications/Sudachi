package com.worksap.nlp.sudachi.dictionary;

import java.nio.CharBuffer;

public class CompactedStrings {
    private final CharBuffer chars;

    public CompactedStrings(CharBuffer chars) {
        this.chars = chars;
    }

    public CharSequence sequence(int pointer) {
        CharBuffer dup = chars.duplicate();
        StringPtr ptr = StringPtr.decode(pointer);
        dup.position(ptr.getOffset());
        dup.limit(ptr.getOffset() + ptr.getLength());
        return dup;
    }

    public String string(int pointer) {
        return sequence(pointer).toString();
    }
}
