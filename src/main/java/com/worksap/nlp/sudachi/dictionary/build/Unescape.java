/*
 * Copyright (c) 2022 Works Applications Co., Ltd.
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

package com.worksap.nlp.sudachi.dictionary.build;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Unescape {
    private static final Pattern unicodeLiteral = Pattern.compile("\\\\u(?:[0-9a-fA-F]{4}|\\{[0-9a-fA-F]+})");

    /**
     * Resolve unicode escape sequences in the string
     * <p>
     * Sequences are defined to be:
     * <ul>
     * <li>\\u0000-\\uFFFF: exactly four hexadecimal characters preceded by \\u</li>
     * <li>\\u{...}: a correct unicode character inside brackets</li>
     * </ul>
     *
     * @param text
     *            to to resolve sequences
     * @return string with unicode escapes resolved
     */
    public static String unescape(String text) {
        Matcher m = unicodeLiteral.matcher(text);
        if (!m.find()) {
            return text;
        }

        StringBuilder sb = new StringBuilder(text.length());
        int start = 0;
        do {
            int pos = m.start();
            int textStart = pos + 2;
            int textEnd = m.end();
            if (text.charAt(textStart) == '{') {
                textStart += 1;
                textEnd -= 1;
            }
            sb.append(text, start, m.start());
            // in future use zero-copying API when using JDK 9+
            String hexCodepoint = text.substring(textStart, textEnd);
            sb.appendCodePoint(Integer.parseInt(hexCodepoint, 16));
            start = m.end();
        } while (m.find());
        sb.append(text, start, text.length());
        return sb.toString();
    }
}
