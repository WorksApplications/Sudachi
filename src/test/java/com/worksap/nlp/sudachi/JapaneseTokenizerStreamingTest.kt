/*
 * Copyright (c) 2023 Works Applications Co., Ltd.
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

class JapaneseTokenizerStreamingTest {
  private val tokenizer = TestDictionary.user0().create()

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
    val reader = StringReader("あ".repeat(5000))
    val result = tokenizer.tokenizeSentences(Tokenizer.SplitMode.C, reader)
    val totalLength = result.sumOf { sent -> sent.sumOf { mrph -> mrph.end() - mrph.begin() } }
    assertEquals(5000, totalLength)
  }

  @Test
  fun streamingTestWithBadReader() {
    val reader = BadReader("あ".repeat(5000))
    val result = tokenizer.tokenizeSentences(Tokenizer.SplitMode.C, reader)
    val totalLength = result.sumOf { sent -> sent.sumOf { mrph -> mrph.end() - mrph.begin() } }
    assertEquals(5000, totalLength)
  }
}
