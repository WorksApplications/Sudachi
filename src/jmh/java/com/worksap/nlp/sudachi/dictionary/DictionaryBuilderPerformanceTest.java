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

package com.worksap.nlp.sudachi.dictionary;

import com.worksap.nlp.sudachi.dictionary.build.DicBuilder;
import org.openjdk.jmh.annotations.*;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@Threads(1)
@Fork(jvmArgs = "-Xmx1g")
public class DictionaryBuilderPerformanceTest {
    public static void main(String[] args) throws IOException {
        org.openjdk.jmh.Main.main(args);
    }

    private final static Path ROOT = Paths.get("src/test/resources/dict");

    @Benchmark
    public long smallCase() throws IOException {
        MemChannelJmh mc = new MemChannelJmh();
        DicBuilder
                .system()
                .matrix(ROOT.resolve("matrix.def"))
                .lexicon(ROOT.resolve("lex.csv"))
                .build(mc);
        return mc.size();
    }
}
