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

package com.worksap.nlp.sudachi.dictionary

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.SeekableByteChannel

class MemChannelJmh : SeekableByteChannel {
  private var buffer: ByteBuffer = ByteBuffer.allocate(1024 * 1024)
  private var size = 0L

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
    val newSize = buffer.capacity() * 2
    val newBuf = ByteBuffer.allocate(newSize)
    newBuf.order(ByteOrder.LITTLE_ENDIAN)
    buffer.flip()
    newBuf.put(buffer)
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
