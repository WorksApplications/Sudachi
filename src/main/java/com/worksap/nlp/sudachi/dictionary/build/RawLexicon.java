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

import com.worksap.nlp.sudachi.dictionary.Block;
import com.worksap.nlp.sudachi.dictionary.DoubleArrayLexicon;
import com.worksap.nlp.sudachi.dictionary.Ints;
import com.worksap.nlp.sudachi.dictionary.WordInfoList;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Dictionary part: Lexicon loaded from csv files.
 */
public class RawLexicon {
    // word id must be integer size.
    // However, current implementation (ByteBuffer) cannot handle offset larger than
    // Integer.MAX_VALUE.
    private static final long MAX_OFFSET = (long) Integer.MAX_VALUE * WordInfoList.OFFSET_ALIGNMENT;
    // put empty entry at the first
    private static final int INITIAL_OFFSET = 32;

    // full list of word entries, in the order in csv.
    private final List<RawWordEntry> entries = new ArrayList<>();
    // entries loaded from the referencing system dictionary (for user
    // dict build).
    private final List<CompiledWordEntry> preloadedEntries = new ArrayList<>();
    private int nPhantomEntries = 0;

    private final Index index = new Index();
    private final List<RawWordEntry> notIndexed = new ArrayList<>();
    private final StringStorage strings = new StringStorage();
    private boolean isUser = false;

    // offset for next entry
    private long offset = INITIAL_OFFSET;
    private boolean runtimeCosts = false;

    /**
     * Preload entries from the lexicon (of the system dictionary). They are only
     * used to resolve wordref.
     * 
     * @param lexicon
     *            lexicon of a system dictionary.
     * @return number of entries read.
     */
    public int preloadFrom(DoubleArrayLexicon lexicon, Progress progress) {
        this.isUser = true;

        Ints allIds = new Ints(lexicon.size());
        Iterator<Ints> ids = lexicon.getWordIdTable().wordIds();
        while (ids.hasNext()) {
            allIds.appendAll(ids.next());
        }
        allIds.sort();

        for (int i = 0; i < allIds.length(); i++) {
            preloadedEntries.add(new CompiledWordEntry(lexicon, allIds.get(i)));
            progress.progress(i, allIds.length());
        }
        return preloadedEntries.size();
    }

    /**
     * Read lexicon from InputStream.
     * 
     * @param name
     * @param data
     * @param posTable
     * @throws IOException
     */
    public void read(String name, InputStream data, POSTable posTable, short numLeft, short numRight)
            throws IOException {
        read(name, new InputStreamReader(data, StandardCharsets.UTF_8), posTable, numLeft, numRight);
    }

    /**
     * Read lexicon from Reader.
     * 
     * @param name
     * @param data
     * @param posTable
     * @throws IOException
     */
    public void read(String name, Reader data, POSTable posTable, short numLeft, short numRight) throws IOException {
        CSVParser parser = new CSVParser(data);
        parser.setName(name);
        RawLexiconReader reader = new RawLexiconReader(parser, posTable);

        RawWordEntry entry;
        while ((entry = reader.nextEntry()) != null) {
            if (entry.leftId >= numLeft || entry.rightId >= numRight) {
                throw new IllegalArgumentException(String.format("connection id out of range: %d, %d (line %d of %s)",
                        entry.leftId, entry.rightId, entry.sourceLine, entry.sourceName));
            }

            entry.publishStrings(strings);
            entries.add(entry);
            entry.pointer = pointer(offset);
            offset += entry.computeExpectedSize();
            checkOffset(offset);
            if (entry.shouldBeIndexed()) {
                index.add(entry.indexForm, entry.pointer);
            } else {
                notIndexed.add(entry);
            }
            this.runtimeCosts |= !DoubleArrayLexicon.isNormalCost(entry.cost);
        }
    }

    /**
     * Convert offset to pointer (word id)
     * 
     * @param offset
     * @return
     */
    public static int pointer(long offset) {
        return WordInfoList.offset2wordId(offset);
    }

    /** check if the current offset is valid */
    public void checkOffset(long offset) {
        if ((offset & 0x7) != 0) {
            throw new IllegalArgumentException("offset is not aligned, should not happen");
        }
        if (offset > MAX_OFFSET) {
            throw new IllegalArgumentException("passed dictionary is too large, Sudachi can't handle it");
        }
    }

    /**
     * Write lexicon to the provided block layout.
     * 
     * @param layout
     * @throws IOException
     */
    public void compile(BlockLayout layout) throws IOException {
        index.compile(layout, notIndexed);
        // entry layout requires stringstorage to be compiled beforehand.
        layout.block(Block.STRINGS, this::writeStrings);
        layout.block(Block.ENTRIES, this::writeEntries);
    }

    private Void writeStrings(BlockOutput blockOutput) throws IOException {
        return blockOutput.measured("Strings", p -> {
            strings.compile(p);
            strings.writeCompact(blockOutput.getChannel());
            return null;
        });
    }

    private Void writeEntries(BlockOutput blockOutput) throws IOException {
        return blockOutput.measured("Word Entries", p -> {
            List<RawWordEntry> list = entries;
            EntryLookup lookup = isUser ? new EntryLookup(preloadedEntries, list)
                    : new EntryLookup(list, new ArrayList<>());
            BufferedChannel buf = new BufferedChannel(blockOutput.getChannel(), WordEntryLayout.MAX_LENGTH * 4);
            buf.position(INITIAL_OFFSET);
            WordEntryLayout layout = new WordEntryLayout(lookup, strings, buf, isUser);
            int size = list.size();
            int ptr = pointer(INITIAL_OFFSET);
            for (int i = 0; i < size; ++i) {
                RawWordEntry e = list.get(i);
                if (e.pointer != ptr) {
                    throw new IllegalStateException("expected entry pointer != actual pointer, i=" + i);
                }
                // size may increases with phantom entry
                size += addPhantomEntries(e, list, lookup);
                ptr = layout.put(e);
                p.progress(i, size);
            }
            buf.flush();
            return null;
        });
    }

    /**
     * Add headword-only entry to access via normalized_form reference if necessary.
     * 
     * @param list
     * @param lookup
     * @return 1 if phantom entry added, 0 otherwise
     */
    private int addPhantomEntries(RawWordEntry entry, List<RawWordEntry> list, EntryLookup lookup) {
        if (!(entry.normalizedFormRef instanceof WordRef.RefByHeadword)) {
            return 0;
        }

        WordRef.RefByHeadword ref = (WordRef.RefByHeadword) entry.normalizedFormRef;
        if (lookup.byHeadword(ref.getHeadword()) != null) {
            return 0;
        }

        RawWordEntry phantom = RawWordEntry.makePhantom(entry, ref.getHeadword());
        RawWordEntry last = list.get(list.size() - 1);
        phantom.pointer = pointer((long) WordInfoList.wordId2offset(last.pointer) + last.computeExpectedSize());
        list.add(phantom);
        lookup.add(phantom, isUser);
        nPhantomEntries += 1;
        return 1;
    }

    /** @return number of entries in the TRIE index */
    public int getIndexedEntries() {
        return this.entries.size() - this.notIndexed.size() - nPhantomEntries;
    }

    /** @return number of all entries including non-indexed ones */
    public int getTotalEntries() {
        return this.entries.size() - nPhantomEntries;
    }

    /** @return if lexicon has entries that need runtime cost caluculation */
    public boolean hasRuntimeCosts() {
        return this.runtimeCosts;
    }
}
