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
import java.io.InputStream;

public class TrackingInputStream extends InputStream {
    private final InputStream inner;
    private long position;

    public TrackingInputStream(InputStream inner) {
        this.inner = inner;
    }

    @Override
    public int read() throws IOException {
        return inner.read();
    }

    @Override
    public int read(byte[] b) throws IOException {
        int read = inner.read(b);
        if (read != -1) {
            position += read;
        }
        return read;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int read = inner.read(b, off, len);
        if (read != -1) {
            position += read;
        }
        return read;
    }

    @Override
    public long skip(long n) throws IOException {
        position += n;
        return super.skip(n);
    }

    public long getPosition() {
        return position;
    }
}
