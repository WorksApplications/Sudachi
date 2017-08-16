package com.worksap.nlp.sudachi;

import java.util.List;

import com.worksap.nlp.sudachi.dictionary.Grammar;

/**
 * A Plugin for inhibiting the connections.
 *
 * <p>{@link Dictionary} initialize this plugin with {@link Settings}.
 * It can be referred as {@link Plugin#settings}.
 *
 * <p>The following is an example of settings.
 * <pre>{@code
 *   {
 *     "class" : "com.worksap.nlp.sudachi.InhibitConnectionPlugin",
 *     "inhibitedPair" : [ [ 0, 233 ], [435, 332] ]
 *   }
 * }</pre>
 *
 * {@code inhibitPair} is a list of lists of two numbers.
 * At each pair, the first number is left-ID and the second is
 * right-ID of a connection.
 */
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
