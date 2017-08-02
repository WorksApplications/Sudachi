package com.worksap.nlp.sudachi;

import java.util.List;

public abstract class PathRewritePlugin extends Plugin {

    public abstract void rewrite(InputText<?> text, List<LatticeNode> path, Lattice lattice);
}
