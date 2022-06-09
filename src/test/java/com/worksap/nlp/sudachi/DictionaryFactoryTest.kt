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

package com.worksap.nlp.sudachi

import org.junit.Test
import kotlin.test.assertFails

class DictionaryFactoryTest {
  @Test
  @Deprecated(
      "testing deprecated methods",
      ReplaceWith("DictionaryFactory().create()", "com.worksap.nlp.sudachi.DictionaryFactory"))
  fun everythingNull() {
    val error = assertFails { DictionaryFactory().create(null, null, false) }
    assert(error.message!!.contains("Failed to resolve file: system.dic"))
  }

  @Test
  @Deprecated(
      "testing deprecated methods",
      ReplaceWith("DictionaryFactory().create()", "com.worksap.nlp.sudachi.DictionaryFactory"))
  fun notNullPath() {
    val error = assertFails { DictionaryFactory().create("does-not-exist", null, false) }
    assert(error.message!!.contains("base=does-not-exist"))
  }

  @Test
  @Deprecated(
      "testing deprecated methods",
      ReplaceWith("DictionaryFactory().create()", "com.worksap.nlp.sudachi.DictionaryFactory"))
  fun notNullPathSettings() {
    val error = assertFails {
      DictionaryFactory().create("", """{"systemDict": "test.dic"}""", true)
    }
    assert(error.message!!.contains("Failed to resolve file: test.dic"))
  }
}
