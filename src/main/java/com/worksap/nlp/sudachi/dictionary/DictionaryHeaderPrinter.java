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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;

/**
 * A dictionary header printing tool.
 */
public class DictionaryHeaderPrinter {

    private DictionaryHeaderPrinter() {
    }

    /** print information in the dictionary Description part */
    static void printDescription(String filename, PrintStream output) throws IOException {
        output.printf("File: %s%n", filename);

        ByteBuffer bytes;
        try (FileInputStream input = new FileInputStream(filename); FileChannel inputFile = input.getChannel()) {
            bytes = inputFile.map(FileChannel.MapMode.READ_ONLY, 0, inputFile.size());
            bytes.order(ByteOrder.LITTLE_ENDIAN);
        }
        Description desc = Description.load(bytes);

        if (desc.isSystemDictionary()) {
            output.println("type: system dictionary");
        } else if (desc.isUserDictionary()) {
            output.println("type: user dictionary");
        } else {
            // should not happen
            output.println("invalid file");
            return;
        }
        output.printf("Creation time: %s%n", desc.getCreationTime());
        output.printf("Comment: %s%n", desc.getComment());
        output.printf("Signature: %s%n", desc.getSignature());
        output.printf("Reference: %s%n", desc.getReference());
        output.printf("Entries total: %d%n", desc.getNumTotalEntries());
        output.printf("Entries indexed: %d%n", desc.getNumIndexedEntries());
        for (Description.Block b : desc.getBlocks()) {
            long start = b.getStart();
            output.printf("Block %s: %d - %d%n", b.getName(), start, start + b.getSize());
        }
        output.printf("Flag isRuntimeCosts: %s%n", desc.isRuntimeCosts());
    }

    /**
     * Prints the contents of dictionary header.
     *
     * This tool requires filenames of dictionaries.
     * 
     * @param args
     *            the input filenames
     * @throws IOException
     *             if IO
     */
    public static void main(String[] args) throws IOException {
        for (String filename : args) {
            printDescription(filename, System.out);
        }
    }
}
