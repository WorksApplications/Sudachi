package com.worksap.nlp.sudachi.dictionary;

import java.nio.ShortBuffer;

public final class Connection implements Cloneable {
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

    @SuppressWarnings("MethodDoesntCallSuperMethod")
    @Override
    public Connection clone() {
        ShortBuffer copy = ShortBuffer.allocate(matrix.limit());
        copy.put(matrix);

        return new Connection(
                copy,
                leftSize,
                rightSize
        );
    }
}
