package com.worksap.nlp.sudachi;

import java.io.IOException;

public abstract class InputTextPlugin extends Plugin {

    public void setUp() throws IOException {}

    public abstract void rewrite(InputTextBuilder<?> builder);
}
