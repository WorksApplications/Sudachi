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
import java.nio.channels.SeekableByteChannel;
import java.util.ArrayList;
import java.util.List;

public class ModelOutput implements SeekableByteChannel {
    @FunctionalInterface
    interface IORunnable {
        void run() throws IOException;
    }

    @FunctionalInterface
    interface SizedRunnable {
        long run() throws IOException;
    }

    public static class Part {
        private final String name;
        private final long time;
        private final long size;

        public Part(String name, long time, long size) {
            this.name = name;
            this.time = time;
            this.size = size;
        }

        public String getName() {
            return name;
        }

        public long getTime() {
            return time;
        }

        public long getSize() {
            return size;
        }
    }

    private final SeekableByteChannel internal;
    private final List<Part> parts = new ArrayList<>();
    private Progress progressor;

    public ModelOutput(SeekableByteChannel internal) {
        this.internal = internal;
    }

    public void progressor(Progress progress) {
        this.progressor = progress;
    }

    @Override
    public int read(ByteBuffer byteBuffer) throws IOException {
        return internal.read(byteBuffer);
    }

    @Override
    public int write(ByteBuffer byteBuffer) throws IOException {
        return internal.write(byteBuffer);
    }

    @Override
    public long position() throws IOException {
        return internal.position();
    }

    @Override
    public SeekableByteChannel position(long l) throws IOException {
        return internal.position(l);
    }

    @Override
    public long size() throws IOException {
        return internal.size();
    }

    @Override
    public SeekableByteChannel truncate(long l) throws IOException {
        return internal.truncate(l);
    }

    @Override
    public boolean isOpen() {
        return internal.isOpen();
    }

    @Override
    public void close() throws IOException {
        internal.close();
    }

    public void withPart(String name, IORunnable inner) throws IOException {
        long pos = position();
        long start = System.nanoTime();
        if (progressor != null) {
            progressor.startBlock(name, start, Progress.Kind.OUTPUT);
        }
        inner.run();
        long time = System.nanoTime() - start;
        long size = position() - pos;
        if (progressor != null) {
            progressor.endBlock(size, time);
        }
        parts.add(new Part(name, time, size));
    }

    public void withSizedPart(String name, SizedRunnable inner) throws IOException {
        long start = System.nanoTime();
        if (progressor != null) {
            progressor.startBlock(name, start, Progress.Kind.OUTPUT);
        }
        long size = inner.run();
        long time = System.nanoTime() - start;
        if (progressor != null) {
            progressor.endBlock(size, time);
        }
        parts.add(new Part(name, time, size));
    }

    public List<Part> getParts() {
        return parts;
    }

    public void progress(long current, long max) {
        if (progressor != null) {
            progressor.progress(current, max);
        }
    }
}
