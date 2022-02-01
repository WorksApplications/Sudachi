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

import java.nio.ShortBuffer;

/**
 * CRF weights compressed into 2D u16 matrix in MeCab manner
 */
public final class Connection {
    private final ShortBuffer matrix;
    private final int leftSize;
    private final int rightSize;

    public Connection(ShortBuffer matrix, int leftSize, int rightSize) {
        this.matrix = matrix;
        this.leftSize = leftSize;
        this.rightSize = rightSize;
    }

    private int ix(int left, int right) {
        assert left < leftSize;
        assert right < rightSize;
        return right * leftSize + left;
    }

    /**
     *
     * @param left
     *            left connection index
     * @param right
     *            right connection index
     * @return connection weight in the matrix
     */
    public short cost(int left, int right) {
        return matrix.get(ix(left, right));
    }

    public int getLeftSize() {
        return leftSize;
    }

    public int getRightSize() {
        return rightSize;
    }

    public void setCost(int left, int right, short cost) {
        matrix.put(ix(left, right), cost);
    }

    /**
     * @return a copy of itself with the buffer owned, instead of slice
     */
    public Connection ownedCopy() {
        ShortBuffer copy = ShortBuffer.allocate(matrix.limit());
        copy.put(matrix);

        return new Connection(copy, leftSize, rightSize);
    }

    public void validate(int leftId) {
        if (matrix == null) {
            // should never happen, but elides compiler checks
            throw new NullPointerException("matrix");
        }

        if (leftId >= leftSize) {
            // should never happen, but adds a compiler precondition to the inlined method
            throw new IllegalArgumentException(String.format("leftId < leftSize: (%d, %d)", leftId, leftSize));
        }
    }
}
