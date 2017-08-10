package com.worksap.nlp.sudachi;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.worksap.nlp.sudachi.dictionary.CategoryType;
import com.worksap.nlp.sudachi.dictionary.CategoryTypeSet;
import com.worksap.nlp.sudachi.dictionary.Grammar;

class UTF8InputText implements InputText<byte[]> {
    
    private final String originalText;
    private final String modifiedText;
    private final byte[] bytes;
    private final int[] offsets;
    private final int[] byteIndexes;
    private final List<Set<CategoryType>> charCategories;
    private final List<Integer> charCategoryContinuities;
    
    UTF8InputText(Grammar grammar, String originalText, String modifiedText,
        byte[] bytes, int[] offsets, int[] byteIndexes,
        List<Set<CategoryType>> charCategories, List<Integer> charCategoryContinuities) {
        
        this.originalText = originalText;
        this.modifiedText = modifiedText;
        this.bytes = bytes;
        this.offsets = offsets;
        this.byteIndexes = byteIndexes;
        this.charCategories = charCategories;
        this.charCategoryContinuities = charCategoryContinuities;
    }
    
    @Override
    public String getOriginalText() {
        return originalText;
    }
    
    @Override
    public String getText() {
        return modifiedText;
    }
    
    byte[] getByteText() {
        return bytes;
    }
    
    @Override
    public String getSubstring(int begin, int end)
        throws StringIndexOutOfBoundsException {
        return modifiedText.substring(byteIndexes[begin], byteIndexes[end]);
    }
    
    int getOffsetTextLength(int offset)
        throws IndexOutOfBoundsException {
        return byteIndexes[offset];
    }
    
    public boolean isCharAlignment(int offset) {
        return (bytes[offset] & 0xC0) != 0x80;
    }
    
    @Override
    public int getOriginalOffset(int offset)
        throws IndexOutOfBoundsException {
        return offsets[offset];
    }
    
    @Override
    public Set<CategoryType> getCharCategoryTypes(int offset)
        throws IndexOutOfBoundsException {
        return charCategories.get(byteIndexes[offset]);
    }
    
    @Override
    public Set<CategoryType> getCharCategoryTypes(int begin, int end)
        throws IndexOutOfBoundsException {
        if (begin + getCharCategoryContinuousLength(begin) < end) {
            return Collections.emptySet();
        }
        int b = byteIndexes[begin];
        int e = byteIndexes[end];
        Set<CategoryType> continuousCategory
            = ((CategoryTypeSet)charCategories.get(b)).clone();
        for (int i = b + 1; i < e; i++) {
            continuousCategory.retainAll(charCategories.get(i));
        }
        return continuousCategory;
    }

    @Override
    public int getCharCategoryContinuousLength(int offset)
        throws IndexOutOfBoundsException {
        return charCategoryContinuities.get(offset);
    }
    
    @Override
    public int getCodePointsOffsetLength(int offset, int codePointLength)
        throws IndexOutOfBoundsException {
        int length = 0;
        int target = byteIndexes[offset] + codePointLength;
        for (int i = offset; i < bytes.length; i++) {
            if (byteIndexes[i] >= target) {
                return length;
            }
            length++;
        }
        return length;
    }
    
}
