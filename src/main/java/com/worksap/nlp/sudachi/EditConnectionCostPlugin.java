package com.worksap.nlp.sudachi;

import java.io.IOException;

import com.worksap.nlp.sudachi.dictionary.Grammar;

public abstract class EditConnectionCostPlugin extends Plugin {

    public abstract void setUp(Grammar grammar) throws IOException;

    public abstract void edit(Grammar grammar);

    public void inhibitConnection(Grammar grammar,
                                  short leftId, short rightId) {
        grammar.setConnectCost(leftId, rightId, grammar.INHIBITED_CONNECTION);
    }
}
