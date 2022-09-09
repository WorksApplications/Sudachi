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
import java.util.ArrayList;
import java.util.List;

public class BlockLayout {
    private final SeekableByteChannel channel;
    private final Progress progress;

    public BlockLayout(SeekableByteChannel channel, Progress progress) throws IOException {
        this.channel = channel;
        this.progress = progress;
        channel.position(4096);
    }

    public <T> T block(String name, BlockHandler<T> handler) throws IOException {
        SeekableByteChannel chan = channel;
        long start = chan.position();
        T result = handler.apply(new BlockOutput(chan, progress));
        long end = chan.position();
        long newPosition = Align.align(end, 4096);
        chan.position(newPosition);
        info.add(new BlockInfo(name, start, end));
        return result;
    }

    private final static List<BlockInfo> info = new ArrayList<>();

    private static class BlockInfo {
        String name;
        long start;
        long end;

        public BlockInfo(String name, long start, long end) {
            this.name = name;
            this.start = start;
            this.end = end;
        }
    }
}
