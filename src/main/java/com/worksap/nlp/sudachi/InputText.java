package com.worksap.nlp.sudachi;

import java.util.List;
import java.util.Set;

import com.worksap.nlp.sudachi.dictionary.CategoryType;

public interface InputText<E> {
    
    public String getText();
    
    public String getOriginalText();
    
    public String getSubstring(int begin, int end);
    
    public int getOriginalOffset(int offset)
        throws StringIndexOutOfBoundsException;
    
    public Set<CategoryType> getCharCategoryTypes(int offset);

    public Set<CategoryType> getCharCategoryTypes(int begin, int end);
    
    public int getCharCategoryContinuousLength(int offset);
    
    public int getCodePointsOffsetLength(int offset, int codePointLength);
}
