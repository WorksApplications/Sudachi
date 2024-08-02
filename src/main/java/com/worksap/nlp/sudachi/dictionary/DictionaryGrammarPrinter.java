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
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.worksap.nlp.sudachi.PathAnchor;
import com.worksap.nlp.sudachi.Config;
import com.worksap.nlp.sudachi.DictionaryFactory;
import com.worksap.nlp.sudachi.Settings;

/**
 * A dictionary grammar printing tool.
 */
public class DictionaryGrammarPrinter {
    private DictionaryGrammarPrinter() {
    }

    static void printUsage() {
        Console console = System.console();
        console.printf("usage: DictionaryGrammarPrinter [-r file] \n");
        console.printf("\t-r file\tread settings from file (overrides -s)\n");
        console.printf("\t-s string\tadditional settings (overrides -r)\n");
        console.printf("\t-p directory\troot directory of resources\n");
        console.printf("\t--systemDict file\tpath to a system dictionary (overrides everything)\n");
        console.printf("\t-u file\tpath to an additional user dictionary (appended to -s)\n");
    }

    static void printPos(GrammarImpl grammar, PrintStream output) throws IOException {
        int numPos = grammar.getPartOfSpeechSize();
        for (int i = 0; i < numPos; i++) {
            POS pos = grammar.getPartOfSpeechString((short) i);
            output.println(pos.toString());
        }
    }

    /**
     * Prints the contents of dictionary grammar.
     * 
     * Specify the target dictionary in the same way to SudachiCommandline.
     * Currently it can only print POS table.
     * 
     * @param args
     *            the input filenames
     * @throws IOException
     *             if IO fails
     */
    public static void main(String[] args) throws IOException {
        PathAnchor anchor = PathAnchor.classpath().andThen(PathAnchor.none());
        Settings current = Settings.resolvedBy(anchor)
                .read(DictionaryGrammarPrinter.class.getClassLoader().getResource("sudachi.json"));
        Config additional = Config.empty();

        int i;
        for (i = 0; i < args.length; i++) {
            if (args[i].equals("-h")) {
                printUsage();
                return;
            } else if (args[i].equals("-r") && i + 1 < args.length) {
                Path configPath = Paths.get(args[++i]);
                Path parent = configPath.getParent();
                if (parent == null) { // parent directory of file.txt unfortunately is null :(
                    parent = Paths.get("");
                }
                PathAnchor curAnchor = PathAnchor.filesystem(parent).andThen(PathAnchor.classpath());
                additional = Config.fromFile(configPath, curAnchor).withFallback(additional);
            } else if (args[i].equals("-p") && i + 1 < args.length) {
                String resourcesDirectory = args[++i];
                anchor = PathAnchor.filesystem(Paths.get(resourcesDirectory)).andThen(PathAnchor.classpath());
                // first resolve wrt new directory
                current = Settings.resolvedBy(anchor).withFallback(current);
            } else if (args[i].equals("-s") && i + 1 < args.length) {
                Config other = Config.fromJsonString(args[++i], anchor);
                additional = other.withFallback(additional);
            } else if (args[i].equals("-u")) {
                Path resolved = anchor.resolve(args[++i]);
                additional = additional.addUserDictionary(resolved);
            } else if (args[i].equals("--systemDict")) {
                Path resolved = anchor.resolve(args[++i]);
                additional = additional.systemDictionary(resolved);
            } else {
                break;
            }
        }

        Config config = additional.withFallback(Config.fromSettings(current));
        DictionaryAccess dict = (DictionaryAccess) new DictionaryFactory().create(config);
        GrammarImpl grammar = dict.getGrammar();

        printPos(grammar, System.out);
    }
}
