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

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class PrintDictionary {

    static void printDictionary(String filename) throws IOException {
        MappedByteBuffer bytes;
        try (FileInputStream istream = new FileInputStream(filename);
             FileChannel inputFile = istream.getChannel()) {
            bytes = inputFile.map(FileChannel.MapMode.READ_ONLY, 0,
                                  inputFile.size());
            bytes.order(ByteOrder.LITTLE_ENDIAN);
        }

        int offset = 0;
        DictionaryHeader header = new DictionaryHeader(bytes, offset);
        offset += header.storageSize();
        if (header.getVersion() == DictionaryVersion.SYSTEM_DICT_VERSION) {
            GrammarImpl grammar = new GrammarImpl(bytes, offset);
            offset += grammar.storageSize();
        }

        Lexicon lexicon = new DoubleArrayLexicon(bytes, offset);
        for (int wordId = 0; wordId < lexicon.size(); wordId++) {
            short leftId = lexicon.getLeftId(wordId);
            short rightId = lexicon.getRightId(wordId);
            short cost = lexicon.getCost(wordId);
            WordInfo wordInfo = lexicon.getWordInfo(wordId);

            System.out.println(String.format("%d: (%d %d %d) %s %d %s %s %s",
                                             wordId,
                                             leftId, rightId, cost,
                                             wordInfo.getSurface(),
                                             wordInfo.getPOSId(),
                                             wordInfo.getNormalizedForm(),
                                             wordInfo.getDictionaryForm(),
                                             wordInfo.getReadingForm()));
        }
    }

    /**
     * Prints the contents of dictionary.
     *
     * This tool requires filenames of dictionaries.
     * @param args the input filenames
     * @throws IOException if IO
     */
    public static void main(String[] args) throws IOException {
        for (String filename : args) {
            printDictionary(filename);
        }
    }
}
