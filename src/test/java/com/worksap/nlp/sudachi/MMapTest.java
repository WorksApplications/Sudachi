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

import static org.hamcrest.CoreMatchers.isA;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.File;
import java.nio.ByteBuffer;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class MMapTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    File path;

    @Before
    public void setUp() throws IOException {
        Utils.copyResource(temporaryFolder.getRoot().toPath(), "/system.dic");
        path = temporaryFolder.getRoot();
    }

    @Test
    public void map() throws IOException {
        String filename = new File(path, "system.dic").getPath();
        assertThat(MMap.map(filename), isA(ByteBuffer.class));
    }

    @Test(expected = FileNotFoundException.class)
    public void mapWithNotExist() throws IOException {
        String filename = new File(path, "not_exist_file").getPath();
        MMap.map(filename);
    }

    @Test
    public void unmap() throws IOException {
        String filename = new File(path, "system.dic").getPath();
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
