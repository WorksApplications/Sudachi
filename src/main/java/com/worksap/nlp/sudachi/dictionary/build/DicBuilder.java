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

import com.worksap.nlp.sudachi.JapaneseDictionary;
import com.worksap.nlp.sudachi.dictionary.CSVParser;
import com.worksap.nlp.sudachi.dictionary.Connection;
import com.worksap.nlp.sudachi.dictionary.DictionaryHeader;
import com.worksap.nlp.sudachi.dictionary.DictionaryVersion;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class DicBuilder {
    private DicBuilder() {
        /* instantiations are forbidden */
    }

    public static SystemNoMatrix system() throws IOException {
        return new SystemNoMatrix(new System());
    }

    public static User user(JapaneseDictionary system) {
        return new User(system);
    }

    public static abstract class Base<T extends Base<T>> {
        protected final POSTable pos = new POSTable();
        protected final ConnectionMatrix connection = new ConnectionMatrix();
        protected final Index index = new Index();
        protected String description = "";
        protected long version;
        protected long creationTime = java.lang.System.currentTimeMillis();
        private final List<ModelOutput.Part> inputs = new ArrayList<>();
        private Progress progress;

        protected WordIdResolver resolver() {
            return new WordLookup.Csv(lexicon);
        }

        private T self() {
            // noinspection unchecked
            return (T) this;
        }

        protected final CsvLexicon lexicon = new CsvLexicon(pos);

        public BuildStats build(SeekableByteChannel result) throws IOException {
            lexicon.setResolver(resolver());
            ModelOutput output = new ModelOutput(result);
            if (progress != null) {
                output.progressor(progress);
            }
            DictionaryHeader header = new DictionaryHeader(version, creationTime, description);

            ByteBuffer headerBuffer = ByteBuffer.wrap(header.toByte());

            output.write(headerBuffer);
            pos.writeTo(output);
            connection.writeTo(output);
            index.writeTo(output);
            lexicon.writeTo(output);
            return new BuildStats(inputs, output.getParts());
        }

        public T lexicon(URL data) throws IOException {
            URLConnection conn = data.openConnection();
            try (InputStream is = conn.getInputStream()) {
                long length = data.openConnection().getContentLengthLong();
                return lexiconImpl(data.getPath(), is, length);
            }
        }

        public T lexicon(Path path) throws IOException {
            try (InputStream is = Files.newInputStream(path)) {
                return lexiconImpl(path.getFileName().toString(), is, Files.size(path));
            }
        }

        public T lexicon(InputStream data) throws IOException {
            return lexiconImpl("<input stream>", data, data.available());
        }

        public T lexiconImpl(String name, InputStream data, long size) throws IOException {
            long startTime = java.lang.System.nanoTime();
            if (progress != null) {
                progress.startBlock(name, startTime, Progress.Kind.INPUT);
            }

            TrackingInputStream tracker = new TrackingInputStream(data);
            CSVParser parser = new CSVParser(new InputStreamReader(tracker, StandardCharsets.UTF_8));
            int line = 1;
            while (true) {
                List<String> fields = parser.getNextRecord();
                if (fields == null)
                    break;
                try {
                    CsvLexicon.WordEntry e = lexicon.parseLine(fields);
                    int wordId = lexicon.addEntry(e);
                    if (e.headword != null) {
                        index.add(e.headword, wordId);
                    }
                    line += 1;
                } catch (Exception e) {
                    throw new ReadLexiconException(line, fields.get(0), e);
                }
                if (progress != null) {
                    progress.progress(tracker.getPosition(), size);
                }
            }

            long time = java.lang.System.nanoTime() - startTime;
            if (progress != null) {
                progress.endBlock(line, time);
            }

            return self();
        }

        public T description(String description) {
            this.description = description;
            return self();
        }

        public T progress(Progress progress) {
            this.progress = progress;
            return self();
        }
    }

    public static final class System extends Base<System> {
        public System() {
            version = DictionaryVersion.SYSTEM_DICT_VERSION_2;
        }

        private void readMatrix(InputStream matrix) throws IOException {
            connection.readEntries(matrix);
            lexicon.setLimits(connection.getNumLeft(), connection.getNumRight());
        }
    }

    public static final class User extends Base<User> {
        final JapaneseDictionary dictionary;

        private User(JapaneseDictionary dictionary) {
            this.dictionary = dictionary;
            this.version = DictionaryVersion.USER_DICT_VERSION_3;
            Connection conn = dictionary.getGrammar().getConnection();
            lexicon.setLimits(conn.getLeftSize(), conn.getRightSize());
        }

        @Override
        protected WordIdResolver resolver() {
            return new WordLookup.Chain(new WordLookup.Prebuilt(dictionary.getLexicon()), new WordLookup.Csv(lexicon));
        }
    }

    public static final class SystemNoMatrix {
        private final System inner;

        private SystemNoMatrix(System inner) {
            this.inner = inner;
        }

        public System matrix(InputStream data) throws IOException {
            inner.readMatrix(data);
            return inner;
        }

        public System matrix(URL data) throws IOException {
            try (InputStream is = data.openStream()) {
                return matrix(is);
            }
        }

        public System matrix(Path path) throws IOException {
            try (InputStream is = Files.newInputStream(path)) {
                return matrix(is);
            }
        }
    }
}
