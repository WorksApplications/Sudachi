package com.worksap.nlp.sudachi;

public interface InputText<E> {

    public void replace(int begin, int end, String str)
        throws StringIndexOutOfBoundsException;

    public String getText();

    public String getOriginalText();

    public int getOriginalOffset(int offset)
        throws StringIndexOutOfBoundsException;
}
