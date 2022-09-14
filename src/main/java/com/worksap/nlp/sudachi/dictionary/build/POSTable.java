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

package com.worksap.nlp.sudachi.dictionary.build;

import com.worksap.nlp.sudachi.dictionary.Grammar;
import com.worksap.nlp.sudachi.dictionary.POS;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class POSTable implements WriteDictionary {
    private final List<POS> table = new ArrayList<>();
    private final HashMap<POS, Short> lookup = new HashMap<>();
    private int builtin = 0;

    short getId(POS s) {
        return lookup.computeIfAbsent(s, p -> {
            int next = table.size();
            if (next >= Short.MAX_VALUE) {
                throw new IllegalArgumentException("maximum POS number exceeded by " + s);
            }
            table.add(s);
            return (short) next;
        });
    }

    public void preloadFrom(Grammar grammar) {
        int partOfSpeechSize = grammar.getPartOfSpeechSize();
        for (short i = 0; i < partOfSpeechSize; ++i) {
            POS pos = grammar.getPartOfSpeechString(i);
            table.add(pos);
            lookup.put(pos, i);
        }
        builtin += partOfSpeechSize;
    }

    List<POS> getList() {
        return table;
    }

    @Override
    public void writeTo(ModelOutput output) throws IOException {
        output.withPart("POS table", () -> {
            DicBuffer buffer = new DicBuffer(128 * 1024);
            buffer.putShort((short) ownedLength());
            for (int i = builtin; i < table.size(); ++i) {
                for (String s : table.get(i)) {
                    if (!buffer.put(s)) {
                        // handle buffer overflow, this should be extremely rare
                        buffer.consume(output::write);
                        buffer.put(s);
                    }
                }
            }
            buffer.consume(output::write);
        });
    }

    public int ownedLength() {
        return table.size() - builtin;
    }

    public Void compile(BlockOutput out) throws IOException {
        return out.measured("POS Table", (p) -> {
            ChanneledBuffer cbuf = new ChanneledBuffer(out.getChannel());
            cbuf.byteBuffer(2).putShort((short) table.size());
            for (int i = 0; i < table.size(); ++i) {
                BufWriter writer = cbuf.writer(POS.MAX_BINARY_LENGTH);
                POS pos = table.get(i);
                for (String s : pos) {
                    // strings are always shorter than POS.MAX
                    writer.putShortString(s);
                }
                p.progress(i, table.size());
            }
            return null;
        });

    }
}
