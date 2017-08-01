package com.worksap.nlp.sudachi;

import java.util.List;

public interface InputText<E> {

    public void replace(int begin, int end, String str)
        throws StringIndexOutOfBoundsException;

    public String getText();

    public String getOriginalText();

    public int getOriginalOffset(int offset)
        throws StringIndexOutOfBoundsException;
    
    public List<String> getCharCategoryNameList(int offset);
    
    public int getCharCategoryContinuousLength(int offset);


}
