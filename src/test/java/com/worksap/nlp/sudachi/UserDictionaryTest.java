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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class UserDictionaryTest {

    static final String COMMON_SETTINGS = "{\"systemDict\":\"system.dic\",\"oovProviderPlugin\":[{\"class\":\"com.worksap.nlp.sudachi.SimpleOovProviderPlugin\",\"oovPOS\":[\"名詞\",\"普通名詞\",\"一般\",\"*\",\"*\",\"*\"],\"leftId\":8,\"rightId\":8,\"cost\":6000}],\"userDict\":[";
    static final String COMMON_SETTINGS_TAIL = "]}";
    static final String USER_DICT = "\"user.dic\"";
    static final String USER_DICT2 = "\"user2.dic\"";

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    String path;

    @Before
    public void setUp() throws IOException {
        Utils.copyResource(temporaryFolder.getRoot().toPath(),
                           "/system.dic", "/user.dic", "/user2.dic");
        path = temporaryFolder.getRoot().getPath();
    }

    @Test
    public void fullUserDict() throws IOException {
        ArrayList<String> userDicts = new ArrayList<>();
        for (int i = 0; i < 14; i++) {
            userDicts.add(USER_DICT);
        }
        userDicts.add(USER_DICT2);
        String settings = COMMON_SETTINGS
            + String.join(",", userDicts) + COMMON_SETTINGS_TAIL;

        Dictionary dict = new DictionaryFactory().create(path, settings);
        Tokenizer tokenizer = dict.create();
        List<Morpheme> morphs = tokenizer.tokenize("ぴさる");
        assertThat(morphs.size(), is(1));
        Morpheme m = morphs.get(0);
        assertThat(m.getDictionaryId(), is(15));
        assertThat(m.normalizedForm(), is("ぴさる"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void openTooManyUserDict() throws IOException {
        ArrayList<String> userDicts = new ArrayList<>();
        for (int i = 0; i < 16; i++) {
            userDicts.add(USER_DICT);
        }
        String settings = COMMON_SETTINGS
            + String.join(",", userDicts) + COMMON_SETTINGS_TAIL;
        Dictionary dict = new DictionaryFactory().create(path, settings);
    }

    @Test
    public void splitForUserDict() throws IOException {
        String settings = COMMON_SETTINGS + USER_DICT2
            + ", " + USER_DICT + COMMON_SETTINGS_TAIL;
        Dictionary dict = new DictionaryFactory().create(path, settings);
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
