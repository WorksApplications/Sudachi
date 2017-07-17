package com.worksap.nlp.sudachi;

import java.util.List;

public interface Lattice {
    public List<? extends LatticeNode> getNodesWithBegin(int begin);
    public List<? extends LatticeNode> getNodesWithEnd(int end);
    public List<? extends LatticeNode> getNodes(int begin, int end);
    public void insert(int begin, int end, LatticeNode node);
    public void remove(int begin, int end, LatticeNode node);
    public LatticeNode createNode();
}
