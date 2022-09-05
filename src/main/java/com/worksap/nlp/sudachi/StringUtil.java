/*
 * Copyright (c) 2017-2022 Works Applications Co., Ltd.
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

package com.worksap.nlp.sudachi;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

public class StringUtil {
    private StringUtil() {
    }

    public static String readFully(URL url) throws IOException {
        try (InputStream inputStream = url.openStream()) {
            return readFully(inputStream);
        }
    }

    public static String readFully(Path path) throws IOException {
        try (InputStream is = Files.newInputStream(path)) {
            return readFully(is);
        }
    }

    public static String readFully(InputStream stream) throws IOException {
        InputStreamReader isr = new InputStreamReader(stream, StandardCharsets.UTF_8);
        StringBuilder sb = new StringBuilder();
        CharBuffer cb = CharBuffer.allocate(1024);
        while (isr.read(cb) != -1) {
            cb.flip();
            sb.append(cb);
            cb.clear();
        }
        return sb.toString();
    }

    public static ByteBuffer readAllBytes(URL url) throws IOException {
        try (InputStream is = url.openStream()) {
            return readAllBytes(is);
        }
    }

    public static ByteBuffer readAllBytes(InputStream inputStream) throws IOException {
        byte[] buffer = new byte[inputStream.available() + 1024];
        int offset = 0;

        while (true) {
            int nread = inputStream.read(buffer, offset, buffer.length - offset);
            if (nread >= 0) {
                offset += nread;
                if (offset == buffer.length) {
                    buffer = Arrays.copyOf(buffer, buffer.length * 2);
                }
            } else {
                break;
            }
        }
        ByteBuffer bbuf = ByteBuffer.wrap(buffer);
        bbuf.limit(offset);
        return bbuf;
    }

    public static int count(CharSequence sequence, char toFind) {
        return count(sequence, 0, sequence.length(), toFind);
    }

    public static int count(CharSequence sequence, int start, int end, char toFind) {
        int count = 0;
        for (int i = start; i < end; i++) {
            char c = sequence.charAt(i);
            if (c == toFind) {
                count += 1;
            }
        }
        return count;
    }

    public static String readLengthPrefixed(ByteBuffer buffer) {
        // implementation: use the fact that CharBuffers are CharSequences
        // and the fact that ByteBuffer can be used as CharBuffer
        // remember buffer state
        int limit = buffer.limit();
        int position = buffer.position();
        // read length
        short length = buffer.getShort(position);
        // compute new buffer state
        int newPosition = position + 2;
        buffer.position(newPosition);
        buffer.limit(newPosition + length * 2);
        // use CharBuffer API
        String result = buffer.asCharBuffer().toString();
        // restore previous state
        buffer.position(position);
        buffer.limit(limit);
        return result;
    }

    public static int countUtf8Bytes(CharSequence seq) {
        return countUtf8Bytes(seq, 0, seq.length());
    }

    public static int countUtf8Bytes(CharSequence seq, int start, int end) {
        if (start < 0) {
            throw new IllegalArgumentException("start < 0, was " + start);
        }
        if (start > seq.length()) {
            throw new IllegalArgumentException(String.format("start > length(): %d length()=%d", start, seq.length()));
        }
        if (end > seq.length()) {
            throw new IllegalArgumentException(String.format("end > length(): %d length()=%d", start, seq.length()));
        }

        int result = 0;
        for (int i = start; i < end;) {
            int cpt = Character.codePointAt(seq, i);
            result += utf8Length(cpt);
            i += Character.charCount(cpt);
        }
        return result;
    }

    private static int utf8Length(int codepoint) {
        // https://en.wikipedia.org/wiki/UTF-8#Encoding
        if (codepoint < 0x80) {
            return 1;
        } else if (codepoint < 0x800) {
            return 2;
        } else if (codepoint < 0x10000) {
            return 3;
        } else {
            return 4;
        }
    }
}
