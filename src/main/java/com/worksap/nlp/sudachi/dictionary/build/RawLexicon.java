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

import com.worksap.nlp.sudachi.dictionary.Blocks;
import com.worksap.nlp.sudachi.dictionary.CSVParser;
import com.worksap.nlp.sudachi.dictionary.DoubleArrayLexicon;
import com.worksap.nlp.sudachi.dictionary.Ints;
import com.worksap.nlp.sudachi.dictionary.Lexicon;
import com.worksap.nlp.sudachi.dictionary.WordInfoList;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    private final Index index = new Index();
    private final List<RawWordEntry> notIndexed = new ArrayList<>();
    private final StringStorage strings = new StringStorage();
    private boolean user = false;

    // offset for next entry
    private long offset = INITIAL_OFFSET;
    private boolean runtimeCosts = false;

    // entries loaded from the referencing system dictionary (for user
    // dict build).
    private final List<CompiledWordEntry> preloadedEntries = new ArrayList<>();

    /**
     * Preload entries from the lexicon (of the system dictionary). They are only
     * used to resolve wordref.
     * 
     * @param lexicon
     * @return number of entries read.
     */
    public int preloadFrom(Lexicon lexicon, Progress progress) {
        Ints allIds = new Ints(lexicon.size());
        Iterator<Ints> ids = lexicon.wordIds(0);
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

    /** Full list of entries in referencing system and target lexicon. */
    private List<Lookup2.Entry> lookupEntries() {
        return Stream.concat(preloadedEntries.stream(), entries.stream()).collect(Collectors.toList());
    }

    /**
     * Read lexicon from InputStream.
     * 
     * @param name
     * @param data
     * @param posTable
     * @throws IOException
     */
    public void read(String name, InputStream data, POSTable posTable) throws IOException {
        read(name, new InputStreamReader(data, StandardCharsets.UTF_8), posTable);
    }

    /**
     * Read lexicon from Reader.
     * 
     * @param name
     * @param data
     * @param posTable
     * @throws IOException
     */
    public void read(String name, Reader data, POSTable posTable) throws IOException {
        CSVParser parser = new CSVParser(data);
        parser.setName(name);
        RawLexiconReader reader = new RawLexiconReader(parser, posTable, user);

        long offset = this.offset;
        RawWordEntry entry;
        while ((entry = reader.nextEntry()) != null) {
            entry.publishStrings(strings);
            entries.add(entry);
            entry.pointer = pointer(offset);
            offset += entry.computeExpectedSize();
            checkOffset(offset);
            if (entry.shouldBeIndexed()) {
                index.add(entry.headword, entry.pointer);
            } else {
                notIndexed.add(entry);
            }
            this.runtimeCosts |= !DoubleArrayLexicon.isNormalCost(entry.cost);
        }
        this.offset = offset;
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
     * @param pos
     * @param layout
     * @throws IOException
     */
    public void compile(POSTable pos, BlockLayout layout) throws IOException {
        index.compile(layout, notIndexed);
        // entry layout requires stringstorage to be compiled beforehand.
        layout.block(Blocks.STRINGS, this::writeStrings);
        layout.block(Blocks.ENTRIES, (p) -> writeEntries(pos, p));
    }

    private Void writeEntries(POSTable pos, BlockOutput blockOutput) throws IOException {
        return blockOutput.measured("Word Entries", (p) -> {
            List<RawWordEntry> list = entries;
            Lookup2 lookup = new Lookup2(lookupEntries(), preloadedEntries.size());
            WordRef.Parser refParser = WordRef.parser(pos, !user, false, false);
            BufferedChannel buf = new BufferedChannel(blockOutput.getChannel(), WordEntryLayout.MAX_LENGTH * 4);
            buf.position(INITIAL_OFFSET);
            WordEntryLayout layout = new WordEntryLayout(lookup, strings, refParser, buf);
            int size = list.size();
            int ptr = pointer(INITIAL_OFFSET);
            for (int i = 0; i < size; ++i) {
                RawWordEntry e = list.get(i);
                if (e.pointer != ptr) {
                    throw new IllegalStateException("expected entry pointer != actual pointer, i=" + i);
                }
                // size may increases with phantom entry
                size += e.addPhantomEntries(list, lookup);
                ptr = layout.put(e);
                p.progress(i, size);
            }
            buf.flush();
            return null;
        });
    }

    private Void writeStrings(BlockOutput blockOutput) throws IOException {
        return blockOutput.measured("Strings", (p) -> {
            strings.compile(p);
            strings.writeCompact(blockOutput.getChannel());
            return null;
        });
    }

    /** @return number of entries in the TRIE index */
    public int getIndexedEntries() {
        return this.entries.size() - this.notIndexed.size();
    }

    /** @return number of all entries including non-indexed ones */
    public int getTotalEntries() {
        return this.entries.size();
    }

    /** @return if lexicon has entries that need runtime cost caluculation */
    public boolean hasRuntimeCosts() {
        return this.runtimeCosts;
    }
}
