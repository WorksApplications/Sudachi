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

package com.worksap.nlp.sudachi.dictionary.build;

import com.worksap.nlp.sudachi.dictionary.Connection;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class ConnectionMatrix implements WriteDictionary {
    private short numLeft;
    private short numRight;
    private ByteBuffer compiled;
    private static final Pattern WHITESPACE = Pattern.compile("\\s+");
    private static final Pattern OPT_WHITESPACE = Pattern.compile("\\s*");

    private static final Logger logger = Logger.getLogger(ConnectionMatrix.class.getName());

    public ConnectionMatrix() {
    }

    /**
     * @return compiled binary matrix representation with header
     */
    public ByteBuffer getCompiled() {
        return compiled;
    }

    /**
     * @return compiled binary matrix without header
     */
    public ByteBuffer getCompiledNoHeader() {
        ByteBuffer b = this.compiled;
        int pos = b.position();
        int lim = b.limit();
        b.position(4);
        b.limit(b.capacity());
        ByteBuffer slice = b.slice();
        slice.order(b.order());
        b.position(pos);
        b.limit(lim);
        return slice;
    }

    /**
     * Read connection matrix in text format into the binary representation
     *
     * @param data
     *            input stream containing
     * @return number read of matrix values
     * @throws IOException
     *             when IO fails
     */
    public long readEntries(InputStream data) throws IOException {
        LineNumberReader reader = new LineNumberReader(new InputStreamReader(data, StandardCharsets.UTF_8));

        String header = reader.readLine();
        if (header == null) {
            throw new IllegalArgumentException("invalid format at line " + reader.getLineNumber());
        }

        String[] lr = WHITESPACE.split(header, 2);
        if (lr.length != 2) {
            throw new IllegalArgumentException("invalid header " + header + ", expected two 16-bit integers");
        }

        try {
            numLeft = Short.parseShort(lr[0]);
            numRight = Short.parseShort(lr[1]);
        } catch (NumberFormatException ignored) {
            throw new IllegalArgumentException("invalid header " + header + ", expected two 16-bit integers");
        }

        ByteBuffer buffer = ByteBuffer.allocate(2 * numLeft * numRight + 4);
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        ShortBuffer matrix = buffer.asShortBuffer();
        matrix.put(numLeft);
        matrix.put(numRight);

        matrix = matrix.slice();
        Connection conn = new Connection(matrix, numLeft, numRight);

        long numLines = 0;

        while (true) {
            String line = reader.readLine();
            if (line == null) {
                break;
            }
            if (OPT_WHITESPACE.matcher(line).matches()) {
                continue;
            }
            String[] cols = WHITESPACE.split(line);
            if (cols.length < 3) {
                logger.warning("invalid format at line " + reader.getLineNumber());
                continue;
            }

            try {
                short left = Short.parseShort(cols[0]);
                short right = Short.parseShort(cols[1]);
                short cost = Short.parseShort(cols[2]);
                conn.setCost(left, right, cost);
            } catch (NumberFormatException e) {
                logger.warning("invalid format at line " + reader.getLineNumber());
                continue;
            }

            numLines += 1;
        }
        buffer.position(0);
        compiled = buffer;
        return numLines;
    }

    @Override
    public void writeTo(ModelOutput output) throws IOException {
        output.write(compiled);
    }

    public short getNumLeft() {
        return numLeft;
    }

    public short getNumRight() {
        return numRight;
    }
}
