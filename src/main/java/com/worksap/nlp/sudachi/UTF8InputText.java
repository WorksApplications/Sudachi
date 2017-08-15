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
        if (begin < 0)
            throw new StringIndexOutOfBoundsException(begin);
        if (end > bytes.length)
            throw new StringIndexOutOfBoundsException(end);
        if (begin > end)
            throw new StringIndexOutOfBoundsException(end - begin);

        return modifiedText.substring(byteIndexes[begin], byteIndexes[end]);
    }
    
    int getOffsetTextLength(int index) {
        return byteIndexes[index];
    }
    
    public boolean isCharAlignment(int index) {
        return (bytes[index] & 0xC0) != 0x80;
    }
    
    @Override
    public int getOriginalIndex(int index) {
        return offsets[index];
    }
    
    @Override
    public Set<CategoryType> getCharCategoryTypes(int index) {
        return charCategories.get(byteIndexes[index]);
    }
    
    @Override
    public Set<CategoryType> getCharCategoryTypes(int begin, int end) {
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
    public int getCharCategoryContinuousLength(int index) {
        return charCategoryContinuities.get(index);
    }
    
    @Override
    public int getCodePointsOffsetLength(int index, int codePointOffset) {
        int length = 0;
        int target = byteIndexes[index] + codePointOffset;
        for (int i = index; i < bytes.length; i++) {
            if (byteIndexes[i] >= target) {
                return length;
            }
            length++;
        }
        return length;
    }
}
