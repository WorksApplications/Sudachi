package com.worksap.nlp.sudachi;

import java.io.IOException;

public interface InputTextPlugin {

    public void setUp() throws IOException;

    public void rewrite(InputText<?> text);
}
