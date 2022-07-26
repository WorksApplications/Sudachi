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
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.StringReader;
import java.util.Iterator;
import java.util.List;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;

import com.worksap.nlp.sudachi.sentdetect.SentenceDetector;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;

public class JapaneseTokenizerTest {
    Dictionary dict;
    JapaneseTokenizer tokenizer;

    @Before
    public void setUp() throws IOException {
        dict = TestDictionary.INSTANCE.user1();
        tokenizer = (JapaneseTokenizer) dict.create();
    }

    private static Matcher<Morpheme> morpheme(String surface, int begin, int end) {
        return new BaseMatcher<Morpheme>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("Morpheme=").appendText(surface).appendText(" [").appendValue(begin)
                        .appendText(",").appendValue(end).appendText("]");
            }

            @Override
            public boolean matches(Object actual) {
                if (!(actual instanceof Morpheme)) {
                    return false;
                }
                Morpheme m = (Morpheme) actual;
                return m.surface().equals(surface) && m.begin() == begin && m.end() == end;
            }
        };
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
    public void getSynonymGroupIds() {
        List<Morpheme> ms = tokenizer.tokenize("京都");
        assertThat(ms.size(), is(1));
        assertThat(ms.get(0).getSynonymGroupIds(), is(new int[] { 1, 5 }));

        ms = tokenizer.tokenize("ぴらる");
        assertThat(ms.size(), is(1));
        assertThat(ms.get(0).getSynonymGroupIds().length, is(0));

        ms = tokenizer.tokenize("東京府");
        assertThat(ms.size(), is(1));
        assertThat(ms.get(0).getSynonymGroupIds(), is(new int[] { 1, 3 }));
    }

    @Test
    public void tokenizeKanjiAlphabetWord() {
        assertThat(tokenizer.tokenize("特a").size(), is(1));
        assertThat(tokenizer.tokenize("ab").size(), is(1));
        assertThat(tokenizer.tokenize("特ab").size(), is(2));
    }

    @Test
    public void tokenizeSentences() {
        Iterator<MorphemeList> it = tokenizer.tokenizeSentences("京都。東京.東京都。").iterator();
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
        MorphemeList s = tokenizer.tokenize("京都…");
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
        Iterator<MorphemeList> it = tokenizer.tokenizeSentences("´´").iterator();
        assertThat(it.hasNext(), is(true));
        assertThat(it.next().size(), is(4));
        assertThat(it.hasNext(), is(false));
    }

    @Test
    public void tokenizeSentencesWithSurrogatePair() {
        Iterator<MorphemeList> it = tokenizer.tokenizeSentences("。😀").iterator();
        assertThat(it.hasNext(), is(true));
        assertThat(it.next().size(), is(1));
        assertThat(it.hasNext(), is(true));
        assertThat(it.next().size(), is(1));
        assertThat(it.hasNext(), is(false));
    }

    @Test
    public void tokenizerWithReader() throws IOException {
        StringReader reader = new StringReader("京都。東京.東京都。京都");
        Iterator<MorphemeList> it = tokenizer.tokenizeSentences(reader).iterator();
        assertThat(it.hasNext(), is(true));
        assertThat(it.next().size(), is(2));
        assertThat(it.hasNext(), is(true));
        assertThat(it.next().size(), is(2));
        assertThat(it.hasNext(), is(true));
        assertThat(it.next().size(), is(2));
        assertThat(it.hasNext(), is(true));
        assertThat(it.next().size(), is(1));
        assertThat(it.hasNext(), is(false));
    }

    @Test
    public void tokenizerWithLongReader() throws IOException {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < SentenceDetector.DEFAULT_LIMIT * 2 / 3; i++) {
            sb.append("京都。");
        }
        sb.append("京都");
        StringReader reader = new StringReader(sb.toString());
        Iterator<MorphemeList> it = tokenizer.tokenizeSentences(reader).iterator();
        for (int i = 0; i < SentenceDetector.DEFAULT_LIMIT * 2 / 3; i++) {
            assertThat(it.hasNext(), is(true));
            assertThat(it.next().size(), is(2));
        }
        assertThat(it.hasNext(), is(true));
        assertThat(it.next().size(), is(1));
        assertThat(it.hasNext(), is(false));
    }

    @Test
    public void tokenizerWithReaderAndNormalization() throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("東京都…。");
        for (int i = 0; i < SentenceDetector.DEFAULT_LIMIT / 3; i++) {
            sb.append("京都。");
        }
        StringReader reader = new StringReader(sb.toString());
        Iterator<MorphemeList> it = tokenizer.tokenizeSentences(reader).iterator();
        assertThat(it.hasNext(), is(true));
        assertThat(it.next().size(), is(5));
        for (int i = 0; i < SentenceDetector.DEFAULT_LIMIT / 3; i++) {
            assertThat(it.hasNext(), is(true));
            List<Morpheme> ms = it.next();
            assertThat(ms.size(), is(2));
            assertThat(ms.get(0).surface(), is("京都"));
            assertThat(ms.get(1).surface(), is("。"));
        }
        assertThat(it.hasNext(), is(false));
    }

    @Test
    public void zeroLengthMorpheme() {
        List<Morpheme> s = tokenizer.tokenize("…");
        assertThat(s.size(), is(3));
        assertThat(s.get(0).surface(), is("…"));
        assertThat(s.get(0).normalizedForm(), is("."));
        assertThat(s.get(0).begin(), is(0));
        assertThat(s.get(0).end(), is(1));
        assertThat(s.get(1).surface(), is(""));
        assertThat(s.get(1).normalizedForm(), is("."));
        assertThat(s.get(1).begin(), is(1));
        assertThat(s.get(1).end(), is(1));
        assertThat(s.get(2).surface(), is(""));
        assertThat(s.get(2).normalizedForm(), is("."));
        assertThat(s.get(2).begin(), is(1));
        assertThat(s.get(2).end(), is(1));
    }

    @Test
    public void disableEmptyMorpheme() throws IOException {
        Config config = TestDictionary.INSTANCE.user1Cfg();
        dict = new DictionaryFactory().create(Config.empty().withFallback(config).allowEmptyMorpheme(false));
        tokenizer = (JapaneseTokenizer) dict.create();

        List<Morpheme> s = tokenizer.tokenize("…");
        assertThat(s.size(), is(3));
        assertThat(s.get(0).surface(), is("…"));
        assertThat(s.get(0).normalizedForm(), is("."));
        assertThat(s.get(0).begin(), is(0));
        assertThat(s.get(0).end(), is(1));
        assertThat(s.get(1).surface(), is("…"));
        assertThat(s.get(1).normalizedForm(), is("."));
        assertThat(s.get(1).begin(), is(0));
        assertThat(s.get(1).end(), is(1));
        assertThat(s.get(2).surface(), is("…"));
        assertThat(s.get(2).normalizedForm(), is("."));
        assertThat(s.get(2).begin(), is(0));
        assertThat(s.get(2).end(), is(1));
    }

    @Test
    public void splitAfterTokenizeCtoA() {
        MorphemeList morphemesC = tokenizer.tokenize(Tokenizer.SplitMode.C, "東京都");
        assertThat(morphemesC.size(), is(1));
        MorphemeList morphemesA = morphemesC.split(Tokenizer.SplitMode.A);
        assertThat(morphemesA.size(), is(2));
    }

    @Test
    public void splitAfterTokenizeCtoB() {
        MorphemeList morphemesC = tokenizer.tokenize(Tokenizer.SplitMode.C, "東京都");
        assertThat(morphemesC.size(), is(1));
        MorphemeList morphemesB = morphemesC.split(Tokenizer.SplitMode.B);
        assertThat(morphemesB.size(), is(1));
    }

    @Test
    public void splitAfterTokenizeCtoC() {
        MorphemeList morphemes1 = tokenizer.tokenize(Tokenizer.SplitMode.C, "東京都");
        assertThat(morphemes1.size(), is(1));
        MorphemeList morphemes2 = morphemes1.split(Tokenizer.SplitMode.C);
        assertThat(morphemes2, sameInstance(morphemes1));
    }

    @Test
    public void splitAfterTokenizeAtoC() {
        MorphemeList morphemes1 = tokenizer.tokenize(Tokenizer.SplitMode.A, "東京都");
        assertThat(morphemes1.size(), is(2));
        MorphemeList morphemes2 = morphemes1.split(Tokenizer.SplitMode.C);
        assertThat(morphemes2, sameInstance(morphemes1));
    }

    @Test
    public void splitAfterTokenizeBtoC() {
        MorphemeList morphemes1 = tokenizer.tokenize(Tokenizer.SplitMode.B, "東京都");
        assertThat(morphemes1.size(), is(1));
        MorphemeList morphemes2 = morphemes1.split(Tokenizer.SplitMode.C);
        assertThat(morphemes2, sameInstance(morphemes1));
    }

    @Test
    public void splitWithZeroWidthTokens() {
        MorphemeList morphemes1 = tokenizer.tokenize("…東京都…");
        assertThat(morphemes1.size(), is(7));
        assertThat(morphemes1.get(0), morpheme("…", 0, 1));
        assertThat(morphemes1.get(1), morpheme("", 1, 1));
        assertThat(morphemes1.get(2), morpheme("", 1, 1));
        assertThat(morphemes1.get(3), morpheme("東京都", 1, 4));
        assertThat(morphemes1.get(4), morpheme("…", 4, 5));
        MorphemeList morphemes2 = morphemes1.split(Tokenizer.SplitMode.A);
        assertThat(morphemes2.size(), is(8));
        assertThat(morphemes2.get(3), morpheme("東京", 1, 3));
        assertThat(morphemes2.get(4), morpheme("都", 3, 4));
    }

    @Test
    public void splitSingleToken() {
        MorphemeList morphemes1 = tokenizer.tokenize(Tokenizer.SplitMode.C, "な。な");
        assertThat(morphemes1.size(), is(1));
        assertThat(morphemes1.get(0), morpheme("な。な", 0, 3));
        MorphemeList morphemes2 = morphemes1.split(Tokenizer.SplitMode.A);
        assertThat(morphemes2.get(0), morpheme("な。な", 0, 3));
        assertThat(morphemes2.get(0).normalizedForm(), is("アイウ"));
    }

    @Test
    public void dumpInternalStructures() {
        String json = tokenizer.dumpInternalStructures("東京都");
        JsonReader reader = Json.createReader(new StringReader(json));
        JsonObject root = reader.readObject();

        assertThat(root.getJsonObject("inputText").getString("originalText"), is("東京都"));
        assertThat(root.getJsonObject("inputText").getString("modifiedText"), is("東京都"));

        JsonArray lattice = root.getJsonArray("lattice");
        assertThat(lattice.size(), is(7));

        int i = 0;
        assertThat(lattice.getJsonObject(i).getInt("nodeId"), is(i));
        assertThat(lattice.getJsonObject(i).isNull("begin"), is(true));
        assertThat(lattice.getJsonObject(i).getInt("end"), is(0));
        assertThat(lattice.getJsonObject(i).getString("headword"), is("(null)"));
        assertThat(lattice.getJsonObject(i).getInt("wordId"), is(0));
        assertThat(lattice.getJsonObject(i).getString("pos"), is("BOS/EOS"));
        assertThat(lattice.getJsonObject(i).getInt("rightId"), is(0));
        assertThat(lattice.getJsonObject(i).getInt("leftId"), is(0));
        assertThat(lattice.getJsonObject(i).getInt("cost"), is(0));
        assertThat(lattice.getJsonObject(i).getJsonArray("connectCosts").size(), is(1));

        i = 1;
        assertThat(lattice.getJsonObject(i).getInt("nodeId"), is(i));
        assertThat(lattice.getJsonObject(i).getInt("begin"), is(0));
        assertThat(lattice.getJsonObject(i).getInt("end"), is(3));
        assertThat(lattice.getJsonObject(i).getString("headword"), is("東"));
        assertThat(lattice.getJsonObject(i).getString("pos"), is("名詞,普通名詞,一般,*,*,*"));
        assertThat(lattice.getJsonObject(i).getInt("wordId"), is(4));
        assertThat(lattice.getJsonObject(i).getInt("rightId"), is(7));
        assertThat(lattice.getJsonObject(i).getInt("leftId"), is(7));
        assertThat(lattice.getJsonObject(i).getInt("cost"), is(4675));
        assertThat(lattice.getJsonObject(i).getJsonArray("connectCosts").size(), is(1));

        i = 2;
        assertThat(lattice.getJsonObject(i).getInt("nodeId"), is(i));
        assertThat(lattice.getJsonObject(i).getInt("begin"), is(0));
        assertThat(lattice.getJsonObject(i).getInt("end"), is(6));
        assertThat(lattice.getJsonObject(i).getString("headword"), is("東京"));
        assertThat(lattice.getJsonObject(i).getString("pos"), is("名詞,固有名詞,地名,一般,*,*"));

        i = 3;
        assertThat(lattice.getJsonObject(i).getInt("nodeId"), is(i));
        assertThat(lattice.getJsonObject(i).getInt("begin"), is(0));
        assertThat(lattice.getJsonObject(i).getInt("end"), is(9));
        assertThat(lattice.getJsonObject(i).getString("headword"), is("東京都"));
        assertThat(lattice.getJsonObject(i).getString("pos"), is("名詞,固有名詞,地名,一般,*,*"));

        i = 4;
        assertThat(lattice.getJsonObject(i).getInt("nodeId"), is(i));
        assertThat(lattice.getJsonObject(i).getInt("begin"), is(3));
        assertThat(lattice.getJsonObject(i).getInt("end"), is(9));
        assertThat(lattice.getJsonObject(i).getString("headword"), is("京都"));
        assertThat(lattice.getJsonObject(i).getString("pos"), is("名詞,固有名詞,地名,一般,*,*"));

        i = 5;
        assertThat(lattice.getJsonObject(i).getInt("nodeId"), is(i));
        assertThat(lattice.getJsonObject(i).getInt("begin"), is(6));
        assertThat(lattice.getJsonObject(i).getInt("end"), is(9));
        assertThat(lattice.getJsonObject(i).getString("headword"), is("都"));
        assertThat(lattice.getJsonObject(i).getString("pos"), is("名詞,普通名詞,一般,*,*,*"));

        i = 6;
        assertThat(lattice.getJsonObject(i).getInt("nodeId"), is(i));
        assertThat(lattice.getJsonObject(i).getInt("begin"), is(9));
        assertThat(lattice.getJsonObject(i).isNull("end"), is(true));
        assertThat(lattice.getJsonObject(i).getString("headword"), is("(null)"));
        assertThat(lattice.getJsonObject(i).getInt("wordId"), is(0));
        assertThat(lattice.getJsonObject(i).getString("pos"), is("BOS/EOS"));
        assertThat(lattice.getJsonObject(i).getInt("rightId"), is(0));
        assertThat(lattice.getJsonObject(i).getInt("leftId"), is(0));
        assertThat(lattice.getJsonObject(i).getInt("cost"), is(0));
        assertThat(lattice.getJsonObject(i).getJsonArray("connectCosts").size(), is(3));

        assertThat(root.getJsonArray("bestPath").size(), is(1));
        assertThat(root.getJsonArray("bestPath").getJsonObject(0).getString("headword"), is("東京都"));

        assertThat(root.getJsonArray("rewrittenPath").size(), is(1));
        assertThat(root.getJsonArray("rewrittenPath").getJsonObject(0).getString("headword"), is("東京都"));
    }
}
