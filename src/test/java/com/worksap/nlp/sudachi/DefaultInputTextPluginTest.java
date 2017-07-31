
package com.worksap.nlp.sudachi;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

import java.io.IOException;
import org.junit.*;

public class DefaultInputTextPluginTest {
    
    // U+2F3C（⼼） should not be normalized as U+5FC3（心）
    String originalText = "ÂＢΓД㈱ｶﾞウ゛⼼Ⅲ";
    UTF8InputText text;
    DefaultInputTextPlugin plugin;
    
    @Before
    public void setUp() {
        text = new UTF8InputText(originalText, null);
        plugin = new DefaultInputTextPlugin();
        try {
            plugin.rewriteDef = DefaultInputTextPluginTest.class.getClassLoader()
                .getResource("rewrite.def").getPath();
            plugin.setUp();
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
    @Test
    public void beforeRewrite() {
        assertThat(text.getOriginalText(), is(originalText));
        assertThat(text.getText(), is(originalText));
        byte[] bytes = text.getByteText();
        assertThat(bytes.length, is(30));
        assertArrayEquals(
            new byte[] {
                (byte)0xC3, (byte)0x82, (byte)0xEF, (byte)0xBC,
                (byte)0xA2, (byte)0xCE, (byte)0x93, (byte)0xD0,
                (byte)0x94, (byte)0xE3, (byte)0x88, (byte)0xB1,
                (byte)0xEF, (byte)0xBD, (byte)0xB6, (byte)0xEF,
                (byte)0xBE, (byte)0x9E, (byte)0xE3, (byte)0x82,
                (byte)0xA6, (byte)0xE3, (byte)0x82, (byte)0x9B,
                (byte)0xE2, (byte)0xBC, (byte)0xBC, (byte)0xE2,
                (byte)0x85, (byte)0xA2
            }, bytes
        );
        assertThat(text.getOriginalOffset(0), is(0));
        assertThat(text.getOriginalOffset(1), is(0));
        assertThat(text.getOriginalOffset(2), is(1));
        assertThat(text.getOriginalOffset(4), is(1));
        assertThat(text.getOriginalOffset(8), is(3));
        assertThat(text.getOriginalOffset(12), is(5));
        assertThat(text.getOriginalOffset(24), is(9));
        assertThat(text.getOriginalOffset(26), is(9));
    }
    
    @Test
    public void afterRewrite() {
        plugin.rewrite(text);
        
        assertThat(text.getOriginalText(), is(originalText));
        assertThat(text.getText(), is("âbγд(株)ガヴ⼼ⅲ"));
        byte[] bytes = text.getByteText();
        assertThat(bytes.length, is(24));
        assertArrayEquals(
            new byte[] {
                (byte)0xC3, (byte)0xA2, (byte)0x62, (byte)0xCE,
                (byte)0xB3, (byte)0xD0, (byte)0xB4, (byte)0x28,
                (byte)0xE6, (byte)0xA0, (byte)0xAA, (byte)0x29,
                (byte)0xE3, (byte)0x82, (byte)0xAC, (byte)0xE3,
                (byte)0x83, (byte)0xB4, (byte)0xE2, (byte)0xBC,
                (byte)0xBC, (byte)0xE2, (byte)0x85, (byte)0xB2
            }, bytes
        );
        assertThat(text.getOriginalOffset(0), is(0));
        assertThat(text.getOriginalOffset(1), is(0));
        assertThat(text.getOriginalOffset(2), is(1));
        assertThat(text.getOriginalOffset(3), is(2));
        assertThat(text.getOriginalOffset(7), is(4));
        assertThat(text.getOriginalOffset(11), is(4));
        assertThat(text.getOriginalOffset(15), is(7));
        assertThat(text.getOriginalOffset(17), is(7));
    }
}
