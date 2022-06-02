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

package com.worksap.nlp.sudachi.dictionary;

import com.worksap.nlp.dartsclone.DoubleArray;
import com.worksap.nlp.sudachi.MMap;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.zip.GZIPInputStream;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
@Warmup(time = 5, iterations = 3)
@Measurement(iterations = 7, time = 5)
@Fork(value = 1)
public class DoubleArrayLookupBench {
    private List<byte[]> keyCandidates;

    private final DoubleArray originalImpl = new DoubleArray();
    private DoubleArrayLookup newImpl;

    @Setup()
    public void setup() throws IOException {
        Path keysFile = Paths.get("build/darray/keys.txt");
        if (Files.notExists(keysFile)) {
            // download from internet if not exists
            Files.createDirectories(keysFile.getParent());
            // Sudachi Dictionary keys for all words (full dictionary)
            URL keysUrl = new URL("https://github.com/eiennohito/xtime/releases/download/v0.0.1/keys.txt.gz");
            try (InputStream is = keysUrl.openStream()) {
                GZIPInputStream gzipStream = new GZIPInputStream(is);
                Files.copy(gzipStream, keysFile);
            }
        }
        keyCandidates = Files.lines(keysFile).map(l -> l.getBytes(StandardCharsets.UTF_8)).collect(Collectors.toList());
        keyCandidates.sort((a, b) -> {
            int len = Math.min(a.length, b.length);
            for (int i = 0; i < len; ++i) {
                int xa = Byte.toUnsignedInt(a[i]);
                int xb = Byte.toUnsignedInt(b[i]);
                int cmp = Integer.compare(xa, xb);
                if (cmp != 0) {
                    return cmp;
                }
            }
            return Integer.compare(a.length, b.length);
        });
        Path binfile = Paths.get("build/darray/keys.darray");
        ByteBuffer buffer;
        IntBuffer dataAsInts;
        if (Files.exists(binfile)) {
            buffer = MMap.map(binfile);
            dataAsInts = buffer.asIntBuffer();
            originalImpl.setArray(dataAsInts, buffer.limit() / 4);
        } else {
            byte[][] keys = keyCandidates.toArray(new byte[0][]);
            int[] values = IntStream.range(0, keys.length).toArray();
            originalImpl.build(keys, values, null);
            try (SeekableByteChannel channel = Files.newByteChannel(binfile, StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)) {
                channel.write(originalImpl.byteArray());
            }
            dataAsInts = originalImpl.array();
        }
        newImpl = new DoubleArrayLookup(dataAsInts);
    }

    @State(Scope.Thread)
    public static class KeysToLookup {
        private final ArrayList<byte[]> keys = new ArrayList<>();
        private final Random rng = new Random(0xdeadbeefL);

        @Setup(Level.Invocation)
        public void setup(DoubleArrayLookupBench lookup) {
            int numKeys = 1000;
            keys.clear();
            rng.ints(numKeys, 0, lookup.keyCandidates.size()).forEach(i -> keys.add(lookup.keyCandidates.get(i)));
        }
    }

    @Benchmark
    @OperationsPerInvocation(1000)
    public void traverseTrieOriginalImpl(KeysToLookup toLookup, Blackhole blackhole) {
        for (byte[] key : toLookup.keys) {
            Iterator<int[]> iter = originalImpl.commonPrefixSearch(key, 0);
            while (iter.hasNext()) {
                int[] data = iter.next();
                blackhole.consume(data[0]);
                blackhole.consume(data[1]);
            }
        }
    }

    @Benchmark
    @OperationsPerInvocation(1000)
    public void traverseTrieNewImpl(KeysToLookup toLookup, Blackhole blackhole) {
        DoubleArrayLookup lookup = this.newImpl;
        for (byte[] key : toLookup.keys) {
            lookup.reset(key, 0, key.length);
            while (lookup.next()) {
                blackhole.consume(lookup.getValue());
                blackhole.consume(lookup.getOffset());
            }
        }
    }
}
