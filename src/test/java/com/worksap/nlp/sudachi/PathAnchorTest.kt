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

import com.worksap.nlp.sudachi.Config.Resource
import com.worksap.nlp.sudachi.dictionary.build.DicBuilder
import java.nio.file.Paths
import kotlin.test.*

class PathAnchorTest {
  @Test
  fun classpath() {
    assertNotNull(PathAnchor.classpath())
  }

  @Test
  fun classpathOfClass() {
    val resolver = PathAnchor.classpath(DicBuilder::class.java)
    val path = resolver.resolve("one.csv")
    assertTrue(resolver.exists(path))
    val path2 = resolver.resolve("doesnotexist.file")
    assertFalse(resolver.exists(path2))
  }

  @Test
  fun chainNone() {
    assertEquals(PathAnchor.none(), PathAnchor.none().andThen(PathAnchor.none()))
    assertEquals(PathAnchor.classpath(), PathAnchor.classpath().andThen(PathAnchor.classpath()))
  }

  @Test
  fun chain() {
    val chain = PathAnchor.classpath().andThen(PathAnchor.none())
    assertIs<PathAnchor.Chain>(chain)
    assertEquals(chain.count(), 2)
    val chain2 = chain.andThen(chain)
    assertSame(chain, chain2)
  }

  @Test
  fun chainChains() {
    val chain1 = PathAnchor.classpath().andThen(PathAnchor.none())
    val chain2 = PathAnchor.classpath().andThen(PathAnchor.none())
    val chain3 = chain1.andThen(chain2)
    assertIs<PathAnchor.Chain>(chain3)
    assertEquals(chain3.count(), 2)
  }

  @Test
  fun chainChains2() {
    val chain1 = PathAnchor.classpath().andThen(PathAnchor.filesystem(Paths.get("")))
    val chain3 = PathAnchor.none().andThen(chain1)
    assertIs<PathAnchor.Chain>(chain3)
    assertEquals(chain3.count(), 3)
  }

  @Test
  fun chainExists() {
    val a1 = PathAnchor.classpath(DicBuilder::class.java)
    assertTrue(a1.exists(a1.resolve("one.csv")))
    val a2 = PathAnchor.none()
    assertFalse(a2.exists(a2.resolve("one.csv")))
    val a3 = a2.andThen(a1)
    assertTrue(a3.exists(a3.resolve("one.csv")))
  }

  @Test
  fun hashCodeWorks() {
    val a =
        PathAnchor.classpath()
            .andThen(PathAnchor.filesystem(Paths.get("")))
            .andThen(PathAnchor.none())
    assertNotEquals(a.hashCode(), PathAnchor.none().hashCode())
    assertNotEquals(a, PathAnchor.none())
  }

  @Test
  fun notExistFile() {
    val a = PathAnchor.filesystem(Paths.get(""))
    assertIsNot<Resource.NotFound<*>>(a.toResource<Any>(Paths.get(".gitignore")))
    val x = assertIs<Resource.NotFound<*>>(a.toResource<Any>(Paths.get(".gitignore2")))
    assertFails { x.asByteBuffer() }
    assertFails { x.asInputStream() }
    assertFails { x.consume { throw java.lang.RuntimeException() } }
  }

  @Test
  fun notExistClasspath() {
    val a = PathAnchor.classpath()
    assertIsNot<Resource.NotFound<*>>(a.toResource<Any>(Paths.get("char.def")))
    assertIs<Resource.NotFound<*>>(a.toResource<Any>(Paths.get("char.def2")))
  }

  @Test
  fun notExistChain() {
    val a = PathAnchor.filesystem(Paths.get("")).andThen(PathAnchor.classpath())
    assertIsNot<Resource.NotFound<*>>(a.toResource<Any>(Paths.get("char.def")))
    val x = assertIs<Resource.NotFound<*>>(a.toResource<Any>(Paths.get("char.def2")))
    assertFails { x.asByteBuffer() }
    assertFails { x.asInputStream() }
  }
}
