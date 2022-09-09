/*
 * Copyright (c) 2022 Works Applications Co., Ltd.
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

public class ProgressInputStream extends InputStream {
    private final InputStream inner;
    private long position = 0;
    private final long maxSize;
    private final Progress progress;

    public ProgressInputStream(InputStream inner, long maxSize, Progress progress) {
        this.inner = inner;
        this.maxSize = maxSize;
        this.progress = progress;
    }

    @Override
    public int read(byte[] b) throws IOException {
        int nread = inner.read(b);
        if (nread != -1) {
            position += nread;
            progress.progress(position, maxSize);
        }
        return nread;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int nread = inner.read(b, off, len);
        if (nread != -1) {
            position += nread;
            progress.progress(position, maxSize);
        }
        return nread;
    }

    @Override
    public int available() throws IOException {
        return inner.available();
    }

    @Override
    public void close() throws IOException {
        inner.close();
    }

    @Override
    public int read() throws IOException {
        int read = inner.read();
        if (read != -1) {
            position += 1;
        }
        return read;
    }
}
