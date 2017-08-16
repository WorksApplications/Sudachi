package com.worksap.nlp.sudachi;

import java.util.Collections;
import java.util.List;

import com.worksap.nlp.sudachi.dictionary.Grammar;
import com.worksap.nlp.sudachi.dictionary.WordInfo;

/**
 * Provides the OOVs.
 *
 * <p>The following is an example of settings.
 * <pre>{@code
 *   {
 *     "class"   : "com.worksap.nlp.sudachi.SimpleOovProviderPlugin",
 *     "oovPOS"  : [ "補助記号", "一般", "*", "*", "*", "*" ],
 *     "leftId"  : 5968,
 *     "rigthId" : 5968,
 *     "cost"    : 3857
 *   }
 * }</pre>
 *
 * {@code oovPOS} is the part of speech of the OOVs.
 * {@code leftId} is the left-ID of the OOVs.
 * {@code rightId} is the right-ID of the OOVs.
 * {@code cost} is the cost of the OOVs.
 */
class SimpleOovProviderPlugin extends OovProviderPlugin {

    short oovPOSId;
    short leftId;
    short rightId;
    short cost;

    @Override
    public void setUp(Grammar grammar) {
        List<String> oovPOSStrings = settings.getStringList("oovPOS");
        leftId = (short)settings.getInt("leftId");
        rightId = (short)settings.getInt("rightId");
        cost = (short)settings.getInt("cost");

        if (oovPOSStrings == null) {
            throw new IllegalArgumentException("oovPOS is not specified");
        }
        oovPOSId = grammar.getPartOfSpeechId(oovPOSStrings.toArray(new String[0]));
    }

    @Override
    public List<LatticeNode> provideOOV(InputText<?> inputText, int offset, boolean hasOtherWords) {
        if (!hasOtherWords) {
            LatticeNode node = createNode();
            node.setParameter(leftId, rightId, cost);
            int length = inputText.getCodePointsOffsetLength(offset, 1);
            String s = inputText.getSubstring(offset, offset + length);
            WordInfo info
                = new WordInfo(s, (short)length,
                               oovPOSId, s, s, "");
            node.setWordInfo(info);
            return Collections.singletonList(node);
        } else {
            return Collections.emptyList();
        }
    }
}
