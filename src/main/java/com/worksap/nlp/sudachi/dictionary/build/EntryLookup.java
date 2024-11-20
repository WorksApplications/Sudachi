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

/**
 * Utility to lookup entries from the list. Used to resolve {@link WordRef}.
 */
public class EntryLookup {
    public interface Entry {
        /** @return wordid of the entry. */
        int pointer();

        /** @return if has given pos and reading. */
        boolean matches(short posId, String reading);

        /** @return headword of the entry. */
        String headword();
    }

    /** Wrapper class to distinguish if the entry is system or user. */
    public class EntryWithFlag implements Entry {
        private Entry entry;
        boolean isUser;

        EntryWithFlag(Entry entry, boolean isUser) {
            this.entry = entry;
            this.isUser = isUser;
        }

        @Override
        public int pointer() {
            return entry.pointer();
        }

        @Override
        public boolean matches(short posId, String reading) {
            return entry.matches(posId, reading);
        }

        @Override
        public String headword() {
            return entry.headword();
        }
    }

    // entries
    private final List<? extends Entry> systemEntries;
    private final List<? extends Entry> userEntries;
    // mapping to entries that have same headwords
    private final Map<String, List<EntryWithFlag>> byHeadword;

    public EntryLookup(List<? extends Entry> systemEntries, List<? extends Entry> userEntries) {
        this.systemEntries = systemEntries;
        this.userEntries = userEntries;

        HashMap<String, List<EntryWithFlag>> result = new HashMap<>(
                (systemEntries.size() + userEntries.size()) * 4 / 3);
        for (Entry e : systemEntries) {
            List<EntryWithFlag> sublist = result.computeIfAbsent(e.headword(), x -> new ArrayList<>());
            sublist.add(new EntryWithFlag(e, false));
        }
        for (Entry e : userEntries) {
            List<EntryWithFlag> sublist = result.computeIfAbsent(e.headword(), x -> new ArrayList<>());
            sublist.add(new EntryWithFlag(e, true));
        }
        byHeadword = result;
    }

    /**
     * Lookup an entry by the list index. Make sure you know the order of entries in
     * the list.
     * 
     * @param index
     * @param isUser
     *            if true, lookup from the user lecixons, otherwise from reference
     *            system dict.
     * @return
     */
    public EntryWithFlag byIndex(int index, boolean isUser) {
        // if userEntries is empty (i.e. building system dict), ignore isUser flag
        if (isUser && !userEntries.isEmpty()) {
            return new EntryWithFlag(userEntries.get(index), true);
        }
        return new EntryWithFlag(systemEntries.get(index), false);
    }

    /**
     * Lookup entries by the headword.
     * 
     * @param headword
     * @return
     */
    public List<EntryWithFlag> byHeadword(String headword) {
        return byHeadword.get(headword);
    }

    /**
     * Add an entry for headword search.
     * 
     * @param e
     */
    public void add(Entry e, boolean isUser) {
        byHeadword.computeIfAbsent(e.headword(), x -> new ArrayList<>()).add(new EntryWithFlag(e, isUser));
    }
}
