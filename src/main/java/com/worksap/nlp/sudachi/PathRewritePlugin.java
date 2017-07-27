package com.worksap.nlp.sudachi;

import java.util.List;

public interface PathRewritePlugin {

    public void rewrite(List<LatticeNode> path, Lattice lattice);
}
