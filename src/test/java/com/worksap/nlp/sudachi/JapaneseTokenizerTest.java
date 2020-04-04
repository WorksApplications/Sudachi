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
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class JapaneseTokenizerTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    Dictionary dict;
    JapaneseTokenizer tokenizer;

    @Before
    public void setUp() throws IOException {
        Utils.copyResource(temporaryFolder.getRoot().toPath(), "/system.dic", "/user.dic", "/char.def", "/unk.def");

        String path = temporaryFolder.getRoot().getPath();
        String settings = Utils.readAllResource("/sudachi.json");
        dict = new DictionaryFactory().create(path, settings);
        tokenizer = (JapaneseTokenizer) dict.create();
    }

    @Test
    public void tokenizeSmallKatakanaOnly() {
        assertThat(tokenizer.tokenize("ァ").size(), is(1));
    }

    @Test
    public void partOfSpeech() {
        List<Morpheme> ms = tokenizer.tokenize("京都");
        assertThat(ms.size(), is(1));
        Morpheme m = ms.get(0);
        short pid = m.partOfSpeechId();
        assertTrue(dict.getPartOfSpeechSize() > pid);
        List<String> pos = m.partOfSpeech();
        assertThat(dict.getPartOfSpeechString(pid), is(equalTo(pos)));
    }

    @Test
    public void getWordId() {
        List<Morpheme> ms = tokenizer.tokenize("京都");
        assertThat(ms.size(), is(1));
        int wid = ms.get(0).getWordId();

        ms = tokenizer.tokenize("ぴらる");
        assertThat(ms.size(), is(1));
        assertNotSame(wid, ms.get(0).getWordId());

        ms = tokenizer.tokenize("京");
        assertThat(ms.size(), is(1));
        ms.get(0).getWordId();
    }

    @Test
    public void getDictionaryId() {
        List<Morpheme> ms = tokenizer.tokenize("京都");
        assertThat(ms.size(), is(1));
        assertThat(ms.get(0).getDictionaryId(), is(0));

        ms = tokenizer.tokenize("ぴらる");
        assertThat(ms.size(), is(1));
        assertThat(ms.get(0).getDictionaryId(), is(1));

        ms = tokenizer.tokenize("京");
        assertThat(ms.size(), is(1));
        assertTrue(ms.get(0).getDictionaryId() < 0);
    }

    @Test
    public void tokenizeKanjiAlphabetWord() {
        assertThat(tokenizer.tokenize("特a").size(), is(1));
        assertThat(tokenizer.tokenize("ab").size(), is(1));
        assertThat(tokenizer.tokenize("特ab").size(), is(2));
    }

    @Test
    public void tokenizeSentences() {
        Iterator<List<Morpheme>> it = tokenizer.tokenizeSentences("京都。東京.東京都。").iterator();
        assertThat(it.hasNext(), is(true));
        assertThat(it.next().size(), is(2));
        assertThat(it.hasNext(), is(true));
        assertThat(it.next().size(), is(2));
        assertThat(it.hasNext(), is(true));
        assertThat(it.next().size(), is(2));
        assertThat(it.hasNext(), is(false));

        it = tokenizer.tokenizeSentences("な。なに。").iterator();
        assertThat(it.hasNext(), is(true));
        assertThat(it.next().size(), is(3));
        assertThat(it.hasNext(), is(false));
    }

    @Test
    public void tokenizerWithDots() {
        List<Morpheme> s = tokenizer.tokenize("京都…");
        assertThat(s.size(), is(4));
        assertThat(s.get(1).surface(), is("…"));
        assertThat(s.get(1).normalizedForm(), is("."));
        assertThat(s.get(2).surface(), is(""));
        assertThat(s.get(2).normalizedForm(), is("."));
        assertThat(s.get(3).surface(), is(""));
        assertThat(s.get(1).normalizedForm(), is("."));
    }

    @Test
    public void tokenizerWithModifiedChar() {
        Iterator<List<Morpheme>> it = tokenizer.tokenizeSentences("´´").iterator();
        assertThat(it.hasNext(), is(true));
        assertThat(it.next().size(), is(4));
        assertThat(it.hasNext(), is(false));
    }
}
