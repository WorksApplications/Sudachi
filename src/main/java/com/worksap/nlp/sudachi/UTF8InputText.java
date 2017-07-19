package com.worksap.nlp.sudachi;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

class UTF8InputText implements InputText<byte[]> {

    private String originalText;
    private ArrayList<Byte> utf8Text;
    private ArrayList<Integer> offsets;
    private byte[] byteCache;
    private String modifiedTextCache;

    UTF8InputText(String text) {
        originalText = text;
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        utf8Text = new ArrayList<>(bytes.length);
        for (Byte b : bytes)
            utf8Text.add(b);
        offsets = new ArrayList<Integer>(utf8Text.size() + 1);
        for (int i = 0; i < originalText.length(); i++) {
            int len = utf8Length(originalText.charAt(i));
            for (int j = 0; j < len; j++) {
                offsets.add(i);
            }
        }
        offsets.add(originalText.length());
        byteCache = bytes;
        modifiedTextCache = text;
    }

    public char originalCharAt(int index)
        throws StringIndexOutOfBoundsException {
        return originalText.charAt(index);
    }

    public int originalLength() {
        return originalText.length();
    }

    public void replace(int begin, int end, String str)
        throws StringIndexOutOfBoundsException {
        if (begin < 0)
            throw new StringIndexOutOfBoundsException(begin);
        if (begin > originalText.length())
            throw new StringIndexOutOfBoundsException("begin > length()");
        if (begin > end)
            throw new StringIndexOutOfBoundsException("begin > end");

        if (end > originalText.length())
            end = originalText.length();

        byte[] bytes = str.getBytes();
        int length = bytes.length;

        int offsetBegin = offsets.indexOf(begin);
        int offsetEnd = offsets.indexOf(end);
        if (offsetBegin < 0 || offsetEnd < 0) {
            // character has been removed
            return;
        }

        if (offsetEnd - offsetBegin > length) {
            utf8Text.subList(offsetBegin + length, offsetEnd).clear();
            offsets.subList(offsetBegin + length, offsetEnd).clear();
        }
        for (int i = 0; i < length; i++) {
            if (offsetBegin + i < offsetEnd) {
                utf8Text.set(offsetBegin + i, bytes[i]);
                offsets.set(offsetBegin + i, begin);
            } else {
                utf8Text.add(offsetBegin + i, bytes[i]);
                offsets.add(offsetBegin + i, begin);
            }
        }
        byteCache = null;
        modifiedTextCache = null;
    }

    public String getOriginalText() {
        return originalText;
    }

    public String getText() {
        if (modifiedTextCache == null) {
            modifiedTextCache = new String(getByteText(), StandardCharsets.UTF_8);
        }
        return modifiedTextCache;
    }

    public byte[] getByteText() {
        if (byteCache == null) {
            byteCache = new byte[utf8Text.size()];
            for (int i = 0; i < byteCache.length; i++)
                byteCache[i] = utf8Text.get(i);
        }
        return byteCache;
    }

    public int getOriginalOffset(int offset)
        throws StringIndexOutOfBoundsException {
        return offsets.get(offset);
    }

    private int utf8Length(char ch) {
        if (ch <= 0x7f) {
            return 1;
        } else if (ch <= 0x7FF) {
            return 2;
        } else if (ch <= 0xFFFF) {
            return 3;
        } else if (ch <= 0x1FFFFF) {
            return 4;
        } else if (ch <= 0x3FFFFFF) {
            return 5;
        } else {
            return 6;
        }
    }
}
