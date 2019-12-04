/*
 * Copyright (c) 2019 Works Applications Co., Ltd.
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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import com.worksap.nlp.sudachi.Utils;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class DictionaryPrinterTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Before
    public void setUp() throws IOException {
        Utils.copyResource(temporaryFolder.getRoot().toPath(), "/system.dic", "/user.dic", "/unk.def");
    }

    @Test
    public void printWithSystemDict() throws IOException {
        File inputFile = new File(temporaryFolder.getRoot(), "system.dic");
        String[] actuals;
        try (ByteArrayOutputStream output = new ByteArrayOutputStream(); PrintStream ps = new PrintStream(output)) {
            DictionaryPrinter.printDictionary(inputFile.getPath(), null, ps);
            actuals = output.toString().split("\n");
        }
        assertThat(actuals.length, is(38));
        assertThat(actuals[0], is("た,1,1,8729,た,助動詞,*,*,*,助動詞-タ,終止形-一般,タ,た,*,A,*,*,*"));
    }

    @Test
    public void printWithUserDict() throws IOException {
        File inputFile = new File(temporaryFolder.getRoot(), "user.dic");
        File systemDictFile = new File(temporaryFolder.getRoot(), "system.dic");
        try (BinaryDictionary systemDict = BinaryDictionary.readSystemDictionary(systemDictFile.getPath())) {
            String[] actuals;
            try (ByteArrayOutputStream output = new ByteArrayOutputStream(); PrintStream ps = new PrintStream(output)) {
                DictionaryPrinter.printDictionary(inputFile.getPath(), systemDict, ps);
                actuals = output.toString().split("\n");
            }
            assertThat(actuals.length, is(4));
            assertThat(actuals[2], is("東京府,6,6,2816,東京府,名詞,固有名詞,地名,一般,*,*,トウキョウフ,東京府,*,B,5/U1,*,5/U1"));
            assertThat(actuals[3], is("すだち,6,6,2816,すだち,被子植物門,双子葉植物綱,ムクロジ目,ミカン科,ミカン属,スダチ,スダチ,すだち,*,A,*,*,*"));
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void printWithUserDictWithoutGrammar() throws IOException {
        File inputFile = new File(temporaryFolder.getRoot(), "user.dic");
        try (ByteArrayOutputStream output = new ByteArrayOutputStream(); PrintStream ps = new PrintStream(output)) {
            DictionaryPrinter.printDictionary(inputFile.getPath(), null, ps);
        }
    }

    @Test(expected = IOException.class)
    public void readGrammarWithInvalidFile() throws IOException {
        File inputFile = new File(temporaryFolder.getRoot(), "unk.def");
        BinaryDictionary.readSystemDictionary(inputFile.getPath());
    }
}