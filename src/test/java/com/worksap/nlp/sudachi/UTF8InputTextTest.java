package com.worksap.nlp.sudachi;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

import org.junit.*;

public class UTF8InputTextTest {
    
    // \u2123d uses surrogate pair
    String originalText = "Aあ漢イb字ｳ𡈽ｃ";
    UTF8InputText text;
    
    @Before
    public void setUp() {
        text = new UTF8InputText(originalText);
    }
    
    @Test
    public void getOriginalText() {
        assertThat(text.getOriginalText(), is(originalText));
    }
    
    @Test
    public void getByteText() {
        byte[] bytes = text.getByteText();
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
        assertThat(text.getOriginalOffset(0), is(0));
        assertThat(text.getOriginalOffset(1), is(1));
        assertThat(text.getOriginalOffset(3), is(1));
        assertThat(text.getOriginalOffset(10), is(4));
        assertThat(text.getOriginalOffset(17), is(7));
        assertThat(text.getOriginalOffset(20), is(7));
        assertThat(text.getOriginalOffset(21), is(9));
        assertThat(text.getOriginalOffset(23), is(9));
    }

    @Test
    public void replaceWithSameLength() {
        text.replace(2, 4, "ああ");
        byte[] bytes = text.getByteText();
        assertThat(bytes.length, is(24));
        assertThat(text.getOriginalOffset(0), is(0));
        assertThat(text.getOriginalOffset(4), is(2));
        assertThat(text.getOriginalOffset(5), is(2));
        assertThat(text.getOriginalOffset(6), is(2));
        assertThat(text.getOriginalOffset(7), is(2));
        assertThat(text.getOriginalOffset(8), is(2));
        assertThat(text.getOriginalOffset(9), is(2));
        assertThat(text.getOriginalOffset(10), is(4));
    }

    @Test
    public void replaceWithDeletion() {
        text.replace(2, 4, "あ");
        byte[] bytes = text.getByteText();
        assertThat(bytes.length, is(21));
        assertThat(text.getOriginalOffset(0), is(0));
        assertThat(text.getOriginalOffset(4), is(2));
        assertThat(text.getOriginalOffset(5), is(2));
        assertThat(text.getOriginalOffset(6), is(2));
        assertThat(text.getOriginalOffset(7), is(4));
    }

    @Test
    public void replaceWithInsertion() {
        text.replace(2, 4, "あああ");
        byte[] bytes = text.getByteText();
        assertThat(bytes.length, is(27));
        assertThat(text.getOriginalOffset(0), is(0));
        assertThat(text.getOriginalOffset(4), is(2));
        assertThat(text.getOriginalOffset(7), is(2));
        assertThat(text.getOriginalOffset(10), is(2));
        assertThat(text.getOriginalOffset(13), is(4));
    }
}
