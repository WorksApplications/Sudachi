/*
 * Copyright (c) 2017 Works Applications Co., Ltd.
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

package com.worksap.nlp.sudachi.dictionary;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.junit.Assert.*;
import org.junit.*;

public class CharacterCategoryTest {

    private CharacterCategory category;

    @Before
    public void setUp() throws IOException {
        category = new CharacterCategory();
        category.readCharacterDefinition(null);
    }

    @Test
    public void getCategoryTypes() {
        assertThat(category.getCategoryTypes(Character.codePointAt("熙", 0)),
                   hasItems(CategoryType.KANJI));
        assertThat(category.getCategoryTypes(Character.codePointAt("熙", 0)),
                   not(hasItems(CategoryType.DEFAULT)));
    }
}
