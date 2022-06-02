/*
 * Copyright (c) 2017-2022 Works Applications Co., Ltd.
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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class SudachiCommandLineTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();
    private String inputFileName;
    private String outputFileName;
    private String temporaryFolderName;

    @Before
    public void setUp() throws IOException {
        TestDictionary.INSTANCE.getSystemDictData().writeData(temporaryFolder.getRoot().toPath().resolve("system.dic"));
        TestDictionary.INSTANCE.getUserDict1Data().writeData(temporaryFolder.getRoot().toPath().resolve("user.dic"));
        Utils.copyResource(temporaryFolder.getRoot().toPath(), "/sudachi.json", "/char.def", "/unk.def");
        temporaryFolderName = temporaryFolder.getRoot().getPath();
        outputFileName = temporaryFolder.newFile().getPath();
        inputFileName = temporaryFolder.newFile().getPath();
        try (FileWriter writer = new FileWriter(inputFileName)) {
            writer.write("東京都に行った\n東京府に行った");
        }
    }

    @Test
    public void commandLine() throws IOException {
        SudachiCommandLine.main(new String[] { "-p", temporaryFolderName, "-o", outputFileName, inputFileName });
        try (Stream<String> lines = Files.lines(Paths.get(outputFileName))) {
            assertThat(lines.count(), is(10L));
        }
        try (Stream<String> lines = Files.lines(Paths.get(outputFileName))) {
            assertThat(lines.filter(l -> l.equals("EOS")).count(), is(2L));
        }
    }

    @Test
    public void commandLineSystemDict() throws IOException {
        SudachiCommandLine.main(new String[] { "-p", temporaryFolderName, "-o", outputFileName, "--systemDict",
                "system.dic", inputFileName });
        try (Stream<String> lines = Files.lines(Paths.get(outputFileName))) {
            assertThat(lines.count(), is(10L));
        }
        try (Stream<String> lines = Files.lines(Paths.get(outputFileName))) {
            assertThat(lines.filter(l -> l.equals("EOS")).count(), is(2L));
        }
    }

    @Test
    public void commandLineUserDict() throws IOException {
        SudachiCommandLine.main(new String[] { "-p", temporaryFolderName, "-o", outputFileName, "--userDict",
                "user.dic", inputFileName });
        try (Stream<String> lines = Files.lines(Paths.get(outputFileName))) {
            assertThat(lines.count(), is(10L));
        }
        try (Stream<String> lines = Files.lines(Paths.get(outputFileName))) {
            assertThat(lines.filter(l -> l.equals("EOS")).count(), is(2L));
        }
    }

    @Test
    public void commandLineHelp() throws IOException {
        SudachiCommandLine.main(new String[] { "-h" });
    }

    @Test
    public void commandLineWithAMode() throws IOException {
        SudachiCommandLine
                .main(new String[] { "-p", temporaryFolderName, "-o", outputFileName, "-m", "A", inputFileName });
        try (Stream<String> lines = Files.lines(Paths.get(outputFileName))) {
            assertThat(lines.count(), is(12L));
        }
        try (Stream<String> lines = Files.lines(Paths.get(outputFileName))) {
            assertThat(lines.filter(l -> l.equals("EOS")).count(), is(2L));
        }
    }

    @Test
    public void commandLineWithBMode() throws IOException {
        SudachiCommandLine
                .main(new String[] { "-p", temporaryFolderName, "-o", outputFileName, "-m", "B", inputFileName });
        try (Stream<String> lines = Files.lines(Paths.get(outputFileName))) {
            assertThat(lines.count(), is(10L));
        }
        try (Stream<String> lines = Files.lines(Paths.get(outputFileName))) {
            assertThat(lines.filter(l -> l.equals("EOS")).count(), is(2L));
        }
    }

    @Test
    public void commandLineWithAOption() throws IOException {
        SudachiCommandLine.main(new String[] { "-p", temporaryFolderName, "-o", outputFileName, "-a", inputFileName });
        try (Stream<String> lines = Files.lines(Paths.get(outputFileName))) {
            Optional<String> first = lines.filter(l -> !l.equals("EOS")).findFirst();
            assertTrue(first.isPresent());
            assertThat(first.get().split("\\t").length, is(7));
        }
    }

    @Test
    public void commandLineWithDOption() throws IOException {
        SudachiCommandLine.main(new String[] { "-p", temporaryFolderName, "-o", outputFileName, "-d", inputFileName });
        try (Stream<String> lines = Files.lines(Paths.get(outputFileName))) {
            assertThat(lines.filter(l -> l.equals("EOS")).count(), is(2L));
        }
    }

    @Test
    public void commandLineWithROption() throws IOException {
        String settingsPath = Paths.get(temporaryFolderName, "sudachi.json").toString();
        SudachiCommandLine.main(
                new String[] { "-p", temporaryFolderName, "-o", outputFileName, "-r", settingsPath, inputFileName });
        try (Stream<String> lines = Files.lines(Paths.get(outputFileName))) {
            assertThat(lines.count(), is(10L));
        }
    }

    @Test
    public void commandLineWithSOption() throws IOException {
        String settingsPath = Paths.get(temporaryFolderName, "sudachi.json").toString();
        SudachiCommandLine.main(new String[] { "-p", temporaryFolderName, "-o", outputFileName, "-r", settingsPath,
                "-s", "{\"userDict\":[]}", inputFileName });
        try (Stream<String> lines = Files.lines(Paths.get(outputFileName))) {
            assertThat(lines.count(), is(10L));
        }
    }

    @Test
    public void commandLineWithTOption() throws IOException {
        SudachiCommandLine.main(new String[] { "-p", temporaryFolderName, "-o", outputFileName, "-t", inputFileName });
        try (Stream<String> lines = Files.lines(Paths.get(outputFileName))) {
            assertThat(lines.count(), is(2L));
        }
        try (Stream<String> lines = Files.lines(Paths.get(outputFileName))) {
            Optional<String> first = lines.filter(l -> !l.equals("\n")).findFirst();
            assertTrue(first.isPresent());
            assertThat(first.get().split(" ").length, is(4));
        }
    }

    @Test
    public void commandLineWithTsOption() throws IOException {
        SudachiCommandLine.main(new String[] { "-p", temporaryFolderName, "-o", outputFileName, "-ts", inputFileName });
        try (Stream<String> lines = Files.lines(Paths.get(outputFileName))) {
            assertThat(lines.count(), is(4L));
        }
        try (Stream<String> lines = Files.lines(Paths.get(outputFileName))) {
            Optional<String> first = lines.filter(l -> !l.equals("\n")).findFirst();
            assertTrue(first.isPresent());
            assertThat(first.get().split(" ").length, is(4));
        }
    }

    @Test
    public void commandLineWithTwoFiles() throws IOException {
        SudachiCommandLine
                .main(new String[] { "-p", temporaryFolderName, "-o", outputFileName, inputFileName, inputFileName });
        try (Stream<String> lines = Files.lines(Paths.get(outputFileName))) {
            assertThat(lines.count(), is(20L));
        }
        try (Stream<String> lines = Files.lines(Paths.get(outputFileName))) {
            assertThat(lines.filter(l -> l.equals("EOS")).count(), is(4L));
        }
    }

    @Test
    public void formatterClass() throws IOException {
        SudachiCommandLine.main(new String[] { "-p", temporaryFolderName, "--format",
                SimpleMorphemeFormatter.class.getName(), "-o", outputFileName, inputFileName });
        try (Stream<String> lines = Files.lines(Paths.get(outputFileName))) {
            assertThat(lines.count(), is(10L));
        }
        try (Stream<String> lines = Files.lines(Paths.get(outputFileName))) {
            assertThat(lines.filter(l -> l.equals("EOS")).count(), is(2L));
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidFormatterClass() throws IOException {
        SudachiCommandLine.main(new String[] { "-p", temporaryFolderName, "--format", "blahblah", "-o", outputFileName,
                inputFileName });

    }
}
