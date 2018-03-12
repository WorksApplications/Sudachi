/*
 * Copyright (c) 2018 Works Applications Co., Ltd.
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

package com.worksap.nlp.sudachi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class NumericParserTest {

    NumericParser parser;

    @Before
    public void setUp() {
        parser = new NumericParser();
    }

    @Test
    public void digits() {
        parse("1000");
        assertTrue(parser.done());
        assertEquals("1000", parser.getNormalized());
        parser.clear();
    }

    @Test
    public void startsWithZero() {
        parse("001000");
        assertTrue(parser.done());
        assertEquals("1000", parser.getNormalized());
        parser.clear();

        parse("〇一〇〇〇");
        assertTrue(parser.done());
        assertEquals("1000", parser.getNormalized());
        parser.clear();

        parse("00.1000");
        assertTrue(parser.done());
        assertEquals("0.1", parser.getNormalized());
        parser.clear();

        parse("000");
        assertTrue(parser.done());
        assertEquals("0", parser.getNormalized());
        parser.clear();
    }

    @Test
    public void useSmallUnit() {
        parse("二十七");
        assertTrue(parser.done());
        assertEquals("27", parser.getNormalized());
        parser.clear();

        parse("千三百二十七");
        assertTrue(parser.done());
        assertEquals("1327", parser.getNormalized());
        parser.clear();

        parse("千十七");
        assertTrue(parser.done());
        assertEquals("1017", parser.getNormalized());
        parser.clear();

        parse("千三百二十七.〇五");
        assertTrue(parser.done());
        assertEquals("1327.05", parser.getNormalized());
        parser.clear();

        assertFalse(parse("三百二十百"));
        parser.clear();
    }

    @Test
    public void useLargeUnit() {
        parse("1万");
        assertTrue(parser.done());
        assertEquals("10000", parser.getNormalized());
        parser.clear();

        parse("千三百二十七万");
        assertTrue(parser.done());
        assertEquals("13270000", parser.getNormalized());
        parser.clear();

        parse("千三百二十七万一四");
        assertTrue(parser.done());
        assertEquals("13270014", parser.getNormalized());
        parser.clear();

        parse("千三百二十七万一四.〇五");
        assertTrue(parser.done());
        assertEquals("13270014.05", parser.getNormalized());
        parser.clear();

        parse("三兆2千億千三百二十七万一四.〇五");
        assertTrue(parser.done());
        assertEquals("3200013270014.05", parser.getNormalized());
        parser.clear();
    }

    @Test
    public void floatWithUnit() {
        parse("1.5千");
        assertTrue(parser.done());
        assertEquals("1500", parser.getNormalized());
        parser.clear();

        parse("1.5百万");
        assertTrue(parser.done());
        assertEquals("1500000", parser.getNormalized());
        parser.clear();

        parse("1.5百万1.5千20");
        assertTrue(parser.done());
        assertEquals("1501520", parser.getNormalized());
        parser.clear();

        parse("1.5千5百");
        assertFalse(parser.done());
        parser.clear();

        parse("1.5千500");
        assertFalse(parser.done());
        parser.clear();
    }

    @Test
    public void longNumeric() {
        parse("200000000000000000000万");
        assertEquals("2000000000000000000000000", parser.getNormalized());
        parser.clear();
    }

    @Test
    public void withComma() {
        parse("2,000,000");
        assertTrue(parser.done());
        assertEquals("2000000", parser.getNormalized());
        parser.clear();

        assertFalse(parse("200,00,000"));
        parser.clear();

        assertFalse(parse("000,000"));
        parser.clear();

        assertFalse(parse(",000"));
        parser.clear();
    }

    @Test
    public void notDigit() {
        assertFalse(parse("@@@"));
        parser.clear();
    }

    @Test
    public void duplicatedPoint() {
        assertFalse(parse("1.2.3"));
        parser.clear();
    }

    boolean parse(String s) {
        for (char c : s.toCharArray()) {
            if (!parser.append(c)) {
                return false;
            }
        }
        return true;
    }
}
