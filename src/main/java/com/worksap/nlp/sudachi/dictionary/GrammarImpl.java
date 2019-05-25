/*
 * Copyright (c) 2017 Works Applications Co., Ltd.
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

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GrammarImpl implements Grammar {

    private static final int POS_DEPTH = 6;
    private static final short[] BOS_PARAMETER = new short[] { 0, 0, 0 };
    private static final short[] EOS_PARAMETER = new short[] { 0, 0, 0 };

    private final ByteBuffer bytes;
    private List<List<String>> posList;
    private ByteBuffer connectTableBytes;
    private boolean isCopiedConnectTable;
    private int connectTableOffset;
    private final short leftIdSize;
    private final short rightIdSize;
    
    private CharacterCategory charCategory;

    private int storageSize;

    public GrammarImpl(ByteBuffer bytes, int offset) {
        int originalOffset = offset;
        this.bytes = bytes;
        this.connectTableBytes = bytes;
        isCopiedConnectTable = false;
        int posSize = bytes.getShort(offset);
        offset += 2;
        posList = new ArrayList<>(posSize);
        for (int i = 0; i < posSize; i++) {
            ArrayList<String> pos = new ArrayList<>(POS_DEPTH);
            for (int j = 0; j < POS_DEPTH; j++) {
                pos.add(bufferToString(offset));
                offset += 1 + 2 * pos.get(j).length();
            }
            posList.add(Collections.unmodifiableList(pos));
        }
        leftIdSize = bytes.getShort(offset);
        offset += 2;
        rightIdSize = bytes.getShort(offset);
        offset += 2;
        connectTableOffset = offset;

        storageSize = (offset - originalOffset) + 2 * leftIdSize * rightIdSize;
    }

    public int storageSize() {
        return storageSize;
    }

    @Override
    public int getPartOfSpeechSize() {
        return posList.size();
    }

    @Override
    public List<String> getPartOfSpeechString(short posId) {
        return posList.get(posId);
    }

    @Override
    public short getPartOfSpeechId(List<String> pos) {
        return (short)posList.indexOf(pos);
    }

    @Override
    public short getConnectCost(short leftId, short rightId) {
        return connectTableBytes.getShort(connectTableOffset + leftId * 2
                                          + 2 * leftIdSize * rightId);
    }

    @Override
    public void setConnectCost(short leftId, short rightId, short cost) {
        if (!isCopiedConnectTable) {
            copyConnectTable();
        }
        connectTableBytes.putShort(connectTableOffset + leftId * 2
                                   + 2 * leftIdSize * rightId, cost);
    }

    @Override
    public short[] getBOSParameter() { return BOS_PARAMETER; }

    @Override
    public short[] getEOSParameter() { return EOS_PARAMETER; }

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

    private synchronized void copyConnectTable() {
        ByteBuffer newBuffer = ByteBuffer.allocate(2 * leftIdSize * rightIdSize);
        newBuffer.order(ByteOrder.LITTLE_ENDIAN);
        ByteBuffer srcBuffer = connectTableBytes.duplicate();
        ((Buffer)srcBuffer).position(connectTableOffset);
        srcBuffer.limit(connectTableOffset + 2 * leftIdSize * rightIdSize);
        newBuffer.put(srcBuffer);
        connectTableBytes = newBuffer;
        connectTableOffset = 0;
        isCopiedConnectTable = true;
    }
}
