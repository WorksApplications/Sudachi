/*
 * Copyright (c) 2017 Works Applications Co., Ltd.
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

package com.worksap.nlp.sudachi;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;

/**
 * A command-line morphological analysis tool.
 */
public class SudachiCommandLine {

    static String readAll(InputStream input) throws IOException {
        BufferedReader reader
            = new BufferedReader(new InputStreamReader(input));
        StringBuilder sb = new StringBuilder();
        while (true) {
            String line = reader.readLine();
            if (line == null) {
                break;
            }
            sb.append(line);
        }
        return sb.toString();
    }

    static void run(Tokenizer tokenizer, Tokenizer.SplitMode mode,
                    InputStream input, PrintStream output, boolean printAll)
        throws IOException {

        try (InputStreamReader inputReader = new InputStreamReader(input);
             BufferedReader reader = new BufferedReader(inputReader)) {

            while (true) {
                String line = reader.readLine();
                if (line == null) {
                    break;
                }
                for (Morpheme m : tokenizer.tokenize(mode, line)) {
                    output.print(m.surface());
                    output.print("\t");
                    output.print(String.join(",", m.partOfSpeech()));
                    output.print("\t");
                    output.print(m.normalizedForm());
                    if (printAll) {
                        output.print("\t");
                        output.print(m.dictionaryForm());
                        output.print("\t");
                        output.print(m.readingForm());
                        if (m.isOOV()) {
                            output.print("\t");
                            output.print("(OOV)");
                        }
                    }
                    output.println();
                }
                output.println("EOS");
            }
        }
    }

    /**
     * Analyzes the input texts.
     *
     * <p>Usage: {@code SudachiCommandLine [-r file] [-m A|B|C] [-o file] [-d] [file ...]}
     * <p>The following are the options.
     * <dl>
     * <dt>{@code -r file}</dt><dd>the settings file in JSON format</dd>
     * <dt>{@code -m {A|B|C}}</dt><dd>the mode of splitting</dd>
     * <dt>{@code -o file}</dt><dd>the output file</dd>
     * <dt>{@code -a}</dt><dd>print all of the fields</dd>
     * <dt>{@code -d}</dt><dd>print the debug informations</dd>
     * <dt>{@code -h}</dt><dd>show the usage</dd>
     * </dl>
     * <p>If the output file is not specified, this tool writes the output
     * to the standard output.
     * <p>The {@code file} operands are processed in command-line order.
     * If {@code file} is absent, this tool reads from the starndard input.
     *
     * <p>This tool processes a line as a sentence.
     *
     * @param args the options and the input filenames
     * @throws IOException if IO is failed
     */
    public static void main(String[] args) throws IOException {
        Tokenizer.SplitMode mode = Tokenizer.SplitMode.C;
        String settings = null;
        PrintStream output = System.out;
        boolean isEnableDump = false;
        boolean printAll = false;

        int i = 0;
        for (i = 0; i < args.length; i++) {
            if (args[i].equals("-r") && i + 1 < args.length) {
                try (FileInputStream input = new FileInputStream(args[++i])) {
                    settings = readAll(input);
                }
            } else if (args[i].equals("-m") && i + 1 < args.length) {
                switch (args[++i]) {
                case "A":
                    mode = Tokenizer.SplitMode.A;
                    break;
                case "B":
                    mode = Tokenizer.SplitMode.B;
                    break;
                default:
                    mode = Tokenizer.SplitMode.C;
                    break;
                }
            } else if (args[i].equals("-o") && i + 1 < args.length) {
                output = new PrintStream(args[++i]);
            } else if (args[i].equals("-a")) {
                printAll = true;
            } else if (args[i].equals("-d")) {
                isEnableDump = true;
            } else if (args[i].equals("-h")) {
                System.err.println("usage: SudachiCommandLine [-r file] [-m A|B|C] [-o file] [-d] [file ...]");
                System.err.println("\t-r file\tread settings from file");
                System.err.println("\t-m mode\tmode of splitting");
                System.err.println("\t-o file\toutput to file");
                System.err.println("\t-a\tprint all fields");
                System.err.println("\t-d\tdebug mode");
                return;
            } else {
                break;
            }
        }

        if (settings == null) {
            try (InputStream input
                 = SudachiCommandLine.class
                 .getResourceAsStream("/sudachi.json")) {
                settings = readAll(input);
            }
        }

        try (Dictionary dict = new DictionaryFactory().create(settings)) {
            Tokenizer tokenizer = dict.create();
            if (isEnableDump) {
                tokenizer.setDumpOutput(output);
            }

            if (i < args.length) {
                for ( ; i < args.length; i++) {
                    try (FileInputStream input = new FileInputStream(args[i])) {
                        run(tokenizer, mode, input, output, printAll);
                    }
                }
            } else {
                run(tokenizer, mode, System.in, output, printAll);
            }
        }
        output.close();
    }
}
