
package com.worksap.nlp.sudachi;

public interface InputTextBuilder<E> {
    
    public void replace(int begin, int end, String str)
        throws StringIndexOutOfBoundsException;
    
    public String getOriginalText();
    
    public String getText();
    
    public InputText<E> build();
}
