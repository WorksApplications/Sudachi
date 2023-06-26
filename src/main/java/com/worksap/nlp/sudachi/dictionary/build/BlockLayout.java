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

import com.worksap.nlp.sudachi.dictionary.Description;

import java.io.IOException;
import java.nio.channels.SeekableByteChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public class BlockLayout {
    private final SeekableByteChannel channel;
    private final Progress progress;
    private final List<BlockInfo> info = new ArrayList<>();

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

    public List<Description.Block> blocks() {
        List<Description.Block> result = new ArrayList<>();
        for (BlockInfo b: info) {
            Description.Block published = new Description.Block(b.name, b.start, b.end - b.start);
            result.add(published);
        }
        return result;
    }

    private static class BlockInfo {
        String name;
        long start;
        long end;

        public BlockInfo(String name, long start, long end) {
            this.name = name;
            this.start = start;
            this.end = end;
        }

        @Override
        public String toString() {
            return new StringJoiner(", ", BlockInfo.class.getSimpleName() + "[", "]").add("name='" + name + "'")
                    .add("start=" + start).add("end=" + end).toString();
        }
    }
}
