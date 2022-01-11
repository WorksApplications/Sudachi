/*
 * Copyright (c) 2017-2022 Works Applications Co., Ltd.
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
import com.worksap.nlp.sudachi.dictionary.build.DicBuilder
import com.worksap.nlp.sudachi.dictionary.build.MemChannel
import com.worksap.nlp.sudachi.dictionary.build.res

/** Utility for lazily creating binary dictionaries for test */
object TestDictionary {
  val systemDictData: MemChannel by lazy {
    val result = MemChannel()
    DicBuilder.system()
        .matrix(res("/dict/matrix.def"))
        .lexicon(res("/dict/lex.csv"))
        .description("the system dictionary for the unit tests")
        .build(result)
    result
  }

  val userDict1Data: MemChannel by lazy {
    val chan = MemChannel()
    DicBuilder.user(systemDict).lexicon(res("/dict/user.csv")).build(chan)
    chan
  }

  val systemDict: BinaryDictionary
    get() = BinaryDictionary.loadSystem(systemDictData.buffer())

  val userDict1: BinaryDictionary
    get() = BinaryDictionary.loadUser(userDict1Data.buffer())

  val userDict2: BinaryDictionary by lazy {
    val chan = MemChannel()
    DicBuilder.user(systemDict).lexicon(res("/dict/user2.csv")).build(chan)
    BinaryDictionary.loadUser(chan.buffer())
  }

  fun user0Cfg(): Config {
    return Config.fromClasspath().clearUserDictionaries().systemDictionary(systemDict)
  }

  fun user1Cfg(): Config {
    return user0Cfg().addUserDictionary(userDict1)
  }

  fun user2Cfg(): Config {
    return user1Cfg().addUserDictionary(userDict2)
  }

  /** System only */
  fun user0(): JapaneseDictionary {
    return DictionaryFactory().create(user0Cfg()) as JapaneseDictionary
  }

  /** System + One User dictionary */
  fun user1(): JapaneseDictionary {
    return DictionaryFactory().create(user1Cfg()) as JapaneseDictionary
  }
}
