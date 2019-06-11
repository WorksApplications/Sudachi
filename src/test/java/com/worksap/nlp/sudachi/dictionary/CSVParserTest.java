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

import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.StringReader;

import org.junit.Test;

public class CSVParserTest {

    @Test
    public void empty() throws IOException {
        try (CSVParser parser = new CSVParser(new StringReader(""))) {
            assertNull(parser.getNextRecord());
        }
        try (CSVParser parser = new CSVParser(new StringReader("\n"))) {
            assertTrue(parser.getNextRecord().isEmpty());
            assertNull(parser.getNextRecord());
        }
        try (CSVParser parser = new CSVParser(new StringReader("\n\n"))) {
            assertTrue(parser.getNextRecord().isEmpty());
            assertTrue(parser.getNextRecord().isEmpty());
            assertNull(parser.getNextRecord());
        }
    }

    @Test
    public void unescapedField() throws IOException {
        try (CSVParser parser = new CSVParser(new StringReader("abc,def,ghi\nabc,def,ghi"))) {
            assertThat(parser.getNextRecord(), contains("abc", "def", "ghi"));
            assertThat(parser.getNextRecord(), contains("abc", "def", "ghi"));
            assertNull(parser.getNextRecord());
        }
        try (CSVParser parser = new CSVParser(new StringReader("abc,def,"))) {
            assertThat(parser.getNextRecord(), contains("abc", "def", ""));
            assertNull(parser.getNextRecord());
        }
        try (CSVParser parser = new CSVParser(new StringReader("abc,def,\n"))) {
            assertThat(parser.getNextRecord(), contains("abc", "def", ""));
            assertNull(parser.getNextRecord());
        }
        try (CSVParser parser = new CSVParser(new StringReader(",,ghi"))) {
            assertThat(parser.getNextRecord(), contains("", "", "ghi"));
            assertNull(parser.getNextRecord());
        }
    }

    @Test
    public void escapedField() throws IOException {
        try (CSVParser parser = new CSVParser(new StringReader("abc,\"def\",ghi\nabc,def,ghi"))) {
            assertThat(parser.getNextRecord(), contains("abc", "def", "ghi"));
            assertThat(parser.getNextRecord(), contains("abc", "def", "ghi"));
            assertNull(parser.getNextRecord());
        }
        try (CSVParser parser = new CSVParser(new StringReader("abc,def,\"ghi\nabc\",def,ghi"))) {
            assertThat(parser.getNextRecord(), contains("abc", "def", "ghi\nabc", "def", "ghi"));
            assertNull(parser.getNextRecord());
        }
        try (CSVParser parser = new CSVParser(new StringReader("abc,\"def,ghi\""))) {
            assertThat(parser.getNextRecord(), contains("abc", "def,ghi"));
            assertNull(parser.getNextRecord());
        }
        try (CSVParser parser = new CSVParser(new StringReader("abc,\"def\"\"ghi\""))) {
            assertThat(parser.getNextRecord(), contains("abc", "def\"ghi"));
            assertNull(parser.getNextRecord());
        }
        try (CSVParser parser = new CSVParser(new StringReader("abc,def,\"\""))) {
            assertThat(parser.getNextRecord(), contains("abc", "def", ""));
            assertNull(parser.getNextRecord());
        }
        try (CSVParser parser = new CSVParser(new StringReader("abc,def,\"\"\n"))) {
            assertThat(parser.getNextRecord(), contains("abc", "def", ""));
            assertNull(parser.getNextRecord());
        }
        try (CSVParser parser = new CSVParser(new StringReader("\"\",\"\",ghi"))) {
            assertThat(parser.getNextRecord(), contains("", "", "ghi"));
            assertNull(parser.getNextRecord());
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void escapedFieldWithExtraText() throws IOException {
        try (CSVParser parser = new CSVParser(new StringReader("\"abc\"def"))) {
            parser.getNextRecord();
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void unClosedEscapedField() throws IOException {
        try (CSVParser parser = new CSVParser(new StringReader("\"abc"))) {
            parser.getNextRecord();
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void unscapedFieldWithDoubleQuote() throws IOException {
        try (CSVParser parser = new CSVParser(new StringReader("a\"bc"))) {
            parser.getNextRecord();
        }
    }
}