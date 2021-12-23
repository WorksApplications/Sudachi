/*
 * Copyright (c) 2021 Works Applications Co., Ltd.
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

import static com.worksap.nlp.sudachi.Settings.NOOP_RESOLVER;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.*;

import java.nio.file.Paths;
import java.util.List;

import org.junit.Test;

public class SettingsTest {

    @Test
    public void parseSettings() {
        assertNotNull(Settings.parseSettings("{}", NOOP_RESOLVER));
    }

    @Test(expected = IllegalArgumentException.class)
    public void parseSettingsStartsWithArray() {
        assertNotNull(Settings.parseSettings("[]", NOOP_RESOLVER));
    }

    @Test
    public void getString() {
        Settings settings = Settings.parseSettings("{\"foo\":\"baa\"}", NOOP_RESOLVER);
        assertEquals("baa", settings.getString("foo"));
        assertNull(settings.getString("bazz"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void getStringWithError() {
        Settings settings = Settings.parseSettings("{\"foo\":123}", NOOP_RESOLVER);
        settings.getString("foo");
    }

    @Test
    public void getStringWithDefaultValue() {
        Settings settings = Settings.parseSettings("{\"foo\":\"baa\"}", NOOP_RESOLVER);
        assertEquals("baa", settings.getString("foo", "nyaa"));
        assertEquals("nyaa", settings.getString("bazz", "nyaa"));

        settings = Settings.parseSettings("{\"foo\":123}", NOOP_RESOLVER);
        assertEquals("nyaa", settings.getString("foo", "nyaa"));
    }

    @Test
    public void getStringList() {
        Settings settings = Settings.parseSettings("{\"foo\":[\"baa\",\"bazz\"]}", NOOP_RESOLVER);
        assertThat(settings.getStringList("foo"), allOf(hasItem("baa"), hasItem("bazz")));
        assertTrue(settings.getStringList("baa").isEmpty());
    }

    @Test(expected = IllegalArgumentException.class)
    public void getStringListWithError() {
        Settings settings = Settings.parseSettings("{\"baa\":123}", NOOP_RESOLVER);
        settings.getStringList("baa");
    }

    @Test
    public void getInt() {
        Settings settings = Settings.parseSettings("{\"foo\":123}", NOOP_RESOLVER);
        assertEquals(123, settings.getInt("foo"));
        assertEquals(0, settings.getInt("baa"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void getIntWithError() {
        Settings settings = Settings.parseSettings("{\"foo\":\"baa\"}", NOOP_RESOLVER);
        settings.getInt("foo");
    }

    @Test
    public void getIntWithDefaultValue() {
        Settings settings = Settings.parseSettings("{\"foo\":123}", NOOP_RESOLVER);
        assertEquals(123, settings.getInt("foo", 456));
        assertEquals(456, settings.getInt("bazz", 456));

        settings = Settings.parseSettings("{\"foo\":\"nyaa\"}", NOOP_RESOLVER);
        assertEquals(456, settings.getInt("foo", 456));
    }

    @Test
    public void getIntList() {
        Settings settings = Settings.parseSettings("{\"foo\":[123,456]}", NOOP_RESOLVER);
        assertThat(settings.getIntList("foo"), allOf(hasItem(123), hasItem(456)));
        assertTrue(settings.getIntList("baa").isEmpty());
    }

    @Test(expected = IllegalArgumentException.class)
    public void getIntListWithError() {
        Settings settings = Settings.parseSettings("{\"foo\":123}", NOOP_RESOLVER);
        settings.getIntList("foo");
    }

    @Test
    public void getIntListList() {
        Settings settings = Settings.parseSettings("{\"foo\":[[123],[456, 789]]}", NOOP_RESOLVER);
        List<List<Integer>> list = settings.getIntListList("foo");
        assertEquals(2, list.size());
        assertThat(list.get(0), hasItem(123));
        assertThat(list.get(1), allOf(hasItem(456), hasItem(789)));
        assertTrue(settings.getIntList("baa").isEmpty());
    }

    @Test(expected = IllegalArgumentException.class)
    public void getIntListListWithError() {
        Settings settings = Settings.parseSettings("{\"foo\":123}", NOOP_RESOLVER);
        settings.getIntListList("foo");
    }

    @Test
    public void getPath() {
        Settings settings = Settings.parseSettings("{\"foo\":\"baa\"}", NOOP_RESOLVER);
        assertEquals("baa", settings.getPath("foo"));
        assertNull(settings.getPath("bazz"));

        settings = Settings.parseSettings("{\"path\":\"bazz\",\"foo\":\"baa\"}", NOOP_RESOLVER);
        assertEquals(Paths.get("bazz", "baa").toString(), settings.getPath("foo"));

        settings = Settings.parseSettings("{\"foo\":\"baa\"}", Settings.PathResolver.fileSystem(Paths.get("maa")));
        assertEquals(Paths.get("maa", "baa").toString(), settings.getPath("foo"));

        settings = Settings.parseSettings("{\"path\":\"bazz\",\"foo\":\"baa\"}",
                Settings.PathResolver.fileSystem(Paths.get("maa")));
        assertEquals(Paths.get("bazz", "baa").toString(), settings.getPath("foo"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void getPathWithError() {
        Settings settings = Settings.parseSettings("{\"foo\":123}", NOOP_RESOLVER);
        settings.getPath("foo");
    }

    @Test
    public void getPathList() {
        Settings settings = Settings.parseSettings("{\"foo\":[\"baa\",\"bazz\"]}", NOOP_RESOLVER);
        assertThat(settings.getPathList("foo"), allOf(hasItem("baa"), hasItem("bazz")));
        assertTrue(settings.getPathList("baa").isEmpty());

        settings = Settings.parseSettings("{\"path\":\"bazz\",\"foo\":[\"baa\",\"bazz\"]}", NOOP_RESOLVER);
        assertThat(settings.getPathList("foo"),
                allOf(hasItem(Paths.get("bazz", "baa").toString()), hasItem(Paths.get("bazz", "bazz").toString())));

        settings = Settings.parseSettings("{\"foo\":[\"baa\",\"bazz\"]}",
                Settings.PathResolver.fileSystem(Paths.get("maa")));
        assertThat(settings.getPathList("foo"),
                allOf(hasItem(Paths.get("maa", "baa").toString()), hasItem(Paths.get("maa", "bazz").toString())));

        settings = Settings.parseSettings("{\"path\":\"bazz\",\"foo\":[\"baa\",\"bazz\"]}",
                Settings.PathResolver.fileSystem(Paths.get("maa")));
        assertThat(settings.getPathList("foo"),
                allOf(hasItem(Paths.get("bazz", "baa").toString()), hasItem(Paths.get("bazz", "bazz").toString())));

    }

    @Test(expected = IllegalArgumentException.class)
    public void getPathListWithError() {
        Settings settings = Settings.parseSettings("{\"baa\":123}", NOOP_RESOLVER);
        settings.getPathList("baa");
    }

    @Test
    public void merge() {
        Settings settings = Settings.parseSettings("{\"baa\":\"bazz\",\"list\":[1,2]}", NOOP_RESOLVER);
        Settings settings2 = Settings.parseSettings("{\"baa\":\"boo\",\"list\":[0],\"list2\":[0]}",
                Settings.PathResolver.fileSystem(Paths.get("path")));
        Settings merged = settings.merge(settings2);

        assertThat(merged.getString("baa"), is("boo"));

        assertThat(merged.getIntList("list"), contains(0, 1, 2));
        assertThat(merged.getIntList("list2"), contains(0));
    }

}
