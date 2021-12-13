package com.worksap.nlp.sudachi.dictionary.build

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.SeekableByteChannel

class BytesChannel : SeekableByteChannel {
    var buffer: ByteBuffer = ByteBuffer.allocate(1024 * 1024)
    var size = 0L

    init {
        buffer.order(ByteOrder.LITTLE_ENDIAN)
    }


    override fun close() {}

    override fun isOpen(): Boolean {
        return true
    }

    override fun read(p0: ByteBuffer?): Int {
        throw UnsupportedOperationException()
    }

    override fun write(p0: ByteBuffer?): Int {
        val remaining = p0!!.remaining()
        reserve(remaining)
        buffer.put(p0)
        val pos = buffer.position().toLong()
        if (pos > size) {
            size = pos
        }
        return remaining
    }

    private fun reserve(additional: Int) {
        val remaining = buffer.remaining()
        if (additional <= remaining) {
            return
        }
        val newSize = buffer.capacity() * 2;
        val newBuf = ByteBuffer.allocate(newSize)
        newBuf.put(buffer)
        newBuf.position(buffer.position())
        newBuf.order(ByteOrder.LITTLE_ENDIAN)
        buffer = newBuf
    }

    override fun position(): Long {
        return buffer.position().toLong()
    }

    override fun position(p0: Long): SeekableByteChannel {
        buffer.position(p0.toInt())
        return this
    }

    override fun size(): Long {
        return this.size
    }

    override fun truncate(p0: Long): SeekableByteChannel {
        throw UnsupportedOperationException()
    }

    fun buffer(): ByteBuffer {
        val dup = buffer.duplicate()
        dup.position(0)
        dup.limit(buffer.position())
        dup.order(ByteOrder.LITTLE_ENDIAN)
        return dup
    }
}