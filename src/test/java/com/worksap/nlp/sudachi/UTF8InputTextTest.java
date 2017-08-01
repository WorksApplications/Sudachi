package com.worksap.nlp.sudachi;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.*;

import com.worksap.nlp.sudachi.dictionary.CharacterCategory;
import com.worksap.nlp.sudachi.dictionary.Grammar;

public class UTF8InputTextTest {
    
    // \u2123d uses surrogate pair
    String testText = "Aあ漢イb字ｳ𡈽ｃ";
    // mixed full-width, half-width, w/ accent
    String contText = "âｂC漢あ字１2３4ｲウ";
    UTF8InputText testInput;
    UTF8InputText contInput;
    MockGrammar grammar;
    
    @Before
    public void setUp() {
        grammar = new MockGrammar();
        CharacterCategory charCategory = new CharacterCategory();
        try {
            charCategory.readCharacterDefinition(UTF8InputTextTest.class.getClassLoader()
                .getResource("char.def").getPath());
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }
        grammar.setCharacterCategory(charCategory);
        
        testInput = new UTF8InputText(testText, grammar);
        contInput = new UTF8InputText(contText, grammar);
    }
    
    @Test
    public void getOriginalText() {
        assertThat(testInput.getOriginalText(), is(testText));
        assertThat(testInput.getText(), is(testText));
        assertThat(contInput.getOriginalText(), is(contText));
        assertThat(contInput.getText(), is(contText));
    }
    
    @Test
    public void getByteText() {
        byte[] bytes = testInput.getByteText();
        assertThat(bytes.length, is(24));
        assertArrayEquals(
            new byte[] {
                (byte)0x41, (byte)0xE3, (byte)0x81, (byte)0x82,
                (byte)0xe6, (byte)0xBC, (byte)0xA2, (byte)0xE3,
                (byte)0x82, (byte)0xA4, (byte)0x62, (byte)0xE5,
                (byte)0xAD, (byte)0x97, (byte)0xEF, (byte)0xBD,
                (byte)0xB3, (byte)0xF0, (byte)0xA1, (byte)0x88,
                (byte)0xBD, (byte)0xEF, (byte)0xBD, (byte)0x83,
            }, bytes
        );
    }
    
    @Test
    public void getOriginalOffset() {
        assertThat(testInput.getOriginalOffset(0), is(0));
        assertThat(testInput.getOriginalOffset(1), is(1));
        assertThat(testInput.getOriginalOffset(3), is(1));
        assertThat(testInput.getOriginalOffset(10), is(4));
        assertThat(testInput.getOriginalOffset(17), is(7));
        assertThat(testInput.getOriginalOffset(20), is(7));
        assertThat(testInput.getOriginalOffset(21), is(9));
        assertThat(testInput.getOriginalOffset(23), is(9));
    }

    @Test
    public void replaceWithSameLength() {
        testInput.replace(2, 4, "ああ");
        assertThat(testInput.getOriginalText(), is(testText));
        assertThat(testInput.getText(), is("Aあああb字ｳ𡈽ｃ"));
        byte[] bytes = testInput.getByteText();
        assertThat(bytes.length, is(24));
        assertThat(testInput.getOriginalOffset(0), is(0));
        assertThat(testInput.getOriginalOffset(4), is(2));
        assertThat(testInput.getOriginalOffset(5), is(2));
        assertThat(testInput.getOriginalOffset(6), is(2));
        assertThat(testInput.getOriginalOffset(7), is(2));
        assertThat(testInput.getOriginalOffset(8), is(2));
        assertThat(testInput.getOriginalOffset(9), is(2));
        assertThat(testInput.getOriginalOffset(10), is(4));
    }

    @Test
    public void replaceWithDeletion() {
        testInput.replace(2, 4, "あ");
        assertThat(testInput.getOriginalText(), is(testText));
        assertThat(testInput.getText(), is("Aああb字ｳ𡈽ｃ"));
        byte[] bytes = testInput.getByteText();
        assertThat(bytes.length, is(21));
        assertThat(testInput.getOriginalOffset(0), is(0));
        assertThat(testInput.getOriginalOffset(4), is(2));
        assertThat(testInput.getOriginalOffset(5), is(2));
        assertThat(testInput.getOriginalOffset(6), is(2));
        assertThat(testInput.getOriginalOffset(7), is(4));
    }

    @Test
    public void replaceWithInsertion() {
        testInput.replace(2, 4, "あああ");
        assertThat(testInput.getOriginalText(), is(testText));
        assertThat(testInput.getText(), is("Aああああb字ｳ𡈽ｃ"));
        byte[] bytes = testInput.getByteText();
        assertThat(bytes.length, is(27));
        assertThat(testInput.getOriginalOffset(0), is(0));
        assertThat(testInput.getOriginalOffset(4), is(2));
        assertThat(testInput.getOriginalOffset(7), is(2));
        assertThat(testInput.getOriginalOffset(10), is(2));
        assertThat(testInput.getOriginalOffset(13), is(4));
    }
    
    @Test
    public void getCharCategoryNameList() {
        assertThat(testInput.getCharCategoryNameList(0).get(0), is("ALPHA"));
        assertThat(testInput.getCharCategoryNameList(1).get(0), is("HIRAGANA"));
        assertThat(testInput.getCharCategoryNameList(2).get(0), is("KANJI"));
        assertThat(testInput.getCharCategoryNameList(3).get(0), is("KATAKANA"));
        assertThat(testInput.getCharCategoryNameList(6).get(0), is("KATAKANA"));
        assertThat(testInput.getCharCategoryNameList(7).get(0), is("DEFAULT"));
        assertThat(testInput.getCharCategoryNameList(8).get(0), is("DEFAULT"));
        assertThat(testInput.getCharCategoryNameList(9).get(0), is("ALPHA"));
    }
    
    @Test
    public void getCharCategoryContinuousLength() {
        assertThat(contInput.getCharCategoryContinuousLength(0), is(3));
        assertThat(contInput.getCharCategoryContinuousLength(1), is(2));
        assertThat(contInput.getCharCategoryContinuousLength(2), is(1));
        assertThat(contInput.getCharCategoryContinuousLength(3), is(1));
        assertThat(contInput.getCharCategoryContinuousLength(5), is(1));
        assertThat(contInput.getCharCategoryContinuousLength(6), is(4));
        assertThat(contInput.getCharCategoryContinuousLength(9), is(1));
        assertThat(contInput.getCharCategoryContinuousLength(10), is(2));
        assertThat(contInput.getCharCategoryContinuousLength(11), is(1));
    }
    
    class MockGrammar implements Grammar {
        private CharacterCategory charCategory; 
        
        public int getPartOfSpeechSize() {
            return 0;
        }
        public String[] getPartOfSpeechString(short posId) {
            return null;
        }
        public short getPartOfSpeechId(String... pos) {
            return 0;
        }
        public short getConnectCost(short leftId, short rightId) {
            return 0;
        }
        public void setConnectCost(short leftId, short rightId, short cost) {}
        public short[] getBOSParameter() {
            return null;
        }
        public short[] getEOSParameter() {
            return null;
        }
        public CharacterCategory getCharacterCategory() {
            return charCategory;
        }
        public void setCharacterCategory(CharacterCategory charCategory) {
            this.charCategory = charCategory;
        }
    }
}
