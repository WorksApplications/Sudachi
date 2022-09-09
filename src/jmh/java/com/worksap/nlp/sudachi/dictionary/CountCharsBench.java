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

import org.openjdk.jmh.annotations.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

@BenchmarkMode(Mode.Throughput)
@State(Scope.Benchmark)
@Warmup(time = 5, iterations = 3)
@Measurement(iterations = 7, time = 5)
@Fork(value = 1)
public class CountCharsBench {
    private String[] data;

    @Setup
    public void setup() throws IOException {
        Path keysFile = Paths.get("build/darray/kwdlc.txt");
        Download.downloadIfNotExist(keysFile,
                "https://github.com/ku-nlp/KWDLC/releases/download/release_1_0/leads.org.txt.gz", true);
        try (Stream<String> data = Files.lines(keysFile)) {
            this.data = data.toArray(String[]::new);
        }
    }

    @Benchmark
    @OperationsPerInvocation(15000)
    public int naiveImpl() {
        int count = 0;
        char toFind = 'の';
        for (String s : data) {
            for (int i = 0; i < s.length(); i++) {
                char c = s.charAt(i);
                if (c == toFind) {
                    count += 1;
                }
            }
        }
        return count;
    }

    @Benchmark
    @OperationsPerInvocation(15000)
    public int indexOfImpl() {
        int count = 0;
        char toFind = 'の';
        for (String sequence : data) {
            int idx = 0;
            int end = sequence.length();
            while (idx < end) {
                idx = sequence.indexOf(toFind, idx);
                if (idx < 0) {
                    break;
                }
                idx += 1;
                count += 1;
            }
        }
        return count;
    }

    @Benchmark
    @OperationsPerInvocation(15000)
    public int streamImpl() {
        int count = 0;
        char toFind = 'の';
        for (String sequence : data) {
            count += sequence.chars().filter(c -> c == toFind).count();
        }
        return count;
    }
}
