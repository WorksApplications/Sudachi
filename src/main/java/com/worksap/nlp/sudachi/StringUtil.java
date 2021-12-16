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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;

public class StringUtil {
    private StringUtil() {
    }

    public static String readFully(URL url) throws IOException {
        URLConnection urlConnection = url.openConnection();
        try (InputStream inputStream = urlConnection.getInputStream()) {
            return readFully(inputStream);
        }
    }

    public static String readFully(InputStream stream) throws IOException {
        InputStreamReader isr = new InputStreamReader(stream, StandardCharsets.UTF_8);
        StringBuilder sb = new StringBuilder();
        CharBuffer cb = CharBuffer.allocate(1024);
        while (isr.read(cb) != -1) {
            cb.flip();
            sb.append(cb);
            cb.compact();
        }
        return sb.toString();
    }
}
