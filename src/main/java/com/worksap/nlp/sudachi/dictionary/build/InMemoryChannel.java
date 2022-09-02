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
import java.nio.channels.SeekableByteChannel;

public final class InMemoryChannel implements SeekableByteChannel {
    private ByteBuffer buffer;

    public InMemoryChannel() {
        this(1024 * 1024);
    }

    public InMemoryChannel(int size) {
        buffer = ByteBuffer.allocate(size);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
    }

    public void reserve(int needed) {
        if (buffer.remaining() < needed) {
            ByteBuffer old = buffer;
            buffer = ByteBuffer.allocate(buffer.capacity() * 2);
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            old.flip();
            buffer.put(old);
        }
    }

    @Override
    public int read(ByteBuffer dst) throws IOException {
        int position = buffer.position();
        buffer.put(dst);
        int newPosition = buffer.position();
        return newPosition - position;
    }

    @Override
    public int write(ByteBuffer src) throws IOException {
        reserve(src.remaining());
        int pos = buffer.position();
        buffer.put(src);
        return buffer.position() - pos;
    }

    @Override
    public long position() throws IOException {
        return buffer.position();
    }

    @Override
    public SeekableByteChannel position(long newPosition) throws IOException {
        assert newPosition < Integer.MAX_VALUE;
        buffer.position((int) newPosition);
        return this;
    }

    @Override
    public long size() throws IOException {
        return buffer.limit();
    }

    @Override
    public SeekableByteChannel truncate(long size) {
        assert size < Integer.MAX_VALUE;
        buffer.limit((int) size);
        return this;
    }

    @Override
    public boolean isOpen() {
        return true;
    }

    @Override
    public void close() throws IOException {
        // always open
    }

    public ByteBuffer buffer() {
        ByteBuffer copy = buffer.duplicate();
        copy.position(0);
        copy.limit(buffer.position());
        copy.order(ByteOrder.LITTLE_ENDIAN);
        return copy;
    }
}
