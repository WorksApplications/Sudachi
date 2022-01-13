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

import kotlin.test.Test
import kotlin.test.assertEquals

class MorphemeImplTest {
  @Test
  fun useToString() {
    val dic = TestDictionary.user0()
    val sudachi = dic.create().tokenize("すだち")
    assertEquals(
        "MorphemeImpl{begin=0, end=1, surface=す, pos=4/名詞,普通名詞,一般,*,*,*, wid=(0,0)}",
        sudachi[0].toString())
  }
}
