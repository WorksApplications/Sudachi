package com.worksap.nlp.sudachi.dictionary;

import java.io.IOException;
import java.nio.ByteBuffer;

import static org.junit.Assert.*;
import org.junit.*;

public class GrammarImplTest {

    static final int storageSize = 2 * (56 + 11);

    ByteBuffer storage;
    GrammarImpl grammar;

    @Before
    public void setUp() {
        storage = ByteBuffer.allocate(4 + storageSize);
        storage.putInt(0);      // dummy
        
        buildPartOfSpeech();    // 2 * 56 bytes
        buildConnectTable();    // 2 * 11 bytes
        storage.rewind();
        grammar = new GrammarImpl(storage, 4);
    }

    @Test
    public void storageSize() {
        assertEquals(storageSize, grammar.storageSize());
    }

    @Test
    public void getPartOfSpeechString() {
        assertEquals(6, grammar.getPartOfSpeechString((short)0).length);
        assertEquals("BOS/EOS", grammar.getPartOfSpeechString((short)0)[0]);
        assertEquals("*", grammar.getPartOfSpeechString((short)0)[5]);

        assertEquals("一般", grammar.getPartOfSpeechString((short)1)[1]);
        assertEquals("*", grammar.getPartOfSpeechString((short)1)[5]);

        assertEquals("五段-サ行", grammar.getPartOfSpeechString((short)2)[4]);
        assertEquals("終止形-一般", grammar.getPartOfSpeechString((short)2)[5]);
    }

    @Test
    public void getPartOfSpeechId() {
        assertEquals(0, grammar.getPartOfSpeechId(new String[] {"BOS/EOS",
                                                                "*", "*", "*",
                                                                "*", "*" }));
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
    public void getStorageSize() {
        assertEquals(2 * (56 + 11), grammar.storageSize());
    }

    @Test
    public void readFromFile() throws IOException {
        ByteBuffer bytes = DictionaryReader.read("/system.dic");
        grammar = new GrammarImpl(bytes, 0);

        assertEquals(7, grammar.getPartOfSpeechSize());

        assertEquals(0, grammar.getConnectCost((short)0, (short)0));
        assertEquals(-3361, grammar.getConnectCost((short)1, (short)1));
        assertEquals(126, grammar.getConnectCost((short)3, (short)6));
        assertEquals(1180, grammar.getConnectCost((short)7, (short)2));
        assertEquals(3319, grammar.getConnectCost((short)5, (short)7));
        assertEquals(452, grammar.storageSize());
    }

    void buildPartOfSpeech() {
        storage.putShort((short)3); // # of part of speech

        storage.putShort((short)7);
        storage.putChar('B');
        storage.putChar('O');
        storage.putChar('S');
        storage.putChar('/');
        storage.putChar('E');
        storage.putChar('O');
        storage.putChar('S');
        storage.putShort((short)1);
        storage.putChar('*');
        storage.putShort((short)1);
        storage.putChar('*');
        storage.putShort((short)1);
        storage.putChar('*');
        storage.putShort((short)1);
        storage.putChar('*');
        storage.putShort((short)1);
        storage.putChar('*');

        storage.putShort((short)2);
        storage.putChar('名');
        storage.putChar('詞');
        storage.putShort((short)2);
        storage.putChar('一');
        storage.putChar('般');
        storage.putShort((short)1);
        storage.putChar('*');
        storage.putShort((short)1);
        storage.putChar('*');
        storage.putShort((short)1);
        storage.putChar('*');
        storage.putShort((short)1);
        storage.putChar('*');


        storage.putShort((short)2);
        storage.putChar('動');
        storage.putChar('詞');
        storage.putShort((short)2);
        storage.putChar('一');
        storage.putChar('般');
        storage.putShort((short)1);
        storage.putChar('*');
        storage.putShort((short)1);
        storage.putChar('*');
        storage.putShort((short)5);
        storage.putChar('五');
        storage.putChar('段');
        storage.putChar('-');
        storage.putChar('サ');
        storage.putChar('行');
        storage.putShort((short)6);
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
