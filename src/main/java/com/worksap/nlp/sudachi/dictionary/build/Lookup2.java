/*
 * Copyright (c) 2022 Works Applications Co., Ltd.
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

package com.worksap.nlp.sudachi.dictionary.build;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Lookup2 {
    public interface Entry {
        int pointer();

        boolean matches(short posId, String reading);

        String headword();
    }

    public Lookup2(List<? extends Entry> entries) {
        this.entries = entries;
        HashMap<String, List<Entry>> result = new HashMap<>(entries.size() * 4 / 3);
        for (Entry e : entries) {
            List<Entry> sublist = result.computeIfAbsent(e.headword(), x -> new ArrayList<>());
            sublist.add(e);
        }
        bySurface = result;
    }

    private final List<? extends Entry> entries;
    private final Map<String, List<Entry>> bySurface;

    public Entry byIndex(int index) {
        return entries.get(index);
    }

    public List<Entry> byHeadword(String headword) {
        return bySurface.get(headword);
    }

    public void add(Entry e) {
        bySurface.computeIfAbsent(e.headword(), x -> new ArrayList<>()).add(e);
    }
}
