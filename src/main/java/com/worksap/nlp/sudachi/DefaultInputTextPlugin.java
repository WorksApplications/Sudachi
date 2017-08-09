
package com.worksap.nlp.sudachi;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer.Form;
import java.text.Normalizer;
import java.util.Set;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;

public class DefaultInputTextPlugin extends InputTextPlugin {
    
    public String rewriteDef;

    private Set<Integer> ignoreNormalizeSet = new HashSet<>();
    private Map<Character, Integer> keyLengths = new HashMap<>();
    private Map<String, String> replaceCharMap = new HashMap<>();
    
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
        int offset = 0;
        int nextOffset = 0;
        String text = builder.getText();
        textloop: for (int i = 0; i < text.length(); i = text.offsetByCodePoints(i, 1)) {
            offset += nextOffset;
            nextOffset = 0;
            // 1. replace char without normalize
            for (int l = Math.min(keyLengths.getOrDefault(text.charAt(i), 0), text.length() - i);
                 l > 0; l--) {
                String replace = replaceCharMap.get(text.substring(i, i + l));
                if (replace != null) {
                    builder.replace(i + offset, i + l + offset, replace);
                    nextOffset += replace.length() - l;
                    i += l - 1;
                    continue textloop;
                }
            }

            // 2. normalize
            int original = text.codePointAt(i);
            int charLength = text.offsetByCodePoints(i, 1) - i;

            // 2-1. capital alphabet (not only latin but greek, cyrillic, etc) -> small
            int lower = Character.toLowerCase(original);
            String replace;
            if (ignoreNormalizeSet.contains(lower)) {
                if (original == lower) {
                    continue;
                }
                replace = new String(Character.toChars(lower));
            } else {
                // 2-2. normalize (except in ignoreNormalize)
                //    e.g. full-width alphabet -> half-width / ligature / etc.
                replace = Normalizer.normalize(new String(Character.toChars(lower)),
                                               Form.NFKC);
            }
            nextOffset = replace.length() - charLength;
            if (replace.length() != charLength
                || original != replace.codePointAt(0)) {
                builder.replace(i + offset, i + charLength + offset, replace);
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
                    String key = cols[0];
                    if (key.codePointCount(0, key.length()) != 1) {
                        throw new RuntimeException(cols[0] + " is not a character at line " + reader.getLineNumber());
                    }
                    ignoreNormalizeSet.add(key.codePointAt(0));
                }
                // replace char list
                else if (cols.length == 2) {
                    if (replaceCharMap.containsKey(cols[0])) {
                        throw new RuntimeException(cols[0] + " is already defined at line "
                                                   + reader.getLineNumber());
                    }
                    if (keyLengths.getOrDefault(cols[0].charAt(0), -1) < cols[0].length()) {
                        // store the longest key length
                        keyLengths.put(cols[0].charAt(0), cols[0].length());
                    }
                    replaceCharMap.put(cols[0], cols[1]);
                } else {
                    throw new RuntimeException("invalid format at line "
                                               + reader.getLineNumber());
                }
            }
        }
    }
}
