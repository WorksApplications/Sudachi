package jp.co.worksap.nlp.sudachi;

import static org.junit.Assert.*;
import org.junit.*;
import org.junit.runner.*;

import java.util.List;

public class UTF8InputTextTest {

    // \u2123d uses surrogate pair
    String originalText = "aあ漢いb字𡈽う";
    InputText<byte[]> text;

    @Before
    public void setUp() {
        text = new UTF8InputText(originalText);
    }

    @Test
    public void originalCharAt() {
        assertEquals('漢', text.originalCharAt(2));
        assertEquals('う', text.originalCharAt(8));
    }

    @Test
    public void originalLength() {
        assertEquals(9, text.originalLength());
    }

    @Test
    public void getOriginalText() {
        assertEquals(originalText, text.getOriginalText());
    }

    @Test
    public void getText() {
        byte[] bytes = text.getText();
        assertEquals(21, bytes.length);
        assertArrayEquals(new byte[]
            { (byte)0x61, (byte)0xe3, (byte)0x81, (byte)0x82,
              (byte)0xe6, (byte)0xbc, (byte)0xa2, (byte)0xe3,
              (byte)0x81, (byte)0x84, (byte)0x62, (byte)0xe5,
              (byte)0xad, (byte)0x97, (byte)0xf0, (byte)0xa1,
              (byte)0x88, (byte)0xbd, (byte)0xe3, (byte)0x81,
              (byte)0x86 }, bytes);
    }

    @Test
    public void getOriginalOffset() {
        assertEquals(0, text.getOriginalOffset(0));
        assertEquals(1, text.getOriginalOffset(1));
        assertEquals(1, text.getOriginalOffset(3));
        assertEquals(8, text.getOriginalOffset(20));
    }

    @Test
    public void replaceWithSameLength() {
        text.replace(2, 4, "ああ");
        byte[] bytes = text.getText();
        assertEquals(21, bytes.length);
        assertEquals(0, text.getOriginalOffset(0));
        assertEquals(2, text.getOriginalOffset(4));
        assertEquals(2, text.getOriginalOffset(5));
        assertEquals(2, text.getOriginalOffset(6));
        assertEquals(2, text.getOriginalOffset(7));
        assertEquals(2, text.getOriginalOffset(8));
        assertEquals(2, text.getOriginalOffset(9));
        assertEquals(4, text.getOriginalOffset(10));
    }

    @Test
    public void replaceWithDeletion() {
        text.replace(2, 4, "あ");
        byte[] bytes = text.getText();
        assertEquals(18, bytes.length);
        assertEquals(0, text.getOriginalOffset(0));
        assertEquals(2, text.getOriginalOffset(4));
        assertEquals(2, text.getOriginalOffset(5));
        assertEquals(2, text.getOriginalOffset(6));
        assertEquals(4, text.getOriginalOffset(7));
    }

    @Test
    public void replaceWithInsertion() {
        text.replace(2, 4, "あああ");
        byte[] bytes = text.getText();
        assertEquals(24, bytes.length);
        assertEquals(0, text.getOriginalOffset(0));
        assertEquals(2, text.getOriginalOffset(4));
        assertEquals(2, text.getOriginalOffset(7));
        assertEquals(2, text.getOriginalOffset(10));
        assertEquals(4, text.getOriginalOffset(13));
    }

}
