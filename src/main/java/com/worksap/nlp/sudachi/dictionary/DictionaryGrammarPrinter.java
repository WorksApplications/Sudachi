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
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.worksap.nlp.sudachi.dictionary.build.POSTable;

/**
 * A dictionary grammar printing tool.
 */
public class DictionaryGrammarPrinter {
    private DictionaryGrammarPrinter() {
    }

    static void printUsage() {
        Console console = System.console();
        console.printf("usage: DictionaryGrammarPrinter files... \n");
    }

    static void printHeader(PrintStream output) {
        List<String> columnNames = Arrays.asList(POSTable.POSCSVReader.Column.values()).stream().map(c -> c.name())
                .collect(Collectors.toList());
        output.println(String.join(",", columnNames));
    }

    static void printPos(GrammarImpl grammar, PrintStream output) {
        int numPos = grammar.getPartOfSpeechSize();
        for (int i = 0; i < numPos; i++) {
            POS pos = grammar.getPartOfSpeechString((short) i);
            output.println(i + "," + pos);
        }
    }

    /**
     * Prints the contents of dictionary grammar.
     * 
     * Currently it can only print POS table.
     * 
     * @param args
     *            the input filenames
     * @throws IOException
     *             if IO fails
     */
    public static void main(String[] args) throws IOException {
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-h")) {
                printUsage();
                return;
            }

            BinaryDictionary dict = new BinaryDictionary(args[i]);
            GrammarImpl grammar = dict.getGrammar();
            PrintStream output = System.out;
            printHeader(output);
            printPos(grammar, output);
            dict.close();
        }
    }
}
