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

package com.worksap.nlp.sudachi;

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
        assertThat(detector.getEOS("あいう。えお。"), is(4));
        assertThat(detector.getEOS("あいうえお"), is(5));
        assertThat(detector.getEOS(""), is(0));
    }

    @Test
    public void getEOSWithLimit() {
        detector = new SentenceDetector(5);
        assertThat(detector.getEOS("あい。うえお。"), is(3));
        assertThat(detector.getEOS("あいうえおか"), is(5));
        assertThat(detector.getEOS("あいうえお。"), is(5));
        assertThat(detector.getEOS("あい うえお"), is(3));
        assertThat(detector.getEOS("あ い うえお"), is(4));
    }

    @Test
    public void getEOSWithPeriod() {
        assertThat(detector.getEOS("あいう.えお"), is(4));
        assertThat(detector.getEOS("3.141"), is(5));
        assertThat(detector.getEOS("四百十．〇"), is(5));
    }

    @Test
    public void getEOSWithParenthesis() {
        assertThat(detector.getEOS("あ（いう。え）お"), is(8));
        assertThat(detector.getEOS("（あ（いう）。え）お"), is(10));
        assertThat(detector.getEOS("あ（いう）。えお"), is(6));
    }

    @Test
    public void getEOSWithProhibitedBOS() {
        assertThat(detector.getEOS("あいう?えお"), is(4));
        assertThat(detector.getEOS("あいう?)えお"), is(5));
        assertThat(detector.getEOS("あいう?,えお"), is(5));
    }

    @Test
    public void getEOSWithContinuousPhrase() {
        assertThat(detector.getEOS("あいう?です。"), is(7));
        assertThat(detector.getEOS("あいう?って。"), is(7));
        assertThat(detector.getEOS("あいう?という。"), is(8));
        assertThat(detector.getEOS("あいう?の?です。"), is(4));

        assertThat(detector.getEOS("1.と2.が。"), is(7));
        assertThat(detector.getEOS("1.やb.が。"), is(7));
        assertThat(detector.getEOS("1.の12.が。"), is(8));
    }

}