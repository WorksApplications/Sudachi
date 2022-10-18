package com.worksap.nlp.sudachi;

public class TestLattice {
    public static LatticeNodeImpl.OOVFactory oovFactory(int leftId, int rightId, int cost, int posId) {
        return LatticeNodeImpl.oovFactory((short) leftId, (short) rightId, (short) cost, (short) posId);
    }

    public static LatticeNodeImpl.OOVFactory oovFactory(int posId) {
        return oovFactory(0, 0, 0, posId);
    }
}
