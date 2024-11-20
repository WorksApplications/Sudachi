/*
 * Copyright (c) 2023-2024 Works Applications Co., Ltd.
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

package com.worksap.nlp.sudachi

import java.io.Reader
import java.io.StringReader
import kotlin.math.min
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class JapaneseTokenizerStreamingTest {
  private val tokenizer = TestDictionary.user0().tokenizer()

  class BadReader(private val data: String, private val window: Int = 512) : Reader() {

    private var position: Int = 0
    override fun read(cbuf: CharArray, off: Int, len: Int): Int {
      // mimic ICUNormalizer2CharFilter, but read in 512 char increments instead of 128 (by default)
      check(off >= 0)
      check(off < cbuf.size)
      check(len > 0)

      val dataLen = data.length
      val remaining = dataLen - position
      if (remaining == 0) {
        return -1
      }

      val toRead = min(min(window, remaining), len)
      data.toCharArray(cbuf, off, position, position + toRead)
      position += toRead
      return toRead
    }

    override fun close() {}
  }

  @Test
  fun streamingTest() {
    // Testing deprecated method `tokenizeSentences(Reader)`
    val reader = StringReader("あ".repeat(5000))
    val result = tokenizer.tokenizeSentences(Tokenizer.SplitMode.C, reader)
    val totalLength = result.sumOf { sent -> sent.sumOf { mrph -> mrph.end() - mrph.begin() } }
    assertEquals(5000, totalLength)
  }

  @Test
  fun streamingTestWithBadReader() {
    // Testing deprecated method `tokenizeSentences(Reader)`
    val reader = BadReader("あ".repeat(5000))
    val result = tokenizer.tokenizeSentences(Tokenizer.SplitMode.C, reader)
    val totalLength = result.sumOf { sent -> sent.sumOf { mrph -> mrph.end() - mrph.begin() } }
    assertEquals(5000, totalLength)
  }

  @Test
  fun streamingReadable() {
    val reader = StringReader("あ".repeat(5000))
    val result = tokenizer.lazyTokenizeSentences(Tokenizer.SplitMode.C, reader).asSequence()
    val totalLength = result.sumOf { sent -> sent.sumOf { mrph -> mrph.end() - mrph.begin() } }
    assertEquals(5000, totalLength)
  }

  @Test
  fun callingNextWithoutTextFails() {
    val reader = StringReader("東京")
    val it = tokenizer.lazyTokenizeSentences(Tokenizer.SplitMode.C, reader)

    val morphemes = it.next()
    assertEquals("東京", morphemes.get(0).surface())

    assertFailsWith<java.util.NoSuchElementException>(
        block = { it.next() },
    )
  }

  @Test
  fun streamingBlockingReadable() {
    val reader = BadReader("あ".repeat(5000))
    val result = tokenizer.lazyTokenizeSentences(Tokenizer.SplitMode.C, reader).asSequence()
    val totalLength = result.sumOf { sent -> sent.sumOf { mrph -> mrph.end() - mrph.begin() } }
    assertEquals(5000, totalLength)
  }

  @Test
  fun streamingLongTextShouldNotCauseOOM() {
    val reader = StringReader("あ".repeat(10 * 1024 * 1024))
    val result = tokenizer.lazyTokenizeSentences(Tokenizer.SplitMode.C, reader).asSequence()
    val totalLength = result.sumOf { sent -> sent.sumOf { mrph -> mrph.end() - mrph.begin() } }
    assertEquals(10 * 1024 * 1024, totalLength)
  }

  class FailReader(private val data: String) : Reader() {

    private var position: Int = 0
    override fun read(cbuf: CharArray, off: Int, len: Int): Int {
      // throws IOException after returning all the data
      check(off >= 0)
      check(off < cbuf.size)
      check(len > 0)

      val dataLen = data.length
      val remaining = dataLen - position
      if (remaining == 0) {
        throw java.io.IOException("All data used.")
      }

      val toRead = min(remaining, len)
      data.toCharArray(cbuf, off, position, position + toRead)
      position += toRead
      return toRead
    }

    override fun close() {}
  }

  @Test
  fun failsWhenReaderFails() {
    var reader = FailReader("あ".repeat(500))
    // should not fail on the instantiation
    var it = tokenizer.lazyTokenizeSentences(Tokenizer.SplitMode.C, reader)
    assertFailsWith<java.io.UncheckedIOException>(
        block = { it.hasNext() },
    )

    reader = FailReader("あ".repeat(500))
    it = tokenizer.lazyTokenizeSentences(Tokenizer.SplitMode.C, reader)
    assertFailsWith<java.io.UncheckedIOException>(
        block = { it.next() },
    )
  }
}
