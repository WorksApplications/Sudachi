
package com.worksap.nlp.sudachi;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.worksap.nlp.sudachi.dictionary.Grammar;

public class UTF8InputTextBuilder implements InputTextBuilder<byte[]> {

    private final String originalText;
    private StringBuilder modifiedText;
    private List<Integer> textOffsets;
    
    private final Grammar grammar;
    
    public UTF8InputTextBuilder(String text, Grammar grammar) {
        this.grammar = grammar;
        
        originalText = text;
        modifiedText = new StringBuilder(text);
        textOffsets = new ArrayList<>(modifiedText.length() + 1);
        for (int i = 0, j = 0; i < originalText.length(); i++) {
            if (!Character.isLowSurrogate(originalText.charAt(i))) {
                j = i;
            }
            textOffsets.add(j);
        }
        textOffsets.add(originalText.length());
    }
    
    @Override
    public void replace(int begin, int end, String str)
        throws StringIndexOutOfBoundsException {
        if (begin < 0)
            throw new StringIndexOutOfBoundsException(begin);
        if (begin > modifiedText.length())
            throw new StringIndexOutOfBoundsException("begin > length()");
        if (begin > end)
            throw new StringIndexOutOfBoundsException("begin > end");
        if (begin == end)
            throw new IllegalArgumentException("begin == end");
        
        if (end > modifiedText.length()) {
            end = modifiedText.length();
        }
        
        modifiedText.replace(begin, end, str);

        int offset = textOffsets.get(begin);
        int length = str.length();
        if (end - begin > length) {
            textOffsets.subList(begin + length, end).clear();
        }
        for (int i = 0; i < length; i++) {
            if (begin + i < end) {
                textOffsets.set(begin + i, offset);
            } else {
                textOffsets.add(begin + i, offset);
            }
        }
    }
    
    @Override
    public String getOriginalText() {
        return originalText;
    }
    
    @Override
    public String getText() {
        return modifiedText.toString();
    }
    
    @Override
    public UTF8InputText build() {
        String modifiedStringText = getText();
        byte[] byteText = modifiedStringText.getBytes(StandardCharsets.UTF_8);

        int length = byteText.length;
        int[] byteIndexes = new int[length + 1];
        int[] offsets = new int[length + 1];
        for (int i = 0, j = 0; i < modifiedText.length(); i++) {
            if (Character.isLowSurrogate(modifiedText.charAt(i))) {
                continue;
            }
            for (int k = 0; k < utf8ByteLength(modifiedText.codePointAt(i)); k++) {
                byteIndexes[j] = i;
                offsets[j] = textOffsets.get(i);
                j++;
            }
        }
        byteIndexes[length] = modifiedStringText.length();
        offsets[length] = textOffsets.get(textOffsets.size() - 1);

        List<List<String>> charCategories = getCharCategoryNames(modifiedStringText);
        List<Integer> charCategoryContinuities = getCharCategoryContinuities(modifiedStringText, byteText, byteIndexes, charCategories);
        return new UTF8InputText(grammar, originalText, modifiedStringText, byteText,
                                 offsets,
                                 byteIndexes,
            Collections.unmodifiableList(charCategories),
            Collections.unmodifiableList(charCategoryContinuities));
    }

    private List<List<String>> getCharCategoryNames(String text) {
        List<List<String>> charCategoryNames = new ArrayList<>(text.length());
        for (int i = 0; i < text.length(); i++) {
            charCategoryNames.add(Collections.unmodifiableList(
                grammar.getCharacterCategory().getCategoryNames(text.codePointAt(i))));
        }
        return charCategoryNames;
    }
    
    private List<Integer> getCharCategoryContinuities(String text, byte[] byteText, int[] byteIndexes, List<List<String>> charCategories) {
        if (charCategories == null || charCategories.isEmpty()) {
            return Collections.emptyList();
        }
        List<Integer> charCategoryContinuities = new ArrayList<>(text.length());
        for (int offset = 0; offset < byteText.length; offset++) {
            charCategoryContinuities.add(getCharCategoryContinuousLength(byteText, byteIndexes, charCategories, offset));
        }
        return charCategoryContinuities;
    }
    
    private int getCharCategoryContinuousLength(byte[] byteText, int[] byteIndexes, List<List<String>> charCategories, int offset) {
        int length;
        for (length = 0; length < byteText.length - offset; length++) {
            boolean found = false;
            // compare each category at offset w/ each category at target (offset + length)
            List<String> offsetCharCategory = charCategories.get(byteIndexes[offset]);
            List<String> targetCharCategory = charCategories.get(byteIndexes[offset + length]);
            for (int i = 0; i < offsetCharCategory.size(); i++) {
                for (int j = 0; j < targetCharCategory.size(); j++) {
                    if (offsetCharCategory.get(i).equals(targetCharCategory.get(j))) {
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
    
    private int utf8ByteLength(int cp) {
        if (cp < 0) {
            return 0;
        } else if (cp <= 0x7F) {
            return 1;
        } else if (cp <= 0x7FF) {
            return 2;
        } else if (cp <= 0xFFFF) {
            return 3;
        } else if (cp <= 0x10FFFF) {
            return 4;
        } else {
            return 0;
        }
    }
}
