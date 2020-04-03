/*
 * Copyright (c) 2020 Works Applications Co., Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.worksap.nlp.sudachi;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.nio.file.Paths;
import java.util.List;

import org.junit.Test;

public class SettingsTest {

    @Test
    public void parseSettings() {
        assertTrue(Settings.parseSettings(null, "{}") instanceof Settings);
    }

    @Test(expected = IllegalArgumentException.class)
    public void parseSettingsStartsWithArray() {
        assertTrue(Settings.parseSettings(null, "[]") instanceof Settings);
    }

    @Test
    public void getString() {
        Settings settings = Settings.parseSettings(null, "{\"foo\":\"baa\"}");
        assertEquals("baa", settings.getString("foo"));
        assertNull(settings.getString("bazz"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void getStringWithError() {
        Settings settings = Settings.parseSettings(null, "{\"foo\":123}");
        settings.getString("foo");
    }

    @Test
    public void getStringWithDefaultValue() {
        Settings settings = Settings.parseSettings(null, "{\"foo\":\"baa\"}");
        assertEquals("baa", settings.getString("foo", "nyaa"));
        assertEquals("nyaa", settings.getString("bazz", "nyaa"));

        settings = Settings.parseSettings(null, "{\"foo\":123}");
        assertEquals("nyaa", settings.getString("foo", "nyaa"));
    }

    @Test
    public void getStringList() {
        Settings settings = Settings.parseSettings(null, "{\"foo\":[\"baa\",\"bazz\"]}");
        assertThat(settings.getStringList("foo"), allOf(hasItem("baa"), hasItem("bazz")));
        assertTrue(settings.getStringList("baa").isEmpty());
    }

    @Test(expected = IllegalArgumentException.class)
    public void getStringListWithError() {
        Settings settings = Settings.parseSettings(null, "{\"baa\":123}");
        settings.getStringList("baa");
    }

    @Test
    public void getInt() {
        Settings settings = Settings.parseSettings(null, "{\"foo\":123}");
        assertEquals(123, settings.getInt("foo"));
        assertEquals(0, settings.getInt("baa"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void getIntWithError() {
        Settings settings = Settings.parseSettings(null, "{\"foo\":\"baa\"}");
        settings.getInt("foo");
    }

    @Test
    public void getIntWithDefaultValue() {
        Settings settings = Settings.parseSettings(null, "{\"foo\":123}");
        assertEquals(123, settings.getInt("foo", 456));
        assertEquals(456, settings.getInt("bazz", 456));

        settings = Settings.parseSettings(null, "{\"foo\":\"nyaa\"}");
        assertEquals(456, settings.getInt("foo", 456));
    }

    @Test
    public void getIntList() {
        Settings settings = Settings.parseSettings(null, "{\"foo\":[123,456]}");
        assertThat(settings.getIntList("foo"), allOf(hasItem(123), hasItem(456)));
        assertTrue(settings.getIntList("baa").isEmpty());
    }

    @Test(expected = IllegalArgumentException.class)
    public void getIntListWithError() {
        Settings settings = Settings.parseSettings(null, "{\"foo\":123}");
        settings.getIntList("foo");
    }

    @Test
    public void getIntListList() {
        Settings settings = Settings.parseSettings(null, "{\"foo\":[[123],[456, 789]]}");
        List<List<Integer>> list = settings.getIntListList("foo");
        assertEquals(2, list.size());
        assertThat(list.get(0), hasItem(123));
        assertThat(list.get(1), allOf(hasItem(456), hasItem(789)));
        assertTrue(settings.getIntList("baa").isEmpty());
    }

    @Test(expected = IllegalArgumentException.class)
    public void getIntListListWithError() {
        Settings settings = Settings.parseSettings(null, "{\"foo\":123}");
        settings.getIntListList("foo");
    }

    @Test
    public void getPath() {
        Settings settings = Settings.parseSettings(null, "{\"foo\":\"baa\"}");
        assertEquals("baa", settings.getPath("foo"));
        assertNull(settings.getPath("bazz"));

        settings = Settings.parseSettings(null, "{\"path\":\"bazz\",\"foo\":\"baa\"}");
        assertEquals(Paths.get("bazz", "baa").toString(), settings.getPath("foo"));

        settings = Settings.parseSettings("maa", "{\"foo\":\"baa\"}");
        assertEquals(Paths.get("maa", "baa").toString(), settings.getPath("foo"));

        settings = Settings.parseSettings("maa", "{\"path\":\"bazz\",\"foo\":\"baa\"}");
        assertEquals(Paths.get("bazz", "baa").toString(), settings.getPath("foo"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void getPathWithError() {
        Settings settings = Settings.parseSettings(null, "{\"foo\":123}");
        settings.getPath("foo");
    }

    @Test
    public void getPathList() {
        Settings settings = Settings.parseSettings(null, "{\"foo\":[\"baa\",\"bazz\"]}");
        assertThat(settings.getPathList("foo"), allOf(hasItem("baa"), hasItem("bazz")));
        assertTrue(settings.getPathList("baa").isEmpty());

        settings = Settings.parseSettings(null, "{\"path\":\"bazz\",\"foo\":[\"baa\",\"bazz\"]}");
        assertThat(settings.getPathList("foo"),
                allOf(hasItem(Paths.get("bazz", "baa").toString()), hasItem(Paths.get("bazz", "bazz").toString())));

        settings = Settings.parseSettings("maa", "{\"foo\":[\"baa\",\"bazz\"]}");
        assertThat(settings.getPathList("foo"),
                allOf(hasItem(Paths.get("maa", "baa").toString()), hasItem(Paths.get("maa", "bazz").toString())));

        settings = Settings.parseSettings("maa", "{\"path\":\"bazz\",\"foo\":[\"baa\",\"bazz\"]}");
        assertThat(settings.getPathList("foo"),
                allOf(hasItem(Paths.get("bazz", "baa").toString()), hasItem(Paths.get("bazz", "bazz").toString())));

    }

    @Test(expected = IllegalArgumentException.class)
    public void getPathListWithError() {
        Settings settings = Settings.parseSettings(null, "{\"baa\":123}");
        settings.getPathList("baa");
    }

    static class Foo extends Plugin {
    }

    @Test
    public void getPluginList() {
        Settings settings = Settings.parseSettings(null,
                "{\"foo\":[{\"class\":\"com.worksap.nlp.sudachi.SettingsTest$Foo\",\"baa\":\"bazz\"}]}");
        List<Plugin> list = settings.getPluginList("foo");
        assertEquals(1, list.size());
        assertThat(list.get(0), instanceOf(Foo.class));
        assertEquals("bazz", list.get(0).settings.getString("baa"));

        assertTrue(settings.getIntList("baa").isEmpty());
    }

    @Test(expected = IllegalArgumentException.class)
    public void getPluginListWithoutList() {
        Settings settings = Settings.parseSettings(null, "{\"foo\":123}");
        settings.getPluginList("foo");
    }

    @Test(expected = IllegalArgumentException.class)
    public void getPluginListWithoutClass() {
        Settings settings = Settings.parseSettings(null, "{\"foo\":[{}]}");
        settings.getPluginList("foo");
    }

    @Test(expected = IllegalArgumentException.class)
    public void getPluginListWithNotStringClass() {
        Settings settings = Settings.parseSettings(null, "{\"foo\":[{\"class\":123}]}");
        settings.getPluginList("foo");
    }

    @Test(expected = IllegalArgumentException.class)
    public void getPluginListWithNotInitializableClass() {
        Settings settings = Settings.parseSettings(null, "{\"foo\":[{\"class\":\"bazz\"}]}");
        settings.getPluginList("foo");
    }

    @Test(expected = IllegalArgumentException.class)
    public void getPluginListWithNotPluginClass() {
        Settings settings = Settings.parseSettings(null, "{\"foo\":[{\"class\":\"java.lang.String\"}]}");
        settings.getPluginList("foo");
    }

    @Test
    public void merge() {
        Settings settings = Settings.parseSettings(null,
                "{\"foo\":[{\"class\":\"com.worksap.nlp.sudachi.SettingsTest$Foo\",\"attr1\":123}],\"baa\":\"bazz\",\"list\":[1,2]}");
        Settings settings2 = Settings.parseSettings("path",
                "{\"foo\":[{\"class\":\"com.worksap.nlp.sudachi.SettingsTest$Foo\",\"attr2\":456}],\"baa\":\"boo\",\"list\":[0],\"list2\":[0]}");
        settings.merge(settings2);

        List<Plugin> list = settings.getPluginList("foo");
        assertEquals(1, list.size());
        assertThat(list.get(0), instanceOf(Foo.class));
        assertThat(list.get(0).settings.getInt("attr1"), is(0));
        assertThat(list.get(0).settings.getInt("attr2"), is(456));

        assertThat(settings.getString("baa"), is("boo"));

        assertThat(settings.getIntList("list"), contains(0, 1, 2));
        assertThat(settings.getIntList("list2"), contains(0));
    }
}
