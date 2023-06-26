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

public class ChanneledBuffer {
    private final ByteBuffer buffer;
    private final WritableByteChannel channel;

    private int offset;

    public ChanneledBuffer(WritableByteChannel channel, int size) {
        this.channel = channel;
        this.buffer = ByteBuffer.allocate(size);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
    }

    public ChanneledBuffer(WritableByteChannel channel) {
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
            offset += buffer.position();
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

    public ByteBuffer byteBuffer(int maxLength) throws IOException {
        ByteBuffer buf = buffer;
        int remaining = buf.remaining();
        if (remaining < maxLength) {
            offset += buf.position();
            buf.flip();
            channel.write(buf);
            buf.clear();
            if (buf.remaining() < maxLength) {
                throw new IllegalArgumentException(String.format(
                        "requested additionally: %d bytes, but the buffer size is %d", maxLength, buf.capacity()));
            }
        }
        return buf;
    }

    public BufWriter writer(int maxLength) throws IOException {
        ByteBuffer buf = byteBuffer(maxLength);
        return new BufWriter(buf);
    }

    public void flush() throws IOException {
        buffer.flip();
        channel.write(buffer);
        buffer.clear();
    }

    public int offset() {
        return this.offset + buffer.position();
    }

    public int alignTo(int alignment) {
        ByteBuffer buf = buffer;
        int pos = buf.position();
        int aligned = Align.align(pos, alignment);
        buf.position(aligned);
        return aligned + offset;
    }

    public void position(int newPosition) {
        buffer.position(newPosition);
    }
}
