/*
 * Copyright (c) 2022 Works Applications Co., Ltd.
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

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class BufReader {
    private final ByteBuffer buffer;

    public BufReader(ByteBuffer buffer) {
        this.buffer = buffer;
    }

    long readLong() {
        return buffer.getLong();
    }

    long readVarint64() {
        ByteBuffer b = buffer;
        int first = b.get() & 0xff;
        if (first < 128) {
            return first;
        } else {
            return readVarLongSlowpath(first & 0x7f, b);
        }
    }

    private static long readVarLongSlowpath(long v0, ByteBuffer b) {
        long v1 = b.get() & 0xff;
        if (v1 < 0x80) {
            return (v1 << 7) | v0;
        }
        v0 |= (v1 & 0x7f) << 7;
        long v2 = b.get() & 0xff;
        if (v2 < 0x80) {
            return (v2 << 14) | v0;
        }
        v0 |= (v2 & 0x7f) << 14;
        long v3 = b.get() & 0xff;
        if (v3 < 0x80) {
            return (v3 << 21) | v0;
        }
        v0 |= (v3 & 0x7f) << 21;
        long v4 = b.get() & 0xff;
        if (v4 < 0x80) {
            return (v4 << 28) | v0;
        }
        v0 |= (v4 & 0x7f) << 28;
        long v5 = b.get() & 0xff;
        if (v5 < 0x80) {
            return (v5 << 35) | v0;
        }
        v0 |= (v5 & 0x7f) << 35;
        long v6 = b.get() & 0xff;
        if (v6 < 0x80) {
            return (v6 << 42) | v0;
        }
        v0 |= (v6 & 0x7f) << 42;
        long v7 = b.get() & 0xff;
        if (v7 < 0x80) {
            return (v7 << 49) | v0;
        }
        v0 |= (v7 & 0x7f) << 49;
        long v8 = b.get() & 0xff;
        if (v8 < 0x80) { // only 6 bits are valid here, rest must be 0
            return (v8 << 56) | v0;
        }
        v0 |= (v8 & 0x7f) << 56;
        long v9 = b.get() & 0xff;
        if (v9 < 0x07) { // only 3 bits are valid here, rest must be 0
            return (v8 << 61) | v0;
        }
        throw new IllegalStateException("invalid long varint encoding");
    }

    public int readVarint32() {
        long l = readVarint64();
        if ((l & ~0xffff_ffffL) != 0) {
            throw new IllegalStateException("invalid int varint encoding");
        }
        return (int) l;
    }

    public String readUtf8String() {
        int length = readVarint32();
        if (buffer.remaining() < length) {
            throw new IllegalStateException("invalid string exception, content underflow");
        }
        if (buffer.hasArray()) {
            byte[] arr = buffer.array();
            int offset = buffer.arrayOffset();
            int position = buffer.position();
            String s = new String(arr, offset + position, length, StandardCharsets.UTF_8);
            buffer.position(position + length);
            return s;
        } else {
            byte[] repr = new byte[length];
            buffer.get(repr);
            return new String(repr, StandardCharsets.UTF_8);
        }
    }
}
