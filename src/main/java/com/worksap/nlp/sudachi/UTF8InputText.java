package com.worksap.nlp.sudachi;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import com.worksap.nlp.sudachi.dictionary.Grammar;

class UTF8InputText implements InputText<byte[]> {
    
    private final String originalText;
    private String modifiedTextCache;
    private byte[] byteCache;
    private List<Byte> utf8TextBytes;
    private List<Integer> offsets;
    
    private Grammar grammar;
    
    UTF8InputText(String text, Grammar grammar) {
        this.grammar = grammar;
        originalText = text;
        modifiedTextCache = text;
        byteCache = text.getBytes(StandardCharsets.UTF_8);
        utf8TextBytes = new ArrayList<>(byteCache.length);
        for (byte b : byteCache) {
            utf8TextBytes.add(b);
        }
        offsets = new ArrayList<>(utf8TextBytes.size() + 1);
        for (int i = 0; i < originalText.length(); i++) {
            if (originalText.charAt(i) >= Character.MIN_LOW_SURROGATE) {
                if (originalText.charAt(i) <= Character.MAX_LOW_SURROGATE) {
                    continue;
                }
            }
            for (int j = 0; j < utf8ByteLength(originalText.codePointAt(i)); j++) {
                offsets.add(i);
            }
        }
        offsets.add(originalText.length());
    }
    
    @Override
    public void replace(int begin, int end, String str)
        throws StringIndexOutOfBoundsException {
        if (begin < 0)
            throw new StringIndexOutOfBoundsException(begin);
        if (begin > originalText.length())
            throw new StringIndexOutOfBoundsException("begin > length()");
        if (begin > end)
            throw new StringIndexOutOfBoundsException("begin > end");
        if (begin == end)
            throw new IllegalArgumentException("begin == end");
        
        if (end > originalText.length()) {
            end = originalText.length();
        }
        byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
        int length = bytes.length;
        
        int offsetBegin = offsets.indexOf(begin);
        int offsetEnd = offsets.indexOf(end);
        if (offsetBegin < 0 || offsetEnd < 0) {
            // character has been removed
            return;
        }
        
        if (offsetEnd - offsetBegin > length) {
            utf8TextBytes.subList(offsetBegin + length, offsetEnd).clear();
            offsets.subList(offsetBegin + length, offsetEnd).clear();
        }
        for (int i = 0; i < length; i++) {
            if (offsetBegin + i < offsetEnd) {
                utf8TextBytes.set(offsetBegin + i, bytes[i]);
                offsets.set(offsetBegin + i, begin);
            } else {
                utf8TextBytes.add(offsetBegin + i, bytes[i]);
                offsets.add(offsetBegin + i, begin);
            }
        }
        modifiedTextCache = null;
        byteCache = null;
    }
    
    @Override
    public String getOriginalText() {
        return originalText;
    }
    
    @Override
    public String getText() {
        if (modifiedTextCache == null) {
            modifiedTextCache = new String(getByteText(), StandardCharsets.UTF_8); 
        }
        return modifiedTextCache;
    }
    
    public byte[] getByteText() {
        if (byteCache == null) {
            byteCache = new byte[utf8TextBytes.size()];
            for (int i = 0; i < utf8TextBytes.size(); i++) {
                byteCache[i] = utf8TextBytes.get(i);
            }
        }
        return byteCache;
    }
    
    public String getOffsetText(int offset)
        throws StringIndexOutOfBoundsException {
        byte[] bytes = getByteText();
        return new String(bytes, offset, bytes.length - offset);
    }
    
    public boolean isCharAlignment(int offset) {
        return (utf8TextBytes.get(offset) & 0xC0) != 0x80;
    }
    
    @Override
    public int getOriginalOffset(int offset)
        throws StringIndexOutOfBoundsException {
        return offsets.get(offset);
    }
    
    @Override
    public List<String> getCharCategoryNameList(int offset)
        throws StringIndexOutOfBoundsException {
        int cp = getText().codePointAt(offset);
        return grammar.getCharacterCategory().getCategoryNameList(cp);
    }
    
    @Override
    public int getCharCategoryContinuousLength(int offset)
        throws StringIndexOutOfBoundsException {
        String substr = getText().substring(offset);
        return grammar.getCharacterCategory().getContinuousLength(substr);
    }
    
    public int getUTF8BytesLengthByCodePoints(int offset, int codePointLength)
        throws StringIndexOutOfBoundsException {
        int length = 0;
        for (int i = 0; i < codePointLength; i++) {
            length += utf8ByteLength(getText().codePointAt(offset + codePointLength - 1));
        }
        return length;
    }
    
    private int utf8ByteLength(int cp) {
        if (cp < 0) {
            return 0;
        } else if (cp <= 0x7f) {
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
