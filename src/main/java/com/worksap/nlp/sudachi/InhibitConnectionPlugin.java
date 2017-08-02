package com.worksap.nlp.sudachi;

import java.util.List;

import com.worksap.nlp.sudachi.dictionary.Grammar;

class InhibitConnectionPlugin extends EditConnectionCostPlugin {

    List<List<Integer>> inhibitedPairs;

    @Override
    public void setUp(Grammar grammar) {
        inhibitedPairs = settings.getIntListList("inhibitedPair");
    }

    @Override
    public void edit(Grammar grammar) {
        for (List<Integer> pair : inhibitedPairs) {
            if (pair.size() < 2) {
                continue;
            }
            inhibitConnection(grammar,
                              pair.get(0).shortValue(), pair.get(1).shortValue());
        }
    }
}
