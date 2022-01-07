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

package com.worksap.nlp.sudachi;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;

import static org.hamcrest.CoreMatchers.isA;
import static org.hamcrest.MatcherAssert.assertThat;

public class MMapTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    Path path;

    @Before
    public void setUp() throws IOException {
        path = temporaryFolder.getRoot().toPath();
        TestDictionary.INSTANCE.getSystemDictData().writeData(path.resolve("system.dic"));
    }

    @Test
    public void map() throws IOException {
        String filename = path.resolve("system.dic").toString();
        assertThat(MMap.map(filename), isA(ByteBuffer.class));
    }

    @Test(expected = NoSuchFileException.class)
    public void mapWithNotExist() throws IOException {
        String filename = path.resolve("does_not_exist").toString();
        MMap.map(filename);
    }

    @Test
    public void unmap() throws IOException {
        String filename = path.resolve("system.dic").toString();
        ByteBuffer buffer = MMap.map(filename);
        assertThat(buffer, isA(ByteBuffer.class));
        MMap.unmap(buffer);
    }

    @Test
    public void unmapWithoutMappedByteBuffer() throws IOException {
        ByteBuffer buffer = ByteBuffer.wrap(new byte[] { 0x00, 0x00 });
        MMap.unmap(buffer);
    }

}
