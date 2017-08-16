package com.worksap.nlp.sudachi;

import java.util.List;

import com.worksap.nlp.sudachi.dictionary.CategoryType;
import com.worksap.nlp.sudachi.dictionary.Grammar;

/**
 * A plugin for concatenation of Katakana OOVs.
 *
 * This plugin concatenate the Katakana OOV and
 * the adjacent Katakana morphemes.
 *
 * <p>The concatenated morpheme is OOV, and its part of speech
 * must be specified in the settings.
 *
 * <p>The following is an example of settings.
 * <pre>{@code
 *   {
 *     "class" : "com.worksap.nlp.sudachi.JoinKatakanaOovPlugin",
 *     "oovPOS" : [ "POS1", "POS2", ... ]
 *   }
 * }</pre>
 */
class JoinKatakanaOovPlugin extends PathRewritePlugin {

    short oovPosId;

    @Override
    public void setUp(Grammar grammar) {
        List<String> pos = settings.getStringList("oovPOS");
        oovPosId = grammar.getPartOfSpeechId(pos.toArray(new String[0]));
        if (oovPosId < 0) {
            throw new IllegalArgumentException("oovPOS is invalid");
        }
    }

    @Override
    public void rewrite(InputText<?> text, List<LatticeNode> path, Lattice lattice) {
        for (int i = 0; i < path.size(); i++) {
            LatticeNode node = path.get(i);
            if ((node.isOOV() || isOneChar(text, node))
                && isKatakanaNode(text, node)) {
                int begin = i - 1;
                for ( ; begin >= 0; begin--) {
                    if (!isKatakanaNode(text, path.get(begin))) {
                        begin++;
                        break;
                    }
                }
                if (begin < 0) {
                    begin = 0;
                }
                int end = i + 1;
                for ( ; end < path.size(); end++) {
                    if (!isKatakanaNode(text, path.get(end))) {
                        break;
                    }
                }
                if (end - begin > 1) {
                    concatenateOov(path, begin, end, oovPosId, lattice);
                    i = begin + 1;
                }
            }
        }
    }

    boolean isKatakanaNode(InputText<?> text, LatticeNode node) {
        return getCharCategoryTypes(text, node).contains(CategoryType.KATAKANA);
    }

    boolean isOneChar(InputText<?> text, LatticeNode node) {
        int b = node.getBegin();
        return  b + text.getCodePointsOffsetLength(b, 1) == node.getEnd();
    }
}
