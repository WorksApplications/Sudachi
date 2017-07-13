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
        unit = (unit & ~0xFF) | (label & 0xFF);
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
