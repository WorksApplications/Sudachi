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
import com.worksap.nlp.sudachi.dictionary.build.Progress;

import java.io.Console;
import java.io.IOException;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;

/**
 * A user dictionary building tool. This class provide the converter from the
 * source file in the CSV format to the binary format.
 */
public class UserDictionaryBuilder {
    static void printUsage() {
        Console console = System.console();
        console.printf("usage: UserDictionaryBuilder -o file -s file [-d description] files...\n");
        console.printf("\t-o file\toutput to file\n");
        console.printf("\t-s file\tsystem dictionary\n");
        console.printf("\t-d description\tcomment\n");
    }

    /**
     * Builds the user dictionary.
     * <p>
     * This tool requires three arguments.
     * <ol start="0">
     * <li>{@code -o file} the path of the output file</li>
     * <li>{@code -s file} the path of the system dictionary</li>
     * <li>{@code -d string} (optional) the description which is embedded in the
     * dictionary</li>
     * <li>the paths of the source file in the CSV format</li>
     * </ol>
     *
     * @param args
     *            options and input filenames
     * @throws IOException
     *             if IO or parsing is failed
     */
    public static void main(String[] args) throws IOException {
        String description = "";
        Path outputPath = null;
        String sysDictPath = null;

        int i;
        for (i = 0; i < args.length; i++) {
            if (args[i].equals("-o") && i + 1 < args.length) {
                outputPath = Paths.get(args[++i]);
            } else if (args[i].equals("-s") && i + 1 < args.length) {
                sysDictPath = args[++i];
            } else if (args[i].equals("-d") && i + 1 < args.length) {
                description = args[++i];
            } else if (args[i].equals("-h")) {
                printUsage();
                return;
            } else {
                break;
            }
        }

        if (args.length <= i || outputPath == null || sysDictPath == null) {
            printUsage();
            return;
        }

        List<String> lexiconPaths = Arrays.asList(args).subList(i, args.length);

        try (BinaryDictionary system = new BinaryDictionary(sysDictPath)) {
            DicBuilder.User builder = DicBuilder.user(system).description(description)
                    .progress(new Progress(20, new DictionaryBuilder.StderrProgress()));

            for (String lexicon : lexiconPaths) {
                builder.lexicon(Paths.get(lexicon));
            }

            try (SeekableByteChannel channel = Files.newByteChannel(outputPath, StandardOpenOption.WRITE,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
                builder.build(channel);
            }
        }
    }
}
