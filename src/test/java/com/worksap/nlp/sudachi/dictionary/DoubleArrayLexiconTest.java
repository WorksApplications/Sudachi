package com.worksap.nlp.sudachi.dictionary;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.Assert.*;
import org.junit.*;

public class DoubleArrayLexiconTest {

    static final int GRAMMAR_SIZE = 386;

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
        assertArrayEquals(new int[] { 3, 3 }, results.get(0)); // 東
        assertArrayEquals(new int[] { 4, 6 }, results.get(1)); // 東京
        assertArrayEquals(new int[] { 5, 9 }, results.get(2)); // 東京都
        results
            = lexicon.lookup("東京都に".getBytes(StandardCharsets.UTF_8), 9);
        assertEquals(1, results.size());
        assertArrayEquals(new int[] { 1, 12 }, results.get(0)); // に

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
        assertEquals(5, lexicon.getLeftId(5));
        assertEquals(7, lexicon.getRightId(5));
        assertEquals(5320, lexicon.getCost(5));

        // 都
        assertEquals(7, lexicon.getLeftId(8));
        assertEquals(7, lexicon.getRightId(8));
        assertEquals(2914, lexicon.getCost(8));
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
        wi = lexicon.getWordInfo(7);
        assertEquals("行っ", wi.getSurface());
        assertEquals("行く", wi.getNormalizedForm());
        assertEquals(6, wi.getDictionaryFormWordId());
        assertEquals("行く", wi.getDictionaryForm());

        // 東京都
        wi = lexicon.getWordInfo(5);
        assertEquals("東京都", wi.getSurface());
        assertArrayEquals(new int[] { 4, 8 }, wi.getAunitSplit());
        assertArrayEquals(new int[0], wi.getBunitSplit());
        assertArrayEquals(new int[] { 4, 8 }, wi.getWordStructure());
    }
}
