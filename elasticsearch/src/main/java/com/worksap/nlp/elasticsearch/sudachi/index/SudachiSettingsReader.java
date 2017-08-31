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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.elasticsearch.ElasticsearchException;

public class SudachiSettingsReader {

    private final String esConfig;
    private final String filePath;

    public SudachiSettingsReader(String esConfig, String filePath) {
        this.esConfig = esConfig;
        this.filePath = filePath;
    }

    public String read() {
        String path = new SudachiPathResolver(esConfig, filePath).resolvePathForFile();
        try {
            if(!path.isEmpty()){
                return new String(Files.readAllBytes(Paths.get(path)),
                        StandardCharsets.UTF_8);
            } else {
                return "";
            }
        } catch (FileNotFoundException e) {
            throw new ElasticsearchException(
                    "Sudachi Settings File not found: '" + path + "'.", e);
        } catch (IOException e) {
            throw new ElasticsearchException(
                    "Fail to load Sudachi Settings File.", e);
        }
    }

}
