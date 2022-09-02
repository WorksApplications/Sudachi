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
import java.nio.channels.SeekableByteChannel;
import java.nio.channels.WritableByteChannel;

public class UnicodeBufferResizeable {
    private ResizableBuffer buffer;

    public UnicodeBufferResizeable(int size) {
        this.buffer = new ResizableBuffer(size);
    }

    public UnicodeBufferResizeable() {
        this(64 * 1024);
    }

    public void put(int offset, String data, int start, int end) {
        CharBuffer chars = prepare(offset, end - start);
        chars.put(data, start, end);
    }

    private CharBuffer prepare(int offset, int numChars) {
        ByteBuffer buf = buffer.prepare(offset * 2, numChars * 2);
        return buf.asCharBuffer();
    }

    public void write(WritableByteChannel channel, int start, int end) throws IOException {
        buffer.write(channel, start, end);
    }
}
