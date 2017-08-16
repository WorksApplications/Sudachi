package com.worksap.nlp.sudachi;

import java.io.IOException;

import com.worksap.nlp.sudachi.dictionary.Grammar;

/**
 * A plugin for editing the connection costs.
 *
 * <p>{@link Dictionary} initialize this plugin with {@link Settings}.
 * It can be referred as {@link Plugin#settings}.
 *
 * <p>The following is an example of settings.
 * <pre>{@code
 *   {
 *     "class" : "com.worksap.nlp.sudachi.SampleEditConnectionPlugin",
 *     "example" : "example setting"
 *   }
 * }</pre>
 */
public abstract class EditConnectionCostPlugin extends Plugin {

    /**
     * Set up the plugin.
     *
     * {@link Tokenizer} calls this method for setting up this plugin.
     *
     * @param grammar the grammar of the system dictionary
     * @throws IOException if reading something is failed
     */
    public void setUp(Grammar grammar) throws IOException {}

    /**
     * Edit the connection costs.
     *
     * To edit connection costs, you can use
     * {@link Grammar#getConnectCost},
     * {@link Grammar#setConnectCost}, and {@link inhibitConnection}.
     *
     * @param grammar the grammar of the system dictionary
     */
    public abstract void edit(Grammar grammar);

    /**
     * Inhibit a connection.
     *
     * @param grammar the grammar of the system dictionary
     * @param leftId the left-ID of the connection
     * @param rightId the right-ID of the connection
     */
    public void inhibitConnection(Grammar grammar,
                                  short leftId, short rightId) {
        grammar.setConnectCost(leftId, rightId, Grammar.INHIBITED_CONNECTION);
    }
}
