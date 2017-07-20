package com.worksap.nlp.sudachi;

import java.util.Collections;
import java.util.List;

import com.worksap.nlp.sudachi.dictionary.Grammar;
import com.worksap.nlp.sudachi.dictionary.WordInfo;

class SimpleWordLookingUpPlugin extends WordLookingUpPlugin {

    final short oovPOSId;
    final short leftid;
    final short rightid;
    final short cost;

    SimpleWordLookingUpPlugin(Grammar grammar,
                              short leftid, short rightid, short cost) {
        oovPOSId
            = grammar.getPartOfSpeechId(new String[] {"名詞", "普通名詞",
                                                      "一般", "*", "*", "*"});
        this.leftid = leftid;
        this.rightid = rightid;
        this.cost = cost;
    }

    @Override
    public List<LatticeNode> provideOOV(String text,
                                        List<Integer> otherWordsLength) {

        if (!otherWordsLength.contains(1)) {
            LatticeNode node = createNode();
            node.setParameter(leftid, rightid, cost);
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
