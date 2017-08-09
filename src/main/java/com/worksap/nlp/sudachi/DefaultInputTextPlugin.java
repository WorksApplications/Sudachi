
package com.worksap.nlp.sudachi;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer.Form;
import java.text.Normalizer;
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

        InputStream is;
        if (rewriteDef != null) {
            is = new FileInputStream(rewriteDef);
        } else {
            is = DefaultInputTextPlugin.class.getClassLoader().getResourceAsStream("rewrite.def");
        }
        if (is == null) {
            throw new IOException("rewriteDef is not defined");
        }
        readRewriteLists(is);
    }

    @Override
    public void rewrite(InputTextBuilder<?> builder) {
        int charLength;
        int offset = 0;
        int nextOffset = 0;
        boolean skipNormalize;
        String text = builder.getText();
        for (int i = 0; i < text.length(); i++) {
            offset += nextOffset;
            nextOffset = 0;
            skipNormalize = false;
            // 1. replace char without normalize
            for (int j = 0; j < replaceCharList.size(); j++) {
                if (text.startsWith(replaceCharList.get(j)[0], i)) {
                    String charBefore = replaceCharList.get(j)[0];
                    String charAfter = replaceCharList.get(j)[1];
                    builder.replace(i + offset, i + charBefore.length() + offset, charAfter);
                    nextOffset += charAfter.length() - charBefore.length();
                    i += charBefore.length() - 1;
                    skipNormalize = true;
                    break;
                }
            }
            if (skipNormalize) {
                continue;
            }
            // 2. normalize
            // 2-1. check if surrogate pair
            char ch = text.charAt(i);
            if (Character.isHighSurrogate(ch)) {
                charLength = 2;
            }
            else if (Character.isLowSurrogate(ch)) {
                // do nothing when lower surrogate
                continue;
            }
            else {
                charLength = 1;
            }
            // 2-2. capital alphabet (not only latin but greek, cyrillic, etc) -> small
            String substr = text.substring(i, i + charLength).toLowerCase();;
            // 2-3. normalize (except in ignoreNormalize)
            //    e.g. full-width alphabet -> half-width / ligature / etc.
            if (ignoreNormalizeList.contains(substr)) {
                builder.replace(i + offset, i + charLength + offset, substr);
                continue;
            }
            else {
                int beforeLength = substr.length();
                substr = Normalizer.normalize(substr, Form.NFKC);
                nextOffset += substr.length() - beforeLength;
                builder.replace(i + offset, i + charLength + offset, substr);
            }
        }
    }
    
    private void readRewriteLists(InputStream rewriteDef) throws IOException {
        try (InputStreamReader isr = new InputStreamReader(rewriteDef, StandardCharsets.UTF_8);
             LineNumberReader reader = new LineNumberReader(isr)) {
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
