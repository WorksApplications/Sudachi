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

import com.worksap.nlp.sudachi.dictionary.BinaryDictionary;
import com.worksap.nlp.sudachi.dictionary.Blocks;
import com.worksap.nlp.sudachi.dictionary.Description;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.Objects;

import static java.lang.System.nanoTime;

/**
 * Fluid API for building a binary dictionary from a CSV file. See documentation
 * for the format of the CSV dictionary.
 */
public class DicBuilder {
    @FunctionalInterface
    public interface IOSupplier<T> {
        T get() throws IOException;
    }

    private DicBuilder() {
        // no instances
    }

    private static class Base<T extends Base<T>> {
        protected Progress progress = Progress.NOOP;
        protected final Description description = new Description();
        protected final ConnectionMatrix connection = new ConnectionMatrix();
        protected final POSTable pos = new POSTable();
        protected final RawLexicon lexicon = new RawLexicon();

        @SuppressWarnings("unchecked")
        private T self() {
            return (T) this;
        }

        /**
         * Set the progress handler to the provided one.
         * 
         * @param progress
         *            progress handler
         * @return current object
         */
        public T progress(Progress progress) {
            this.progress = Objects.requireNonNull(progress);
            return self();
        }

        /**
         * Read POS list from the csv file.
         */
        public T posTable(String name, IOSupplier<InputStream> input, long size) throws IOException {
            progress.startBlock(name, nanoTime(), Progress.Kind.ENTRY);
            int nRead;
            try (InputStream is = input.get()) {
                InputStream stream = new ProgressInputStream(is, size, progress);
                nRead = pos.readEntries(stream);
            }
            progress.endBlock(nRead, nanoTime());
            return self();
        }

        /**
         * Read POS list from the csv file.
         */
        public T posTable(URL url) throws IOException {
            String name = url.getPath();
            URLConnection conn = url.openConnection();
            long size = conn.getContentLengthLong();
            return posTable(name, conn::getInputStream, size);
        }

        /**
         * Read POS list from the csv file.
         */
        public T posTable(Path path) throws IOException {
            String name = path.getFileName().toString();
            long size = Files.size(path);
            return posTable(name, () -> Files.newInputStream(path), size);
        }

        /**
         * Import words from the csv lexicon into the binary dictionary compiler.
         *
         * @param name
         *            name of input file
         * @param input
         *            factory for the InputStream with the lexicon content. May be
         *            called several times.
         * @param size
         *            total size of the file in bytes. Used for reporting progress and
         *            can be not very precise.
         * @return current object
         * @throws IOException
         *             when IO fails
         */
        public T lexicon(String name, IOSupplier<InputStream> input, long size) throws IOException {
            int numEntryBefore = lexicon.getTotalEntries();
            progress.startBlock(name, nanoTime(), Progress.Kind.ENTRY);
            short numLeft = connection.nonEmpty() ? connection.getNumLeft() : Short.MAX_VALUE;
            short numRight = connection.nonEmpty() ? connection.getNumRight() : Short.MAX_VALUE;
            try (InputStream is = input.get()) {
                InputStream stream = new ProgressInputStream(is, size, progress);
                lexicon.read(name, stream, pos, numLeft, numRight);
            }
            progress.endBlock((long) lexicon.getTotalEntries() - numEntryBefore, nanoTime());
            return self();
        }

        /**
         * Import words from the csv lexicon into the binary dictionary compiler. This
         * method is for loading resources from classpath mostly, remote access is
         * untested.
         *
         * @param url
         *            pointing to the
         * @return current object
         * @throws IOException
         *             when IO fails
         * @see Class#getResource(String)
         * @see ClassLoader#getResource(String)
         */
        public T lexicon(URL url) throws IOException {
            String name = url.getPath();
            URLConnection conn = url.openConnection();
            long size = conn.getContentLengthLong();
            return lexicon(name, conn::getInputStream, size);
        }

        /**
         * Import words from the csv lexicon into the binary dictionary compiler.
         *
         * @param path
         *            csv file
         * @return current object
         * @throws IOException
         *             when IO fails
         */
        public T lexicon(Path path) throws IOException {
            String name = path.getFileName().toString();
            long size = Files.size(path);
            return lexicon(name, () -> Files.newInputStream(path), size);
        }

        /**
         * Set the comment string in the binary dictionary
         * 
         * @param comment
         *            provided string
         * @return current object
         */
        public T comment(String comment) {
            description.setComment(Objects.requireNonNull(comment));
            return self();
        }

        /**
         * Set the dictionary compilation time
         * 
         * @param instant
         *            time to set
         * @return current object
         */
        public T compilationTime(Instant instant) {
            description.setCompilationTime(Objects.requireNonNull(instant));
            return self();
        }

        /**
         * Compile the binary dictionary and write it to the proviced channel
         * 
         * @param channel
         *            contents will be written here
         * @throws IOException
         *             if io fails
         */
        public void build(SeekableByteChannel channel) throws IOException {
            BlockLayout layout = new BlockLayout(channel, progress);
            layout.keepBlocks(1); // keep space for the Description.
            if (connection.nonEmpty()) {
                layout.block(Blocks.CONNECTION_MATRIX, connection::compile);
            }
            layout.block(Blocks.POS_TABLE, pos::compile);
            lexicon.compile(layout);
            description.setBlocks(layout.blocks());
            description.setNumberOfEntries(lexicon.getIndexedEntries(), lexicon.getTotalEntries());
            description.setRuntimeCosts(lexicon.hasRuntimeCosts());
            description.save(channel);
        }
    }

    /**
     * System dictionary with connection matrix added.
     * 
     * Instanciate via SystemNoMatrix.
     */
    public static final class System extends Base<System> {
        /**
         * Read connection matrix from MeCab matrix.def format text file.
         */
        private System readMatrix(String name, IOSupplier<InputStream> input, long size) throws IOException {
            progress.startBlock(name, nanoTime(), Progress.Kind.BYTE);
            try (InputStream is = input.get()) {
                InputStream stream = new ProgressInputStream(is, size, progress);
                connection.readEntries(stream);
            }
            progress.endBlock(size, nanoTime());
            return this;
        }

        /**
         * Set the system dictionary signature to the provided string. By default, it is
         * current timestamp and a random 8 hexadecimal characters.
         * 
         * @param signature
         *            provided dictionary signature. Can not be empty.
         * @return current object
         */
        public System signature(String signature) {
            if (signature == null) {
                throw new IllegalArgumentException("signature can not be null");
            }
            if (signature.isEmpty()) {
                throw new IllegalArgumentException("signature can not be empty");
            }
            description.setSignature(signature);
            return this;
        }
    }

    /**
     * Typestate pattern for system dictionary that does not have connection matrix
     * added yet.
     */
    public static final class SystemNoMatrix {
        private final System inner;

        private SystemNoMatrix(DicBuilder.System inner) {
            this.inner = inner;
        }

        /**
         * Read connection matrix from MeCab matrix.def format text file.
         * 
         * @param name
         *            name of the file
         * @param data
         *            factory for the InputStream which contains the file. This can be
         *            called more than once.
         * @param size
         *            total number of bytes for the file. This information will be only
         *            used for calculating progress.
         * @return system dictionary builder
         * @throws IOException
         *             if IO fails
         */
        public DicBuilder.System matrix(String name, IOSupplier<InputStream> data, long size) throws IOException {
            return inner.readMatrix(name, data, size);
        }

        /**
         * Read connection matrix from MeCab matrix.def format text file. Classpath
         * version.
         * 
         * @param data
         *            name of the file
         * @return system dictionary builder
         * @throws IOException
         *             if IO fails
         */
        public DicBuilder.System matrix(URL data) throws IOException {
            String name = data.getPath();
            URLConnection conn = data.openConnection();
            long size = conn.getContentLengthLong();
            return matrix(name, conn::getInputStream, size);
        }

        /**
         * Read connection matrix from MeCab matrix.def format text file. Filesystem
         * version.
         * 
         * @param path
         *            path to matrix.def format file
         * @return system dictionary builder
         * @throws IOException
         *             if IO fails
         */
        public DicBuilder.System matrix(Path path) throws IOException {
            String name = path.getFileName().toString();
            long size = Files.size(path);
            return matrix(name, () -> Files.newInputStream(path), size);
        }

        /**
         * Set the progress handler to the provided one
         * 
         * @param progress
         *            progress handler
         * @return current object
         */
        public SystemNoMatrix progress(Progress progress) {
            inner.progress(progress);
            return this;
        }
    }

    /**
     * Create a new system dictionary compiler
     * 
     * @return new system dictionary compiler object
     */
    public static SystemNoMatrix system() {
        return new SystemNoMatrix(new System());
    }

    /**
     * User dictionary with reference system dictionary added.
     * 
     * Instanciate via UserNoSystem.
     */
    public static final class User extends Base<User> {
        /**
         * Preload data from given system dictionary.
         * 
         */
        public User system(BinaryDictionary system) {
            progress.startBlock("system dict entries", nanoTime(), Progress.Kind.ENTRY);
            int nread = lexicon.preloadFrom(system.getLexicon(), progress);
            progress.endBlock(nread, nanoTime());

            progress.startBlock("system dict pos list", nanoTime(), Progress.Kind.ENTRY);
            pos.preloadFrom(system.getGrammar());
            progress.endBlock(pos.getList().size(), nanoTime());

            description.setSignature("");
            description.setReference(system.getDictionaryHeader().getSignature());
            return this;
        }
    }

    /**
     * Typestate pattern for user dictionary that does not have system dictionary
     * added yet.
     */
    public static final class UserNoSystem {
        private final User inner;

        private UserNoSystem(DicBuilder.User inner) {
            this.inner = inner;
        }

        /**
         * Preload data from given system dictionary.
         * 
         * @param system
         *            referenced dictionary
         * @return
         */
        public DicBuilder.User system(BinaryDictionary system) {
            return inner.system(system);
        }

        /**
         * Set the progress handler to the provided one
         * 
         * @param progress
         *            progress handler
         * @return current object
         */
        public UserNoSystem progress(Progress progress) {
            inner.progress(progress);
            return this;
        }
    }

    /**
     * Create a new user dictionary compiler which will reference the provided user
     * dictionary.
     * 
     * @return new dictionary compiler object
     */
    public static UserNoSystem user() {
        return new UserNoSystem(new User());
    }

    /** entry point to test Base build with single lexicon (first arg). */
    public static void main(String[] args) throws IOException {
        Base<?> b = new Base<>();
        Path input = Paths.get(args[0]);
        b.lexicon(input);
        Path output = Paths.get(args[1]);
        Files.createDirectories(output.getParent());
        try (SeekableByteChannel chan = Files.newByteChannel(output, StandardOpenOption.WRITE,
                StandardOpenOption.CREATE)) {
            b.build(chan);
        }
    }
}
