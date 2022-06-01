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

package com.worksap.nlp.sudachi;

import com.worksap.nlp.sudachi.dictionary.Grammar;
import com.worksap.nlp.sudachi.dictionary.POS;
import com.worksap.nlp.sudachi.dictionary.WordInfo;

import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Provides OOVs based on a regular expression. Does not produce OOVs if one
 * with the same boundaries was already present in the dictionary or was
 * produced by a previous OOV provider.
 *
 * <p>
 * Configuration example:
 * 
 * <pre>
 * {@code
 *  {
 *      "class": "com.worksap.nlp.sudachi.RegexOovProvider",
 *      "regex": "[0-9a-z-]+",
 *      "pos": [ "補助記号", "一般", "*", "*", "*", "*" ],
 *      "leftId": 500,
 *      "rightId": 500,
 *      "cost": 5000
 *  }
 * }
 * </pre>
 *
 * Recommendations on regular expressions:
 * <ul>
 * <li>Make regex as simple as possible, it will be evaluated many times</li>
 * <li>Do not use capturing groups {@code (test)}, instead use non-capturing
 * groups {@code (?:test)}</li>
 * <li>Do not use lookahead and lookaround</li>
 * </ul>
 */
public class RegexOovProvider extends OovProviderPlugin {
    private Pattern pattern;
    private short posId = -1;
    private short cost = Short.MIN_VALUE;
    private short leftId = Short.MIN_VALUE;
    private short rightId = Short.MIN_VALUE;

    @Override
    public void setUp(Grammar grammar) throws IOException {
        super.setUp(grammar);
        POS stringPos = new POS(settings.getStringList("pos"));
        posId = grammar.getPartOfSpeechId(stringPos);
        if (posId == -1) {
            throw new IllegalArgumentException("POS " + stringPos + " was not present in the dictionary");
        }
        cost = checkedShort(settings, "cost");
        leftId = checkedShort(settings, "leftId");
        rightId = checkedShort(settings, "rightId");
        pattern = checkPattern(settings.getString("regex"));
        // force compilation of the pattern here
        pattern.matcher("test").reset();
    }

    @Override
    public int provideOOV(InputText inputText, int offset, long otherWords, List<LatticeNodeImpl> nodes) {
        String text = inputText.getText();
        Matcher matcher = pattern.matcher(text);
        byte[] byteText = inputText.getByteText();
        int textLength = byteText.length;
        int regionStartChars = inputText.modifiedOffset(offset);
        int regionEndBytes = Math.min(offset + 64, textLength);
        int regionEndChars = inputText.modifiedOffset(regionEndBytes);
        matcher.region(regionStartChars, regionEndChars);

        if (matcher.find()) {
            int endChar = matcher.end();
            int oovLength = inputText.getCodePointsOffsetLength(offset, endChar - regionStartChars);
            if (WordMask.hasNth(otherWords, oovLength)) {
                return 0;
            }

            LatticeNodeImpl node = new LatticeNodeImpl(null, leftId, rightId, cost, -1);
            node.setOOV();
            String oov = text.substring(matcher.start(), matcher.end());
            WordInfo info = new WordInfo(oov, (short) oovLength, posId, oov, oov, "");
            node.setWordInfo(info);
            nodes.add(node);
            return 1;
        } else {
            return 0;
        }
    }

    private static short checkedShort(Settings settings, String name) {
        int value = settings.getInt(name);
        if (value > Short.MAX_VALUE) {
            throw new IllegalArgumentException(
                    "The value of parameter " + name + " was larger than " + Short.MAX_VALUE);
        }
        return (short) value;
    }

    private static Pattern checkPattern(String regex) {
        // prepend begin of sequence for fast non-matching case
        if (!regex.startsWith("^")) {
            regex = "^" + regex;
        }
        Pattern pattern = Pattern.compile(regex);
        // force compilation of the pattern here no matter the situation
        Matcher x = pattern.matcher("does not matter");
        x.reset();
        return pattern;
    }
}
