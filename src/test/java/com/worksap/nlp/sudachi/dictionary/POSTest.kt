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

import kotlin.test.assertFails
import org.junit.Test

class POSTest {
  @Test
  fun invalidPos() {
    assertFails { POS() }
    assertFails { POS("1") }
    assertFails { POS("1", "2") }
    assertFails { POS("1", "2", "3") }
    assertFails { POS("1", "2", "3", "4") }
    assertFails { POS("1", "2", "3", "4", "5") }
    assertFails { POS("1", "2", "3", "4", "5", null) }
    assertFails { POS("1", "2", "3", "4", "5", "6", "7") }
    assertFails { POS("1", "2", "3", "4", "5", "6".repeat(POS.MAX_COMPONENT_LENGTH + 1)) }
  }
}
