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

package com.worksap.nlp.sudachi.sentdetect;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SentenceDetector {

    public interface NonBreakCheker {
        boolean hasNonBreakWord(int eos);
    }

    static private String PERIODS = "。|？|！|♪|…|\\?|\\!";
    static private String DOT = "(\\.|．)";
    static private String CDOT = "・";
    static private String COMMA = "(,|，|、)";
    static private String BR_TAG = "(<br>|<BR>){2,}";
    static private String ALPHABET_OR_NUMBER = "[a-z]|[A-Z]|[0-9]|[ａ-ｚ]|[Ａ-Ｚ]|[０-９]|〇|一|二|三|四|五|六|七|八|九|十|百|千|万|億|兆";
    static private Pattern SENTENCE_BREAKER = Pattern
            .compile("(" + PERIODS + "|" + CDOT + "{3,}+|((?<!(" + ALPHABET_OR_NUMBER + "))" + DOT + "(?!("
                    + ALPHABET_OR_NUMBER + "|" + COMMA + "))))(" + DOT + "|" + PERIODS + ")*|" + BR_TAG);

    static private String OPEN_PARENTHESIS = "\\(|\\{|｛|\\[|（|「|【|『|［|≪|〔|“";
    static private String CLOSE_PARENTHESIS = "\\)|\\}|\\]|）|」|｝|】|』|］|〕|≫|”";
    static private Pattern PARENTHESIS = Pattern.compile("(" + OPEN_PARENTHESIS + ")|(" + CLOSE_PARENTHESIS + ")");

    static private String ITEMIZE_HEADER = "(" + ALPHABET_OR_NUMBER + ")" + "(" + DOT + ")";

    static final int DEFAULT_LIMIT = 4096;

    private int limit;

    public SentenceDetector() {
        this(-1);
    }

    public SentenceDetector(int limit) {
        this.limit = (limit > 0) ? limit : DEFAULT_LIMIT;
    }

    public int getEOS(String input, NonBreakCheker checker) {
        if (input.isEmpty()) {
            return 0;
        }

        String s = (input.length() > limit) ? input.substring(0, limit) : input;
        Matcher matcher = SENTENCE_BREAKER.matcher(s);
        while (matcher.find()) {
            int eos = matcher.end();
            if (parenthesisLevel(s.substring(0, eos)) == 0) {
                if (eos < s.length()) {
                    eos += prohibitedBOS(s.substring(eos));
                }
                if (s.substring(0, eos).matches(ITEMIZE_HEADER)) {
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

        final Pattern SPACES = Pattern.compile(".+\\s+");
        Matcher m = SPACES.matcher(s);
        if (m.find()) {
            return m.end();
        }

        return Math.min(input.length(), limit);
    }

    int parenthesisLevel(String s) {
        Matcher matcher = PARENTHESIS.matcher(s);
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

    int prohibitedBOS(String s) {
        final Pattern PROHIBITED_BOS = Pattern.compile("\\A(" + CLOSE_PARENTHESIS + "|" + COMMA + "|" + PERIODS + ")+");
        Matcher m = PROHIBITED_BOS.matcher(s);
        return (m.find()) ? m.end() : 0;
    }

    boolean isContinuousPhrase(String s, int eos) {
        final Pattern QUOTE_MARKER = Pattern.compile("(！|？|\\!|\\?|" + CLOSE_PARENTHESIS + ")(と|っ|です)");
        Matcher m = QUOTE_MARKER.matcher(s);
        if (m.find(eos - 1) && m.start() == eos - 1) {
            return true;
        }

        final Pattern EOS_ITEMIZE_HEADER = Pattern.compile(ITEMIZE_HEADER + "\\z");
        char c = s.charAt(eos);
        if ((c == 'と' || c == 'や' || c == 'の') && EOS_ITEMIZE_HEADER.matcher(s.substring(0, eos)).find()) {
            return true;
        }

        return false;
    }
}