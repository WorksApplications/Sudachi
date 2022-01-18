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

package com.worksap.nlp.sudachi.dictionary;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

import com.worksap.nlp.sudachi.TestDictionary;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class UserDictionaryBuilderTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    File systemDictFile;

    @Before
    public void setUp() throws IOException {
        File root = temporaryFolder.getRoot();
        TestDictionary.INSTANCE.getSystemDictData().writeData(root.toPath().resolve("system.dic"));
        systemDictFile = new File(root, "system.dic");
    }

    @Test
    public void commandLine() throws IOException {
        File outputFile = temporaryFolder.newFile();
        File inputFile = temporaryFolder.newFile();

        try (FileWriter writer = new FileWriter(inputFile)) {
            writer.write(
                    "東京都市,0,0,0,東京都市,名詞,固有名詞,地名,一般,*,*,ヒガシキョウトシ,東京都市,*,B,\"東,名詞,普通名詞,一般,*,*,*,ヒガシ/3/U1\",*,\"4/3/市,名詞,普通名詞,一般,*,*,*,シ\",*\n");
            writer.write("市,-1,-1,0,市,名詞,普通名詞,一般,*,*,*,シ,市,*,A,*,*,*,*\n");
        }

        UserDictionaryBuilder.main(new String[] { "-o", outputFile.getPath(), "-s", systemDictFile.getPath(), "-d",
                "test", inputFile.getPath() });

        try (BinaryDictionary dictionary = new BinaryDictionary(outputFile.getPath())) {
            DictionaryHeader header = dictionary.getDictionaryHeader();
            assertThat(header.getVersion(), is(DictionaryVersion.USER_DICT_VERSION_3));
            assertThat(header.getDescription(), is("test"));

            Lexicon lexicon = dictionary.getLexicon();
            assertThat(lexicon.size(), is(2));

            assertThat(lexicon.getLeftId(0), is((short) 0));
            assertThat(lexicon.getCost(0), is((short) 0));
            WordInfo info = lexicon.getWordInfo(0);
            assertThat(info.getSurface(), is("東京都市"));
            assertThat(info.getNormalizedForm(), is("東京都市"));
            assertThat(info.getDictionaryFormWordId(), is(-1));
            assertThat(info.getReadingForm(), is("ヒガシキョウトシ"));
            assertThat(info.getPOSId(), is((short) 3));
            assertThat(info.getAunitSplit(), is(new int[] { 4, 3, 1 | (1 << 28) }));
            assertThat(info.getBunitSplit().length, is(0));
            assertThat(info.getWordStructure(), is(new int[] { 4, 3, 1 | (1 << 28) }));
            Iterator<int[]> i = lexicon.lookup("東京都市".getBytes(StandardCharsets.UTF_8), 0);
            assertTrue(i.hasNext());
            assertThat(i.next(), is(new int[] { 0, "東京都市".getBytes(StandardCharsets.UTF_8).length }));
            assertFalse(i.hasNext());

            assertThat(lexicon.getLeftId(1), is((short) -1));
            assertThat(lexicon.getCost(1), is((short) 0));
            info = lexicon.getWordInfo(1);
            assertThat(info.getSurface(), is("市"));
            assertThat(info.getNormalizedForm(), is("市"));
            assertThat(info.getDictionaryFormWordId(), is(-1));
            assertThat(info.getReadingForm(), is("シ"));
            assertThat(info.getPOSId(), is((short) 4));
            assertThat(info.getAunitSplit().length, is(0));
            assertThat(info.getBunitSplit().length, is(0));
            i = lexicon.lookup("市".getBytes(StandardCharsets.UTF_8), 0);
            assertFalse(i.hasNext());
        }
    }
}