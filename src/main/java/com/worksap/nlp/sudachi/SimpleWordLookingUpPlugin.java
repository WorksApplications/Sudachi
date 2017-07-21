package com.worksap.nlp.sudachi;

import java.util.Collections;
import java.util.ArrayList;
import java.util.List;

import com.worksap.nlp.sudachi.dictionary.Grammar;
import com.worksap.nlp.sudachi.dictionary.WordInfo;

public class SimpleWordLookingUpPlugin extends WordLookingUpPlugin {

    public ArrayList<String> oovPOSStrings;
    short oovPOSId;
    public short leftid;
    public short rightid;
    public short cost;

    @Override
    public void setUp(Grammar grammar) {
        oovPOSId = grammar.getPartOfSpeechId(oovPOSStrings.toArray(new String[0]));
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
