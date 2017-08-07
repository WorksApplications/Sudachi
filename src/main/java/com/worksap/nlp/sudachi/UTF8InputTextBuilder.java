
package com.worksap.nlp.sudachi;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.worksap.nlp.sudachi.dictionary.Grammar;

public class UTF8InputTextBuilder implements InputTextBuilder<byte[]> {

    private final String originalText;
    private String modifiedText;
    private List<Byte> utf8TextBytes;
    private List<Integer> textOffsets;
    private List<Integer> byteIndexes;
    
    private byte[] byteCache;
    private Grammar grammar;
    
    public UTF8InputTextBuilder(String text, Grammar grammar) {
        this.grammar = grammar;
        
        originalText = text;
        modifiedText = text;
        byteCache = text.getBytes(StandardCharsets.UTF_8);
        utf8TextBytes = new ArrayList<>(byteCache.length);
        for (byte b : byteCache) {
            utf8TextBytes.add(b);
        }
        textOffsets = new ArrayList<>(byteCache.length + 1);
        byteIndexes = new ArrayList<>(byteCache.length + 1);
        for (int i = 0; i < originalText.length(); i++) {
            if (isLowSurrogateChar(originalText.charAt(i))) {
                continue;
            }
            for (int j = 0; j < utf8ByteLength(originalText.codePointAt(i)); j++) {
                textOffsets.add(i);
                byteIndexes.add(i);
            }
        }
        textOffsets.add(originalText.length());
        byteIndexes.add(originalText.length());
    }
    
    @Override
    public void replace(int begin, int end, String str)
        throws StringIndexOutOfBoundsException {
        if (begin < 0)
            throw new StringIndexOutOfBoundsException(begin);
        if (begin > getText().length())
            throw new StringIndexOutOfBoundsException("begin > length()");
        if (begin > end)
            throw new StringIndexOutOfBoundsException("begin > end");
        if (begin == end)
            throw new IllegalArgumentException("begin == end");
        
        if (end > getText().length()) {
            end = getText().length();
        }
        
        int offsetBegin = byteIndexes.indexOf(begin);
        int offsetEnd = byteIndexes.indexOf(end);
        if (offsetBegin < 0 || offsetEnd < 0) {
            // character has been removed
            return;
        }
        replaceTextBytesAndOffsets(offsetBegin, offsetEnd, str);
        byteCache = null;
        replaceByteIndexes();
    }
    
    private void replaceTextBytesAndOffsets(int begin, int end, String str) {
        byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
        int length = bytes.length;
        int textOffset = textOffsets.get(begin);
        if (end - begin > length) {
            utf8TextBytes.subList(begin + length, end).clear();
            textOffsets.subList(begin + length, end).clear();
        }
        for (int i = 0; i < length; i++) {
            if (begin + i < end) {
                utf8TextBytes.set(begin + i, bytes[i]);
                textOffsets.set(begin + i, textOffset);
            }
            else {
                utf8TextBytes.add(begin + i, bytes[i]);
                textOffsets.add(begin + i, textOffset);
            }
        }
    }
    
    private void replaceByteIndexes() {
        byteIndexes.clear();
        for (int i = 0; i < getText().length(); i++) {
            if (isLowSurrogateChar(getText().charAt(i))) {
                continue;
            }
            for (int j = 0; j < utf8ByteLength(getText().codePointAt(i)); j++) {
                byteIndexes.add(i);
            }
        }
        byteIndexes.add(getText().length());
    }
    
    @Override
    public String getOriginalText() {
        return originalText;
    }
    
    @Override
    public String getText() {
        if (byteCache == null) {
            modifiedText = new String(getByteText(), StandardCharsets.UTF_8);
        }
        return modifiedText;
    }
    
    @Override
    public UTF8InputText build() {
        List<List<String>> charCategories = getCharCategoryNames();
        List<Integer> charCategoryContinuities = getCharCategoryContinuities(charCategories);
        return new UTF8InputText(grammar, originalText, modifiedText, getByteText(),
            Collections.unmodifiableList(textOffsets),
            Collections.unmodifiableList(byteIndexes),
            Collections.unmodifiableList(charCategories),
            Collections.unmodifiableList(charCategoryContinuities));
    }
    
    private List<List<String>> getCharCategoryNames() {
        List<List<String>> charCategoryNames = new ArrayList<>(getText().length());
        for (int i = 0; i < getText().length(); i++) {
            charCategoryNames.add(Collections.unmodifiableList(
                grammar.getCharacterCategory().getCategoryNames(getText().codePointAt(i))));
        }
        return charCategoryNames;
    }
    
    private List<Integer> getCharCategoryContinuities(List<List<String>> charCategories) {
        if (charCategories == null || charCategories.isEmpty()) {
            return Collections.emptyList();
        }
        List<Integer> charCategoryContinuities = new ArrayList<>(getText().length());
        for (int offset = 0; offset < getByteText().length; offset++) {
            charCategoryContinuities.add(getCharCategoryContinuousLength(charCategories, offset));
        }
        return charCategoryContinuities;
    }
    
    private int getCharCategoryContinuousLength(List<List<String>> charCategories, int offset) {
        int length;
        for (length = 0; length < byteCache.length - offset; length++) {
            boolean found = false;
            // compare each category at offset w/ each category at target (offset + length)
            List<String> offsetCharCategory = charCategories.get(byteIndexes.get(offset));
            List<String> targetCharCategory = charCategories.get(byteIndexes.get(offset + length));
            for (int i = 0; i < offsetCharCategory.size(); i++) {
                for (int j = 0; j < targetCharCategory.size(); j++) {
                    if (offsetCharCategory.get(i).equals(targetCharCategory.get(j))) {
                        found = true; 
                    }
                }
            }
            if (!found) {
                return length;
            }
        }
        return length;
    }
    
    private byte[] getByteText() {
        if (byteCache == null) {
            byteCache = new byte[utf8TextBytes.size()];
            for (int i = 0; i < utf8TextBytes.size(); i++) {
                byteCache[i] = utf8TextBytes.get(i);
            }
        }
        return byteCache;
    }
    
    private boolean isLowSurrogateChar(char ch) {
        if (ch >= Character.MIN_LOW_SURROGATE) {
            if (ch <= Character.MAX_LOW_SURROGATE) {
                return true;
            }
        }
        return false;
    }
    
    private int utf8ByteLength(int cp) {
        if (cp < 0) {
            return 0;
        } else if (cp <= 0x7F) {
            return 1;
        } else if (cp <= 0x7FF) {
            return 2;
        } else if (cp <= 0xFFFF) {
            return 3;
        } else if (cp <= 0x10FFFF) {
            return 4;
        } else {
            return 0;
        }
    }
}
