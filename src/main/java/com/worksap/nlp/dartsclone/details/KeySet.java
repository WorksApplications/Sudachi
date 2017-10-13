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

public class KeySet {

    private byte[][] keys;
    private int[] values;

    public KeySet(byte[][] keys, int[] values) {
        this.keys = keys;
        this.values = values;
    }

    int size() { return keys.length; }
    
    byte[] getKey(int id) { return keys[id]; }
    
    byte getKeyByte(int keyId, int byteId) {
        if (byteId >= keys[keyId].length) {
            return 0;
        }
        return keys[keyId][byteId];
    }

    boolean hasValues() { return values != null; }

    int getValue(int id) {
        return (hasValues()) ? values[id] : id;
    }
}
