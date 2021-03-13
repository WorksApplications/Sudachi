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
import static org.hamcrest.CoreMatchers.isA;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.IOException;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class JapaneseDictionaryTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    String path;
    String settings;
    Dictionary dict;

    @Before
    public void setUp() throws IOException {
        Utils.copyResource(temporaryFolder.getRoot().toPath(), "/system.dic", "/user.dic", "/char.def", "/unk.def");

        path = temporaryFolder.getRoot().getPath();
        settings = Utils.readAllResource("/sudachi.json");
        dict = new DictionaryFactory().create(path, settings);
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
        assertThat(dict.getPartOfSpeechSize(), is(9));
    }

    @Test
    public void getPartOfSpeechString() {
        List<String> pos = dict.getPartOfSpeechString((short) 0);
        assertThat(pos, notNullValue());
        assertThat(pos.get(0), is("助動詞"));
    }

    @Test
    public void createWithMergingSettings() throws IOException {
        dict.close();
        dict = new DictionaryFactory().create(path, "{\"userDict\":[]}", true);
        Tokenizer t = dict.create();
        assertThat(t.tokenize("東京府").size(), is(2));
    }

    @Test
    public void createWithMergingNullSettings() throws IOException {
        dict.close();
        dict = new DictionaryFactory().create(path, null, true);
        Tokenizer t = dict.create();
        assertThat(t.tokenize("東京府").size(), is(1));
    }
}
