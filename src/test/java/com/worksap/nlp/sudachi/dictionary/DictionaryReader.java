package com.worksap.nlp.sudachi.dictionary;

import java.io.InputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

class DictionaryReader {

    static ByteBuffer read(String filename) throws IOException {
        InputStream input = DictionaryReader.class.getResourceAsStream(filename);
        ArrayList<Byte> buffer = new ArrayList<>();
        for (int c = input.read(); c >= 0; c = input.read()) {
            buffer.add((byte)c);
        }
        ByteBuffer bytes = ByteBuffer.allocate(buffer.size());
        bytes.order(ByteOrder.LITTLE_ENDIAN);
        for (Byte b : buffer) {
            bytes.put(b);
        }
        bytes.rewind();

        return bytes;
    }
}
