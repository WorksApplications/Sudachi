/*
 * Copyright (c) 2017-2022 Works Applications Co., Ltd.
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
import static org.hamcrest.CoreMatchers.isA;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.IOException;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class JapaneseDictionaryTest {
    Dictionary dict;

    @Before
    public void setUp() throws IOException {
        dict = TestDictionary.INSTANCE.user0();
    }

    @After
    public void tearDown() throws IOException {
        dict.close();
    }

    @Test
    public void create() {
        assertThat(dict.create(), isA(Tokenizer.class));
    }

    @Test
    public void getPartOfSpeechSize() {
        assertThat(dict.getPartOfSpeechSize(), is(8));
    }

    @Test
    public void getPartOfSpeechString() {
        List<String> pos = dict.getPartOfSpeechString((short) 0);
        assertThat(pos, notNullValue());
        assertThat(pos.get(0), is("助動詞"));
    }

    @Test
    public void instantiateConfigWithoutCharDef() throws IOException {
        Config cfg = Config.fromClasspath("sudachi_minimum.json");
        cfg.systemDictionary(TestDictionary.INSTANCE.getSystemDict());
        try (JapaneseDictionary jd = (JapaneseDictionary) new DictionaryFactory().create(cfg)) {
            assertThat(jd, notNullValue());
            assertThat(jd.create(), notNullValue());
        }
    }
}
