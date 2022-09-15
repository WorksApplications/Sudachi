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

package com.worksap.nlp.sudachi.dictionary.build;

import com.worksap.nlp.sudachi.dictionary.Ints;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class BufWriter {
    private final ByteBuffer buffer;

    public BufWriter(ByteBuffer buffer) {
        this.buffer = buffer;
    }

    public BufWriter putByte(byte val) {
        buffer.put(val);
        return this;
    }

    // Encode int as LEB128
    public BufWriter putVarint32(int val) {
        if ((val & 0xff) == 0) {
            putByte((byte) val);
        } else {
            putVarintSlow(val & 0xffff_ffffL);
        }
        return this;
    }

    public BufWriter putVarint64(long val) {
        if ((val & 0xff) == 0) {
            putByte((byte) val);
        } else {
            putVarintSlow(val);
        }
        return this;
    }

    private void putVarintSlow(long val) {
        while ((val & ~0x7fL) != 0) {
            long b = 0x80 | (val & 0x7f);
            putByte((byte) b);
            val >>>= 7;
        }
        putByte((byte) val);
    }

    public BufWriter putShort(short val) {
        buffer.putShort(val);
        return this;
    }

    public BufWriter putInt(int val) {
        buffer.putInt(val);
        return this;
    }

    public BufWriter putInts(Ints value, int length) {
        if (length <= 0) {
            return this;
        }
        ByteBuffer buf = buffer; // read field only once
        int pos = buf.position();
        for (int i = 0; i < length; ++i) {
            buf.putInt(pos + i * 4, value.get(i));
        }
        buf.position(pos + length * 4);
        return this;
    }

    /**
     * Put string which has length is shorter than Short.MAX_VALUE
     * 
     * @param s
     *            string to put in the buffer
     */
    public void putShortString(String s) {
        int length = s.length();
        assert length < Short.MAX_VALUE;
        putShort((short) length);
        for (int i = 0; i < length; ++i) {
            putShort((short) s.charAt(i));
        }
    }

    public BufWriter putStringUtf8(String s) {
        byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
        putVarint32(bytes.length);
        buffer.put(bytes);
        return this;
    }

    public void putLong(long x) {
        buffer.putLong(x);
    }
}
