/*
 *  Copyright (c) 2017 Works Applications Co., Ltd.
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

package com.worksap.nlp.elasticsearch.sudachi.index;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assume.assumeTrue;

import java.io.IOException;

import org.junit.Test;

public class TestSudachiPathResolver {

    private final boolean OS_WINDOWS = System.getProperty("os.name")
            .startsWith("Windows");
    private final boolean NOT_OS_WINDOWS = !System.getProperty("os.name")
            .startsWith("Windows");
    
    @Test
    public void testEmptyForWindows() throws IOException {
        assumeTrue(OS_WINDOWS);
        assertThat(
                new SudachiPathResolver(
                        "C:\\elasticsearch\\analysis-sudachi\\config",
                        "").resolvePathForFile(),
                is(""));
    }

    @Test
    public void testEmptyForWindowsForDirectory() throws IOException {
        assumeTrue(OS_WINDOWS);
        assertThat(
                new SudachiPathResolver(
                        "C:\\elasticsearch\\analysis-sudachi\\config",
                        "").resolvePathForDirectory(),
                is("C:\\elasticsearch\\analysis-sudachi\\config"));
    }
    
    @Test
    public void testOnlyFilenameForWindows() throws IOException {
        assumeTrue(OS_WINDOWS);
        assertThat(
                new SudachiPathResolver(
                        "C:\\elasticsearch\\analysis-sudachi\\config",
                        "sudachiSettings.json").resolvePathForFile(),
                is("C:\\elasticsearch\\analysis-sudachi\\config\\sudachiSettings.json"));
    }

    @Test
    public void testCurrentDirectoryForWindows() throws IOException {
        assumeTrue(OS_WINDOWS);
        assertThat(
                new SudachiPathResolver(
                        "C:\\elasticsearch\\analysis-sudachi\\config",
                        ".\\sudachiSettings.json").resolvePathForFile(),
                is("C:\\elasticsearch\\analysis-sudachi\\config\\sudachiSettings.json"));

    }

    @Test
    public void testPreDirectoryOnceForWindows() throws IOException {
        assumeTrue(OS_WINDOWS);
        assertThat(new SudachiPathResolver(
                "C:\\elasticsearch\\analysis-sudachi\\config",
                "..\\sudachiSettings.json").resolvePathForFile(),
                is("C:\\elasticsearch\\analysis-sudachi\\sudachiSettings.json"));

    }

    @Test
    public void testPreDirectoryTwiceForWindows() throws IOException {
        assumeTrue(OS_WINDOWS);
        assertThat(new SudachiPathResolver(
                "C:\\elasticsearch\\analysis-sudachi\\config",
                "..\\..\\sudachiSettings.json").resolvePathForFile(),
                is("C:\\elasticsearch\\sudachiSettings.json"));

    }

    @Test
    public void testPreDirectoryThreeTimesForWindows() throws IOException {
        assumeTrue(OS_WINDOWS);
        assertThat(new SudachiPathResolver(
                "C:\\elasticsearch\\analysis-sudachi\\config",
                "..\\..\\..\\sudachiSettings.json").resolvePathForFile(),
                is("C:\\sudachiSettings.json"));

    }

    @Test
    public void testDdriveForWindows() throws IOException {
        assumeTrue(OS_WINDOWS);
        assertThat(
                new SudachiPathResolver(
                        "D:\\elasticsearch\\analysis-sudachi\\config",
                        "sudachiSettings.json").resolvePathForFile(),
                is("D:\\elasticsearch\\analysis-sudachi\\config\\sudachiSettings.json"));

    }

    @Test
    public void testEdriveForWindows() throws IOException {
        assumeTrue(OS_WINDOWS);
        assertThat(
                new SudachiPathResolver(
                        "E:\\elasticsearch\\analysis-sudachi\\config",
                        "sudachiSettings.json").resolvePathForFile(),
                is("E:\\elasticsearch\\analysis-sudachi\\config\\sudachiSettings.json"));

    }

    @Test
    public void testAddBackSlashForWindows() throws IOException {
        assumeTrue(OS_WINDOWS);
        assertThat(
                new SudachiPathResolver(
                        "C:\\elasticsearch\\analysis-sudachi\\config",
                        "\\sudachiSettings.json").resolvePathForFile(),
                is("C:\\elasticsearch\\analysis-sudachi\\config\\sudachiSettings.json"));

    }

    @Test
    public void testAddFolderForWindows() throws IOException {
        assumeTrue(OS_WINDOWS);
        assertThat(
                new SudachiPathResolver(
                        "C:\\elasticsearch\\analysis-sudachi\\config",
                        "sudachi\\sudachiSettings.json").resolvePathForFile(),
                is("C:\\elasticsearch\\analysis-sudachi\\config\\sudachi\\sudachiSettings.json"));

    }

    @Test
    public void testAddFolderAndBackSlashForWindows() throws IOException {
        assumeTrue(OS_WINDOWS);
        assertThat(
                new SudachiPathResolver(
                        "C:\\elasticsearch\\analysis-sudachi\\config",
                        "\\sudachi\\sudachiSettings.json").resolvePathForFile(),
                is("C:\\elasticsearch\\analysis-sudachi\\config\\sudachi\\sudachiSettings.json"));

    }

    @Test
    public void testAddBackSlashTwiceForWindows() throws IOException {
        assumeTrue(OS_WINDOWS);
        assertThat(new SudachiPathResolver(
                "C:\\elasticsearch\\analysis-sudachi\\config",
                "\\\\sudachi\\sudachiSettings.json").resolvePathForFile(),
                is("\\\\sudachi\\sudachiSettings.json"));

    }

    @Test
    public void testAddBackSlashTwiceAndCdriveForWindows() throws IOException {
        assumeTrue(OS_WINDOWS);
        assertThat(new SudachiPathResolver(
                "C:\\elasticsearch\\analysis-sudachi\\config",
                "C:\\\\sudachi\\sudachiSettings.json").resolvePathForFile(),
                is("C:\\\\sudachi\\sudachiSettings.json"));

    }

    @Test
    public void testEmptyForNotWindows() throws IOException {
        assumeTrue(NOT_OS_WINDOWS);
        assertThat(new SudachiPathResolver("/usr/share/elasticsearch/config",
                "").resolvePathForFile(),
                is(""));

    }
    
    @Test
    public void testOnlyFilenameForNotWindows() throws IOException {
        assumeTrue(NOT_OS_WINDOWS);
        assertThat(new SudachiPathResolver("/usr/share/elasticsearch/config",
                "sudachiSettings.json").resolvePathForFile(),
                is("/usr/share/elasticsearch/config/sudachiSettings.json"));

    }

    @Test
    public void testCurrentDirectoryForNotWindows() throws IOException {
        assumeTrue(NOT_OS_WINDOWS);
        assertThat(new SudachiPathResolver("/usr/share/elasticsearch/config",
                "./sudachiSettings.json").resolvePathForFile(),
                is("/usr/share/elasticsearch/config/sudachiSettings.json"));

    }

    @Test
    public void testPreDirectoryOnceForNotWindows() throws IOException {
        assumeTrue(NOT_OS_WINDOWS);
        assertThat(new SudachiPathResolver("/usr/share/elasticsearch/config",
                "../sudachiSettings.json").resolvePathForFile(),
                is("/usr/share/elasticsearch/sudachiSettings.json"));

    }

    @Test
    public void testPreDirectoryTwiceForNotWindows() throws IOException {
        assumeTrue(NOT_OS_WINDOWS);
        assertThat(new SudachiPathResolver("/usr/share/elasticsearch/config",
                "../../sudachiSettings.json").resolvePathForFile(),
                is("/usr/share/sudachiSettings.json"));
    }

    @Test
    public void testPreDirectoryThreeTimesForNotWindows() throws IOException {
        assumeTrue(NOT_OS_WINDOWS);
        assertThat(new SudachiPathResolver("/usr/share/elasticsearch/config",
                "../../../sudachiSettings.json").resolvePathForFile(),
                is("/usr/share/sudachiSettings.json"));

    }

    @Test
    public void testAddSlashForNotWindows() throws IOException {
        assumeTrue(NOT_OS_WINDOWS);
        assertThat(new SudachiPathResolver("/usr/share/elasticsearch/config",
                "/sudachiSettings.json").resolvePathForFile(),
                is("/sudachiSettings.json"));

    }

    @Test
    public void testAddFolderForNotWindows() throws IOException {
        assumeTrue(NOT_OS_WINDOWS);
        assertThat(
                new SudachiPathResolver("/usr/share/elasticsearch/config",
                        "analysis-sudachi/sudachiSettings.json").resolvePathForFile(),
                is("/usr/share/elasticsearch/config/analysis-sudachi/sudachiSettings.json"));

    }

    @Test
    public void testAddFolderAndSlashForNotWindows() throws IOException {
        assumeTrue(NOT_OS_WINDOWS);
        assertThat(new SudachiPathResolver("/usr/share/elasticsearch/config",
                "/analysis-sudachi/sudachiSettings.json").resolvePathForFile(),
                is("/analysis-sudachi/sudachiSettings.json"));

    }
    
    @Test
    public void testEmptyForNotWindowsForDirectory() throws IOException {
        assumeTrue(NOT_OS_WINDOWS);
        assertThat(new SudachiPathResolver("/usr/share/elasticsearch/config",
                "/analysis-sudachi/sudachiSettings.json").resolvePathForDirectory(),
                is("/usr/share/elasticsearch/config"));

    }
}
