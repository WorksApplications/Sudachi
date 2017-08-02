package com.worksap.nlp.sudachi;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import org.junit.*;

import java.nio.file.Paths;
import java.util.List;

public class SettingsTest {

    @Test
    public void parseSettings() {
        assertTrue(Settings.parseSettings("{}") instanceof Settings);
    }

    @Test(expected = IllegalArgumentException.class)
    public void parseSettingsStartsWithArray() {
        assertTrue(Settings.parseSettings("[]") instanceof Settings);
    }

    @Test
    public void getString() {
        Settings settings = Settings.parseSettings("{\"foo\":\"baa\"}");
        assertEquals("baa", settings.getString("foo"));
        assertNull(settings.getString("bazz"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void getStringWithError() {
        Settings settings = Settings.parseSettings("{\"foo\":123}");
        settings.getString("foo");
    }

    @Test
    public void getStringWithDefaultValue() {
        Settings settings = Settings.parseSettings("{\"foo\":\"baa\"}");
        assertEquals("baa", settings.getString("foo", "nyaa"));
        assertEquals("nyaa", settings.getString("bazz", "nyaa"));

        settings = Settings.parseSettings("{\"foo\":123}");
        assertEquals("nyaa", settings.getString("foo", "nyaa"));
    }

    @Test
    public void getStringList() {
        Settings settings = Settings.parseSettings("{\"foo\":[\"baa\",\"bazz\"]}");
        assertThat(settings.getStringList("foo"),
                   allOf(hasItem("baa"), hasItem("bazz")));
        assertTrue(settings.getStringList("baa").isEmpty());
    }

    @Test(expected = IllegalArgumentException.class)
    public void getStringListWithError() {
        Settings settings = Settings.parseSettings("{\"baa\":123}");
        settings.getStringList("baa");
    }

    @Test
    public void getInt() {
        Settings settings = Settings.parseSettings("{\"foo\":123}");
        assertEquals(123, settings.getInt("foo"));
        assertEquals(0, settings.getInt("baa"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void getIntWithError() {
        Settings settings = Settings.parseSettings("{\"foo\":\"baa\"}");
        settings.getInt("foo");
    }

    @Test
    public void getIntList() {
        Settings settings = Settings.parseSettings("{\"foo\":[123,456]}");
        assertThat(settings.getIntList("foo"),
                   allOf(hasItem(123), hasItem(456)));
        assertTrue(settings.getIntList("baa").isEmpty());
    }

    @Test(expected = IllegalArgumentException.class)
    public void getIntListWithError() {
        Settings settings = Settings.parseSettings("{\"foo\":123}");
        settings.getIntList("foo");
    }

    @Test
    public void getIntListList() {
        Settings settings = Settings.parseSettings("{\"foo\":[[123],[456, 789]]}");
        List<List<Integer>> list = settings.getIntListList("foo");
        assertEquals(2, list.size());
        assertThat(list.get(0), hasItem(123));
        assertThat(list.get(1), allOf(hasItem(456), hasItem(789)));
        assertTrue(settings.getIntList("baa").isEmpty());
    }

    @Test(expected = IllegalArgumentException.class)
    public void getIntListListWithError() {
        Settings settings = Settings.parseSettings("{\"foo\":123}");
        settings.getIntListList("foo");
    }

    @Test
    public void getPath() {
        Settings settings = Settings.parseSettings("{\"foo\":\"baa\"}");
        assertEquals("baa", settings.getPath("foo"));
        assertNull(settings.getPath("bazz"));

        settings = Settings.parseSettings("{\"path\":\"bazz\",\"foo\":\"baa\"}");
        assertEquals(Paths.get("bazz", "baa").toString(),
                     settings.getPath("foo"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void getPathWithError() {
        Settings settings = Settings.parseSettings("{\"foo\":123}");
        settings.getPath("foo");
    }

    @Test
    public void getPathList() {
        Settings settings = Settings.parseSettings("{\"foo\":[\"baa\",\"bazz\"]}");
        assertThat(settings.getPathList("foo"),
                   allOf(hasItem("baa"), hasItem("bazz")));
        assertTrue(settings.getPathList("baa").isEmpty());

        settings = Settings.parseSettings("{\"path\":\"bazz\",\"foo\":[\"baa\",\"bazz\"]}");
        assertThat(settings.getPathList("foo"),
                   allOf(hasItem(Paths.get("bazz", "baa").toString()),
                         hasItem(Paths.get("bazz", "bazz").toString())));
    }

    @Test(expected = IllegalArgumentException.class)
    public void getPathListWithError() {
        Settings settings = Settings.parseSettings("{\"baa\":123}");
        settings.getPathList("baa");
    }

    static class Foo extends Plugin {
    }

    @Test
    public void getPluginList() {
        Settings settings = Settings.parseSettings("{\"foo\":[{\"class\":\"com.worksap.nlp.sudachi.SettingsTest$Foo\",\"baa\":\"bazz\"}]}");
        List<Plugin> list = settings.getPluginList("foo");
        assertEquals(1, list.size());
        assertThat(list.get(0), instanceOf(Foo.class));
        assertEquals("bazz", list.get(0).settings.getString("baa"));

        assertTrue(settings.getIntList("baa").isEmpty());
    }

    @Test(expected = IllegalArgumentException.class)
    public void getPluginListWithoutList() {
        Settings settings = Settings.parseSettings("{\"foo\":123}");
        List<Plugin> list = settings.getPluginList("foo");
    }

    @Test(expected = IllegalArgumentException.class)
    public void getPluginListWithoutClass() {
        Settings settings = Settings.parseSettings("{\"foo\":[{}]}");
        List<Plugin> list = settings.getPluginList("foo");
    }

    @Test(expected = IllegalArgumentException.class)
    public void getPluginListWithNotStringClass() {
        Settings settings = Settings.parseSettings("{\"foo\":[{\"class\":123}]}");
        List<Plugin> list = settings.getPluginList("foo");
    }

    @Test(expected = IllegalArgumentException.class)
    public void getPluginListWithNotInitializableClass() {
        Settings settings = Settings.parseSettings("{\"foo\":[{\"class\":\"bazz\"}]}");
        List<Plugin> list = settings.getPluginList("foo");
    }

    @Test(expected = IllegalArgumentException.class)
    public void getPluginListWithNotPluginClass() {
        Settings settings = Settings.parseSettings("{\"foo\":[{\"class\":\"java.lang.String\"}]}");
        List<Plugin> list = settings.getPluginList("foo");
    }
}
