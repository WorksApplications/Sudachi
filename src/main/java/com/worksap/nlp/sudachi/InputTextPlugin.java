package com.worksap.nlp.sudachi;

import java.io.IOException;

public abstract class InputTextPlugin extends Plugin {

    public abstract void setUp() throws IOException;

    public abstract void rewrite(InputText<?> text);
}
