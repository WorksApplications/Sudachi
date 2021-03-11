/*
 * Copyright (c) 2020 Works Applications Co., Ltd.
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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.worksap.nlp.sudachi.dictionary.Grammar;

/**
 * A plugin that rewrites the characters of input texts.
 *
 * <p>
 * This plugin rewites the characters by the user-defined rules, converts them
 * to lower case by {@link Character#toLowerCase} and normalized by
 * {@link java.text.Normalizer#normalize} with {@code NFKC}.
 *
 * <p>
 * {@link Dictionary} initialize this plugin with {@link Settings}. It can be
 * refered as {@link Plugin#settings}.
 *
 * <p>
 * The following is an example of settings.
 *
 * <pre>
 * {@code
 *   {
 *     "class" : "com.worksap.nlp.sudachi.DefaultInputTextPlugin",
 *     "rewriteDef" : "rewrite.def"
 *   }
 * }
 * </pre>
 *
 * {@code rewriteDef} is the file path of the rules of character rewriting. If
 * {@code rewirteDef} is not defined, this plugin uses the default rules.
 *
 * <p>
 * The following is an example of rewriting rules.
 *
 * <pre>
 * {@code
 * # single code point: this character is skipped in character normalization
 * 髙
 * # rewrite rule: <target> <replacement>
 * A' Ā
 * }
 * </pre>
 */
class DefaultInputTextPlugin extends InputTextPlugin {

    /** the file path of the rules */
    String rewriteDef;

    private Set<Integer> ignoreNormalizeSet = new HashSet<>();
    private Map<Character, Integer> keyLengths = new HashMap<>();
    private Map<String, String> replaceCharMap = new HashMap<>();

    /**
     * Reads the rewriting rules from the specified file.
     *
     * @throws IOException
     *             if the file is not available.
     */
    @Override
    public void setUp(Grammar grammar) throws IOException {
        if (rewriteDef == null) {
            rewriteDef = settings.getPath("rewriteDef");
        }

        InputStream is;
        if (rewriteDef != null) {
            is = new FileInputStream(rewriteDef);
        } else {
            is = DefaultInputTextPlugin.class.getClassLoader().getResourceAsStream("rewrite.def");
        }
        if (is == null) {
            throw new IOException("rewriteDef is not defined");
        }
        readRewriteLists(is);
    }

    @Override
    public void rewrite(InputTextBuilder builder) {
        int offset = 0;
        int nextOffset = 0;
        String text = builder.getText();
        textloop: for (int i = 0; i < text.length(); i = text.offsetByCodePoints(i, 1)) {
            offset += nextOffset;
            nextOffset = 0;
            // 1. replace char without normalize
            for (int l = Math.min(keyLengths.getOrDefault(text.charAt(i), 0), text.length() - i); l > 0; l--) {
                String replace = replaceCharMap.get(text.substring(i, i + l));
                if (replace != null) {
                    builder.replace(i + offset, i + l + offset, replace);
                    nextOffset += replace.length() - l;
                    i += l - 1;
                    continue textloop;
                }
            }

            // 2. normalize
            int original = text.codePointAt(i);
            int charLength = text.offsetByCodePoints(i, 1) - i;

            // 2-1. capital alphabet (not only latin but greek, cyrillic, etc) -> small
            int lower = Character.toLowerCase(original);
            String replace;
            if (ignoreNormalizeSet.contains(lower)) {
                if (original == lower) {
                    continue;
                }
                replace = new String(Character.toChars(lower));
            } else {
                // 2-2. normalize (except in ignoreNormalize)
                // e.g. full-width alphabet -> half-width / ligature / etc.
                replace = Normalizer.normalize(new String(Character.toChars(lower)), Form.NFKC);
            }
            nextOffset = replace.length() - charLength;
            if (replace.length() != charLength || original != replace.codePointAt(0)) {
                builder.replace(i + offset, i + charLength + offset, replace);
            }
        }
    }

    private void readRewriteLists(InputStream rewriteDef) throws IOException {
        try (InputStreamReader isr = new InputStreamReader(rewriteDef, StandardCharsets.UTF_8);
                LineNumberReader reader = new LineNumberReader(isr)) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.matches("\\s*") || line.startsWith("#")) {
                    continue;
                }
                String[] cols = line.split("\\s+");
                // ignored normalize list
                if (cols.length == 1) {
                    String key = cols[0];
                    if (key.codePointCount(0, key.length()) != 1) {
                        throw new IllegalArgumentException(
                                cols[0] + " is not a character at line " + reader.getLineNumber());
                    }
                    ignoreNormalizeSet.add(key.codePointAt(0));
                }
                // replace char list
                else if (cols.length == 2) {
                    if (replaceCharMap.containsKey(cols[0])) {
                        throw new IllegalArgumentException(
                                cols[0] + " is already defined at line " + reader.getLineNumber());
                    }
                    if (keyLengths.getOrDefault(cols[0].charAt(0), -1) < cols[0].length()) {
                        // store the longest key length
                        keyLengths.put(cols[0].charAt(0), cols[0].length());
                    }
                    replaceCharMap.put(cols[0], cols[1]);
                } else {
                    throw new IllegalArgumentException("invalid format at line " + reader.getLineNumber());
                }
            }
        }
    }
}
