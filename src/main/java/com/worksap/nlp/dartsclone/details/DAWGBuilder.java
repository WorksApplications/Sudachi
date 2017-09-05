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

import java.util.ArrayList;
import java.util.List;

class DAWGBuilder {

    static class Node {
        int child;
        int sibling;
        byte label;
        boolean isState;
        boolean hasSibling;

        void reset() {
            child = 0;
            sibling = 0;
            label = (byte)0;
            isState = false;
            hasSibling = false;
        }

        int getValue() { return child; }
        void setValue(int value) { child = value; }
        int unit() {
            if (label == 0) {
                return (child << 1) | (hasSibling ? 1 : 0);
            }
            return (child << 2) | (isState ? 2 : 0) | (hasSibling ? 1 : 0);
        }
    }

    static class Unit {
        int unit;
        
        int child() { return unit >>> 2; }
        boolean hasSibling() { return (unit & 1) == 1; }
        int value() { return unit >>> 1; }
        boolean isState() { return (unit & 2) == 2; }
    }

    private static final int INITIAL_TABLE_SIZE = 1 << 10;

    private ArrayList<Node> nodes = new ArrayList<>();
    private ArrayList<Unit> units = new ArrayList<>();
    private ArrayList<Byte> labels = new ArrayList<>();
    private BitVector isIntersections = new BitVector();
    private ArrayList<Integer> table = new ArrayList<>();
    private ArrayList<Integer> nodeStack = new ArrayList<>();
    private ArrayList<Integer> recycleBin = new ArrayList<>();
    private int numStates;

    int root() { return 0; }

    int child(int id) { return units.get(id).child(); }

    int sibling(int id) {
        return (units.get(id).hasSibling()) ? (id + 1) : 0;
    }

    int value(int id) { return units.get(id).value(); }

    boolean isLeaf(int id) { return label(id) == 0; }

    byte label(int id) { return labels.get(id); }

    boolean isIntersection(int id) { return isIntersections.get(id); }

    int intersectionId(int id) { return isIntersections.rank(id) - 1; }

    int numIntersections() { return isIntersections.numOnes();}

    int size() { return units.size(); }

    void init() {
        table.ensureCapacity(INITIAL_TABLE_SIZE);
        for (int i = 0; i < INITIAL_TABLE_SIZE; i++) {
            table.add(0);
        }

        appendNode();
        appendUnit();

        numStates = 1;

        nodes.get(0).label = (byte)0xFF;
        nodeStack.add(0);
    }

    void finish() {
        flush(0);

        units.get(0).unit = nodes.get(0).unit();
        labels.set(0, nodes.get(0).label);

        nodes = null;
        table = null;
        nodeStack = null;
        recycleBin = null;

        isIntersections.build();
    }

    void insert(byte[] key, int value) {
        if (value < 0) {
            throw new IllegalArgumentException("negative value");
        }
        if (key.length == 0) {
            throw new IllegalArgumentException("zero-length key");
        }

        int id = 0;
        int keyPos = 0;

        for ( ; keyPos <= key.length; keyPos++) {
            int childId = nodes.get(id).child;
            if (childId == 0) {
                break;
            }

            byte keyLabel = (keyPos < key.length) ? key[keyPos] : 0;
            if (keyPos < key.length &&  keyLabel == 0) {
                throw new IllegalArgumentException("invalid null character");
            }

            byte unitLabel = nodes.get(childId).label;
            if (Byte.toUnsignedInt(keyLabel) < Byte.toUnsignedInt(unitLabel)) {
                throw new IllegalArgumentException("wrong key order");
            } else if (Byte.toUnsignedInt(keyLabel) > Byte.toUnsignedInt(unitLabel)) {
                nodes.get(childId).hasSibling = true;
                flush(childId);
                break;
            }
            id = childId;
        }

        if (keyPos > key.length) {
            return;
        }

        for ( ; keyPos <= key.length; keyPos++) {
            byte keyLabel = (keyPos < key.length) ? key[keyPos] : 0;
            int childId = appendNode();

            if (nodes.get(id).child == 0) {
                nodes.get(childId).isState = true;
            }
            nodes.get(childId).sibling = nodes.get(id).child;
            nodes.get(childId).label = keyLabel;
            nodes.get(id).child = childId;
            nodeStack.add(childId);

            id = childId;
        }
        nodes.get(id).setValue(value);
    }

    void clear() {
        nodes = null;
        units = null;
        labels = null;
        isIntersections = null;
        table = null;
        nodeStack = null;
        recycleBin = null;
    }

    private void flush(int id) {
        while (stackTop(nodeStack) != id) {
            int nodeId = stackTop(nodeStack);
            stackPop(nodeStack);

            if (numStates >= table.size() - (table.size() >>> 2)) {
                expandTable();
            }

            int numSiblings = 0;
            for (int i = nodeId; i != 0; i = nodes.get(i).sibling) {
                numSiblings++;
            }

            int[] findResult = findNode(nodeId);
            int matchId = findResult[0];
            int hashId = findResult[1];

            if (matchId != 0) {
                isIntersections.set(matchId, true);
            } else {
                int unitId = 0;
                for (int i = 0; i < numSiblings; i++) {
                    unitId = appendUnit();
                }
                for (int i = nodeId; i != 0; i = nodes.get(i).sibling) {
                    units.get(unitId).unit = nodes.get(i).unit();
                    labels.set(unitId, nodes.get(i).label);
                    unitId--;
                }
                matchId = unitId + 1;
                table.set(hashId, matchId);
                numStates++;
            }

            for (int i = nodeId, next; i != 0; i = next) {
                next = nodes.get(i).sibling;
                freeNode(i);
            }

            nodes.get(stackTop(nodeStack)).child = matchId;
        }
        stackPop(nodeStack);
    }

    void expandTable() {
        int tableSize = table.size() << 1;
        table.clear();
        table.ensureCapacity(tableSize);
        for (int i = 0; i < tableSize; i++) {
            table.add(0);
        }

        for (int id = 1; id < units.size(); id++) {
            if (labels.get(id) == 0 || units.get(id).isState()) {
                int[] findResult = findUnit(id);
                int hashId = findResult[1];
                table.set(hashId, id);
            }
        }
    }

    private int[] findUnit(int id) {
        int[] result = new int[2];
        int hashId = hashUnit(id) % table.size();
        for ( ; ; hashId = (hashId + 1) % table.size()) {
            int unitId = table.get(hashId);
            if (unitId == 0) {
                break;
            }
        }
        result[1] = hashId;
        return result; 
    }

    private int[] findNode(int nodeId) {
        int[] result = new int[2];
        int hashId = hashNode(nodeId) % table.size();
        for ( ; ; hashId = (hashId + 1) % table.size()) {
            int unitId = table.get(hashId);
            if (unitId == 0) {
                break;
            }

            if (areEqual(nodeId, unitId)) {
                result[0] = unitId;
                result[1] = hashId;
                return result;
            }
        }
        result[1] = hashId;
        return result;
    }

    private boolean areEqual(int nodeId, int unitId) {
        for (int i = nodes.get(nodeId).sibling; i != 0; i = nodes.get(i).sibling) {
            if (!units.get(unitId).hasSibling()) {
                return false;
            }
            unitId++;
        }
        if (units.get(unitId).hasSibling()) {
            return false;
        }

        for (int i = nodeId; i != 0; i = nodes.get(i).sibling, unitId--) {
            if (nodes.get(i).unit() != units.get(unitId).unit ||
                nodes.get(i).label != labels.get(unitId)) {
                return false;
            }
        }
        return true;
    }

    private int hashUnit(int id) {
        int hashValue = 0;
        for (; id != 0; id++) {
            int unit = units.get(id).unit;
            byte label = labels.get(id);
            hashValue ^= hash((Byte.toUnsignedInt(label) << 24) ^ unit);

            if (!units.get(id).hasSibling()) {
                break;
            }
        }
        return hashValue;
    }

    private int hashNode(int id) {
        int hashValue = 0;
        for (; id != 0; id = nodes.get(id).sibling) {
            int unit = nodes.get(id).unit();
            byte label = nodes.get(id).label;
            hashValue ^= hash((Byte.toUnsignedInt(label) << 24) ^ unit);
        }
        return hashValue;
    }

    private int appendUnit() {
        isIntersections.append();
        units.add(new Unit());
        labels.add((byte)0);

        return isIntersections.size() - 1;
    }

    private int appendNode() {
        int id;
        if (recycleBin.isEmpty()) {
            id = nodes.size();
            nodes.add(new Node());
        } else {
            id = stackTop(recycleBin);
            nodes.get(id).reset();
            stackPop(recycleBin);
        }
        return id;
    }

    private void freeNode(int id) {
        recycleBin.add(id);
    }

    private static int hash(int key) {
        key = ~key + (key << 15);  // key = (key << 15) - key - 1;
        key = key ^ (key >> 12);
        key = key + (key << 2);
        key = key ^ (key >> 4);
        key = key * 2057;  // key = (key + (key << 3)) + (key << 11);
        key = key ^ (key >> 16);
        return key;
    }

    private static <E> E stackTop(List<E> stack) {
        return stack.get(stack.size() - 1);
    }

    private static <E> void stackPop(List<E> stack) {
        stack.remove(stack.size() - 1);
    }
}
