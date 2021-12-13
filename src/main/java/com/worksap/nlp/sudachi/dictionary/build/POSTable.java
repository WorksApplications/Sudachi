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

import com.worksap.nlp.sudachi.dictionary.GrammarImpl;
import com.worksap.nlp.sudachi.dictionary.POS;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class POSTable implements WriteDictionary {
    private int builtin = 0;
    private final List<POS> table = new ArrayList<>();

    short getId(POS s) {
        int id = table.indexOf(s);
        if (id < 0) {
            id = table.size();
            if (id >= Short.MAX_VALUE) {
                throw new IllegalArgumentException("maximum POS number exceeded by " + s);
            }
            table.add(s);
        }
        return (short) id;
    }

    public void preloadFrom(GrammarImpl grammar) {
        int partOfSpeechSize = grammar.getPartOfSpeechSize();
        for (short i = 0; i < partOfSpeechSize; ++i) {
            table.add(grammar.getPartOfSpeechString(i));
        }
        builtin += partOfSpeechSize;
    }

    List<POS> getList() {
        return table;
    }

    @Override
    public void writeTo(ModelOutput output) throws IOException {
        output.withPart("POS table", () -> {
            Strings buffer = new Strings(256, ownedLength() * POS.DEPTH);
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

}
