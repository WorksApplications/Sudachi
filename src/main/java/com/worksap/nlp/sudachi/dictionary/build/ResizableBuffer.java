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
import java.nio.channels.WritableByteChannel;

public class ResizableBuffer {
    private ByteBuffer buffer;

    public ResizableBuffer(int capacity) {
        ByteBuffer buf = ByteBuffer.wrap(new byte[capacity]);
        buf.order(ByteOrder.LITTLE_ENDIAN);
        buffer = buf;
    }

    public ByteBuffer prepare(int additional) {
        ByteBuffer buf = buffer;
        if (buf.remaining() >= additional) {
            return buf;
        } else {
            return grow(additional);
        }
    }

    public ByteBuffer prepare(int offset, int size) {
        ByteBuffer buf = buffer;
        int capacity = buf.capacity();
        if (capacity < offset + size) {
            buf = grow(offset + size - capacity);
        }
        ByteBuffer duplicate = buf.duplicate();
        duplicate.order(ByteOrder.LITTLE_ENDIAN);
        duplicate.position(offset);
        buf.position(offset + size);
        return duplicate;
    }

    private ByteBuffer grow(int additional) {
        ByteBuffer current = buffer;
        int newSize = Math.max(current.capacity() * 2, current.capacity() + additional);
        ByteBuffer fresh = ByteBuffer.wrap(new byte[newSize]);
        fresh.order(ByteOrder.LITTLE_ENDIAN);
        current.flip();
        fresh.put(current);
        buffer = fresh;
        return fresh;
    }

    public ByteBuffer getBuffer() {
        return buffer;
    }

    public void write(WritableByteChannel channel, int start, int end) throws IOException {
        ByteBuffer buf = buffer;
        int pos = buf.position();
        int limit = buf.limit();
        try {
            buf.position(start);
            buf.limit(end);
            channel.write(buf);
        } finally {
            buf.position(pos);
            buf.limit(limit);
        }
    }
}
