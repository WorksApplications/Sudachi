/*
 * Copyright (c) 2024 Works Applications Co., Ltd.
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

import com.worksap.nlp.sudachi.dictionary.CharacterCategory
import com.worksap.nlp.sudachi.dictionary.DictionaryAccess
import com.worksap.nlp.sudachi.dictionary.POS
import java.net.URL

fun DictionaryAccess.setCharacterCategory(
    url: URL = javaClass.getResource("char.def")
): DictionaryAccess {
  val resource = Config.Resource.Classpath<CharacterCategory>(url)
  this.grammar.setCharacterCategory(CharacterCategory.load(resource))
  return this
}

fun DictionaryAccess.morpheme(id: Int): Morpheme {
  val node = LatticeNodeImpl(lexicon, 0, id)
  node.setRange(0, node.getWordInfo().getLength().toInt())

  // UTF8InputTextBuilder requires charcat
  if (grammar.getCharacterCategory() == null) {
    setCharacterCategory()
  }

  val l =
      MorphemeList(
          UTF8InputTextBuilder(node.surface, grammar).build(),
          grammar,
          lexicon,
          listOf(node),
          false,
          Tokenizer.SplitMode.A)
  return l[0]
}

val String.pos: POS
  get() = POS(this.split(","))
