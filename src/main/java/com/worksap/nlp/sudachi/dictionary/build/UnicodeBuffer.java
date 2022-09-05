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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.nio.channels.WritableByteChannel;

public class UnicodeBuffer {
    private final ByteBuffer buffer;
    private final WritableByteChannel channel;

    public UnicodeBuffer(WritableByteChannel channel, int size) {
        this.channel = channel;
        this.buffer = ByteBuffer.allocate(size);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
    }

    public UnicodeBuffer(WritableByteChannel channel) {
        this(channel, 64 * 1024);
    }

    public void put(String data) throws IOException {
        put(data, 0, data.length());
    }

    public void put(String data, int start, int end) throws IOException {
        CharBuffer chars = prepare(end - start);
        chars.put(data, start, end);
    }

    private CharBuffer prepare(int numChars) throws IOException {
        int remaining = buffer.remaining();
        int byteLength = numChars * 2;
        if (remaining < byteLength) {
            buffer.flip();
            channel.write(buffer);
            buffer.clear();
            if (buffer.remaining() < byteLength) {
                throw new IllegalArgumentException("string length is too long: " + numChars);
            }
        }
        CharBuffer chars = buffer.asCharBuffer();
        buffer.position(buffer.position() + byteLength);
        return chars;
    }

    public void flush() throws IOException {
        channel.write(buffer);
    }
}