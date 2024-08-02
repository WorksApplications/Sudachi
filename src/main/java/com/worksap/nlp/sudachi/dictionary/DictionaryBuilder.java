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

import java.io.Console;
import java.io.IOException;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;

import com.worksap.nlp.sudachi.dictionary.build.DicBuilder;
import com.worksap.nlp.sudachi.dictionary.build.Progress;

/**
 * A system dictionary building tool entry point.
 */
public class DictionaryBuilder {
    private DictionaryBuilder() {
    }

    static void printUsage() {
        Console console = System.console();
        console.printf("usage: DictionaryBuilder -o file -m file [-d description] [-s signature] files...\n");
        console.printf("\t-o file\toutput to file\n");
        console.printf("\t-m file\tmatrix file\n");
        console.printf("\t-d description\tcomment\n");
        console.printf("\t-p file\tpos file (optional)\n");
        console.printf("\t-s signature\tsignature\n");
    }

    /**
     * Builds the system dictionary.
     * <p>
     * This tool requires three arguments.
     * <ol start="0">
     * <li>{@code -o file} the path of the output file</li>
     * <li>{@code -m file} the path of the connection matrix file in MeCab's
     * matrix.def format</li>
     * <li>{@code -d string} (optional) the description which is embedded in the
     * dictionary</li>
     * <li>the paths of the source files in the CSV format</li>
     * </ol>
     *
     * @param args
     *            the options and input filenames
     * @throws IOException
     *             if IO or parsing is failed
     */
    public static void main(String[] args) throws IOException {
        String description = "";
        String outputPath = null;
        String matrixPath = null;
        String posPath = null;
        String signature = null;

        int i;
        for (i = 0; i < args.length; i++) {
            if (args[i].equals("-o") && i + 1 < args.length) {
                outputPath = args[++i];
            } else if (args[i].equals("-m") && i + 1 < args.length) {
                matrixPath = args[++i];
            } else if (args[i].equals("-p") && i + 1 < args.length) {
                posPath = args[++i];
            } else if (args[i].equals("-d") && i + 1 < args.length) {
                description = args[++i];
            } else if (args[i].equals("-s")) {
                signature = args[++i];
            } else if (args[i].equals("-h")) {
                printUsage();
                return;
            } else {
                break;
            }
        }

        if (args.length <= i || outputPath == null || matrixPath == null) {
            printUsage();
            return;
        }

        List<String> lexiconPaths = Arrays.asList(args).subList(i, args.length);

        DicBuilder.System builder = DicBuilder.system().progress(Progress.syserr(20)).matrix(Paths.get(matrixPath))
                .comment(description);
        if (posPath != null) {
            builder = builder.posTable(Paths.get(posPath));
        }
        if (signature != null) {
            builder.signature(signature);
        }
        for (String lexiconPath : lexiconPaths) {
            builder = builder.lexicon(Paths.get(lexiconPath));
        }

        try (SeekableByteChannel ch = Files.newByteChannel(Paths.get(outputPath), StandardOpenOption.WRITE,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            builder.build(ch);
        }
    }

}
