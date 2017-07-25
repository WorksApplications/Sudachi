
package com.worksap.nlp.sudachi;

import java.io.IOException;
import java.text.Normalizer;
import java.text.Normalizer.Form;

public class DefaultInputTextPlugin implements InputTextPlugin {
    
    @Override
    public void setUp() throws IOException {
        //    nothing to do in this plug-in
    }
    
    @Override
    public void rewrite(InputText<?> text) {
        char ch;
        String substr;
        String originalText = text.getText();
        for (int i = 0; i < originalText.length(); i++) {
            ch = originalText.charAt(i);
            //    1. capital alphabet (not only latin but greek, cyril, etc) -> small
            //    2. normalize : full alphabet -> half / half katakana -> full
            if ((ch >= Character.MIN_LOW_SURROGATE) && (ch <= Character.MAX_LOW_SURROGATE)) {
                substr = originalText.substring(i, i + 2).toLowerCase();
                text.replace(i, i + 2, Normalizer.normalize(substr, Form.NFKC));
            }
            else {
                substr = originalText.substring(i, i + 1).toLowerCase();
                text.replace(i, i + 1, Normalizer.normalize(substr, Form.NFKC));
            }
        }
    }
}
