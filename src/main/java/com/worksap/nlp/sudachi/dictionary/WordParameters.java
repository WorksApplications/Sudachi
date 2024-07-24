/*
 * Copyright (c) 2024 Works Applications Co., Ltd.
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

package com.worksap.nlp.sudachi.dictionary;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import com.worksap.nlp.sudachi.WordId;

public class WordParameters {
    private final ByteBuffer data;

    private WordParameters(ByteBuffer data) {
        this.data = data;
    }

    public long loadParams(int wordId) {
        int addr = WordInfoList.wordId2offset(WordId.word(wordId));
        return data.getLong(addr);
    }

    public void setCost(int wordId, short cost) {
        int addr = WordInfoList.wordId2offset(WordId.word(wordId)) + 6;
        data.putShort(addr, cost);
    }

    public static WordParameters readOnly(ByteBuffer full, Description desc) {
        ByteBuffer data = desc.slice(full, Blocks.ENTRIES);
        data.order(ByteOrder.LITTLE_ENDIAN);
        return new WordParameters(data);
    }

    public static WordParameters readWrite(ByteBuffer full, Description desc) {
        WordParameters ro = readOnly(full, desc);
        ByteBuffer roBuf = ro.data;
        int lim = roBuf.limit();
        ByteBuffer buf = ByteBuffer.allocate(lim);
        buf.order(ByteOrder.LITTLE_ENDIAN);
        buf.put(roBuf);
        buf.position(0);
        return new WordParameters(buf);
    }

    public static short leftId(long packed) {
        return (short) (packed & 0xffff);
    }

    public static short rightId(long packed) {
        return (short) ((packed >>> 16) & 0xffff);
    }

    public static short cost(long packed) {
        return (short) ((packed >>> 32) & 0xffff);
    }
}
