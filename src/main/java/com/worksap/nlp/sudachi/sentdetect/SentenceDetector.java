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

package com.worksap.nlp.sudachi.sentdetect;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A sentence boundary detector.
 */
public class SentenceDetector {

    /**
     * A checher for words that cross boundaries.
     */
    public interface NonBreakCheker {
        /**
         * Returns whether there is a word that crosses the boundary.
         *
         * @param eos
         *            the index of the detected boundary
         * @return {@code true} if, and only if there is a word that crosses the
         *         boundary
         */
        boolean hasNonBreakWord(int eos);
    }

    private static final String PERIODS = "。？！♪…\\?\\!";
    private static final String DOT = "\\.．";
    private static final String CDOT = "・";
    private static final String COMMA = ",，、";
    private static final String BR_TAG = "(<br>|<BR>){2,}";
    private static final String ALPHABET_OR_NUMBER = "a-zA-Z0-9ａ-ｚＡ-Ｚ０-９〇一二三四五六七八九十百千万億兆";
    private static final Pattern SENTENCE_BREAKER_PATTERN = Pattern
            .compile("([" + PERIODS + "]|" + CDOT + "{3,}+|((?<!([" + ALPHABET_OR_NUMBER + "]))[" + DOT + "](?!(["
                    + ALPHABET_OR_NUMBER + COMMA + "]))))([" + DOT + PERIODS + "])*|" + BR_TAG);

    private static final String OPEN_PARENTHESIS = "\\(\\{｛\\[（「【『［≪〔“";
    private static final String CLOSE_PARENTHESIS = "\\)\\}\\]）」｝】』］〕≫”";

    private static final String ITEMIZE_HEADER = "([" + ALPHABET_OR_NUMBER + "])" + "([" + DOT + "])";
    private static final Pattern ITEMIZE_HEADER_PATTERN = Pattern.compile(ITEMIZE_HEADER);

    /** the default maximum length of a sentence */
    public static final int DEFAULT_LIMIT = 4096;

    private int limit;

    /**
     * Initialize a newly created {@code SentenceDetector} object.
     */
    public SentenceDetector() {
        this(-1);
    }

    /**
     * Constructs a new {@code SentenceDetector} with length limitation of sentence.
     *
     * @param limit
     *            the maximum length of a sentence
     */
    public SentenceDetector(int limit) {
        this.limit = (limit > 0) ? limit : DEFAULT_LIMIT;
    }

    /**
     * Returns the index of the detected end of the sentence.
     *
     * If {@code checker} is not @{code null}, it is used to determine if there is a
     * word that crosses the detected boundary, and if so, the next boundary is
     * returned.
     *
     * If there is no boundary, it returns a relatively harmless boundary as a
     * negative value.
     *
     * @param input
     *            text
     * @param checker
     *            a checher for words that cross boundaries
     * @return the index of the end of the sentence
     */
    public int getEos(CharSequence input, NonBreakCheker checker) {
        if (input.length() == 0) {
            return 0;
        }

        CharSequence s = (input.length() > limit) ? input.subSequence(0, limit) : input;
        Matcher matcher = SENTENCE_BREAKER_PATTERN.matcher(s);
        while (matcher.find()) {
            int eos = matcher.end();
            if (parenthesisLevel(s.subSequence(0, eos)) == 0) {
                if (eos < s.length()) {
                    eos += prohibitedBOS(s.subSequence(eos, s.length()));
                }
                if (ITEMIZE_HEADER_PATTERN.matcher(s.subSequence(0, eos)).matches()) {
                    continue;
                }
                if (eos < s.length() && isContinuousPhrase(s, eos)) {
                    continue;
                }
                if (checker != null && checker.hasNonBreakWord(eos)) {
                    continue;
                }
                return eos;
            }
        }

        final Pattern spaces = Pattern.compile(".+\\s+");
        Matcher m = spaces.matcher(s);
        if (m.find()) {
            return -m.end();
        }

        return -Math.min(input.length(), limit);
    }

    private static final Pattern PARENTHESIS_PATTERN = Pattern
            .compile("([" + OPEN_PARENTHESIS + "])|([" + CLOSE_PARENTHESIS + "])");

    int parenthesisLevel(CharSequence s) {
        Matcher matcher = PARENTHESIS_PATTERN.matcher(s);
        int level = 0;
        while (matcher.find()) {
            if (matcher.group(1) != null) { // open
                level++;
            } else {
                level--;
            }
            if (level < 0) {
                level = 0;
            }
        }
        return level;
    }

    private static final Pattern PROHIBITED_BOS_PATTERN = Pattern
            .compile("\\A([" + CLOSE_PARENTHESIS + COMMA + PERIODS + "])+");

    int prohibitedBOS(CharSequence s) {
        Matcher m = PROHIBITED_BOS_PATTERN.matcher(s);
        return (m.find()) ? m.end() : 0;
    }

    private static final Pattern QUOTE_MARKER_PATTERN = Pattern
            .compile("(！|？|\\!|\\?|[" + CLOSE_PARENTHESIS + "])(と|っ|です)");
    private static final Pattern EOS_ITEMIZE_HEADER_PATTERN = Pattern.compile(ITEMIZE_HEADER + "\\z");

    boolean isContinuousPhrase(CharSequence s, int eos) {
        Matcher m = QUOTE_MARKER_PATTERN.matcher(s);
        if (m.find(eos - 1) && m.start() == eos - 1) {
            return true;
        }

        char c = s.charAt(eos);
        return (c == 'と' || c == 'や' || c == 'の') && EOS_ITEMIZE_HEADER_PATTERN.matcher(s.subSequence(0, eos)).find();
    }
}
