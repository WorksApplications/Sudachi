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

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.worksap.nlp.sudachi.dictionary.CategoryType;
import com.worksap.nlp.sudachi.dictionary.Grammar;

/**
 * A plugin that ignores the Yomigana written in brackets after the Kanji.
 *
 * <p>
 * {@link Dictionary} initialize this plugin with {@link Settings}. It can be
 * referred as {@link Plugin#settings}.
 *
 * <p>
 * The following is an example of settings.
 *
 * <pre>
 * {@code
 *   {
 *     "class" : "com.worksap.nlp.sudachi.IgnoreYomiganaPlugin",
 *       "leftBrackets": ["(", "（"],
 *       "rightBrackets": [")", "）"]
 *   }
 * }
 * </pre>
 *
 * {@code leftBrackets} is the list of symbols to be used as left bracket.
 * {@code rightBrackets} is the list of symbols to be used as right bracket.
 *
 * <p>
 * With above setting example, the plugin rewrites input "徳島（とくしま）に行(い)く" to
 * "徳島に行く".
 */
class IgnoreYomiganaPlugin extends InputTextPlugin {

    private Set<Integer> leftBracketSet = new HashSet<>();
    private Set<Integer> rightBracketSet = new HashSet<>();
    private Grammar grammar;

    @Override
    public void setUp(Grammar grammar) throws IOException {
        this.grammar = grammar;
        List<String> leftBracketString = settings.getStringList("leftBrackets");
        for (String s : leftBracketString) {
            leftBracketSet.add(s.codePointAt(0));
        }
        List<String> rightBracketString = settings.getStringList("rightBrackets");
        for (String s : rightBracketString) {
            rightBracketSet.add(s.codePointAt(0));
        }
    }

    @Override
    public void rewrite(InputTextBuilder builder) {
        String text = builder.getText();

        int n = text.length();
        int startBracketPoint = -1;
        int offset = 0;
        boolean hasYomigana = false;
        for (int i = 1; i < n; i++) {
            int cp = text.codePointAt(i);

            if (isKanji(text.codePointAt(i - 1)) && leftBracketSet.contains(cp)) {
                startBracketPoint = i;
            } else if (hasYomigana && rightBracketSet.contains(cp)) {
                builder.replace(startBracketPoint - 1 - offset, i + 1 - offset,
                        text.substring(startBracketPoint - 1, startBracketPoint));
                offset += i - startBracketPoint + 1;
                startBracketPoint = -1;
                hasYomigana = false;
            } else if (startBracketPoint != -1) {
                if (isHiragana(cp) || isKatakana(cp)) {
                    hasYomigana = true;
                } else {
                    startBracketPoint = -1;
                    hasYomigana = false;
                }
            }
        }
    }

    private Boolean isKanji(int cp) {
        return grammar.getCharacterCategory().getCategoryTypes(cp).contains(CategoryType.KANJI);
    }

    private Boolean isHiragana(int cp) {
        return grammar.getCharacterCategory().getCategoryTypes(cp).contains(CategoryType.HIRAGANA);
    }

    private Boolean isKatakana(int cp) {
        return grammar.getCharacterCategory().getCategoryTypes(cp).contains(CategoryType.KATAKANA);
    }

}
