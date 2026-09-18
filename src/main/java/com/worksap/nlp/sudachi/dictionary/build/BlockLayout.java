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

import com.worksap.nlp.sudachi.dictionary.Description.BlockInfo;

import java.io.IOException;
import java.nio.channels.SeekableByteChannel;
import java.util.ArrayList;
import java.util.List;

/**
 * Output channel wrapper to write dictionary parts in block layout. Also
 * provides access to the Progress.
 */
public class BlockLayout {
    private static final long BLOCK_SIZE = 4096;
    private final SeekableByteChannel channel;
    private final Progress progress;
    private final List<BlockInfo> info = new ArrayList<>();

    public BlockLayout(SeekableByteChannel channel, Progress progress) {
        this.channel = channel;
        this.progress = progress;
    }

    public BlockLayout(SeekableByteChannel channel) {
        this.channel = channel;
        this.progress = Progress.NOOP;
    }

    /**
     * Align the current position of output channel.
     * 
     * @return new position of channel
     */
    private long alignPosition() throws IOException {
        SeekableByteChannel chan = channel;
        long end = chan.position();
        long newPosition = Align.align(end, BLOCK_SIZE);
        chan.position(newPosition);
        return newPosition;
    }

    /**
     * Keep space for the specified number of blocks for the later use.
     * 
     * @return start position of keeped blocks.
     */
    public long keepBlocks(int numBlocks) throws IOException {
        long blockSize = numBlocks * BLOCK_SIZE;
        long startPosition = Align.align(channel.position(), BLOCK_SIZE);
        channel.position(startPosition + blockSize);
        return startPosition;
    }

    /** Function that works with BlockOutput */
    public interface BlockHandler<T> {
        T apply(BlockOutput output) throws IOException;
    }

    /**
     * Let handler write data in block layout.
     * 
     * @param <T>
     *            return type of the handler.
     * @param name
     *            the name for the block used as key in BlockInfo.
     * @param handler
     *            handler that works on the channel and progress.
     * @return result of the handler.
     * @throws IOException
     */
    public <T> T block(String name, BlockHandler<T> handler) throws IOException {
        SeekableByteChannel chan = channel;
        long start = alignPosition();
        T result = handler.apply(new BlockOutput(chan, progress));
        long end = chan.position();
        info.add(new BlockInfo(name, start, end - start));
        return result;
    }

    /**
     * Returns the summary of block written.
     * 
     * @return block information list.
     */
    public List<BlockInfo> blocks() {
        return info;
    }
}
