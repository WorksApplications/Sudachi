package com.worksap.nlp.sudachi;

import java.util.Collections;
import java.util.ArrayList;
import java.util.List;

import com.worksap.nlp.sudachi.dictionary.Grammar;
import com.worksap.nlp.sudachi.dictionary.WordInfo;

class SimpleOovProviderPlugin extends OovProviderPlugin {

    List<String> oovPOSStrings;
    short oovPOSId;
    short leftId;
    short rightId;
    short cost;

    @Override
    public void setUp(Grammar grammar) {
        oovPOSStrings = settings.getStringList("oovPOSStrings");
        leftId = (short)settings.getInt("leftId");
        rightId = (short)settings.getInt("rightId");
        cost = (short)settings.getInt("cost");

        if (oovPOSStrings != null) {
            oovPOSId = grammar.getPartOfSpeechId(oovPOSStrings.toArray(new String[0]));
        }
    }

    @Override
    public List<LatticeNode> provideOOV(InputText<?> inputText, int offset, boolean hasOtherWords) {
        if (!hasOtherWords) {
            LatticeNode node = createNode();
            node.setParameter(leftId, rightId, cost);
            String text = inputText.getText();
            String s = text.substring(offset, text.offsetByCodePoints(offset, 1));
            WordInfo info
                = new WordInfo(s, oovPOSId, s, s, "");
            node.setWordInfo(info);
            return Collections.singletonList(node);
        } else {
            return Collections.emptyList();
        }
    }
}
