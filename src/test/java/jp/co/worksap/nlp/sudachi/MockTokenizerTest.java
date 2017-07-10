package jp.co.worksap.nlp.sudachi;

import static org.junit.Assert.*;
import org.junit.*;
import org.junit.runner.*;

import java.util.List;

public class MockTokenizerTest {

    Dictionary dictionary;

    @Before
    public void setUp() {
	dictionary = new MockDictionary(null);
    }

    @After
    public void tearDown() {
	dictionary.close();
    }

    @Test
    public void tokenize() {
	Tokenizer t = dictionary.create();
	List<Morpheme> ms = t.tokenize("今日はいい天気");
	assertNotNull(ms);
	assertEquals(3, ms.size());
    }

    @Test
    public void tokenizeA() {
	Tokenizer t = dictionary.create();
	List<Morpheme> ms = t.tokenize(Tokenizer.SplitMode.A,
				       "今日はいい天気");
	assertNotNull(ms);
	assertEquals(7, ms.size());
    }

    @Test
    public void tokenizeB() {
	Tokenizer t = dictionary.create();
	List<Morpheme> ms = t.tokenize(Tokenizer.SplitMode.B,
				       "今日はいい天気");
	assertNotNull(ms);
	assertEquals(5, ms.size());
    }
}
