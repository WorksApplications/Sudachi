package com.worksap.nlp.sudachi.dictionary.build;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * A version of {@link java.util.function.Consumer} which allows throwing IOException
 */
@FunctionalInterface
public interface IOConsumer<T> {
    T accept(ByteBuffer arg) throws IOException;
}
