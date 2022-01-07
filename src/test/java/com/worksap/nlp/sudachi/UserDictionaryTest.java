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

import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

public class UserDictionaryTest {

    @Test
    public void fullUserDict() throws IOException {
        TestDictionary instance = TestDictionary.INSTANCE;
        Config config = instance.user0Cfg();

        for (int i = 0; i < 13; i++) {
            config.addUserDictionary(instance.getUserDict1());
        }
        config.addUserDictionary(instance.getUserDict2());

        try (Dictionary dict = new DictionaryFactory().create(config)) {
            Tokenizer tokenizer = dict.create();
            List<Morpheme> morphs = tokenizer.tokenize("ぴさる");
            assertThat(morphs.size(), is(1));
            Morpheme m = morphs.get(0);
            assertThat(m.getDictionaryId(), is(14));
            assertThat(m.normalizedForm(), is("ぴさる"));
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void openTooManyUserDict() throws IOException {
        TestDictionary instance = TestDictionary.INSTANCE;
        Config config = instance.user0Cfg();
        for (int i = 0; i < 15; i++) {
            config.addUserDictionary(instance.getUserDict1());
        }
        new DictionaryFactory().create(config);
    }

    @Test
    public void splitForUserDict() throws IOException {
        TestDictionary td = TestDictionary.INSTANCE;
        Config config = td.user0Cfg().addUserDictionary(td.getUserDict2()).addUserDictionary(td.getUserDict1());
        try (Dictionary dict = new DictionaryFactory().create(config)) {
            Tokenizer tokenizer = dict.create();
            List<Morpheme> morphs = tokenizer.tokenize("東京府");
            assertThat(morphs.size(), is(1));
            Morpheme m = morphs.get(0);
            List<Morpheme> splits = m.split(Tokenizer.SplitMode.A);
            assertThat(splits.size(), is(2));
            assertThat(splits.get(0).surface(), is("東京"));
            assertThat(splits.get(1).surface(), is("府"));
        }
    }

    @Test
    public void userDefinedPos() throws IOException {
        Config config = TestDictionary.INSTANCE.user2Cfg();
        try (Dictionary dict = new DictionaryFactory().create(config)) {
            Tokenizer tokenizer = dict.create();
            List<Morpheme> morphs = tokenizer.tokenize("すだちかぼす");
            assertThat(morphs.size(), is(2));
            Morpheme m = morphs.get(0);
            assertThat(m.partOfSpeech(), contains("被子植物門", "双子葉植物綱", "ムクロジ目", "ミカン科", "ミカン属", "スダチ"));
            m = morphs.get(1);
            assertThat(m.partOfSpeech(), contains("被子植物門", "双子葉植物綱", "ムクロジ目", "ミカン科", "ミカン属", "カボス"));
        }

        TestDictionary td = TestDictionary.INSTANCE;
        config = td.user0Cfg().addUserDictionary(td.getUserDict2()).addUserDictionary(td.getUserDict1());
        try (Dictionary dict = new DictionaryFactory().create(config)) {
            Tokenizer tokenizer = dict.create();
            List<Morpheme> morphs = tokenizer.tokenize("すだちかぼす");
            assertThat(morphs.size(), is(2));
            Morpheme m = morphs.get(0);
            assertThat(m.partOfSpeech(), contains("被子植物門", "双子葉植物綱", "ムクロジ目", "ミカン科", "ミカン属", "スダチ"));
            m = morphs.get(1);
            assertThat(m.partOfSpeech(), contains("被子植物門", "双子葉植物綱", "ムクロジ目", "ミカン科", "ミカン属", "カボス"));
        }
    }
}
