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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;

/**
 * Compiles model parameters into the binary format
 */
public class Parameters implements WriteDictionary {
    private ByteBuffer data;
    private ShortBuffer params;
    private int maxLeft = Integer.MAX_VALUE;
    private int maxRight = Integer.MAX_VALUE;

    public Parameters(int initialSize) {
        data = ByteBuffer.allocate(initialSize);
        data.order(ByteOrder.LITTLE_ENDIAN);
        params = data.asShortBuffer();
    }

    public Parameters() {
        this(1024 * 1024); // default 1M
    }

    public void add(short left, short right, short cost) {
        maybeResize();
        if (left >= maxLeft) {
            throw new IllegalArgumentException(String.format("left %d is larger than max value %d", left, maxLeft));
        }
        if (right >= maxRight) {
            throw new IllegalArgumentException(String.format("right %d is larger than max value %d", right, maxRight));
        }
        params.put(left);
        params.put(right);
        params.put(cost);
    }

    public void setLimits(int left, int right) {
        this.maxLeft = left;
        this.maxRight = right;
    }

    private void maybeResize() {
        if (params.remaining() < 3) {
            ByteBuffer newData = ByteBuffer.allocate(data.capacity() * 2);
            newData.order(ByteOrder.LITTLE_ENDIAN);
            int position = params.position();
            data.position(0);
            data.limit(position * 2);
            newData.put(data);
            newData.clear();
            data = newData;
            params = newData.asShortBuffer();
            params.position(position);
            assert params.remaining() > 3;
        }
    }

    @Override
    public void writeTo(ModelOutput output) throws IOException {
        output.withPart("word parameters", () -> {
            data.limit(params.position() * 2);
            output.write(data);
        });
    }
}
