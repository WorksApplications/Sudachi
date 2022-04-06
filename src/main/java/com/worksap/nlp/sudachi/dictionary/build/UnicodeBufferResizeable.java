package com.worksap.nlp.sudachi.dictionary.build;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.channels.WritableByteChannel;

public class UnicodeBufferResizeable {
    private ByteBuffer buffer;

    public UnicodeBufferResizeable(int size) {
        this.buffer = ByteBuffer.allocate(size);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
    }

    public UnicodeBufferResizeable() {
        this(64 * 1024);
    }

    public void put(int offset, String data, int start, int end) {
        CharBuffer chars = prepare(offset, end - start);
        chars.put(data, start, end);
    }

    private CharBuffer prepare(int offset, int numChars) {
        buffer.position(offset);
        int remaining = buffer.remaining();
        int byteLength = numChars * 2;
        while (remaining < byteLength) {
            ByteBuffer newBuffer = ByteBuffer.allocate(buffer.capacity() * 2);
            newBuffer.order(ByteOrder.LITTLE_ENDIAN);
            buffer.flip();
            newBuffer.put(buffer);
            buffer = newBuffer;
            remaining = newBuffer.remaining();
        }
        CharBuffer chars = buffer.asCharBuffer();
        buffer.position(buffer.position() + byteLength);
        return chars;
    }

    public void write(SeekableByteChannel channel) throws IOException {
        buffer.flip();
        channel.write(buffer);
        buffer.clear();
    }
}
