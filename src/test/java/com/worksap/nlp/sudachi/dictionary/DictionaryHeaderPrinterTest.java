/*
 * Copyright (c) 2021 Works Applications Co., Ltd.
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

import static org.hamcrest.core.StringStartsWith.startsWith;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import com.worksap.nlp.sudachi.TestDictionary;
import com.worksap.nlp.sudachi.Utils;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class DictionaryHeaderPrinterTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Before
    public void setUp() throws IOException {
        TestDictionary.INSTANCE.getSystemDictData().writeData(temporaryFolder.getRoot().toPath().resolve("system.dic"));
        TestDictionary.INSTANCE.getUserDict1Data().writeData(temporaryFolder.getRoot().toPath().resolve("user.dic"));
        Utils.copyResource(temporaryFolder.getRoot().toPath(), "/unk.def");
    }

    @Test
    public void printHeaderWithSystemDict() throws IOException {
        File inputFile = new File(temporaryFolder.getRoot(), "system.dic");
        String[] actuals;
        try (ByteArrayOutputStream output = new ByteArrayOutputStream(); PrintStream ps = new PrintStream(output)) {
            DictionaryHeaderPrinter.printHeader(inputFile.getPath(), ps);
            actuals = output.toString().split(System.lineSeparator());
        }
        assertThat(actuals.length, is(4));
        assertThat(actuals[0], is("filename: " + inputFile.getPath()));
        assertThat(actuals[1], is("type: system dictionary"));
        assertThat(actuals[2], is(startsWith("createTime: ")));
        assertThat(actuals[3], is("description: the system dictionary for the unit tests"));
    }

    @Test
    public void printHeaderWithUserDict() throws IOException {
        File inputFile = new File(temporaryFolder.getRoot(), "user.dic");
        String[] actuals;
        try (ByteArrayOutputStream output = new ByteArrayOutputStream(); PrintStream ps = new PrintStream(output)) {
            DictionaryHeaderPrinter.printHeader(inputFile.getPath(), ps);
            actuals = output.toString().split(System.lineSeparator());
        }
        assertThat(actuals.length, is(4));
        assertThat(actuals[0], is("filename: " + inputFile.getPath()));
        assertThat(actuals[1], is("type: user dictionary"));
        assertThat(actuals[2], is(startsWith("createTime: ")));
        assertThat(actuals[3], is("description: "));
    }

    @Test
    public void printHeaderWithInvalidFile() throws IOException {
        File inputFile = new File(temporaryFolder.getRoot(), "unk.def");
        String[] actuals;
        try (ByteArrayOutputStream output = new ByteArrayOutputStream(); PrintStream ps = new PrintStream(output)) {
            DictionaryHeaderPrinter.printHeader(inputFile.getPath(), ps);
            actuals = output.toString().split(System.lineSeparator());
        }
        assertThat(actuals.length, is(2));
        assertThat(actuals[0], is("filename: " + inputFile.getPath().replaceAll("\r", "")));
        assertThat(actuals[1], is("invalid file"));
    }
}