/*
 *  Copyright (c) 2017 Works Applications Co., Ltd.
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


package com.worksap.nlp.lucene.sudachi.ja;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionLengthAttribute;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class TestSudachiTokenizer {
    private static final String RESOURCE_NAME_SUDACHI_SETTINGS = "sudachiSettings.json";
    private static final String RESOURCE_NAME_SYSTEM_DIC = "system.dic";
    private static final String RESOURCE_NAME_CHAR_DEF = "char.def";
    private static final String RESOURCE_NAME_UNK_DEF = "unk.def";
    private static final String RESOURCE_NAME_REWRITE_DEF = "rewrite.def";

    private SudachiTokenizer tokenizer;
    private SudachiTokenizer tokenizerExtended;
    private SudachiTokenizer tokenizerNormal;
    private SudachiTokenizer tokenizerPunctuation;

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Before
    public void setup() throws IOException {
        tempFolder.create();
        File tempFile = tempFolder.newFolder("sudachiDictionary");

        Files.copy(TestAnalysisSudachi.class
                .getResourceAsStream(RESOURCE_NAME_SUDACHI_SETTINGS), Paths
                .get(tempFile.getPath())
                .resolve(RESOURCE_NAME_SUDACHI_SETTINGS));
        Files.copy(TestAnalysisSudachi.class
                .getResourceAsStream(RESOURCE_NAME_SYSTEM_DIC),
                Paths.get(tempFile.getPath()).resolve(RESOURCE_NAME_SYSTEM_DIC));
        Files.copy(TestAnalysisSudachi.class
                .getResourceAsStream(RESOURCE_NAME_CHAR_DEF),
                Paths.get(tempFile.getPath()).resolve(RESOURCE_NAME_CHAR_DEF));
        Files.copy(TestAnalysisSudachi.class
                .getResourceAsStream(RESOURCE_NAME_UNK_DEF),
                Paths.get(tempFile.getPath()).resolve(RESOURCE_NAME_UNK_DEF));
        Files.copy(TestAnalysisSudachi.class
                .getResourceAsStream(RESOURCE_NAME_REWRITE_DEF),
                Paths.get(tempFile.getPath())
                        .resolve(RESOURCE_NAME_REWRITE_DEF));

        tokenizer = new SudachiTokenizer(true, SudachiTokenizer.Mode.SEARCH,
                tempFile.getPath(),
                SudachiAnalyzer.DefaultSudachiSettingsReader());

        tokenizerExtended = new SudachiTokenizer(true,
                SudachiTokenizer.Mode.EXTENDED, tempFile.getPath(),
                SudachiAnalyzer.DefaultSudachiSettingsReader());

        tokenizerNormal = new SudachiTokenizer(true,
                SudachiTokenizer.Mode.NORMAL, tempFile.getPath(),
                SudachiAnalyzer.DefaultSudachiSettingsReader());

        tokenizerPunctuation = new SudachiTokenizer(false,
                SudachiTokenizer.Mode.SEARCH, tempFile.getPath(),
                SudachiAnalyzer.DefaultSudachiSettingsReader());
    }

    @Test
    public void incrementTokenShiftJis() throws IOException {
        String str = new String("私は宇宙人です。".getBytes("Shift_JIS"), "Shift_JIS");
        tokenizer.setReader(new StringReader(str));
        tokenizer.reset();
        String[] answerListAUnit = { "私", "は", "宇宙", "人", "です" };
        int[] answerListPosIncAUnit = { 1, 1, 1, 1, 1 };
        int[] answerListPosLengthAUnit = { 1, 1, 1, 1, 1 };
        int i = 0;
        while (tokenizer.incrementToken()) {
            assertThat(tokenizer.getAttribute(CharTermAttribute.class)
                    .toString(), is(answerListAUnit[i]));
            assertThat(tokenizer.getAttribute(PositionIncrementAttribute.class)
                    .getPositionIncrement(), is(answerListPosIncAUnit[i]));
            assertThat(tokenizer.getAttribute(PositionLengthAttribute.class)
                    .getPositionLength(), is(answerListPosLengthAUnit[i]));
            i++;
        }
    }

    @Test
    public void incrementToken() throws IOException {
        tokenizer.setReader(new StringReader("私は宇宙人です。"));
        tokenizer.reset();
        String[] answerList = { "私", "は", "宇宙", "人", "です" };
        int[] answerListPosInc = { 1, 1, 1, 1, 1 };
        int[] answerListPosLength = { 1, 1, 1, 1, 1 };
        int i = 0;
        while (tokenizer.incrementToken()) {
            assertThat(tokenizer.getAttribute(CharTermAttribute.class)
                    .toString(), is(answerList[i]));
            assertThat(tokenizer.getAttribute(PositionIncrementAttribute.class)
                    .getPositionIncrement(), is(answerListPosInc[i]));
            assertThat(tokenizer.getAttribute(PositionLengthAttribute.class)
                    .getPositionLength(), is(answerListPosLength[i]));
            i++;
        }

        tokenizerExtended.setReader(new StringReader("私は宇宙人です。"));
        tokenizerExtended.reset();
        String[] answerListExtended = { "私", "は", "宇宙", "人", "です" };
        int[] answerListPosIncExtended = { 1, 1, 1, 1, 1 };
        int[] answerListPosLengthExtended = { 1, 1, 1, 1, 1 };
        i = 0;
        while (tokenizerExtended.incrementToken()) {
            assertThat(tokenizerExtended.getAttribute(CharTermAttribute.class)
                    .toString(), is(answerListExtended[i]));
            assertThat(
                    tokenizerExtended.getAttribute(
                            PositionIncrementAttribute.class)
                            .getPositionIncrement(),
                    is(answerListPosIncExtended[i]));
            assertThat(
                    tokenizerExtended.getAttribute(
                            PositionLengthAttribute.class).getPositionLength(),
                    is(answerListPosLengthExtended[i]));
            i++;
        }

        tokenizerNormal.setReader(new StringReader("私は宇宙人です。"));
        tokenizerNormal.reset();
        String[] answerListNormal = { "私", "は", "宇宙", "人", "です" };
        int[] answerListPosIncNormal = { 1, 1, 1, 1, 1 };
        int[] answerListPosLengthNormal = { 1, 1, 1, 1, 1 };
        i = 0;
        while (tokenizerNormal.incrementToken()) {
            assertThat(tokenizerNormal.getAttribute(CharTermAttribute.class)
                    .toString(), is(answerListNormal[i]));
            assertThat(
                    tokenizerNormal.getAttribute(
                            PositionIncrementAttribute.class)
                            .getPositionIncrement(),
                    is(answerListPosIncNormal[i]));
            assertThat(
                    tokenizerNormal.getAttribute(PositionLengthAttribute.class)
                            .getPositionLength(),
                    is(answerListPosLengthNormal[i]));
            i++;
        }

        tokenizerPunctuation.setReader(new StringReader("私は宇宙人です。"));
        tokenizerPunctuation.reset();
        String[] answerListPunctuation = { "私", "は", "宇宙", "人", "です", "。" };
        int[] answerListPosIncPunctuation = { 1, 1, 1, 1, 1, 1 };
        int[] answerListPosLengthPunctuation = { 1, 1, 1, 1, 1, 1 };
        i = 0;
        while (tokenizerPunctuation.incrementToken()) {
            assertThat(
                    tokenizerPunctuation.getAttribute(CharTermAttribute.class)
                            .toString(), is(answerListPunctuation[i]));
            assertThat(
                    tokenizerPunctuation.getAttribute(
                            PositionIncrementAttribute.class)
                            .getPositionIncrement(),
                    is(answerListPosIncPunctuation[i]));
            assertThat(
                    tokenizerPunctuation.getAttribute(
                            PositionLengthAttribute.class).getPositionLength(),
                    is(answerListPosLengthPunctuation[i]));
            i++;
        }
    }

    @Test
    public void incrementTokenPunctuation() throws IOException {
        tokenizer.setReader(new StringReader("私は宇宙人です。東京都へ行った。"));
        tokenizer.reset();
        String[] answerList = { "私", "は", "宇宙", "人", "です", "東京都", "東京", "都",
                "へ", "行く", "た" };
        int[] answerListPosInc = { 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1 };
        int[] answerListPosLength = { 1, 1, 1, 1, 1, 2, 1, 1, 1, 1, 1, 1 };
        int i = 0;
        while (tokenizer.incrementToken()) {
            assertThat(tokenizer.getAttribute(CharTermAttribute.class)
                    .toString(), is(answerList[i]));
            assertThat(tokenizer.getAttribute(PositionIncrementAttribute.class)
                    .getPositionIncrement(), is(answerListPosInc[i]));
            assertThat(tokenizer.getAttribute(PositionLengthAttribute.class)
                    .getPositionLength(), is(answerListPosLength[i]));
            i++;
        }

        tokenizerExtended.setReader(new StringReader("私は宇宙人です。東京都へ行った。"));
        tokenizerExtended.reset();
        String[] answerListExtended = { "私", "は", "宇宙", "人", "です", "東京都", "東京",
                "都", "へ", "行く", "た" };
        int[] answerListPosIncExtended = { 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1 };
        int[] answerListPosLengthExtended = { 1, 1, 1, 1, 1, 2, 1, 1, 1, 1, 1,
                1 };
        i = 0;
        while (tokenizerExtended.incrementToken()) {
            assertThat(tokenizerExtended.getAttribute(CharTermAttribute.class)
                    .toString(), is(answerListExtended[i]));
            assertThat(
                    tokenizerExtended.getAttribute(
                            PositionIncrementAttribute.class)
                            .getPositionIncrement(),
                    is(answerListPosIncExtended[i]));
            assertThat(
                    tokenizerExtended.getAttribute(
                            PositionLengthAttribute.class).getPositionLength(),
                    is(answerListPosLengthExtended[i]));
            i++;
        }

        tokenizerNormal.setReader(new StringReader("私は宇宙人です。東京都へ行った。"));
        tokenizerNormal.reset();
        String[] answerListNormal = { "私", "は", "宇宙", "人", "です", "東京都", "へ",
                "行く", "た" };
        int[] answerListPosIncNormal = { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 };
        int[] answerListPosLengthNormal = { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 };
        i = 0;
        while (tokenizerNormal.incrementToken()) {
            assertThat(tokenizerNormal.getAttribute(CharTermAttribute.class)
                    .toString(), is(answerListNormal[i]));
            assertThat(
                    tokenizerNormal.getAttribute(
                            PositionIncrementAttribute.class)
                            .getPositionIncrement(),
                    is(answerListPosIncNormal[i]));
            assertThat(
                    tokenizerNormal.getAttribute(PositionLengthAttribute.class)
                            .getPositionLength(),
                    is(answerListPosLengthNormal[i]));
            i++;
        }

        tokenizerPunctuation.setReader(new StringReader("私は宇宙人です。東京都へ行った。"));
        tokenizerPunctuation.reset();
        String[] answerListPunctuation = { "私", "は", "宇宙", "人", "です", "。",
                "東京都", "東京", "都", "へ", "行く", "た", "。" };
        int[] answerListPosIncPunctuation = { 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1,
                1, 1, 1 };
        int[] answerListPosLengthPunctuation = { 1, 1, 1, 1, 1, 1, 2, 1, 1, 1,
                1, 1, 1, 1 };
        i = 0;
        while (tokenizerPunctuation.incrementToken()) {
            assertThat(
                    tokenizerPunctuation.getAttribute(CharTermAttribute.class)
                            .toString(), is(answerListPunctuation[i]));
            assertThat(
                    tokenizerPunctuation.getAttribute(
                            PositionIncrementAttribute.class)
                            .getPositionIncrement(),
                    is(answerListPosIncPunctuation[i]));
            assertThat(
                    tokenizerPunctuation.getAttribute(
                            PositionLengthAttribute.class).getPositionLength(),
                    is(answerListPosLengthPunctuation[i]));
            i++;
        }
    }

    @Test
    public void incrementTokenAUnit() throws IOException {
        tokenizer.setReader(new StringReader("東京都へ行った。"));
        tokenizer.reset();
        String[] answerList = { "東京都", "東京", "都", "へ", "行く", "た" };
        int[] answerListPosInc = { 1, 0, 1, 1, 1, 1 };
        int[] answerListPosLength = { 2, 1, 1, 1, 1, 1 };
        int i = 0;
        while (tokenizer.incrementToken()) {
            assertThat(tokenizer.getAttribute(CharTermAttribute.class)
                    .toString(), is(answerList[i]));
            assertThat(tokenizer.getAttribute(PositionIncrementAttribute.class)
                    .getPositionIncrement(), is(answerListPosInc[i]));
            assertThat(tokenizer.getAttribute(PositionLengthAttribute.class)
                    .getPositionLength(), is(answerListPosLength[i]));
            i++;
        }

        tokenizerExtended.setReader(new StringReader("東京都へ行った。"));
        tokenizerExtended.reset();
        String[] answerListExtended = { "東京都", "東京", "都", "へ", "行く", "た" };
        int[] answerListPosIncExtended = { 1, 0, 1, 1, 1, 1 };
        int[] answerListPosLengthExtended = { 2, 1, 1, 1, 1, 1 };
        i = 0;
        while (tokenizerExtended.incrementToken()) {
            assertThat(tokenizerExtended.getAttribute(CharTermAttribute.class)
                    .toString(), is(answerListExtended[i]));
            assertThat(
                    tokenizerExtended.getAttribute(
                            PositionIncrementAttribute.class)
                            .getPositionIncrement(),
                    is(answerListPosIncExtended[i]));
            assertThat(
                    tokenizerExtended.getAttribute(
                            PositionLengthAttribute.class).getPositionLength(),
                    is(answerListPosLengthExtended[i]));
            i++;
        }

        tokenizerNormal.setReader(new StringReader("東京都へ行った。"));
        tokenizerNormal.reset();
        String[] answerListNormal = { "東京都", "へ", "行く", "た" };
        int[] answerListPosIncNormal = { 1, 1, 1, 1 };
        int[] answerListPosLengthNormal = { 1, 1, 1, 1 };
        i = 0;
        while (tokenizerNormal.incrementToken()) {
            assertThat(tokenizerNormal.getAttribute(CharTermAttribute.class)
                    .toString(), is(answerListNormal[i]));
            assertThat(
                    tokenizerNormal.getAttribute(
                            PositionIncrementAttribute.class)
                            .getPositionIncrement(),
                    is(answerListPosIncNormal[i]));
            assertThat(
                    tokenizerNormal.getAttribute(PositionLengthAttribute.class)
                            .getPositionLength(),
                    is(answerListPosLengthNormal[i]));
            i++;
        }

        tokenizerPunctuation.setReader(new StringReader("東京都へ行った。"));
        tokenizerPunctuation.reset();
        String[] answerListPunctuation = { "東京都", "東京", "都", "へ", "行く", "た",
                "。" };
        int[] answerListPosIncPunctuation = { 1, 0, 1, 1, 1, 1, 1 };
        int[] answerListPosLengthPunctuation = { 2, 1, 1, 1, 1, 1, 1 };
        i = 0;
        while (tokenizerPunctuation.incrementToken()) {
            assertThat(
                    tokenizerPunctuation.getAttribute(CharTermAttribute.class)
                            .toString(), is(answerListPunctuation[i]));
            assertThat(
                    tokenizerPunctuation.getAttribute(
                            PositionIncrementAttribute.class)
                            .getPositionIncrement(),
                    is(answerListPosIncPunctuation[i]));
            assertThat(
                    tokenizerPunctuation.getAttribute(
                            PositionLengthAttribute.class).getPositionLength(),
                    is(answerListPosLengthPunctuation[i]));
            i++;
        }
    }

    @Test
    public void incrementTokenOOV() throws IOException {
        tokenizer.setReader(new StringReader("私はカマンチョメンガーに乗る。"));
        tokenizer.reset();
        String[] answerList = { "私", "は", "カマンチョメンガー", "に", "乗る" };
        int[] answerListPosInc = { 1, 1, 1, 1, 1 };
        int[] answerListPosLength = { 1, 1, 1, 1, 1 };
        int i = 0;
        while (tokenizer.incrementToken()) {
            assertThat(tokenizer.getAttribute(CharTermAttribute.class)
                    .toString(), is(answerList[i]));
            assertThat(tokenizer.getAttribute(PositionIncrementAttribute.class)
                    .getPositionIncrement(), is(answerListPosInc[i]));
            assertThat(tokenizer.getAttribute(PositionLengthAttribute.class)
                    .getPositionLength(), is(answerListPosLength[i]));
            i++;
        }

        tokenizerExtended.setReader(new StringReader("私はカマンチョメンガーに乗る。"));
        tokenizerExtended.reset();
        String[] answerListExtended = { "私", "は", "カマンチョメンガー", "カ", "マ", "ン",
                "チ", "ョ", "メ", "ン", "ガ", "ー", "に", "乗る" };
        int[] answerListPosIncExtended = { 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1,
                1, 1 };
        int[] answerListPosLengthExtended = { 1, 1, 9, 1, 1, 1, 1, 1, 1, 1, 1,
                1, 1, 1 };
        i = 0;
        while (tokenizerExtended.incrementToken()) {
            assertThat(tokenizerExtended.getAttribute(CharTermAttribute.class)
                    .toString(), is(answerListExtended[i]));
            assertThat(
                    tokenizerExtended.getAttribute(
                            PositionIncrementAttribute.class)
                            .getPositionIncrement(),
                    is(answerListPosIncExtended[i]));
            assertThat(
                    tokenizerExtended.getAttribute(
                            PositionLengthAttribute.class).getPositionLength(),
                    is(answerListPosLengthExtended[i]));
            i++;
        }

        tokenizerNormal.setReader(new StringReader("私はカマンチョメンガーに乗る。"));
        tokenizerNormal.reset();
        String[] answerListNormal = { "私", "は", "カマンチョメンガー", "に", "乗る" };
        int[] answerListPosIncNormal = { 1, 1, 1, 1, 1 };
        int[] answerListPosLengthNormal = { 1, 1, 1, 1, 1 };
        i = 0;
        while (tokenizerNormal.incrementToken()) {
            assertThat(tokenizerNormal.getAttribute(CharTermAttribute.class)
                    .toString(), is(answerListNormal[i]));
            assertThat(
                    tokenizerNormal.getAttribute(
                            PositionIncrementAttribute.class)
                            .getPositionIncrement(),
                    is(answerListPosIncNormal[i]));
            assertThat(
                    tokenizerNormal.getAttribute(PositionLengthAttribute.class)
                            .getPositionLength(),
                    is(answerListPosLengthNormal[i]));
            i++;
        }

        tokenizerPunctuation.setReader(new StringReader("私はカマンチョメンガーに乗る。"));
        tokenizerPunctuation.reset();
        String[] answerListPunctuation = { "私", "は", "カマンチョメンガー", "に", "乗る",
                "。" };
        int[] answerListPosIncPunctuation = { 1, 1, 1, 1, 1, 1 };
        int[] answerListPosLengthPunctuation = { 1, 1, 1, 1, 1, 1, 1, 1 };
        i = 0;
        while (tokenizerPunctuation.incrementToken()) {
            assertThat(
                    tokenizerPunctuation.getAttribute(CharTermAttribute.class)
                            .toString(), is(answerListPunctuation[i]));
            assertThat(
                    tokenizerPunctuation.getAttribute(
                            PositionIncrementAttribute.class)
                            .getPositionIncrement(),
                    is(answerListPosIncPunctuation[i]));
            assertThat(
                    tokenizerPunctuation.getAttribute(
                            PositionLengthAttribute.class).getPositionLength(),
                    is(answerListPosLengthPunctuation[i]));
            i++;
        }
    }

    @Test
    public void testReadSentences() throws IOException {
        tokenizer.setReader(new StringReader("私は宇宙人です。"));
        tokenizer.reset();
        assertThat(tokenizer.readSentences(), is("私は宇宙人です。"));

        tokenizerExtended.setReader(new StringReader("私は宇宙人です。"));
        tokenizerExtended.reset();
        assertThat(tokenizerExtended.readSentences(), is("私は宇宙人です。"));
    }

    @Test
    public void testReadSentencesTwoSentences() throws IOException {
        tokenizer.setReader(new StringReader("私は宇宙人です。東京都へ行った。"));
        tokenizer.reset();
        assertThat(tokenizer.readSentences(), is("私は宇宙人です。東京都へ行った。"));
    }

    @Test
    public void testReadSentencesNoLastPunctuation() throws IOException {
        tokenizer.setReader(new StringReader("私は宇宙人です。東京都へ行った"));
        tokenizer.reset();
        String[] answerList = { "私は宇宙人です。", "東京都へ行った" };
        for (int i = 0; i < answerList.length; i++) {
            assertThat(tokenizer.readSentences(), is(answerList[i]));
        }
    }

    @Test
    public void testReadSentencesTwoPunctuation() throws IOException {
        tokenizer.setReader(new StringReader("私は宇宙人です。。東京都へ行った"));
        tokenizer.reset();
        String[] answerList = { "私は宇宙人です。。", "東京都へ行った" };
        for (int i = 0; i < answerList.length; i++) {
            assertThat(tokenizer.readSentences(), is(answerList[i]));
        }
    }

    @Test
    public void testReadSentencesJapaneseComma() throws IOException {
        tokenizer.setReader(new StringReader("私は宇宙人です、東京都へ行った"));
        tokenizer.reset();
        String[] answerList = { "私は宇宙人です、", "東京都へ行った" };
        for (int i = 0; i < answerList.length; i++) {
            assertThat(tokenizer.readSentences(), is(answerList[i]));
        }
    }

    @Test
    public void testReadSentencesPeriod() throws IOException {
        tokenizer.setReader(new StringReader("私は宇宙人です。。東京都へ行った."));
        tokenizer.reset();
        assertThat(tokenizer.readSentences(), is("私は宇宙人です。。東京都へ行った."));
    }

    @Test
    public void testReadSentencesComma() throws IOException {
        tokenizer.setReader(new StringReader("私は宇宙人です,東京都へ行った"));
        tokenizer.reset();
        String[] answerList = { "私は宇宙人です,", "東京都へ行った" };
        for (int i = 0; i < answerList.length; i++) {
            assertThat(tokenizer.readSentences(), is(answerList[i]));
        }
    }

    @Test
    public void testReadSentencesLongSentence() throws IOException {
        tokenizer.setReader(new StringReader("岩波文庫は平福百穂画伯の装幀をもって昭和二年刊行された。"
                + "これを発表した時の影響の絶大なりしことは実に驚いた。" + "讃美、激励、希望等の書信が数千通に達した。"
                + "「私の教養の一切を岩波文庫に托する」などという感激の文字もあった。"
                + "私はよい仕事だ、高貴な永遠の事業だ、達成すべき企てだ、"
                + "後には必ず成就する仕事だと考えたが、かくまで速かに、かくまで盛んに歓迎されるとは思わなかった。"
                + "Du kannst, denn du sollst. は私の絶愛の句であるが、誠心誠意、"
                + "読書子のために計る仕事は必ず酬いられるものであるとの確信を得た。"
                + "私は本屋になった甲斐のあったことを初めて知り、責任のますます重きを痛感した。　"
                + "岩波文庫は古今東西の古典の普及を使命とする。古典の尊重すべきは言うまでもない。"
                + "その普及の程度は直ちに文化の水準を示すものである。したがって文庫出版については敬虔なる態度を持し、"
                + "古典に対する尊敬と愛とを失ってはならない。私は及ばずながらもこの理想を実現しようと心がけ、"
                + "一般単行本に対するよりも、さらに厳粛なる態度をもって文庫の出版に臨んだ。"
                + "文庫の編入すべき典籍の厳選はもちろん、編集、校訂、翻訳等、"
                + "その道の権威者を煩わして最善をつくすことに人知れぬ苦心をしたのである。"
                + "従来行なわれているものをそのまま編入すれば便宜なる時も、"
                + "よりよい原稿を得るために新たに最適任者に懇請して数年を費やしてようやく刊行するに至った例も少なくない。"
                + "しかるにこの態度は今もなお一般に理解されず、"
                + "往々にして著者から謙遜のつもりで文庫にでも入れてもらいたいなど出版を申し込まれる場合がある。"
                + "私は単行本には引き受けられても文庫には引き受けぬといって拒絶するほど、文庫を尊重愛護するのである。　"
                + "典籍の範囲をいずれに限定すべきか、従来既刊の岩波文庫を見るに必ずしも標準が一定しなかったうらみがあるが、"
                + "今後の方針としては古典的価値の水準をますます高めるとともに、厳選方針を強化することにした。"
                + "経済的価値高くとも本質的価値乏しいものはこれを編入しないことに特に意を用いるとともに、"
                + "経済的価値低くとも古典的価値の豊かなるものはつとめて編入し、この点において岩波文庫本来の特色を発揮しようと思っている。"
                + "往年外遊の際、レクラム会社を見学してその事業的規模の大なるには驚いたが、"
                + "編集態度においては岩波文庫が、その先蹤たるレクラム文庫を恥ずかしむるものでないと、ひそかに慰むるところがあった。"));
        tokenizer.reset();
        String[] answerList = {
                "岩波文庫は平福百穂画伯の装幀をもって昭和二年刊行された。"
                        + "これを発表した時の影響の絶大なりしことは実に驚いた。"
                        + "讃美、激励、希望等の書信が数千通に達した。"
                        + "「私の教養の一切を岩波文庫に托する」などという感激の文字もあった。"
                        + "私はよい仕事だ、高貴な永遠の事業だ、達成すべき企てだ、後には必ず成就する仕事だと考えたが、"
                        + "かくまで速かに、かくまで盛んに歓迎されるとは思わなかった。Du kannst, denn du sollst. は私の絶愛の句であるが、"
                        + "誠心誠意、読書子のために計る仕事は必ず酬いられるものであるとの確信を得た。"
                        + "私は本屋になった甲斐のあったことを初めて知り、責任のますます重きを痛感した。　"
                        + "岩波文庫は古今東西の古典の普及を使命とする。古典の尊重すべきは言うまでもない。"
                        + "その普及の程度は直ちに文化の水準を示すものである。したがって文庫出版については敬虔なる態度を持し、"
                        + "古典に対する尊敬と愛とを失ってはならない。私は及ばずながらもこの理想を実現しようと心がけ、一般単行本に対するよりも、"
                        + "さらに厳粛なる態度をもって文庫の出版に臨んだ。文庫の編入すべき典籍の厳選はもちろん、編集、校訂、翻訳等、",
                "その道の権威者を煩わして最善をつくすことに人知れぬ苦心をしたのである。従来行なわれているものをそのまま編入すれば便宜なる時も、"
                        + "よりよい原稿を得るために新たに最適任者に懇請して数年を費やしてようやく刊行するに至った例も少なくない。"
                        + "しかるにこの態度は今もなお一般に理解されず、"
                        + "往々にして著者から謙遜のつもりで文庫にでも入れてもらいたいなど出版を申し込まれる場合がある。"
                        + "私は単行本には引き受けられても文庫には引き受けぬといって拒絶するほど、文庫を尊重愛護するのである。　"
                        + "典籍の範囲をいずれに限定すべきか、従来既刊の岩波文庫を見るに必ずしも標準が一定しなかったうらみがあるが、"
                        + "今後の方針としては古典的価値の水準をますます高めるとともに、厳選方針を強化することにした。"
                        + "経済的価値高くとも本質的価値乏しいものはこれを編入しないことに特に意を用いるとともに、"
                        + "経済的価値低くとも古典的価値の豊かなるものはつとめて編入し、"
                        + "この点において岩波文庫本来の特色を発揮しようと思っている。"
                        + "往年外遊の際、レクラム会社を見学してその事業的規模の大なるには驚いたが、編集態度においては岩波文庫が、"
                        + "その先蹤たるレクラム文庫を恥ずかしむるものでないと、", "ひそかに慰むるところがあった。" };
        for (int i = 0; i < answerList.length; i++) {
            assertThat(tokenizer.readSentences(), is(answerList[i]));
        }
    }

}
