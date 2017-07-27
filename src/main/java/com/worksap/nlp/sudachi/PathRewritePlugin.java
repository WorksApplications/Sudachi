package com.worksap.nlp.sudachi;

import java.util.List;

public abstract class PathRewritePlugin {

    public abstract void rewrite(List<LatticeNode> path);

    LatticeNode createNode() {
        LatticeNode node = new LatticeNodeImpl();
        return node;
    }

}
