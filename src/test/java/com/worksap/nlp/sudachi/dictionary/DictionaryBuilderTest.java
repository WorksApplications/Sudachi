/*
 * Copyright (c) 2019 Works Applications Co., Ltd.
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

package com.worksap.nlp.sudachi.dictionary;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Iterator;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class DictionaryBuilderTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void parseLine() {
        DictionaryBuilder builder = new DictionaryBuilder();

        DictionaryBuilder.WordEntry entry = builder
                .parseLine("京都,6,6,5293,京都,名詞,固有名詞,地名,一般,*,*,キョウト,京都,*,A,*,*,*".split(","));
        assertThat(entry.headword, is("京都"));
        assertThat(entry.parameters, is(new short[] { 6, 6, 5293 }));
        assertThat(entry.wordInfo.getPOSId(), is((short) 0));
        assertThat(entry.aUnitSplitString, is("*"));
        assertThat(entry.bUnitSplitString, is("*"));

        entry = builder.parseLine("京都,-1,-1,0,京都,名詞,固有名詞,地名,一般,*,*,キョウト,京都,*,A,*,*,*".split(","));
        assertNull(entry.headword);
        assertThat(entry.wordInfo.getPOSId(), is((short) 0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void parseLineWithInvalidColumns() {
        DictionaryBuilder builder = new DictionaryBuilder();
        builder.parseLine("京都,6,6,5293,京都,名詞,固有名詞,地名,一般,*,*,キョウト,京都,*,A,*,*".split(","));
    }

    @Test(expected = IllegalArgumentException.class)
    public void parseLineWithEmptyHeadword() {
        DictionaryBuilder builder = new DictionaryBuilder();
        builder.parseLine(",6,6,5293,京都,名詞,固有名詞,地名,一般,*,*,キョウト,京都,*,A,*,*,*".split(","));
    }

    @Test(expected = IllegalArgumentException.class)
    public void parseLineWithTooLongHeadword() {
        DictionaryBuilder builder = new DictionaryBuilder();
        StringBuilder sb = new StringBuilder();
        sb.setLength(Short.MAX_VALUE + 1);
        sb.append(",6,6,5293,京都,名詞,固有名詞,地名,一般,*,*,キョウト,京都,*,A,*,*,*");
        builder.parseLine(sb.toString().split(","));
    }

    @Test(expected = IllegalArgumentException.class)
    public void parseLineWithInvalidSplits() {
        DictionaryBuilder builder = new DictionaryBuilder();
        builder.parseLine("京都,6,6,5293,京都,名詞,固有名詞,地名,一般,*,*,キョウト,京都,*,A,1/2,1/2,1/2".split(","));
    }

    @Test(expected = IllegalArgumentException.class)
    public void parseLineWithTooManySplits() {
        DictionaryBuilder builder = new DictionaryBuilder();
        builder.parseLine(
                "京都,6,6,5293,京都,名詞,固有名詞,地名,一般,*,*,キョウト,京都,*,B,0/1/2/3/4/5/6/7/8/9/0/0/1/2/3/4/5/6/7/8/9/0/0/1/2/3/4/5/6/7/8/9/0/0/1/2/3/4/5/6/7/8/9/0/0/1/2/3/4/5/6/7/8/9/0/0/1/2/3/4/5/6/7/8/9/0/0/1/2/3/4/5/6/7/8/9/0/0/1/2/3/4/5/6/7/8/9/0/0/1/2/3/4/5/6/7/8/9/0/0/1/2/3/4/5/6/7/8/9/0/0/1/2/3/4/5/6/7/8/9/0/0/1/2/3/4/5/6/7/8/9/0/0/1/2/3/4/5/6/7/8/9/0,*,*"
                        .split(","));
    }

    @Test
    public void parseLineWithSameReadingForm() {
        DictionaryBuilder builder = new DictionaryBuilder();
        DictionaryBuilder.WordEntry entry = builder.parseLine("〒,6,6,5293,〒,名詞,普通名詞,一般,*,*,*,〒,〒,*,A,*,*,*".split(","));
        assertThat(entry.wordInfo.getReadingForm(), is("〒"));
    }

    @Test
    public void addToTrie() {
        DictionaryBuilder builder = new DictionaryBuilder();
        builder.addToTrie("abc", 0);
        builder.addToTrie("abc", 1);
        builder.addToTrie("abcd", 2);
        assertThat(builder.trieKeys.get("abc".getBytes(StandardCharsets.UTF_8)), contains(0, 1));
    }

    @Test
    public void convertPOSTable() {
        DictionaryBuilder builder = new DictionaryBuilder();
        builder.convertPOSTable(Arrays.asList("a,b,c,d,e,f", "g,h,i,j,k,l"));
        assertThat(builder.buffer.position(), is(2 + 3 * 12));
    }

    @Test
    public void convertMatrix() throws IOException {
        InputStream input = new ByteArrayInputStream(
                "2 3\n0 0 0\n0 1 1\n0 2 2\n\n1 0 3\n1 1 4\n1 2 5\n".getBytes(StandardCharsets.UTF_8));
        DictionaryBuilder builder = new DictionaryBuilder();
        ByteBuffer matrix = builder.convertMatrix(input);
        assertThat(builder.buffer.getShort(0), is((short) 2));
        assertThat(builder.buffer.getShort(2), is((short) 3));
        assertThat(matrix.limit(), is(2 * 3 * 2));
        assertThat(matrix.getShort(0), is((short) 0));
        assertThat(matrix.getShort((2 + 1) * 2), is((short) 4));
    }

    @Test
    public void decode() {
        assertThat(DictionaryBuilder.decode("a\\u002cc"), is("a,c"));
        assertThat(DictionaryBuilder.decode("a\\u{002c}c"), is("a,c"));
        assertThat(DictionaryBuilder.decode("a\\u{20b9f}c"), is("a\ud842\udf9fc"));
    }

    @Test
    public void parseSplitInfo() {
        DictionaryBuilder builder = new DictionaryBuilder();
        assertThat(builder.parseSplitInfo("*").length, is(0));
        assertThat(builder.parseSplitInfo("1/2/3"), is(new int[] { 1, 2, 3 }));
        assertThat(builder.parseSplitInfo("1/U2/3")[1], is(2));

        builder = new UserDictionaryBuilder(null, null);
        assertThat(builder.parseSplitInfo("1/U2/3")[1], is(2 | 1 << 28));
    }

    @Test
    public void writeString() {
        DictionaryBuilder builder = new DictionaryBuilder();
        int position = builder.buffer.position();
        builder.writeString("");
        assertThat(builder.buffer.get(position), is((byte) 0));
        assertThat(builder.buffer.position(), is(position + 1));

        position = builder.buffer.position();
        builder.writeString("あ𠮟");
        assertThat(builder.buffer.get(position), is((byte) 3));
        assertThat(builder.buffer.getChar(position + 1), is('あ'));
        assertThat(builder.buffer.getChar(position + 3), is('\ud842'));
        assertThat(builder.buffer.getChar(position + 5), is('\udf9f'));
        assertThat(builder.buffer.position(), is(position + 7));

        position = builder.buffer.position();
        final String longString = "0123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789";
        int length = longString.length();
        builder.writeString(longString);
        assertThat(builder.buffer.get(position), is((byte) (length >> 8 | 0x80)));
        assertThat(builder.buffer.get(position + 1), is((byte) (length & 0xff)));
        assertThat(builder.buffer.position(), is(position + 2 + length * 2));
    }

    @Test
    public void writeIntArray() {
        DictionaryBuilder builder = new DictionaryBuilder();
        int position = builder.buffer.position();
        builder.writeIntArray(new int[] {});
        assertThat(builder.buffer.get(position), is((byte) 0));
        builder.writeIntArray(new int[] { 1, 2, 3 });
        assertThat(builder.buffer.get(position + 1), is((byte) 3));
        assertThat(builder.buffer.getInt(position + 2), is(1));
        assertThat(builder.buffer.getInt(position + 6), is(2));
        assertThat(builder.buffer.getInt(position + 10), is(3));
        assertThat(builder.buffer.position(), is(position + 14));
    }

    @Test
    public void commandLine() throws IOException {
        File outputFile = temporaryFolder.newFile();
        File matrixFile = temporaryFolder.newFile();
        File inputFile = temporaryFolder.newFile();

        try (FileWriter writer = new FileWriter(matrixFile)) {
            writer.write("1 1\n0 0 200\n");
        }

        try (FileWriter writer = new FileWriter(inputFile)) {
            writer.write("東京都,0,0,0,東京都,名詞,固有名詞,地名,一般,*,*,ヒガシキョウト,東京都,*,B,\"東,名詞,普通名詞,一般,*,*,*,ヒガシ/2\",*,1/2\n");
            writer.write("東,-1,-1,0,東,名詞,普通名詞,一般,*,*,*,ヒガシ,ひがし,*,A,*,*,*\n");
            writer.write("京都,0,0,0,京都,名詞,固有名詞,地名,一般,*,*,キョウト,京都,*,A,*,*,*\n");
        }

        DictionaryBuilder.main(new String[] { "-o", outputFile.getPath(), "-m", matrixFile.getPath(), "-d", "test",
                inputFile.getPath() });

        try (BinaryDictionary dictionary = new BinaryDictionary(outputFile.getPath())) {

            DictionaryHeader header = dictionary.getDictionaryHeader();
            assertThat(header.getVersion(), is(DictionaryVersion.SYSTEM_DICT_VERSION));
            assertThat(header.getDescription(), is("test"));

            Grammar grammar = dictionary.getGrammar();
            assertThat(grammar.getPartOfSpeechSize(), is(2));
            assertThat(grammar.getPartOfSpeechString((short) 0), contains("名詞", "固有名詞", "地名", "一般", "*", "*"));
            assertThat(grammar.getPartOfSpeechString((short) 1), contains("名詞", "普通名詞", "一般", "*", "*", "*"));
            assertThat(grammar.getConnectCost((short) 0, (short) 0), is((short) 200));

            Lexicon lexicon = dictionary.getLexicon();
            assertThat(lexicon.size(), is(3));

            assertThat(lexicon.getLeftId(0), is((short) 0));
            assertThat(lexicon.getCost(0), is((short) 0));
            WordInfo info = lexicon.getWordInfo(0);
            assertThat(info.getSurface(), is("東京都"));
            assertThat(info.getNormalizedForm(), is("東京都"));
            assertThat(info.getDictionaryFormWordId(), is(-1));
            assertThat(info.getReadingForm(), is("ヒガシキョウト"));
            assertThat(info.getPOSId(), is((short) 0));
            assertThat(info.getAunitSplit(), is(new int[] { 1, 2 }));
            assertThat(info.getBunitSplit().length, is(0));
            Iterator<int[]> i = lexicon.lookup("東京都".getBytes(StandardCharsets.UTF_8), 0);
            assertTrue(i.hasNext());
            assertThat(i.next(), is(new int[] { 0, "東京都".getBytes(StandardCharsets.UTF_8).length }));
            assertFalse(i.hasNext());

            assertThat(lexicon.getLeftId(1), is((short) -1));
            assertThat(lexicon.getCost(1), is((short) 0));
            info = lexicon.getWordInfo(1);
            assertThat(info.getSurface(), is("東"));
            assertThat(info.getNormalizedForm(), is("ひがし"));
            assertThat(info.getDictionaryFormWordId(), is(-1));
            assertThat(info.getReadingForm(), is("ヒガシ"));
            assertThat(info.getPOSId(), is((short) 1));
            assertThat(info.getAunitSplit().length, is(0));
            assertThat(info.getBunitSplit().length, is(0));
            i = lexicon.lookup("東".getBytes(StandardCharsets.UTF_8), 0);
            assertFalse(i.hasNext());
        }
    }
}