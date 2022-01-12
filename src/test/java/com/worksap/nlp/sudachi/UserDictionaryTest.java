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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class UserDictionaryTest {
    URL userDic1;
    URL userDic2;

    @Before
    public void setUp() throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        userDic1 = classLoader.getResource("user.dic");
        userDic2 = classLoader.getResource("user2.dic");
        assertThat(userDic1, notNullValue());
        assertThat(userDic2, notNullValue());
    }

    @Test
    public void fullUserDict() throws IOException {
        Config config = Config.fromClasspath().clearUserDictionaries();

        for (int i = 0; i < 13; i++) {
            config.addUserDictionary(userDic1);
        }
        config.addUserDictionary(userDic2);

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
        Config config = Config.fromClasspath();
        for (int i = 0; i < 14; i++) { // there is one from basic configuration
            config.addUserDictionary(userDic1);
        }
        new DictionaryFactory().create(config);
    }

    @Test
    public void splitForUserDict() throws IOException {
        Config config = Config.fromClasspath().clearUserDictionaries().addUserDictionary(userDic2)
                .addUserDictionary(userDic1);
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
        Config config = Config.fromClasspath().addUserDictionary(userDic2);
        try (Dictionary dict = new DictionaryFactory().create(config)) {
            Tokenizer tokenizer = dict.create();
            List<Morpheme> morphs = tokenizer.tokenize("すだちかぼす");
            assertThat(morphs.size(), is(2));
            Morpheme m = morphs.get(0);
            assertThat(m.partOfSpeech(), contains("被子植物門", "双子葉植物綱", "ムクロジ目", "ミカン科", "ミカン属", "スダチ"));
            m = morphs.get(1);
            assertThat(m.partOfSpeech(), contains("被子植物門", "双子葉植物綱", "ムクロジ目", "ミカン科", "ミカン属", "カボス"));
        }

        config = Config.fromClasspath().clearUserDictionaries().addUserDictionary(userDic2).addUserDictionary(userDic1);
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
