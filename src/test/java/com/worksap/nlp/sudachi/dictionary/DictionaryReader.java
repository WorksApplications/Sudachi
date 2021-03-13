/*
 * Copyright (c) 2021 Works Applications Co., Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.worksap.nlp.sudachi.dictionary;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

class DictionaryReader {

    static ByteBuffer read(String filename) throws IOException {
        InputStream input = DictionaryReader.class.getResourceAsStream(filename);
        ArrayList<Byte> buffer = new ArrayList<>();
        for (int c = input.read(); c >= 0; c = input.read()) {
            buffer.add((byte) c);
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
