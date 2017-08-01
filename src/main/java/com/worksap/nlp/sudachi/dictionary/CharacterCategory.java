
package com.worksap.nlp.sudachi.dictionary;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CharacterCategory {
    
    static class Range {
        int low;
        int high;
        List<String> categories = new ArrayList<>();
        
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
        
    public List<String> getCategoryNameList(int codePoint) {
        for (Range range: rangeList) {
            if (range.contains(codePoint)) {
                return range.categories;
            }
        }
        return Collections.emptyList();
    }
    
    public int getContinuousLength(String text) {
        boolean found;
        List<String> categoryList = getCategoryNameList(text.codePointAt(0));
        int length;
        for (length = 0; length < text.length(); length++) {
            found =false;
            List<String> nextCategoryList = getCategoryNameList(text.codePointAt(length));
            for (int i = 0; i < categoryList.size(); i++) {
                for (int j = 0; j < nextCategoryList.size(); j++) {
                    if (categoryList.get(i).equals(nextCategoryList.get(j))) {
                        found = true; 
                    }
                }
            }
            if (!found) {
                return length;
            }
        }
        return length;
    }
    
    public void readCharacterDefinition(String charDef) throws IOException {
        try (
            FileInputStream fin = new FileInputStream(charDef);
            LineNumberReader reader
                = new LineNumberReader(new InputStreamReader(fin, StandardCharsets.UTF_8));
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
                        range.categories.add(cols[i]);
                    }
                    rangeList.add(range);
                }
            }
            Range defaultRange = new Range();
            defaultRange.low = 0;
            defaultRange.high = Integer.MAX_VALUE;
            defaultRange.categories.add("DEFAULT");
            rangeList.add(defaultRange);
        }
    }
}
