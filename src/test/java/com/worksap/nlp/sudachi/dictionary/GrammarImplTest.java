/*
 * Copyright (c) 2017 Works Applications Co., Ltd.
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

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

public class GrammarImplTest {

    static final int allocateSize = 4096;

    ByteBuffer storage;
    GrammarImpl grammar;
    int storageSize;

    @Before
    public void setUp() {
        storage = ByteBuffer.allocate(allocateSize);
        storage.putInt(0);      // dummy
        
        int base = storage.position();
        buildPartOfSpeech();
        buildConnectTable();
        storageSize = storage.position() - base;
        storage.rewind();
        grammar = new GrammarImpl(storage, base);
    }

    @Test
    public void storageSize() {
        assertEquals(storageSize, grammar.storageSize());
    }

    @Test
    public void getPartOfSpeechString() {
        assertEquals(6, grammar.getPartOfSpeechString((short)0).size());
        assertEquals("BOS/EOS", grammar.getPartOfSpeechString((short)0).get(0));
        assertEquals("*", grammar.getPartOfSpeechString((short)0).get(5));

        assertEquals("一般", grammar.getPartOfSpeechString((short)1).get(1));
        assertEquals("*", grammar.getPartOfSpeechString((short)1).get(5));

        assertEquals("五段-サ行", grammar.getPartOfSpeechString((short)2).get(4));
        assertEquals("終止形-一般", grammar.getPartOfSpeechString((short)2).get(5));
    }

    @Test
    public void getPartOfSpeechId() {
        assertEquals(0, grammar.getPartOfSpeechId(Arrays.asList("BOS/EOS",
                                                                "*", "*", "*",
                                                                "*", "*")));
    }

    @Test
    public void getConnectCost() {
        assertEquals(0, grammar.getConnectCost((short)0, (short)0));
        assertEquals(-100, grammar.getConnectCost((short)2, (short)1));
        assertEquals(200, grammar.getConnectCost((short)1, (short)2));
    }

    @Test
    public void setConnectCost() {
        grammar.setConnectCost((short)0, (short)0, (short)300);
        assertEquals(300, grammar.getConnectCost((short)0, (short)0));
    }

    @Test
    public void getBOSParameter() {
        assertEquals(0, grammar.getBOSParameter()[0]);
        assertEquals(0, grammar.getBOSParameter()[1]);
        assertEquals(0, grammar.getBOSParameter()[2]);
    }

    @Test
    public void getEOSParameter() {
        assertEquals(0, grammar.getEOSParameter()[0]);
        assertEquals(0, grammar.getEOSParameter()[1]);
        assertEquals(0, grammar.getEOSParameter()[2]);
    }

    @Test
    public void readFromFile() throws IOException {
        ByteBuffer bytes = DictionaryReader.read("/system.dic");
        DictionaryHeader header = new DictionaryHeader(bytes, 0);

        grammar = new GrammarImpl(bytes, header.storageSize());

        assertEquals(8, grammar.getPartOfSpeechSize());

        assertEquals(0, grammar.getConnectCost((short)0, (short)0));
        assertEquals(-3361, grammar.getConnectCost((short)1, (short)1));
        assertEquals(126, grammar.getConnectCost((short)3, (short)6));
        assertEquals(1180, grammar.getConnectCost((short)7, (short)2));
        assertEquals(3319, grammar.getConnectCost((short)5, (short)7));
        assertEquals(470, grammar.storageSize());
    }

    void buildPartOfSpeech() {
        storage.putShort((short)3); // # of part of speech

        storage.put((byte)7);
        storage.putChar('B');
        storage.putChar('O');
        storage.putChar('S');
        storage.putChar('/');
        storage.putChar('E');
        storage.putChar('O');
        storage.putChar('S');
        storage.put((byte)1);
        storage.putChar('*');
        storage.put((byte)1);
        storage.putChar('*');
        storage.put((byte)1);
        storage.putChar('*');
        storage.put((byte)1);
        storage.putChar('*');
        storage.put((byte)1);
        storage.putChar('*');

        storage.put((byte)2);
        storage.putChar('名');
        storage.putChar('詞');
        storage.put((byte)2);
        storage.putChar('一');
        storage.putChar('般');
        storage.put((byte)1);
        storage.putChar('*');
        storage.put((byte)1);
        storage.putChar('*');
        storage.put((byte)1);
        storage.putChar('*');
        storage.put((byte)1);
        storage.putChar('*');


        storage.put((byte)2);
        storage.putChar('動');
        storage.putChar('詞');
        storage.put((byte)2);
        storage.putChar('一');
        storage.putChar('般');
        storage.put((byte)1);
        storage.putChar('*');
        storage.put((byte)1);
        storage.putChar('*');
        storage.put((byte)5);
        storage.putChar('五');
        storage.putChar('段');
        storage.putChar('-');
        storage.putChar('サ');
        storage.putChar('行');
        storage.put((byte)6);
        storage.putChar('終');
        storage.putChar('止');
        storage.putChar('形');
        storage.putChar('-');
        storage.putChar('一');
        storage.putChar('般');
    }

    void buildConnectTable() {
        storage.putShort((short)3); // # of leftId
        storage.putShort((short)3); // # of rightId
        
        storage.putShort((short)0);    // # of rightId
        storage.putShort((short)-300); // # of rightId
        storage.putShort((short)3000); // # of rightId

        storage.putShort((short)300);  // # of rightId
        storage.putShort((short)-500); // # of rightId
        storage.putShort((short)-100); // # of rightId

        storage.putShort((short)-3000); // # of rightId
        storage.putShort((short)200);   // # of rightId
        storage.putShort((short)2000);  // # of rightId
    }
}
