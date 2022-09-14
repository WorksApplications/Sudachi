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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class RawLexicon {
    private static final long MAX_OFFSET = Integer.MAX_VALUE * 8L;
    private static final int INITIAL_OFFSET = 32;
    private final StringStorage strings = new StringStorage();
    private final List<RawWordEntry> entries = new ArrayList<>();
    private final List<RawWordEntry> notIndexed = new ArrayList<>();

    private final Index index = new Index();
    private boolean user;

    private long offset = INITIAL_OFFSET;

    public void read(String name, InputStream data, POSTable posTable) throws IOException {
        read(name, new InputStreamReader(data, StandardCharsets.UTF_8), posTable);
    }

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
        }
        this.offset = offset;
    }

    public static int pointer(long offset) {
        return (int) (offset >>> 3);
    }

    public void checkOffset(long offset) {
        if ((offset & 0x7) != 0) {
            throw new IllegalArgumentException("offset is not aligned, should not happen");
        }
        if (offset > MAX_OFFSET) {
            throw new IllegalArgumentException("passed dictionary is too large, Sudachi can't handle it");
        }
    }

    public void compile(POSTable pos, BlockLayout layout) throws IOException {
        index.compile(layout, notIndexed);
        layout.block(Blocks.STRINGS, this::writeStrings);
        layout.block(Blocks.ENTRIES, (p) -> writeEntries(pos, p));
    }

    private Void writeEntries(POSTable pos, BlockOutput blockOutput) throws IOException {
        return blockOutput.measured("Word Entries", (p) -> {
            List<RawWordEntry> list = entries;
            Lookup2 lookup = new Lookup2(list);
            WordRef.Parser refParser = WordRef.parser(pos, !user, false);
            ChanneledBuffer buf = new ChanneledBuffer(blockOutput.getChannel(), WordEntryLayout.MAX_LENGTH * 4);
            buf.position(INITIAL_OFFSET);
            WordEntryLayout layout = new WordEntryLayout(lookup, strings, refParser, buf);
            int size = list.size();
            int ptr = pointer(INITIAL_OFFSET);
            for (int i = 0; i < size; ++i) {
                RawWordEntry e = list.get(i);
                if (e.pointer != ptr) {
                    throw new IllegalStateException("expected entry pointer != actual pointer, i=" + i);
                }
                size += e.addPhantomEntries(list, lookup);
                ptr = layout.put(e);
                p.progress(i, size);
            }
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
}
