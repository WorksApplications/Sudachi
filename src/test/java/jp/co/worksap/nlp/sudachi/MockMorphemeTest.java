package jp.co.worksap.nlp.sudachi;

import static org.junit.Assert.*;
import org.junit.*;
import org.junit.runner.*;

import java.util.List;

public class MockMorphemeTest {

    Dictionary dictionary;
    List<Morpheme> aUnits;
    List<Morpheme> bUnits;
    List<Morpheme> cUnits;
    String text = "今日はいい天気";
    // A mode: 今 日 は い い 天 気
    // B mode: 今日 は い い 天気
    // C mode: 今日 はいい 天気

    @Before
    public void setUp() {
        dictionary = new MockDictionary(null);
        Tokenizer tokenizer = dictionary.create();
        aUnits = tokenizer.tokenize(Tokenizer.SplitMode.A, text);
        bUnits = tokenizer.tokenize(Tokenizer.SplitMode.B, text);
        cUnits = tokenizer.tokenize(Tokenizer.SplitMode.C, text);
    }

    @After
    public void tearDown() {
        dictionary.close();
    }

    @Test
    public void begin() {
        assertEquals(0, aUnits.get(0).begin());
        assertEquals(6, aUnits.get(6).begin());
        assertEquals(2, bUnits.get(1).begin());
        assertEquals(4, bUnits.get(3).begin());
        assertEquals(2, cUnits.get(1).begin());
        assertEquals(5, cUnits.get(2).begin());
    }

    @Test
    public void end() {
        assertEquals(1, aUnits.get(0).end());
        assertEquals(7, aUnits.get(6).end());
        assertEquals(3, bUnits.get(1).end());
        assertEquals(5, bUnits.get(3).end());
        assertEquals(5, cUnits.get(1).end());
        assertEquals(7, cUnits.get(2).end());
    }

    @Test
    public void surface() {
        assertEquals("今", aUnits.get(0).surface());
        assertEquals("今日", bUnits.get(0).surface());
        assertEquals("はいい", cUnits.get(1).surface());
    }

    @Test
    public void partOfSpeech() {
        String[] pos1
            = { "名詞", "普通名詞" ,"一般", "*", "*", "*", };
        String[] pos2
            = { "動詞", "一般", "*", "*", "五段-サ行", "連用形-一般", };
        String[] pos3
            = { "名詞", "固有名詞", "地名", "一般", "*", "*", };

        assertArrayEquals(pos1, aUnits.get(0).partOfSpeech());
        assertArrayEquals(pos3, aUnits.get(2).partOfSpeech());
        assertArrayEquals(pos2, bUnits.get(1).partOfSpeech());
        assertArrayEquals(pos3, cUnits.get(2).partOfSpeech());
    }

    @Test
    public void dictionaryForm() {
        assertEquals("今", aUnits.get(0).dictionaryForm());
        assertEquals("日る", aUnits.get(1).dictionaryForm());
        assertEquals("はいいる", cUnits.get(1).dictionaryForm());
    }

    @Test
    public void normalizedForm() {
        assertEquals("今", aUnits.get(0).normalizedForm());
        assertEquals("今日", bUnits.get(0).normalizedForm());
        assertEquals("はいい", cUnits.get(1).normalizedForm());
    }

    @Test
    public void reading() {
        assertEquals("ア", aUnits.get(0).reading());
        assertEquals("アア", bUnits.get(0).reading());
        assertEquals("アアア", cUnits.get(1).reading());
    }

    @Test
    public void split() {
        Morpheme a = aUnits.get(0);
        assertEquals(1, a.split(Tokenizer.SplitMode.A).size());
        assertEquals(1, a.split(Tokenizer.SplitMode.B).size());
        assertEquals(1, a.split(Tokenizer.SplitMode.C).size());
        Morpheme b = bUnits.get(0);
        assertEquals(2, b.split(Tokenizer.SplitMode.A).size());
        assertEquals(1, b.split(Tokenizer.SplitMode.B).size());
        assertEquals(1, b.split(Tokenizer.SplitMode.C).size());
        Morpheme c = cUnits.get(1);
        assertEquals(3, c.split(Tokenizer.SplitMode.A).size());
        assertEquals(3, c.split(Tokenizer.SplitMode.B).size());
        assertEquals(1, c.split(Tokenizer.SplitMode.C).size());
    }

    @Test
    public void isOOV() {
        assertFalse(aUnits.get(0).isOOV());
        assertFalse(bUnits.get(0).isOOV());
        assertFalse(cUnits.get(1).isOOV());
    }
}

