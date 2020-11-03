/*
 * Copyright (c) 2020 Works Applications Co., Ltd.
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
import java.io.Console;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.List;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * A command-line morphological analysis tool.
 */
public class SudachiCommandLine {

    static Logger logger;

    static class FileOrStdoutPrintStream extends PrintStream {

        private boolean isFile;

        FileOrStdoutPrintStream(String fileName) throws FileNotFoundException {
            super(fileName == null ? System.out : new FileOutputStream(fileName), fileName == null);
            isFile = fileName != null;
        }

        @Override
        public void close() {
            if (isFile) {
                super.close();
            } else {
                flush();
            }
        }
    }

    static void run(Tokenizer tokenizer, Tokenizer.SplitMode mode, InputStream input, PrintStream output,
            MorphemeFormatterPlugin formatter, boolean ignoreError) throws IOException {

        try (InputStreamReader inputReader = new InputStreamReader(input);
                BufferedReader reader = new BufferedReader(inputReader)) {

            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                try {
                    for (List<Morpheme> sentence : tokenizer.tokenizeSentences(mode, line)) {
                        formatter.printSentence(sentence, output);
                    }
                } catch (RuntimeException e) {
                    if (ignoreError) {
                        logger.warning(e.getMessage() + "\n");
                    } else {
                        throw e;
                    }
                }
            }
        }
    }

    static MorphemeFormatterPlugin getFormatter(String path, String jsonString, boolean mergeSettings)
            throws IOException {
        Settings settings = JapaneseDictionary.buildSettings(path, jsonString, mergeSettings);
        List<MorphemeFormatterPlugin> formatters = settings.getPluginList("formatterPlugin");
        int n = formatters.size();
        if (n < 1) {
            throw new IllegalArgumentException("no morpheme formatter");
        }
        MorphemeFormatterPlugin formatter = formatters.get(n - 1);
        formatter.setUp();
        return formatter;
    }

    /**
     * Analyzes the input texts.
     *
     * <p>
     * Usage:
     * {@code SudachiCommandLine [-r file] [-m A|B|C] [-o file] [-d] [file ...]}
     * <p>
     * The following are the options.
     * <dl>
     * <dt>{@code -r file}</dt>
     * <dd>the settings file in JSON format (overrides -s)</dd>
     * <dt>{@code -s string}</dt>
     * <dd>an additional settings string in JSON format (overrides -r)</dd>
     * <dt>{@code -m {A|B|C}}</dt>
     * <dd>the mode of splitting</dd>
     * <dt>{@code -o file}</dt>
     * <dd>the output file</dd>
     * <dt>{@code -a}</dt>
     * <dd>show details</dd>
     * <dt>{@code -d}</dt>
     * <dd>print the debug informations</dd>
     * <dt>{@code -h}</dt>
     * <dd>show the usage</dd>
     * </dl>
     * <p>
     * If the output file is not specified, this tool writes the output to the
     * standard output.
     * <p>
     * The {@code file} operands are processed in command-line order. If
     * {@code file} is absent, this tool reads from the starndard input.
     *
     * <p>
     * This tool processes a line as a sentence.
     *
     * @param args
     *            the options and the input filenames
     * @throws IOException
     *             if IO is failed
     */
    public static void main(String[] args) throws IOException {
        InputStream is = SudachiCommandLine.class.getResourceAsStream("/logger.properties");
        if (is != null) {
            LogManager.getLogManager().readConfiguration(is);
        }
        logger = Logger.getLogger(SudachiCommandLine.class.getName());

        Tokenizer.SplitMode mode = Tokenizer.SplitMode.C;
        String settings = null;
        boolean mergeSettings = false;
        String resourcesDirectory = null;
        String outputFileName = null;
        boolean isEnableDump = false;
        boolean showDetails = false;
        boolean ignoreError = false;

        int i = 0;
        for (i = 0; i < args.length; i++) {
            if (args[i].equals("-r") && i + 1 < args.length) {
                try (FileInputStream input = new FileInputStream(args[++i])) {
                    settings = JapaneseDictionary.readAll(input);
                    mergeSettings = false;
                }
            } else if (args[i].equals("-p") && i + 1 < args.length) {
                resourcesDirectory = args[++i];
            } else if (args[i].equals("-s") && i + 1 < args.length) {
                settings = args[++i];
                mergeSettings = true;
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
                outputFileName = args[++i];
            } else if (args[i].equals("-a")) {
                showDetails = true;
            } else if (args[i].equals("-d")) {
                isEnableDump = true;
            } else if (args[i].equals("-f")) {
                ignoreError = true;
            } else if (args[i].equals("-h")) {
                Console console = System.console();
                console.printf("usage: SudachiCommandLine [-r file] [-m A|B|C] [-o file] [file ...]\n");
                console.printf("\t-r file\tread settings from file (overrides -s)\n");
                console.printf("\t-s string\tadditional settings (overrides -r)\n");
                console.printf("\t-p directory\troot directory of resources\n");
                console.printf("\t-m mode\tmode of splitting\n");
                console.printf("\t-o file\toutput to file\n");
                console.printf("\t-a\tshow details\n");
                console.printf("\t-f\tignore error\n");
                console.printf("\t-d\tdebug mode\n");
                return;
            } else {
                break;
            }
        }

        MorphemeFormatterPlugin formatter = getFormatter(resourcesDirectory, settings, mergeSettings);
        if (showDetails) {
            formatter.showDetails();
        }

        try (PrintStream output = new FileOrStdoutPrintStream(outputFileName);
                Dictionary dict = new DictionaryFactory().create(resourcesDirectory, settings, mergeSettings)) {
            Tokenizer tokenizer = dict.create();
            if (isEnableDump) {
                tokenizer.setDumpOutput(output);
            }

            if (i < args.length) {
                for (; i < args.length; i++) {
                    try (FileInputStream input = new FileInputStream(args[i])) {
                        run(tokenizer, mode, input, output, formatter, ignoreError);
                    }
                }
            } else {
                run(tokenizer, mode, System.in, output, formatter, ignoreError);
            }
        }
    }
}
