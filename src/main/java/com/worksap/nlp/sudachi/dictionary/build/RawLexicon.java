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
    private final StringStorage strings = new StringStorage();
    private final List<RawWordEntry> entries = new ArrayList<>();

    private long offset = 0;

    public void read(InputStream data, POSTable posTable) throws IOException {
        read(new InputStreamReader(data, StandardCharsets.UTF_8), posTable);
    }

    public void read(Reader data, POSTable posTable) throws IOException {
        CSVParser parser = new CSVParser(data);
        RawLexiconReader reader = new RawLexiconReader(parser, posTable);

        long offset = this.offset;
        RawWordEntry entry;
        while ((entry = reader.nextEntry()) != null) {
            strings.add(entry.headword);
            strings.add(entry.reading);
            entries.add(entry);
            entry.pointer = pointer(offset);
            offset += entry.computeExpectedSize();
            checkOffset(offset);
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
}
