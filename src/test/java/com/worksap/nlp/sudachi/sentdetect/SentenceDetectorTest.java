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

package com.worksap.nlp.sudachi.sentdetect;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

public class SentenceDetectorTest {

    private SentenceDetector detector;

    @Before
    public void setUp() {
        detector = new SentenceDetector();
    }

    @Test
    public void getEOS() {
        assertThat(detector.getEos("あいう。えお。", null), is(4));
        assertThat(detector.getEos("あいう。えお。", null), is(4));
        assertThat(detector.getEos("あいうえお", null), is(5));
        assertThat(detector.getEos("", null), is(0));
        assertThat(detector.getEos("あいう。。えお。", null), is(5));
    }

    @Test
    public void getEOSWithLimit() {
        detector = new SentenceDetector(5);
        assertThat(detector.getEos("あい。うえお。", null), is(3));
        assertThat(detector.getEos("あいうえおか", null), is(5));
        assertThat(detector.getEos("あいうえお。", null), is(5));
        assertThat(detector.getEos("あい うえお", null), is(3));
        assertThat(detector.getEos("あ い うえお", null), is(4));
    }

    @Test
    public void getEOSWithPeriod() {
        assertThat(detector.getEos("あいう.えお", null), is(4));
        assertThat(detector.getEos("3.141", null), is(5));
        assertThat(detector.getEos("四百十．〇", null), is(5));
    }

    @Test
    public void getEOSWithParenthesis() {
        assertThat(detector.getEos("あ（いう。え）お", null), is(8));
        assertThat(detector.getEos("（あ（いう）。え）お", null), is(10));
        assertThat(detector.getEos("あ（いう）。えお", null), is(6));
    }

    @Test
    public void getEOSWithProhibitedBOS() {
        assertThat(detector.getEos("あいう?えお", null), is(4));
        assertThat(detector.getEos("あいう?)えお", null), is(5));
        assertThat(detector.getEos("あいう?,えお", null), is(5));
    }

    @Test
    public void getEOSWithContinuousPhrase() {
        assertThat(detector.getEos("あいう?です。", null), is(7));
        assertThat(detector.getEos("あいう?って。", null), is(7));
        assertThat(detector.getEos("あいう?という。", null), is(8));
        assertThat(detector.getEos("あいう?の?です。", null), is(4));

        assertThat(detector.getEos("1.と2.が。", null), is(7));
        assertThat(detector.getEos("1.やb.が。", null), is(7));
        assertThat(detector.getEos("1.の12.が。", null), is(8));
    }

    class Checker implements SentenceDetector.NonBreakCheker {
        private String text;

        Checker(String text) {
            this.text = text;
        }

        @Override
        public boolean hasNonBreakWord(int eos) {
            return text.substring(eos - 2).startsWith("な。な");
        }
    }

    @Test
    public void getEOSWithNonBreakWord() {
        String text = "ばな。なです。";
        Checker checker = new Checker(text);
        assertThat(detector.getEos(text, checker), is(7));
    }
}