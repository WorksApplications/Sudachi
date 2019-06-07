/*
 * Copyright (c) 2019 Works Applications Co., Ltd.
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

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Utils {
    public static void copyResource(Path folder, String... files) throws IOException {
        for (String file : files) {
            try {
                URL src = Utils.class.getResource(file);
                Path dest = Paths.get(src.toURI()).getFileName();
                Files.copy(src.openStream(), folder.resolve(dest));
            } catch (URISyntaxException e) {
                throw new IOException(e);
            }
        }
    }

    public static String readAllResource(String file) throws IOException {
        try (InputStream src = Utils.class.getResourceAsStream(file)) {
            return JapaneseDictionary.readAll(src);
        }
    }
}
