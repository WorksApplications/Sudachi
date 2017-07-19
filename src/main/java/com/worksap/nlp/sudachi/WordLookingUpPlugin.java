package com.worksap.nlp.sudachi;

import java.util.List;
import java.util.Collections;

public abstract class WordLookingUpPlugin {

    public abstract void provideOOV();

    List<LatticeNode> getOOV(InputText<?> inputText, int offset,
                             List<int[]> otherWords) {
        return Collections.emptyList();
    }

    LatticeNode createNode() {
        return new LatticeNodeImpl();
    }
}
