/*
 * Copyright (c) 2017 Works Applications Co., Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.worksap.nlp.sudachi.dictionary;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

/**
 * A classifier of the categories of characters.
 */
public class CharacterCategory {
    
    static class Range {
        int low;
        int high;
        EnumSet<CategoryType> categories = EnumSet.noneOf(CategoryType.class);
        
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
    
    /**
     * Returns the set of the category types of the character (Unicode
     * code point).
     *
     * @param codePoint the code point value of the character
     * @return the set of the category types of the character
     */
    public EnumSet<CategoryType> getCategoryTypes(int codePoint) {
        EnumSet<CategoryType> categories = EnumSet.noneOf(CategoryType.class);
        for (Range range: rangeList) {
            if (range.contains(codePoint)) {
                categories.addAll(range.categories);
            }
        }

        if (categories.isEmpty()) {
            categories.add(CategoryType.DEFAULT);
        }
        return categories;
    }
    
    /**
     * Reads the definitions of the character categories from the file
     * which is specified by {@code charDef}.
     * If {@code charDef} is {@code null}, uses the default definitions.
     *
     * <p>The following is the format of definitions.
     * <pre>{@code
     * 0x0020 SPACE              # a white space
     * 0x0041..0x005A ALPHA      # Latin alphabets
     * 0x4E00 KANJINUMERIC KANJI # Kanji numeric and Kanji
     * }</pre>
     * Lines that do not start with "0x" are ignored.
     *
     * @param charDef the file of the definitions of character categories.
     * @throws IOException if the definition file is not available.
     */
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
        }
    }
}
