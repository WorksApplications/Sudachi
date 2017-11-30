/*
 * Copyright (c) 2017 Works Applications Co., Ltd.
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

        import java.util.Set;

/**
 * A plugin that rewrites the Katakana-Hiragana Prolonged Sound Mark (Chōonpu) and similar symbols.
 *
 * <p>This plugin combines the continuous sequence of prolonged sound marks to 1 character.
 *
 * <p>{@link Dictionary} initialize this plugin with {@link Settings}.
 * It can be referred as {@link Plugin#settings}.
 *
 * <p>The following is an example of settings.
 * <pre>{@code
 *   {
 *     "class" : "com.worksap.nlp.sudachi.ProlongedSoundMarkInputTextPlugin",
 *   }
 * }</pre>
 */
class ProlongedSoundMarkTextPlugin extends InputTextPlugin {

    char prolongedSoundMark = 'ー';

    @Override
    public void rewrite(InputTextBuilder<?> builder) {
        String text = builder.getText();

        int n = text.length();
        int markStartIndex = n;
        boolean isProlongedSoundMark = false;
        for (int i = 0; i < n; i++) {
            char c = text.charAt(i);
            if (!isProlongedSoundMark && c == prolongedSoundMark) {
                isProlongedSoundMark = true;
                markStartIndex = i;
            }
            else if (isProlongedSoundMark && c != prolongedSoundMark) {
                if ((i - markStartIndex) > 1) {
                    builder.replace(markStartIndex, i, "ー");
                }
                isProlongedSoundMark = false;
            }
        }
        if (isProlongedSoundMark && (n - markStartIndex) > 1) {
            builder.replace(markStartIndex, n, "ー");
        }
    }
}
