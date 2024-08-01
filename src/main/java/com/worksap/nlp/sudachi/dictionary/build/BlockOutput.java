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
import java.nio.channels.SeekableByteChannel;

/**
 * Data class for BlockLayout.BlockHandler argument.
 */
public class BlockOutput {
    private SeekableByteChannel chan;
    private Progress progress;

    public BlockOutput(SeekableByteChannel chan, Progress progress) {
        this.chan = chan;
        this.progress = progress;
    }

    public BlockOutput(SeekableByteChannel chan) {
        this.chan = chan;
        this.progress = Progress.NOOP;
    }

    public SeekableByteChannel getChannel() {
        return chan;
    }

    public Progress getProgress() {
        return progress;
    }

    @FunctionalInterface
    public interface IOFunction<R, T> {
        R apply(T arg) throws IOException;
    }

    /**
     * Function decorator to measure output progress.
     * 
     * @param <T>
     *            return type of the fun
     * @param name
     *            name for progress block.
     * @param fun
     *            actual process to measure progress. Must take Progress as an only
     *            arg.
     * @return
     * @throws IOException
     */
    public <T> T measured(String name, IOFunction<T, Progress> fun) throws IOException {
        Progress p = progress;
        long start = chan.position();
        p.startBlock(name, System.nanoTime(), Progress.Kind.BYTE);
        T result = fun.apply(p);
        long size = chan.position() - start;
        p.endBlock(size, System.nanoTime());
        return result;
    }
}
