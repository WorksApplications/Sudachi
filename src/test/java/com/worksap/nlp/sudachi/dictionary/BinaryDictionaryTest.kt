/*
 * Copyright (c) 2025 Works Applications Co., Ltd.
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

import com.worksap.nlp.sudachi.dictionary.BinaryDictionary
import java.io.IOException
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BinaryDictionaryTest {
  @Test
  fun failToLoadSystemAsUser() {
    assertFailsWith(IOException::class) {
      BinaryDictionary.loadUser(TestDictionary.systemDictData.buffer())
    }
  }

  @Test
  fun failToLoadUserAsSystem() {
    assertFailsWith(IOException::class) {
      BinaryDictionary.loadSystem(TestDictionary.userDict1Data.buffer())
    }
  }

  @Test
  fun compatibleDicts() {
    assertTrue(TestDictionary.systemDict.isCompatibleWith(TestDictionary.systemDict))
    assertTrue(TestDictionary.userDict1.isCompatibleWith(TestDictionary.userDict1))

    assertTrue(TestDictionary.systemDict.isCompatibleWith(TestDictionary.userDict1))
    assertTrue(TestDictionary.userDict1.isCompatibleWith(TestDictionary.systemDict))
    assertTrue(TestDictionary.userDict1.isCompatibleWith(TestDictionary.userDict2))
  }

  @Test
  fun incompatibleDicts() {
    // build another system dict (should have different signature)
    val anotherSystemDict =
        BinaryDictionary.loadSystem(
            TestDictionary.buildSystemDictData("another system dictionary for the unit tests")
                .buffer())
    val anotherUserDict =
        BinaryDictionary.loadUser(
            TestDictionary.buildUserDictData(anotherSystemDict, res("/dict/user.csv")).buffer())
    assertTrue(anotherSystemDict.isCompatibleWith(anotherUserDict))

    assertFalse(anotherSystemDict.isCompatibleWith(TestDictionary.systemDict))
    assertFalse(anotherSystemDict.isCompatibleWith(TestDictionary.userDict1))
    assertFalse(anotherUserDict.isCompatibleWith(TestDictionary.systemDict))
    assertFalse(anotherUserDict.isCompatibleWith(TestDictionary.userDict2))
  }
}
