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

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.*;

import java.nio.file.Paths;
import java.util.List;

import org.junit.Test;

public class SettingsTest {

    @Test
    public void parseSettings() {
        assertNotNull(Settings.parse("{}", SettingsAnchor.none()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void parseSettingsStartsWithArray() {
        assertNotNull(Settings.parse("[]", SettingsAnchor.none()));
    }

    @Test
    public void getString() {
        Settings settings = Settings.parse("{\"foo\":\"baa\"}", SettingsAnchor.none());
        assertEquals("baa", settings.getString("foo"));
        assertNull(settings.getString("bazz"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void getStringWithError() {
        Settings settings = Settings.parse("{\"foo\":123}", SettingsAnchor.none());
        settings.getString("foo");
    }

    @Test
    public void getStringWithDefaultValue() {
        Settings settings = Settings.parse("{\"foo\":\"baa\"}", SettingsAnchor.none());
        assertEquals("baa", settings.getString("foo", "nyaa"));
        assertEquals("nyaa", settings.getString("bazz", "nyaa"));

        settings = Settings.parse("{\"foo\":123}", SettingsAnchor.none());
        assertEquals("nyaa", settings.getString("foo", "nyaa"));
    }

    @Test
    public void getStringList() {
        Settings settings = Settings.parse("{\"foo\":[\"baa\",\"bazz\"]}", SettingsAnchor.none());
        assertThat(settings.getStringList("foo"), allOf(hasItem("baa"), hasItem("bazz")));
        assertTrue(settings.getStringList("baa").isEmpty());
    }

    @Test(expected = IllegalArgumentException.class)
    public void getStringListWithError() {
        Settings settings = Settings.parse("{\"baa\":123}", SettingsAnchor.none());
        settings.getStringList("baa");
    }

    @Test
    public void getInt() {
        // using null as anchor to check that codepath
        Settings settings = Settings.parse("{\"foo\":123}", null);
        assertEquals(123, settings.getInt("foo"));
        assertEquals(0, settings.getInt("baa"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void getIntWithError() {
        Settings settings = Settings.parse("{\"foo\":\"baa\"}", SettingsAnchor.none());
        settings.getInt("foo");
    }

    @Test
    public void getIntWithDefaultValue() {
        // using null as anchor to check that codepath
        Settings settings = Settings.parse("{\"foo\":123}", null);
        assertEquals(123, settings.getInt("foo", 456));
        assertEquals(456, settings.getInt("bazz", 456));

        // using null as anchor to check that codepath
        settings = Settings.parse("{\"foo\":\"nyaa\"}", null);
        assertEquals(456, settings.getInt("foo", 456));
    }

    @Test
    public void getIntList() {
        // using null as anchor to check that codepath
        Settings settings = Settings.parse("{\"foo\":[123,456]}", null);
        assertThat(settings.getIntList("foo"), allOf(hasItem(123), hasItem(456)));
        assertTrue(settings.getIntList("baa").isEmpty());
    }

    @Test(expected = IllegalArgumentException.class)
    public void getIntListWithError() {
        Settings settings = Settings.parse("{\"foo\":123}", SettingsAnchor.none());
        settings.getIntList("foo");
    }

    @Test
    public void getIntListList() {
        Settings settings = Settings.parse("{\"foo\":[[123],[456, 789]]}", SettingsAnchor.none());
        List<List<Integer>> list = settings.getIntListList("foo");
        assertEquals(2, list.size());
        assertThat(list.get(0), hasItem(123));
        assertThat(list.get(1), allOf(hasItem(456), hasItem(789)));
        assertTrue(settings.getIntList("baa").isEmpty());
    }

    @Test(expected = IllegalArgumentException.class)
    public void getIntListListWithError() {
        Settings settings = Settings.parse("{\"foo\":123}", SettingsAnchor.none());
        settings.getIntListList("foo");
    }

    @Deprecated
    @Test
    public void getPath() {
        Settings settings = Settings.parse("{\"foo\":\"baa\"}", SettingsAnchor.none());
        assertEquals("baa", settings.getPath("foo"));
        assertNull(settings.getPath("bazz"));

        // using null as anchor to check that codepath
        settings = Settings.parse("{\"path\":\"bazz\",\"foo\":\"baa\"}", null);
        assertEquals(Paths.get("bazz", "baa").toString(), settings.getPath("foo"));

        settings = Settings.parse("{\"foo\":\"baa\"}", SettingsAnchor.filesystem(Paths.get("maa")));
        assertEquals(Paths.get("maa", "baa").toString(), settings.getPath("foo"));
    }

    @Deprecated
    @Test(expected = IllegalArgumentException.class)
    public void getPathWithError() {
        Settings settings = Settings.parse("{\"foo\":123}", SettingsAnchor.none());
        settings.getPath("foo");
    }

    @Test
    public void getPathList() {
        Settings settings = Settings.parse("{\"foo\":[\"baa\",\"bazz\"]}", SettingsAnchor.none());
        assertThat(settings.getPathList("foo"), allOf(hasItem("baa"), hasItem("bazz")));
        assertTrue(settings.getPathList("baa").isEmpty());

        // using null as anchor to check that codepath
        settings = Settings.parse("{\"path\":\"bazz\",\"foo\":[\"baa\",\"bazz\"]}", null);
        assertThat(settings.getPathList("foo"),
                allOf(hasItem(Paths.get("bazz", "baa").toString()), hasItem(Paths.get("bazz", "bazz").toString())));

        settings = Settings.parse("{\"foo\":[\"baa\",\"bazz\"]}", SettingsAnchor.filesystem(Paths.get("maa")));
        assertThat(settings.getPathList("foo"),
                allOf(hasItem(Paths.get("maa", "baa").toString()), hasItem(Paths.get("maa", "bazz").toString())));

    }

    @Test(expected = IllegalArgumentException.class)
    public void getPathListWithError() {
        Settings settings = Settings.parse("{\"baa\":123}", SettingsAnchor.none());
        settings.getPathList("baa");
    }

    @Test
    public void merge() {
        Settings settings = Settings.parse("{\"baa\":\"bazz\",\"list\":[1,2]}", SettingsAnchor.none());
        Settings settings2 = Settings.parse("{\"baa\":\"boo\",\"list\":[0],\"list2\":[0]}",
                SettingsAnchor.filesystem(Paths.get("path")));
        Settings merged = settings.merge(settings2);

        assertThat(merged.getString("baa"), is("boo"));

        assertThat(merged.getIntList("list"), contains(1, 2, 0));
        assertThat(merged.getIntList("list2"), contains(0));
    }

}
