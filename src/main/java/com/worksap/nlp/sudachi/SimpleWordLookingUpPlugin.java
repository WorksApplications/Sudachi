package com.worksap.nlp.sudachi;

import java.util.Collections;
import java.util.List;

import com.worksap.nlp.sudachi.dictionary.WordInfo;

class SimpleWordLookingUpPlugin extends WordLookingUpPlugin {

    final short oovPOSId = 4;

    @Override
    public List<LatticeNode> provideOOV(String text,
                                        List<Integer> otherWordsLength) {

        if (!otherWordsLength.contains(1)) {
            LatticeNode node = createNode();
            node.setParameter((short)7, (short)7, (short)14657);
            String s = text.substring(0, 1);
            WordInfo info
                = new WordInfo(s, oovPOSId, s, s, "");
            node.setWordInfo(info);
            return Collections.singletonList(node);
        } else {
            return Collections.emptyList();
        }
    }
}
