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

class BitVector {

    private static final int UNIT_SIZE = 32;
    
    private ArrayList<Integer> units = new ArrayList<>();
    private int[] ranks;
    private int numOnes;
    private int size;

    boolean get(int id) {
        return (units.get(id / UNIT_SIZE) >>> (id % UNIT_SIZE) & 1) == 1;
    }

    int rank(int id) {
        int unitId = id / UNIT_SIZE;
        return ranks[unitId]
            + popCount(units.get(unitId) &
                       (~0 >>> (UNIT_SIZE - (id % UNIT_SIZE) - 1)));
    }

    void set(int id, boolean bit) {
        if (bit) {
            units.set(id / UNIT_SIZE,
                      units.get(id / UNIT_SIZE) | 1 << (id % UNIT_SIZE));
        } else {
            units.set(id / UNIT_SIZE,
                      units.get(id / UNIT_SIZE) & ~(1 << (id % UNIT_SIZE)));
        }
    }

    boolean isEmpty() { return units.isEmpty(); }

    int numOnes() { return numOnes; }

    int size() { return size; }

    void append() {
        if ((size % UNIT_SIZE) == 0) {
            units.add(0);
        }
        size++;
    }

    void build() {
        ranks = new int[units.size()];
        numOnes = 0;
        for (int i = 0; i < units.size(); i++) {
            ranks[i] = numOnes;
            numOnes += popCount(units.get(i));
        }
    }

    void clear() {
        units.clear();
        ranks = null;
    }

    private int popCount(int unit) {
        unit = ((unit & 0xAAAAAAAA) >>> 1) + (unit & 0x55555555);
        unit = ((unit & 0xCCCCCCCC) >>> 2) + (unit & 0x33333333);
        unit = ((unit >>> 4) + unit) & 0x0F0F0F0F;
        unit += unit >>> 8;
        unit += unit >>> 16;
        return unit & 0xFF;
    }
}
