package com.worksap.nlp.sudachi;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

class UTF8InputText implements InputText<byte[]> {
    
    private final String originalText;
    private String modifiedText;
    private byte[] byteCache;
    private ArrayList<Byte> utf8TextBytes;
    private ArrayList<Integer> offsets;
    
    UTF8InputText(String text) {
        originalText = text;
        modifiedText = text;
        
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
        modifiedText = new String(getByteText(), StandardCharsets.UTF_8);
    }
    
    @Override
    public String getOriginalText() {
        return originalText;
    }
    
    @Override
    public String getText() {
        return modifiedText;
    }
    
    public byte[] getByteText() {
        byteCache = new byte[utf8TextBytes.size()];
        for (int i = 0; i < utf8TextBytes.size(); i++) {
            byteCache[i] = utf8TextBytes.get(i);
        }
        return byteCache;
    }
    
    public boolean isCharAlignment(int offset) {
        return (utf8TextBytes.get(offset) & 0xC0) != 0x80;
    }
    
    @Override
    public int getOriginalOffset(int offset)
        throws StringIndexOutOfBoundsException {
        return offsets.get(offset);
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
