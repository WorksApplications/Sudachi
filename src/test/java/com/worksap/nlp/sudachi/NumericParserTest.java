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

package com.worksap.nlp.sudachi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

public class NumericParserTest {

    NumericParser parser;

    @Before
    public void setUp() {
        parser = new NumericParser();
    }

    @Test
    public void digits() {
        assertTrue(parse("1000"));
        assertEquals("1000", parser.getNormalized());
        parser.clear();
    }

    @Test
    public void startsWithZero() {
        assertTrue(parse("001000"));
        assertEquals("001000", parser.getNormalized());
        parser.clear();

        assertTrue(parse("〇一〇〇〇"));
        assertEquals("01000", parser.getNormalized());
        parser.clear();

        assertTrue(parse("00.1000"));
        assertEquals("00.1", parser.getNormalized());
        parser.clear();

        assertTrue(parse("000"));
        assertEquals("000", parser.getNormalized());
        parser.clear();
    }

    @Test
    public void useSmallUnit() {
        assertTrue(parse("二十七"));
        assertEquals("27", parser.getNormalized());
        parser.clear();

        assertTrue(parse("千三百二十七"));
        assertEquals("1327", parser.getNormalized());
        parser.clear();

        assertTrue(parse("千十七"));
        assertEquals("1017", parser.getNormalized());
        parser.clear();

        assertTrue(parse("千三百二十七.〇五"));
        assertEquals("1327.05", parser.getNormalized());
        parser.clear();

        assertFalse(parse("三百二十百"));
        parser.clear();
    }

    @Test
    public void useLargeUnit() {
        assertTrue(parse("1万"));
        assertEquals("10000", parser.getNormalized());
        parser.clear();

        assertTrue(parse("千三百二十七万"));
        assertEquals("13270000", parser.getNormalized());
        parser.clear();

        assertTrue(parse("千三百二十七万一四"));
        assertEquals("13270014", parser.getNormalized());
        parser.clear();

        assertTrue(parse("千三百二十七万一四.〇五"));
        assertEquals("13270014.05", parser.getNormalized());
        parser.clear();

        assertTrue(parse("三兆2千億千三百二十七万一四.〇五"));
        assertEquals("3200013270014.05", parser.getNormalized());
        parser.clear();

        assertFalse(parse("億万"));
        parser.clear();
    }

    @Test
    public void floatWithUnit() {
        assertTrue(parse("1.5千"));
        assertEquals("1500", parser.getNormalized());
        parser.clear();

        assertTrue(parse("1.5百万"));
        assertEquals("1500000", parser.getNormalized());
        parser.clear();

        assertTrue(parse("1.5百万1.5千20"));
        assertEquals("1501520", parser.getNormalized());
        parser.clear();

        assertFalse(parse("1.5千5百"));
        parser.clear();

        assertFalse(parse("1.5千500"));
        parser.clear();
    }

    @Test
    public void longNumeric() {
        assertTrue(parse("200000000000000000000万"));
        assertEquals("2000000000000000000000000", parser.getNormalized());
        parser.clear();
    }

    @Test
    public void withComma() {
        assertTrue(parse("2,000,000"));
        assertEquals("2000000", parser.getNormalized());
        parser.clear();

        assertTrue(parse("259万2,300"));
        assertEquals("2592300", parser.getNormalized());
        parser.clear();

        assertFalse(parse("200,00,000"));
        assertEquals(NumericParser.Error.COMMA, parser.errorState);
        parser.clear();

        assertFalse(parse("2,4"));
        assertEquals(NumericParser.Error.COMMA, parser.errorState);
        parser.clear();

        assertFalse(parse("000,000"));
        assertEquals(NumericParser.Error.COMMA, parser.errorState);
        parser.clear();

        assertFalse(parse(",000"));
        assertEquals(NumericParser.Error.COMMA, parser.errorState);
        parser.clear();

        assertFalse(parse("256,55.1"));
        assertEquals(NumericParser.Error.COMMA, parser.errorState);
        parser.clear();
    }

    @Test
    public void notDigit() {
        assertFalse(parse("@@@"));
        parser.clear();
    }

    @Test
    public void floatPoint() {
        assertTrue(parse("6.0"));
        assertEquals("6", parser.getNormalized());
        parser.clear();

        assertFalse(parse("6."));
        assertEquals(NumericParser.Error.POINT, parser.errorState);
        parser.clear();

        assertFalse(parse("1.2.3"));
        assertEquals(NumericParser.Error.POINT, parser.errorState);
        parser.clear();
    }

    boolean parse(String s) {
        for (char c : s.toCharArray()) {
            if (!parser.append(c)) {
                return false;
            }
        }
        return parser.done();
    }
}
