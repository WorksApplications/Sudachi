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
import java.util.Set;
import java.util.List;

import com.worksap.nlp.sudachi.dictionary.Grammar;

/**
 * A plugin that rewrites the Katakana-Hiragana Prolonged Sound Mark (Chōonpu)
 * and similar symbols.
 *
 * <p>
 * This plugin combines the continuous sequence of prolonged sound marks to 1
 * character.
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
 *     "class" : "com.worksap.nlp.sudachi.ProlongedSoundMarkInputTextPlugin",
        "prolongedSoundMarks": ["ー", "〜", "〰"],
        "replacementSymbol": "ー"
 *   }
 * }
 * </pre>
 *
 * {@code prolongedSoundMarks} is the list of symbols to be combined.
 * {@code replacementSymbol} is the symbol for replacement, after combining
 * prolonged sound mark sequences.
 *
 * <p>
 * With above setting example, the plugin rewrites input "エーービ〜〜〜シ〰〰〰〰" to
 * "エービーシー".
 */
class ProlongedSoundMarkInputTextPlugin extends InputTextPlugin {

    private Set<Integer> prolongedSoundMarkSet = new HashSet<>();
    private String replacementSymbol;

    @Override
    public void setUp(Grammar Grammar) throws IOException {
        List<String> prolongedSoundMarkStrings = settings.getStringList("prolongedSoundMarks");
        for (String s : prolongedSoundMarkStrings) {
            prolongedSoundMarkSet.add(s.codePointAt(0));
        }
        replacementSymbol = settings.getString("replacementSymbol");
    }

    @Override
    public void rewrite(InputTextBuilder builder) {
        String text = builder.getText();

        int n = text.length();
        int offset = 0;
        int markStartIndex = n;
        boolean isProlongedSoundMark = false;
        for (int i = 0; i < n; i++) {
            int cp = text.codePointAt(i);
            if (!isProlongedSoundMark && prolongedSoundMarkSet.contains(cp)) {
                isProlongedSoundMark = true;
                markStartIndex = i;
            } else if (isProlongedSoundMark && !prolongedSoundMarkSet.contains(cp)) {
                if ((i - markStartIndex) > 1) {
                    builder.replace(markStartIndex - offset, i - offset, replacementSymbol);
                    offset += i - markStartIndex - 1;
                }
                isProlongedSoundMark = false;
            }
        }
        if (isProlongedSoundMark && (n - markStartIndex) > 1) {
            builder.replace(markStartIndex - offset, n - offset, replacementSymbol);
        }
    }
}
