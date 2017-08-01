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
        if (oovPOSStrings != null) {
            oovPOSId = grammar.getPartOfSpeechId(oovPOSStrings.toArray(new String[0]));
        }
    }

    @Override
    public List<LatticeNode> provideOOV(InputText<?> inputText, int offset, boolean hasOtherWords) {
        if (!hasOtherWords) {
            LatticeNode node = createNode();
            node.setParameter(leftid, rightid, cost);
            String s = inputText.getText().substring(offset, offset + 1);
            WordInfo info
                = new WordInfo(s, oovPOSId, s, s, "");
            node.setWordInfo(info);
            return Collections.singletonList(node);
        } else {
            return Collections.emptyList();
        }
    }
}
