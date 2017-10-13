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

package com.worksap.nlp.dartsclone;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.IntBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class DartsTest {

    static final int NUM_VALID_KEYS = 1 << 16;
    static final int NUM_INVALID_KEYS = 1 << 17;
    static final int MAX_NUM_RESULT = 16;

    Random random = new Random();
    byte[][] keys;
    byte[][] invalidKeys;
    int[] values;

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Before
    public void setUp() {
        SortedSet<byte[]> validKeys = generateValidKeys(NUM_VALID_KEYS);
        Set<byte[]> invalidKeys = generateInvalidKeys(NUM_INVALID_KEYS,
                                                      validKeys);

        this.keys = validKeys.toArray(new byte[0][0]);
        this.invalidKeys = invalidKeys.toArray(new byte[0][0]);
        values = new int[this.keys.length];
        int keyId = 0;
        for (int i = 0; i < this.keys.length; i++) {
            values[i] = keyId++;
        }

    }

    @Test
    public void buildWithoutValue() {
        DoubleArray dic = new DoubleArray();
        dic.build(keys, null, null);
        testDic(dic);
    }

    @Test
    public void build() {
        DoubleArray dic = new DoubleArray();
        dic.build(keys, values, null);
        testDic(dic);
    }

    @Test
    public void size() {
        DoubleArray dic = new DoubleArray();
        dic.build(keys, values, null);
        assertEquals(dic.size() * 4, dic.totalSize());
    }

    @Test
    public void buildWithRandomValue() {
        for (int i = 0; i < values.length; i++) {
            values[i] = random.nextInt(10);
        }
        DoubleArray dic = new DoubleArray();
        dic.build(keys, values, null);
        testDic(dic);
    }

    @Test
    public void saveAndOpen() throws IOException {
        DoubleArray dic = new DoubleArray();
        dic.build(keys,  values, null);

        File dicFile = temporaryFolder.newFile();
        FileOutputStream ostream = new FileOutputStream(dicFile);
        FileChannel outputFile = ostream.getChannel();
        dic.save(outputFile);
        ostream.close();

        DoubleArray dicCopy = new DoubleArray();
        FileInputStream istream = new FileInputStream(dicFile);
        FileChannel inputFile = istream.getChannel();
        dicCopy.open(inputFile, 0, -1);
        istream.close();
        testDic(dicCopy);
    }

    @Test
    public void array() { 
        DoubleArray dic = new DoubleArray();
        dic.build(keys,  values, null);
        IntBuffer array = dic.array();
        int size = dic.size();
 
        DoubleArray dicCopy = new DoubleArray();
        dicCopy.setArray(array, size);
        testDic(dicCopy);
    }

    @Test
    public void commonPrefixSearch() {
        DoubleArray dic = new DoubleArray();
        dic.build(keys, values, null);

        for (int i = 0; i < keys.length; i++) {
            List<int[]> results
                = dic.commonPrefixSearch(keys[i], 0, MAX_NUM_RESULT);

            assertTrue(results.size() >= 1);
            assertTrue(results.size() < MAX_NUM_RESULT);

            assertEquals(values[i], results.get(results.size() - 1)[0]);
            assertEquals(keys[i].length, results.get(results.size() - 1)[1]);
        }

        for (byte[] key : invalidKeys) {
            List<int[]> results = dic.commonPrefixSearch(key, 0, MAX_NUM_RESULT);
            assertTrue(results.size() < MAX_NUM_RESULT);

            if (!results.isEmpty()) {
                assertNotEquals(-1, results.get(results.size() - 1)[0]);
                assertTrue(key.length > results.get(results.size() - 1)[1]);
            }
        }
    }

    @Test
    public void commonPrefixSearchWithIterator() {
        DoubleArray dic = new DoubleArray();
        dic.build(keys, values, null);

        for (int i = 0; i < keys.length; i++) {
            Iterator<int[]> iterator = dic.commonPrefixSearch(keys[i], 0);

            assertTrue(iterator.hasNext());

            int[] result = null;
            while (iterator.hasNext()) {
                result = iterator.next();
            }
            assertFalse(iterator.hasNext());
            assertEquals(values[i], result[0]);
            assertEquals(keys[i].length, result[1]);
        }

        for (byte[] key : invalidKeys) {
            Iterator<int[]> iterator = dic.commonPrefixSearch(key, 0);

            int[] result = null;
            while (iterator.hasNext()) {
                result = iterator.next();
            }
            if (result != null) {
                assertNotEquals(-1, result[0]);
                assertTrue(key.length > result[1]);
            }
        }
    }

    void testDic(DoubleArray dic) {
        for (int i = 0; i < keys.length; i++) {
            int[] result = dic.exactMatchSearch(keys[i]);
            assertEquals(keys[i].length, result[1]);
            assertEquals(values[i], result[0]);
        }
        
        for (byte[] key : invalidKeys) {
            int[] result = dic.exactMatchSearch(key);
            assertEquals(-1, result[0]);
        }
    }

    SortedSet<byte[]> generateValidKeys(int numKeys) {
        SortedSet<byte[]> keys = new TreeSet<>(new Comparator<byte[]>() {
                @Override
                public int compare(byte[] b1, byte[] b2) {
                    int n1 = b1.length;
                    int n2 = b2.length;
                    int min = Math.min(n1, n2);
                    for (int i = 0; i < min; i++) {
                        int c1 = b1[i] & 0xFF;
                        int c2 = b2[i] & 0xFF;
                        if (c1 != c2) {
                            return c1 - c2;
                        }
                    }
                    return n1 - n2;
                }
            });

        StringBuilder key = new StringBuilder();
        while (keys.size() < numKeys) {
            key.setLength(0);
            int length = random.nextInt(8) + 1;
            for (int i = 0; i < length; i++) {
                key.append((char)(1 + random.nextInt(65535)));
            }
            keys.add(key.toString().getBytes(StandardCharsets.UTF_8));
        }
        return keys;
    }

    Set<byte[]> generateInvalidKeys(int numKeys, Set<byte[]> validKeys) {
        Set<byte[]> keys = new HashSet<>();
        StringBuilder key = new StringBuilder();
        while (keys.size() < numKeys) {
            key.setLength(0);
            int length = random.nextInt(8) + 1;
            for (int i = 0; i < length; i++) {
                key.append((char)(1 + random.nextInt(65535)));
            }
            byte[] k = key.toString().getBytes(StandardCharsets.UTF_8);
            if (!validKeys.contains(k)) {
                keys.add(k);
            }
        }
        return keys;
    }
}
