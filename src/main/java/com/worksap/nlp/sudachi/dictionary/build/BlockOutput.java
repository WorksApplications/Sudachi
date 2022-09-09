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

public class BlockOutput {
    private SeekableByteChannel chan;
    private Progress progress;

    private Stats stats;

    public BlockOutput(SeekableByteChannel chan, Progress progress) {
        this.chan = chan;
        this.progress = progress;
    }

    public SeekableByteChannel getChannel() {
        return chan;
    }

    public Progress getProgress() {
        return progress;
    }

    public <T> T measured(String name, IOFunction<T, Progress> fun) throws IOException {
        Progress p = progress;
        long start = chan.position();
        p.startBlock(name, System.nanoTime(), Progress.Kind.OUTPUT);
        T result = fun.apply(p);
        long size = chan.position() - start;
        p.endBlock(size, System.nanoTime());
        return result;
    }
}
