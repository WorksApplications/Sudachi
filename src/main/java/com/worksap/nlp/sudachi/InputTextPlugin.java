package com.worksap.nlp.sudachi;

import java.io.IOException;

/**
 * A plugin that rewrites the characters of input texts.
 *
 * <p>{@link Dictionary} initialize this plugin with {@link Settings}.
 * It can be referred as {@link Plugin#settings}.
 *
 * <p>The following is an example of settings.
 * <pre>{@code
 *   {
 *     "class" : "com.worksap.nlp.sudachi.InputTextPlugin",
 *     "example" : "example setting"
 *   }
 * }</pre>
 */
public abstract class InputTextPlugin extends Plugin {

    /**
     * Set up the plugin.
     *
     * {@link Tokenizer} calls this method for setting up this plugin.
     *
     * @throws IOException if reading something is failed
     */
    public void setUp() throws IOException {}

    /**
     * Rewrite the input text.
     *
     * To rewrite the input text, you can use
     * {@link InputTextBuilder#replace}.
     *
     * @param builder the input text
     */
    public abstract void rewrite(InputTextBuilder<?> builder);
}
