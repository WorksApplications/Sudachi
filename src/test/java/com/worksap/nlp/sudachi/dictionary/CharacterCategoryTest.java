/*
 * Copyright (c) 2020 Works Applications Co., Ltd.
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

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class CharacterCategoryTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void rangeContainingLength() {
        CharacterCategory.Range range = new CharacterCategory.Range();
        range.low = 0x41; // A
        range.high = 0x5A; // Z
        assertThat(range.containingLength("ABC12"), is(3));
        assertThat(range.containingLength("熙"), is(0));
    }

    @Test
    public void getCategoryTypes() throws IOException {
        CharacterCategory category = new CharacterCategory();
        category.readCharacterDefinition(null);
        assertThat(category.getCategoryTypes(Character.codePointAt("熙", 0)), hasItems(CategoryType.KANJI));
        assertThat(category.getCategoryTypes(Character.codePointAt("熙", 0)), not(hasItems(CategoryType.DEFAULT)));
    }

    @Test
    public void readCharacterDefinition() throws IOException {
        File inputFile = temporaryFolder.newFile();
        try (FileWriter writer = new FileWriter(inputFile)) {
            writer.write("#\n \n");
            writer.write("0x0030..0x0039 NUMERIC\n");
            writer.write("0x3007         KANJI\n");
        }
        CharacterCategory category = new CharacterCategory();
        category.readCharacterDefinition(inputFile.getPath());
        assertThat(category.getCategoryTypes(0x0030), hasItems(CategoryType.NUMERIC));
        assertThat(category.getCategoryTypes(0x0039), hasItems(CategoryType.NUMERIC));
        assertThat(category.getCategoryTypes(0x3007), hasItems(CategoryType.KANJI));
    }

    @Test(expected = IllegalArgumentException.class)
    public void readCharacterDefinitionWithInvalidFormat() throws IOException {
        File inputFile = temporaryFolder.newFile();
        try (FileWriter writer = new FileWriter(inputFile)) {
            writer.write("0x0030..0x0039\n");
        }
        CharacterCategory category = new CharacterCategory();
        category.readCharacterDefinition(inputFile.getPath());
    }

    @Test(expected = IllegalArgumentException.class)
    public void readCharacterDefinitionWithInvalidRange() throws IOException {
        File inputFile = temporaryFolder.newFile();
        try (FileWriter writer = new FileWriter(inputFile)) {
            writer.write("0x0030..0x0029 NUMERIC\n");
        }
        CharacterCategory category = new CharacterCategory();
        category.readCharacterDefinition(inputFile.getPath());
    }

    @Test(expected = IllegalArgumentException.class)
    public void readCharacterDefinitionWithInvalidType() throws IOException {
        File inputFile = temporaryFolder.newFile();
        try (FileWriter writer = new FileWriter(inputFile)) {
            writer.write("0x0030..0x0039 FOO\n");
        }
        CharacterCategory category = new CharacterCategory();
        category.readCharacterDefinition(inputFile.getPath());
    }
}
