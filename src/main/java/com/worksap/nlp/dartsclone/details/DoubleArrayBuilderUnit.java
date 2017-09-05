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

class DoubleArrayBuilderUnit {

    int unit;

    void setHasLeaf(boolean hasLeaf) {
        if (hasLeaf) {
            unit |= 1 << 8;
        } else {
            unit &= ~(1 << 8);
        }
    }

    void setValue(int value) {
        unit = value | (1 << 31);
    }

    void setLabel(byte label) {
        unit = (unit & ~0xFF) | Byte.toUnsignedInt(label);
    }

    void setOffset(int offset) {
        unit &= (1 << 31) | (1 << 8) | 0xFF;
        if (offset < 1 << 21) {
            unit |= (offset << 10);
        } else {
            unit |= (offset << 2) | (1 << 9);
        }
    }
}
