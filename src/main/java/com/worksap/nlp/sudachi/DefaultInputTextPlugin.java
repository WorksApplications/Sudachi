
package com.worksap.nlp.sudachi;

import java.io.IOException;
import java.text.Normalizer;
import java.text.Normalizer.Form;

public class DefaultInputTextPlugin implements InputTextPlugin {
    
    private static final char[][] ignoreNormalizeBounds = {
        {'Ⅰ', 'ⅿ'},
        {'⺀', '⿕'},
        {'豈', '龎'}
    };
    private static final char[][] joinedCharBounds = {
        {'ﾞ', 'ｳ', 'ｳ'},
        {'ﾞ', 'ｶ', 'ﾄ'},
        {'ﾞ', 'ﾊ', 'ﾎ'},
        {'ﾟ', 'ﾊ', 'ﾎ'},
        {'ﾞ', 'う', 'う'},
        {'ﾞ', 'か', 'と'},
        {'ﾞ', 'は', 'ほ'},
        {'ﾟ', 'は', 'ほ'},
        {'゙', 'う', 'う'},
        {'゙', 'か', 'と'},
        {'゙', 'は', 'ほ'},
        {'゚', 'は', 'ほ'}
    };
    
    @Override
    public void setUp() throws IOException {
        // nothing to do in this plug-in
        // or may be better if reads char[][] lists as an external file here
    }
    
    @Override
    public void rewrite(InputText<?> text) {
        int charLength;
        char ch;
        String substr;
        String originalText = text.getText();
        for (int i = 0; i < originalText.length(); i++) {
            ch = originalText.charAt(i);
            // check if surrogate pair
            if ((ch >= Character.MIN_HIGH_SURROGATE) && (ch <= Character.MAX_HIGH_SURROGATE)) {
                charLength = 2;
            } else if ((ch >= Character.MIN_LOW_SURROGATE) && (ch <= Character.MAX_LOW_SURROGATE)) {
                continue;
            } else {
                charLength = 1;
            }
            // 1. joinable char -> joined    e.g. kana & sound mark
            if (i != originalText.length() - 1) {
                for (int j = 0; j < joinedCharBounds.length; j++) {
                    if (originalText.charAt(i + 1) == joinedCharBounds[j][0]) {
                        if ((ch >= joinedCharBounds[j][1]) && (ch <= joinedCharBounds[j][2])) {
                            charLength++;
                            break;
                        }
                    }
                }
            }
            // 2. capital alphabet (not only latin but greek, cyrillic, etc.) -> small
            substr = originalText.substring(i, i + charLength).toLowerCase();
            // 3. normalize (except in ignoreNormalizeBounds)
            //    e.g. full-width alphabet -> half-width / ligature -> separated / etc.
            if (isCharInIgnoreNormalizeBounds(ch)) {
                text.replace(i, i + charLength, substr);
            }
            else {
                text.replace(i, i + charLength, Normalizer.normalize(substr, Form.NFKC));
            }
        }
    }
    
    private boolean isCharInIgnoreNormalizeBounds(char ch) {
        for (int i = 0; i < ignoreNormalizeBounds.length; i++) {
            if ((ch >= ignoreNormalizeBounds[i][0]) && (ch <= ignoreNormalizeBounds[i][1])) {
                return true;
            }
        }
        return false;
    }
}
