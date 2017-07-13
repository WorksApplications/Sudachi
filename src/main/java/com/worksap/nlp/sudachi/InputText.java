package com.worksap.nlp.sudachi;

public interface InputText<E> {

    public char originalCharAt(int index)
        throws StringIndexOutOfBoundsException;
    public int originalLength();

    public void replace(int begin, int end, String str)
        throws StringIndexOutOfBoundsException;

    public String getOriginalText();
    public E getText();

    public int getOriginalOffset(int offset)
        throws StringIndexOutOfBoundsException;
}
