package com.worksap.nlp.sudachi.dictionary;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.Assert.*;
import org.junit.*;

public class DoubleArrayLexiconTest {

    static final int GRAMMAR_SIZE = 452;

    DoubleArrayLexicon lexicon;

    @Before
    public void setUp() throws IOException {
        ByteBuffer bytes = DictionaryReader.read("/system.dic");
        lexicon = new DoubleArrayLexicon(bytes, GRAMMAR_SIZE);
    }

    @Test
    public void lookup() {
        List<int[]> results
            = lexicon.lookup("東京都".getBytes(StandardCharsets.UTF_8), 0);
        assertEquals(3, results.size());
        assertArrayEquals(new int[] { 4, 3 }, results.get(0)); // 東
        assertArrayEquals(new int[] { 5, 6 }, results.get(1)); // 東京
        assertArrayEquals(new int[] { 6, 9 }, results.get(2)); // 東京都
        results
            = lexicon.lookup("東京都に".getBytes(StandardCharsets.UTF_8), 9);
        assertEquals(2, results.size());
        assertArrayEquals(new int[] { 1, 12 }, results.get(0)); // に(接続助詞)
        assertArrayEquals(new int[] { 2, 12 }, results.get(1)); // に(格助詞)

        results
            = lexicon.lookup("あれ".getBytes(StandardCharsets.UTF_8), 0);
        assertEquals(0, results.size());
    }

    @Test
    public void parameters() {
        // た
        assertEquals(1, lexicon.getLeftId(0)); 
        assertEquals(1, lexicon.getRightId(0));
        assertEquals(8729, lexicon.getCost(0));

        // 東京都
        assertEquals(6, lexicon.getLeftId(6));
        assertEquals(8, lexicon.getRightId(6));
        assertEquals(5320, lexicon.getCost(6));

        // 都
        assertEquals(8, lexicon.getLeftId(9));
        assertEquals(8, lexicon.getRightId(9));
        assertEquals(2914, lexicon.getCost(9));
    }

    @Test
    public void wordInfo() {
        // た
        WordInfo wi = lexicon.getWordInfo(0);
        assertEquals("た", wi.getSurface());
        assertEquals(3, wi.getLength());
        assertEquals(0, wi.getPOSId());
        assertEquals("た", wi.getNormalizedForm());
        assertEquals(-1, wi.getDictionaryFormWordId());
        assertEquals("た", wi.getDictionaryForm());
        assertEquals("タ", wi.getReading());
        assertArrayEquals(new int[0], wi.getAunitSplit());
        assertArrayEquals(new int[0], wi.getBunitSplit());
        assertArrayEquals(new int[0], wi.getWordStructure());

        // 行っ
        wi = lexicon.getWordInfo(8);
        assertEquals("行っ", wi.getSurface());
        assertEquals("行く", wi.getNormalizedForm());
        assertEquals(7, wi.getDictionaryFormWordId());
        assertEquals("行く", wi.getDictionaryForm());

        // 東京都
        wi = lexicon.getWordInfo(6);
        assertEquals("東京都", wi.getSurface());
        assertArrayEquals(new int[] { 5, 9 }, wi.getAunitSplit());
        assertArrayEquals(new int[0], wi.getBunitSplit());
        assertArrayEquals(new int[] { 5, 9 }, wi.getWordStructure());
    }
}