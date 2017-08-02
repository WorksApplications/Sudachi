
package com.worksap.nlp.sudachi;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.ArrayList;
import java.util.List;

public class DefaultInputTextPlugin extends InputTextPlugin {
    
    public String rewriteDef;

    private List<String> ignoreNormalizeList = new ArrayList<>();
    private List<String[]> replaceCharList = new ArrayList<>();
    
    @Override
    public void setUp() throws IOException {
        if (rewriteDef == null) {
            rewriteDef = settings.getPath("rewriteDef");
        }
        if (rewriteDef == null) {
            rewriteDef = DefaultInputTextPlugin.class.getClassLoader().getResource("rewrite.def").getPath();
        }
        readRewriteLists(rewriteDef);
    }
    
    @Override
    public void rewrite(InputText<?> text) {
        int charLength;
        char ch;
        String substr;
        String originalText = text.getText();
        for (int i = 0; i < originalText.length(); i++) {
            substr = originalText.substring(i);
            // 1. replace char without normalize
            for (int j = 0; j < replaceCharList.size(); j++) {
                if (substr.startsWith(replaceCharList.get(j)[0])) {
                    text.replace(i, i + replaceCharList.get(j)[0].length(), replaceCharList.get(j)[1]);
                    break;
                }
            }
            // 2. normalize
            // 2-1. check if surrogate pair
            ch = substr.charAt(0);
            if ((ch >= Character.MIN_HIGH_SURROGATE) && (ch <= Character.MAX_HIGH_SURROGATE)) {
                charLength = 2;
            } else if ((ch >= Character.MIN_LOW_SURROGATE) && (ch <= Character.MAX_LOW_SURROGATE)) {
                //    do nothing if lower surrogate
                continue;
            } else {
                charLength = 1;
            }
            // 2-2. capital alphabet (not only latin but greek, cyrillic, etc) -> small
            substr = substr.substring(0, charLength).toLowerCase();
            // 2-3. normalize (except in ignoreNormalize)
            //    e.g. full-width alphabet -> half-width / ligature / etc.
            if (ignoreNormalizeList.contains(substr)) {
                text.replace(i, i + charLength, substr);
            }
            else {
                text.replace(i, i + charLength, Normalizer.normalize(substr, Form.NFKC));
            }
        }
    }
    
    private void readRewriteLists(String rewriteDef) throws IOException {
        try (
            FileInputStream fin = new FileInputStream(rewriteDef);
            LineNumberReader reader
                = new LineNumberReader(new InputStreamReader(fin, StandardCharsets.UTF_8))
        ){
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.matches("\\s*") || line.startsWith("#")) {
                    continue;
                }
                String[] cols = line.split("\\s+");
                // ignored normalize list
                if (cols.length == 1) {
                    ignoreNormalizeList.add(cols[0]);
                }
                // replace char list
                else if (cols.length == 2) {
                    for (String[] definedPair : replaceCharList) {
                        if (cols[0].equals(definedPair[0])) {
                            throw new RuntimeException(
                                cols[0] + " is already defined at line " + reader.getLineNumber()
                            );
                        }
                    }
                    replaceCharList.add(new String[]{cols[0], cols[1]});
                }
                else {
                    throw new RuntimeException(
                        "invalid format at line " + reader.getLineNumber()
                    );
                }
            }
        }
    }
}
