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
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class DicBuilder2 {
    private DicBuilder2() {
        // no instances
    }

    public static class Base<T extends Base<T>> {
        protected final POSTable pos = new POSTable();
        protected final ConnectionMatrix connection = new ConnectionMatrix();
        protected Progress progress = Progress.NOOP;
        protected RawLexicon lexicon = new RawLexicon();

        @SuppressWarnings("unchecked")
        private T self() {
            return (T) this;
        }

        public T lexicon(String name, IOSupplier<InputStream> input, long size) throws IOException {
            progress.startBlock(name, System.nanoTime(), Progress.Kind.INPUT);
            try (InputStream is = input.get()) {
                InputStream stream = new TrackingInputStream(is);
                lexicon.read(name, stream, pos);
            }
            progress.endBlock(size, System.nanoTime());
            return self();
        }

        public void write(SeekableByteChannel channel) throws IOException {
            BlockLayout layout = new BlockLayout(channel, progress);
            lexicon.compile(pos, layout);
        }
    }

    public static void main(String[] args) throws IOException {
        Base<?> b = new Base<>();
        Path input = Paths.get(args[0]);
        b.lexicon(input.getFileName().toString(), () -> Files.newInputStream(input), Files.size(input));
        Path output = Paths.get(args[1]);
        Files.createDirectories(output.getParent());
        try (SeekableByteChannel chan = Files.newByteChannel(output, StandardOpenOption.WRITE,
                StandardOpenOption.CREATE)) {
            b.write(chan);
        }
    }
}
