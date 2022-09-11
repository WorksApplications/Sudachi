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

import com.worksap.nlp.sudachi.dictionary.Grammar;
import com.worksap.nlp.sudachi.dictionary.POS;
import com.worksap.nlp.sudachi.dictionary.WordInfo;

import java.util.List;

/**
 * Provides the OOVs which consists of a maximum run of characters of a single
 * character class. Does not produce OOVs if there was any other word at the
 * boundary.
 *
 * <p>
 * The following is an example of settings.
 * 
 * <pre>
 * {@code
 *   {
 *     "class"   : "com.worksap.nlp.sudachi.SimpleOovProviderPlugin",
 *     "oovPOS"  : [ "補助記号", "一般", "*", "*", "*", "*" ],
 *     "leftId"  : 5968,
 *     "rigthId" : 5968,
 *     "cost"    : 3857
 * }
 * }
 * </pre>
 *
 * {@code oovPOS} is the part of speech of the OOVs. {@code leftId} is the
 * left-ID of the OOVs. {@code rightId} is the right-ID of the OOVs.
 * {@code cost} is the cost of the OOVs.
 */
class SimpleOovProviderPlugin extends OovProviderPlugin {

    short oovPOSId;
    short leftId;
    short rightId;
    short cost;

    @Override
    public void setUp(Grammar grammar) {
        POS pos = new POS(settings.getStringList("oovPOS"));
        leftId = (short) settings.getInt("leftId");
        rightId = (short) settings.getInt("rightId");
        cost = (short) settings.getInt("cost");
        String userPosMode = settings.getString(USER_POS, USER_POS_FORBID);
        oovPOSId = posIdOf(grammar, pos, userPosMode);
    }

    @Override
    public int provideOOV(InputText inputText, int offset, long otherWords, List<LatticeNodeImpl> nodes) {
        if (otherWords == 0) {
            LatticeNodeImpl node = createNode();
            node.setParameter(leftId, rightId, cost);
            int length = inputText.getWordCandidateLength(offset);
            String s = inputText.getSubstring(offset, offset + length);
            WordInfo info = new WordInfo(s, (short) length, oovPOSId, s, s, "");
            node.setWordInfo(info);
            nodes.add(node);
            return 1;
        } else {
            return 0;
        }
    }
}
