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

        /** @return reference-id of the entry, or null if unset. */
        String referenceId();
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

        @Override
        public String referenceId() {
            return entry.referenceId();
        }
    }

    // entries
    private final List<? extends Entry> systemEntries;
    private final List<? extends Entry> userEntries;
    private final Map<String, List<EntryWithFlag>> byHeadword;
    private final Map<String, EntryWithFlag> byReferenceId;

    public EntryLookup(List<? extends Entry> systemEntries, List<? extends Entry> userEntries) {
        this.systemEntries = systemEntries;
        this.userEntries = userEntries;

        HashMap<String, List<EntryWithFlag>> result = new HashMap<>(
                (systemEntries.size() + userEntries.size()) * 4 / 3);
        HashMap<String, EntryWithFlag> refs = new HashMap<>((systemEntries.size() + userEntries.size()) / 10);
        // put user entries first to prioritize them over system entries.
        for (Entry e : userEntries) {
            register(result, refs, e, true);
        }
        for (Entry e : systemEntries) {
            register(result, refs, e, false);
        }
        byHeadword = result;
        byReferenceId = refs;
    }

    private void register(Map<String, List<EntryWithFlag>> headwords, Map<String, EntryWithFlag> refs, Entry e,
            boolean isUser) {
        EntryWithFlag wrapped = new EntryWithFlag(e, isUser);
        headwords.computeIfAbsent(e.headword(), x -> new ArrayList<>()).add(wrapped);

        String referenceId = e.referenceId();
        if (referenceId == null) {
            return;
        }

        EntryWithFlag existing = refs.putIfAbsent(referenceId, wrapped);
        // User/system overlap is allowed here because user resolution intentionally
        // checks the user side first and falls back to the system side only if the
        // user dictionary does not define the same reference-id.
        if (existing != null && existing.isUser == isUser) {
            throw new IllegalArgumentException("duplicated reference_id: " + referenceId);
        }
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

    public EntryWithFlag byReferenceId(String referenceId) {
        return byReferenceId.get(referenceId);
    }

    /**
     * Add an entry for headword search.
     * 
     * @param e
     */
    public void add(Entry e, boolean isUser) {
        register(byHeadword, byReferenceId, e, isUser);
    }
}
