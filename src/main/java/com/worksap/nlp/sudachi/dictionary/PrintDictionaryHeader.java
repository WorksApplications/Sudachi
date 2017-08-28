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

package com.worksap.nlp.sudachi.dictionary;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.time.Instant;
import java.time.ZoneId;

/**
 * A dictionary header printing tool.
 */
public class PrintDictionaryHeader {

    static void printHeader(String filename) throws IOException {
        ByteBuffer bytes;
        try (FileInputStream input = new FileInputStream(filename);
             FileChannel inputFile = input.getChannel()) {
            bytes = inputFile.map(FileChannel.MapMode.READ_ONLY, 0,
                                  inputFile.size());
            bytes.order(ByteOrder.LITTLE_ENDIAN);
        }
        DictionaryHeader header = new DictionaryHeader(bytes, 0);

        System.out.println("filename: " + filename);

        long version = header.getVersion();
        if (version == DictionaryVersion.SYSTEM_DICT_VERSION) {
            System.out.println("type: system dictionary");
        } else if (version == DictionaryVersion.USER_DICT_VERSION) {
            System.out.println("type: user dictionary");
        } else {
            System.out.println("invalid file");
            return;
        }

        System.out.println("createTime: " + Instant.ofEpochSecond(header.getCreateTime()).atZone(ZoneId.systemDefault()).toString());
        System.out.println("description: " + header.getDescription());
    }

    /**
     * Prints the contents of dictionary header.
     *
     * This tool requires filenames of dictionaries.
     * @param args the input filenames
     * @throws IOException if IO
     */
    public static void main(String[] args) throws IOException {
        for (String filename : args) {
            printHeader(filename);
        }
    }
}
