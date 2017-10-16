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

package com.worksap.nlp.dartsclone.details;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.function.BiConsumer;

public class DoubleArrayBuilder {

    private static final int BLOCK_SIZE = 256;
    private static final int NUM_EXTRA_BLOCKS = 16;
    private static final int NUM_EXTRAS = BLOCK_SIZE * NUM_EXTRA_BLOCKS;

    private static final int UPPER_MASK = 0xFF << 21;
    private static final int LOWER_MASK = 0xFF;

    static class DoubleArrayBuilderExtraUnit {
        int prev;
        int next;
        boolean isFixed;
        boolean isUsed;
    }
    
    private BiConsumer<Integer, Integer> progressFunction;
    private ArrayList<DoubleArrayBuilderUnit> units = new ArrayList<>();
    private DoubleArrayBuilderExtraUnit[] extras;
    private ArrayList<Byte> labels = new ArrayList<>();
    private int[] table;
    private int extrasHead;

    public DoubleArrayBuilder(BiConsumer<Integer, Integer> progressFunction) {
        this.progressFunction = progressFunction;
    }

    public void build(KeySet keySet) {
        if (keySet.hasValues()) {
            DAWGBuilder dawgBuilder = new DAWGBuilder();
            buildDAWG(keySet, dawgBuilder);
            buildFromDAWG(dawgBuilder);
            dawgBuilder.clear();
        } else {
            buildFromKeySet(keySet);
        }
    }

    public ByteBuffer copy() {
        ByteBuffer buffer = ByteBuffer.allocate(units.size() * 4);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        for (DoubleArrayBuilderUnit u : units) {
            buffer.putInt(u.unit);
        }
        buffer.rewind();
        return buffer;
    }

    public void clear() {
        units = null;
        extras = null;
        labels = null;
        table = null;
    }

    int numBlocks() { return units.size() / BLOCK_SIZE; }
    
    DoubleArrayBuilderExtraUnit extras(int id) {
        return extras[id % NUM_EXTRAS];
    }

    void buildDAWG(KeySet keySet, DAWGBuilder dawgBuilder) {
        dawgBuilder.init();
        for (int i = 0; i < keySet.size(); i++) {
            dawgBuilder.insert(keySet.getKey(i), keySet.getValue(i));
            if (progressFunction != null) {
                progressFunction.accept(i + 1, keySet.size() + 1);
            }
        }
        dawgBuilder.finish();
    }

    void buildFromDAWG(DAWGBuilder dawg) {
        int numUnits = 1;
        while (numUnits < dawg.size()) {
            numUnits <<= 1;
        }
        units.ensureCapacity(numUnits);

        table = new int[dawg.numIntersections()];

        extras = new DoubleArrayBuilderExtraUnit[NUM_EXTRAS];
        for (int i = 0; i < extras.length; i++) {
            extras[i] = new DoubleArrayBuilderExtraUnit();
        }

        reserveId(0);
        extras(0).isUsed = true;
        units.get(0).setOffset(1);
        units.get(0).setLabel((byte)0);

        if (dawg.child(dawg.root()) != 0) {
            buildFromDAWG(dawg, dawg.root(), 0);
        }

        fixAllBlocks();

        extras = null;
        labels = null;
        table = null;
    }

    void buildFromDAWG(DAWGBuilder dawg, int dawgId, int dicId) {
        int dawgChildId = dawg.child(dawgId);
        if (dawg.isIntersection(dawgChildId)) {
            int intersectionId = dawg.intersectionId(dawgChildId);
            int offset = table[intersectionId];
            if (offset != 0) {
                offset ^= dicId;
                if ((offset & UPPER_MASK) == 0 || (offset & LOWER_MASK) == 0) {
                    if (dawg.isLeaf(dawgChildId)) {
                        units.get(dicId).setHasLeaf(true);
                    }
                    units.get(dicId).setOffset(offset);
                    return;
                }
            }
        }

        int offset = arrangeFromDAWG(dawg, dawgId, dicId);
        if (dawg.isIntersection(dawgChildId)) {
            table[dawg.intersectionId(dawgChildId)] = offset;
        }

        do {
            byte childLabel = dawg.label(dawgChildId);
            int dicChildId = offset ^ Byte.toUnsignedInt(childLabel);
            if (childLabel != 0) {
                buildFromDAWG(dawg, dawgChildId, dicChildId);
            }
            dawgChildId = dawg.sibling(dawgChildId);
        } while (dawgChildId != 0);
    }

    int arrangeFromDAWG(DAWGBuilder dawg, int dawgId, int dicId) {
        labels.clear();
        
        int dawgChildId = dawg.child(dawgId);
        while (dawgChildId != 0) {
            labels.add(dawg.label(dawgChildId));
            dawgChildId = dawg.sibling(dawgChildId);
        }

        int offset = findValidOffset(dicId);
        units.get(dicId).setOffset(dicId ^ offset);

        dawgChildId = dawg.child(dawgId);
        for (byte l : labels) {
            int dicChildId = offset ^ Byte.toUnsignedInt(l);
            reserveId(dicChildId);

            if (dawg.isLeaf(dawgChildId)) {
                units.get(dicId).setHasLeaf(true);
                units.get(dicChildId).setValue(dawg.value(dawgChildId));
            } else {
                units.get(dicChildId).setLabel(l);
            }

            dawgChildId = dawg.sibling(dawgChildId);
        }
        extras(offset).isUsed = true;

        return offset;
    }

    void buildFromKeySet(KeySet keySet) {
        int numUnits = 1;
        while (numUnits < keySet.size()) {
            numUnits <<= 1;
        }
        units.ensureCapacity(numUnits);

        extras = new DoubleArrayBuilderExtraUnit[NUM_EXTRAS];
        for (int i = 0; i < extras.length; i++) {
            extras[i] = new DoubleArrayBuilderExtraUnit();
        }

        reserveId(0);
        extras(0).isUsed = true;
        units.get(0).setOffset(1);
        units.get(0).setLabel((byte)0);

        if (keySet.size() > 0) {
            buildFromKeySet(keySet, 0, keySet.size(), 0, 0);
        }

        fixAllBlocks();

        extras = null;
        labels = null;
    }

    void buildFromKeySet(KeySet keySet, int begin, int end, int depth,
                         int dicId) {
        int offset = arrangeFromKeySet(keySet, begin, end, depth, dicId);

        while (begin < end) {
            if (keySet.getKeyByte(begin, depth) != 0) {
                break;
            }
            begin++;
        }
        if (begin == end) {
            return;
        }

        int lastBegin = begin;
        byte lastLabel = keySet.getKeyByte(begin, depth);
        while (++begin < end) {
            byte label = keySet.getKeyByte(begin, depth);
            if (label != lastLabel) {
                buildFromKeySet(keySet, lastBegin, begin, depth + 1,
                                offset ^ Byte.toUnsignedInt(lastLabel));
                lastBegin = begin;
                lastLabel = keySet.getKeyByte(begin, depth);
            }
        }
        buildFromKeySet(keySet, lastBegin, end, depth + 1,
                        offset ^ Byte.toUnsignedInt(lastLabel));
    }

    int arrangeFromKeySet(KeySet keySet, int begin, int end, int depth,
                          int dicId) {
        labels.clear();

        int value = -1;
        for (int i = begin; i < end; i++) {
            byte label = keySet.getKeyByte(i, depth);
            if (label == 0) {
                if (depth < keySet.getKey(i).length) {
                    throw new IllegalArgumentException("invalid null character");
                } else if (keySet.getValue(i) < 0) {
                    throw new IllegalArgumentException("negative value");
                }

                if (value == -1) {
                    value = keySet.getValue(i);
                }
                if (progressFunction != null) {
                    progressFunction.accept(i + 1, keySet.size() + 1);
                }
            }

            if (labels.isEmpty()) {
                labels.add(label);
            } else if (label != labels.get(labels.size() - 1)) {
                if (Byte.toUnsignedInt(label)
                    < Byte.toUnsignedInt(labels.get(labels.size() - 1))) {
                    throw new IllegalArgumentException("wrong key order");
                }
                labels.add(label);
            }
        }

        int offset = findValidOffset(dicId);
        units.get(dicId).setOffset(dicId ^ offset);

        for (byte l : labels) {
            int dicChildId = offset ^ Byte.toUnsignedInt(l);
            reserveId(dicChildId);
            if (l == 0) {
                units.get(dicId).setHasLeaf(true);
                units.get(dicChildId).setValue(value);
            } else {
                units.get(dicChildId).setLabel(l);
            }
        }
        extras(offset).isUsed = true;

        return offset;
    }

    int findValidOffset(int id) {
        if (extrasHead >= units.size()) {
            return units.size() | (id & LOWER_MASK);
        }

        int unfixedId = extrasHead;
        do {
            int offset = unfixedId ^ Byte.toUnsignedInt(labels.get(0));
            if (isValidOffset(id, offset)) {
                return offset;
            }
            unfixedId = extras(unfixedId).next;
        } while (unfixedId != extrasHead);
        
        return units.size() | (id & LOWER_MASK);
    }

    boolean isValidOffset(int id, int offset) {
        if (extras(offset).isUsed) {
            return false;
        }

        int relOffset = id ^ offset;
        if ((relOffset & LOWER_MASK) != 0 && (relOffset & UPPER_MASK) != 0) {
            return false;
        }

        for (int i = 1; i < labels.size(); i++) {
            if (extras(offset ^ Byte.toUnsignedInt(labels.get(i))).isFixed) {
                return false;
            }
        }

        return true;
    }

    void reserveId(int id) {
        if (id >= units.size()) {
            expandUnits();
        }    

        if (id == extrasHead) {
            extrasHead = extras(id).next;
            if (extrasHead == id) {
                extrasHead = units.size();
            }
        }
        extras(extras(id).prev).next = extras(id).next;
        extras(extras(id).next).prev = extras(id).prev;
        extras(id).isFixed = true;
    }

    void expandUnits() {
        int srcNumUnits = units.size();
        int srcNumBlocks = numBlocks();

        int destNumUnits = srcNumUnits + BLOCK_SIZE;
        int destNumBlocks = srcNumBlocks + 1;

        if (destNumBlocks > NUM_EXTRA_BLOCKS) {
            fixBlock(srcNumBlocks - NUM_EXTRA_BLOCKS);
        }

        for (int i = srcNumUnits; i < destNumUnits; i++) {
            units.add(new DoubleArrayBuilderUnit());
        }

        if (destNumBlocks > NUM_EXTRA_BLOCKS) {
            for (int id = srcNumUnits; id < destNumUnits; id++) {
                extras(id).isUsed = false;
                extras(id).isFixed = false;
            }
        }

        for (int i = srcNumUnits + 1; i < destNumUnits; i++) {
            extras(i - 1).next = i;
            extras(i).prev = i - 1;
        }

        extras(srcNumUnits).prev = destNumUnits - 1; // XXX: ???
        extras(destNumUnits - 1).next = srcNumUnits;

        extras(srcNumUnits).prev = extras(extrasHead).prev;
        extras(destNumUnits - 1).next = extrasHead;

        extras(extras(extrasHead).prev).next = srcNumUnits;
        extras(extrasHead).prev = destNumUnits - 1;
    }

    void fixAllBlocks() {
        int begin = 0;
        if (numBlocks() > NUM_EXTRA_BLOCKS) {
            begin = numBlocks() - NUM_EXTRA_BLOCKS;
        }
        int end = numBlocks();

        for (int blockId = begin; blockId != end; blockId++) {
            fixBlock(blockId);
        }
    }

    void fixBlock(int blockId) {
        int begin = blockId * BLOCK_SIZE;
        int end = begin + BLOCK_SIZE;

        int unusedOffset = 0;
        for (int offset = begin; offset != end; offset++) {
            if (!extras(offset).isUsed) {
                unusedOffset = offset;
                break;
            }
        }

        for (int id = begin; id != end; id++) {
            if (!extras(id).isFixed) {
                reserveId(id);
                units.get(id).setLabel((byte)(id ^ unusedOffset));
            }
        }
    }
}
