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

import com.worksap.nlp.sudachi.dictionary.Blocks;
import com.worksap.nlp.sudachi.dictionary.Description;
import com.worksap.nlp.sudachi.dictionary.DictionaryAccess;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import static java.lang.System.nanoTime;

public class DicBuilder2 {
    private DicBuilder2() {
        // no instances
    }

    private static class Base<T extends Base<T>> {
        protected final POSTable pos = new POSTable();
        protected final ConnectionMatrix connection = new ConnectionMatrix();
        protected Progress progress = Progress.NOOP;
        protected RawLexicon lexicon = new RawLexicon();
        protected final Description description = new Description();

        @SuppressWarnings("unchecked")
        private T self() {
            return (T) this;
        }

        public T lexicon(String name, IOSupplier<InputStream> input, long size) throws IOException {
            progress.startBlock(name, nanoTime(), Progress.Kind.INPUT);
            try (InputStream is = input.get()) {
                InputStream stream = new TrackingInputStream(is);
                lexicon.read(name, stream, pos);
            }
            progress.endBlock(size, nanoTime());
            return self();
        }

        public T lexicon(URL url) throws IOException {
            String name = url.getPath();
            URLConnection conn = url.openConnection();
            long size = conn.getContentLengthLong();
            return lexicon(name, conn::getInputStream, size);
        }

        public T lexicon(Path path) throws IOException {
            String name = path.getFileName().toString();
            long size = Files.size(path);
            return lexicon(name, () -> Files.newInputStream(path), size);
        }

        public void write(SeekableByteChannel channel) throws IOException {
            BlockLayout layout = new BlockLayout(channel, progress);
            if (connection.nonEmpty()) {
                layout.block(Blocks.CONNECTION_MATRIX, connection::compile);
            }
            layout.block(Blocks.POS_TABLE, pos::compile);
            lexicon.compile(pos, layout);
        }
    }

    public static final class System extends Base<System> {
        private System readMatrix(String name, IOSupplier<InputStream> input, long size) throws IOException {
            progress.startBlock(name, nanoTime(), Progress.Kind.INPUT);
            try (InputStream is = input.get()) {
                InputStream stream = new ProgressInputStream(is, size, progress);
                connection.readEntries(stream);
            }
            progress.endBlock(size, nanoTime());
            return this;
        }
    }

    public static final class SystemNoMatrix {
        private final System inner;

        private SystemNoMatrix(DicBuilder2.System inner) {
            this.inner = inner;
        }

        public DicBuilder2.System matrix(String name, IOSupplier<InputStream> data, long size) throws IOException {
            return inner.readMatrix(name, data, size);
        }

        public DicBuilder2.System matrix(URL data) throws IOException {
            String name = data.getPath();
            URLConnection conn = data.openConnection();
            long size = conn.getContentLengthLong();
            return matrix(name, conn::getInputStream, size);
        }

        public DicBuilder2.System matrix(Path path) throws IOException {
            String name = path.getFileName().toString();
            long size = Files.size(path);
            return matrix(name, () -> Files.newInputStream(path), size);
        }
    }

    public static final class User extends Base<User> {
        private User(DictionaryAccess system) {
            pos.preloadFrom(system.getGrammar());
            description.setSignature("");
        }
    }

    public static SystemNoMatrix system() {
        return new SystemNoMatrix(new System());
    }

    public static User user(DictionaryAccess system) {
        return new User(system);
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
