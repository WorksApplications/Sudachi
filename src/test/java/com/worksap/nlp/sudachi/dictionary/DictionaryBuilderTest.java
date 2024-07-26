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

package com.worksap.nlp.sudachi.dictionary;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.worksap.nlp.sudachi.WordId;

public class DictionaryBuilderTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void commandLine() throws IOException {
        File outputFile = temporaryFolder.newFile();
        File matrixFile = temporaryFolder.newFile();
        File inputFile = temporaryFolder.newFile();

        try (FileWriter writer = new FileWriter(matrixFile)) {
            writer.write("1 1\n0 0 200\n");
        }

        try (FileWriter writer = new FileWriter(inputFile)) {
            writer.write("東京都,0,0,0,東京都,名詞,固有名詞,地名,一般,*,*,ヒガシキョウト,東京都,*,B,\"東,名詞,普通名詞,一般,*,*,*,ヒガシ/2\",*,1/2,1/2\n");
            writer.write("東,-1,-1,0,東,名詞,普通名詞,一般,*,*,*,ヒガシ,ひがし,*,A,*,*,*,*\n");
            writer.write("京都,0,0,0,京都,名詞,固有名詞,地名,一般,*,*,キョウト,京都,*,A,*,*,*,*\n");
        }
        int[] wordIds = { 4, 11, 15, 19 };

        DictionaryBuilder.main(new String[] { "-o", outputFile.getPath(), "-m", matrixFile.getPath(), "-d", "test",
                inputFile.getPath() });

        try (BinaryDictionary dictionary = new BinaryDictionary(outputFile.getPath())) {
            Description header = dictionary.getDictionaryHeader();
            assertTrue(header.isSystemDictionary());
            assertThat(header.getComment(), is("test"));

            Grammar grammar = dictionary.getGrammar();
            assertThat(grammar.getPartOfSpeechSize(), is(2));
            assertThat(grammar.getPartOfSpeechString((short) 0), contains("名詞", "固有名詞", "地名", "一般", "*", "*"));
            assertThat(grammar.getPartOfSpeechString((short) 1), contains("名詞", "普通名詞", "一般", "*", "*", "*"));
            assertThat(grammar.getConnectCost((short) 0, (short) 0), is((short) 200));

            Lexicon lexicon = dictionary.getLexicon();
            assertThat(lexicon.size(), is(4)); // 3 + phantom for "ひがし"

            // first entry
            int wordId = wordIds[0];
            long params = lexicon.parameters(wordId);
            assertThat(WordParameters.leftId(params), is((short) 0));
            assertThat(WordParameters.cost(params), is((short) 0));
            WordInfo info = lexicon.getWordInfo(wordId);
            assertThat(lexicon.string(0, info.getSurface()), is("東京都"));
            assertThat(info.getNormalizedForm(), is(WordId.make(0, wordId)));
            assertThat(info.getDictionaryForm(), is(WordId.make(0, wordId)));
            assertThat(lexicon.string(0, info.getReadingForm()), is("ヒガシキョウト"));
            assertThat(info.getPOSId(), is((short) 0));
            assertThat(info.getAunitSplit(), is(new int[] { wordIds[1], wordIds[2] }));
            assertThat(info.getBunitSplit().length, is(0));
            assertThat(info.getSynonymGroupIds(), is(new int[] { 1, 2 }));
            Iterator<int[]> i = lexicon.lookup("東京都".getBytes(StandardCharsets.UTF_8), 0);
            assertTrue(i.hasNext());
            assertThat(i.next(), is(new int[] { wordId, "東京都".getBytes(StandardCharsets.UTF_8).length }));
            assertFalse(i.hasNext());

            // second entry
            wordId = wordIds[1];
            params = lexicon.parameters(wordId);
            assertThat(WordParameters.leftId(params), is((short) -1));
            assertThat(WordParameters.cost(params), is((short) 0));
            info = lexicon.getWordInfo(wordId);
            assertThat(lexicon.string(0, info.getSurface()), is("東"));
            assertThat(info.getNormalizedForm(), is(WordId.make(0, wordIds[3]))); // phantom entry
            assertThat(info.getDictionaryForm(), is(WordId.make(0, wordId)));
            assertThat(lexicon.string(0, info.getReadingForm()), is("ヒガシ"));
            assertThat(info.getPOSId(), is((short) 1));
            assertThat(info.getAunitSplit().length, is(0));
            assertThat(info.getBunitSplit().length, is(0));
            i = lexicon.lookup("東".getBytes(StandardCharsets.UTF_8), 0);
            assertFalse(i.hasNext());
        }
    }
}