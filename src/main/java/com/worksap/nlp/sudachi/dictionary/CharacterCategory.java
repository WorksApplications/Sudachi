
package com.worksap.nlp.sudachi.dictionary;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CharacterCategory {
    
    private static final String DEFAULT_CATEGORY = "DEFAULT";
    
    static class Range {
        int low;
        int high;
        Set<CategoryType> categories = new CategoryTypeSet();
        
        boolean contains(int cp) {
            if (cp >= low && cp <= high) {
                return true;
            }
            return false;
        }
        
        int containingLength(String text) {
            for (int i = 0; i < text.length();
                i = text.offsetByCodePoints(i, 1)) {
                int c = text.codePointAt(i);
                if (c < low || c > high) {
                    return i;
                }
            }
            return text.length();
        }
    }
    
    private List<Range> rangeList = new ArrayList<>();
    
    public Set<CategoryType> getCategoryTypes(int codePoint) {
        for (Range range: rangeList) {
            if (range.contains(codePoint)) {
                return range.categories;
            }
        }
        return Collections.singleton(CategoryType.DEFAULT);
    }
    
    public void readCharacterDefinition(String charDef) throws IOException {
        try (
            InputStream in = (charDef != null)
                ? new FileInputStream(charDef)
                : CharacterCategory.class.getClassLoader().getResourceAsStream("char.def");
            LineNumberReader reader
                = new LineNumberReader(new InputStreamReader(in, StandardCharsets.UTF_8));
        ) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.matches("\\s*") || line.startsWith("#")) {
                    continue;
                }
                String[] cols = line.split("\\s+");
                if (cols.length < 2) {
                    throw new RuntimeException(
                        "invalid format at line " + reader.getLineNumber()
                    );
                }
                if (cols[0].startsWith("0x")) {
                    Range range = new Range();
                    String[] r = cols[0].split("\\.\\.");
                    range.low = range.high = Integer.decode(r[0]);
                    if (r.length > 1) {
                        range.high = Integer.decode(r[1]);
                    }
                    if (range.low > range.high) {
                        throw new RuntimeException(
                            "invalid range at line " + reader.getLineNumber()
                        );
                    }
                    for (int i = 1; i < cols.length; i++) {
                        if (cols[i].startsWith("#")) {
                            break;
                        }
                        CategoryType type = CategoryType.valueOf(cols[i]);
                        if (type == null) {
                            throw new RuntimeException(cols[i] + " is invalid type at line "
                                                       + reader.getLineNumber());
                        }
                        range.categories.add(type);
                    }
                    rangeList.add(range);
                }
            }
            Range defaultRange = new Range();
            defaultRange.low = 0;
            defaultRange.high = Integer.MAX_VALUE;
            defaultRange.categories.add(CategoryType.DEFAULT);
            rangeList.add(defaultRange);
        }
    }
}
