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

package com.worksap.nlp.sudachi.dictionary;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GrammarImpl implements Grammar {
    private static final int POS_DEPTH = POS.DEPTH;
    private static final short[] BOS_PARAMETER = new short[] { 0, 0, 0 };
    private static final short[] EOS_PARAMETER = new short[] { 0, 0, 0 };

    private final ByteBuffer bytes;
    private final List<POS> posList;
    private boolean isCopiedConnectTable;
    private Connection matrix;

    private CharacterCategory charCategory;

    private int storageSize;

    public GrammarImpl(ByteBuffer bytes, int offset) {
        int originalOffset = offset;
        this.bytes = bytes;
        isCopiedConnectTable = false;
        int posSize = bytes.getShort(offset);
        offset += 2;
        posList = new ArrayList<>(posSize);
        for (int i = 0; i < posSize; i++) {
            String[] pos = new String[POS_DEPTH];
            for (int j = 0; j < POS_DEPTH; j++) {
                pos[j] = bufferToString(offset);
                offset += 1 + 2 * pos[j].length();
            }
            posList.add(new POS(pos));
        }
        int leftIdSize = bytes.getShort(offset);
        offset += 2;
        int rightIdSize = bytes.getShort(offset);
        offset += 2;
        ByteBuffer dup = bytes.duplicate();
        dup.position(offset);
        dup.order(bytes.order());
        dup.limit(offset + leftIdSize * rightIdSize * 2);
        matrix = new Connection(dup.asShortBuffer(), leftIdSize, rightIdSize);
        storageSize = (offset - originalOffset) + 2 * leftIdSize * rightIdSize;
    }

    public GrammarImpl() {
        bytes = ByteBuffer.allocate(0);
        posList = Collections.emptyList();
    }

    public int storageSize() {
        return storageSize;
    }

    public void addPosList(GrammarImpl grammar) {
        posList.addAll(grammar.posList);
    }

    @Override
    public int getPartOfSpeechSize() {
        return posList.size();
    }

    @Override
    public POS getPartOfSpeechString(short posId) {
        return posList.get(posId);
    }

    @Override
    public short getPartOfSpeechId(List<String> pos) {
        // POS.equals() is compatible with List<String>, this is OK
        // noinspection SuspiciousMethodCalls
        return (short) posList.indexOf(pos);
    }

    @Override
    public short getConnectCost(short left, short right) {
        return matrix.cost(left, right);
    }

    @Override
    public void setConnectCost(short left, short right, short cost) {
        if (!isCopiedConnectTable) {
            matrix = matrix.ownedCopy();
            isCopiedConnectTable = true;
        }
        matrix.setCost(left, right, cost);
    }

    @Override
    public short[] getBOSParameter() {
        return BOS_PARAMETER;
    }

    @Override
    public short[] getEOSParameter() {
        return EOS_PARAMETER;
    }

    @Override
    public CharacterCategory getCharacterCategory() {
        return charCategory;
    }

    @Override
    public void setCharacterCategory(CharacterCategory charCategory) {
        this.charCategory = charCategory;
    }

    private String bufferToString(int offset) {
        int length = Byte.toUnsignedInt(bytes.get(offset++));
        char[] str = new char[length];
        for (int i = 0; i < length; i++) {
            str[i] = bytes.getChar(offset + 2 * i);
        }
        return new String(str);
    }
}
