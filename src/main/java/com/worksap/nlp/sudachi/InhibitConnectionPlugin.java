/*
 * Copyright (c) 2019 Works Applications Co., Ltd.
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

import java.util.List;

import com.worksap.nlp.sudachi.dictionary.Grammar;

/**
 * A Plugin for inhibiting the connections.
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
 *     "class" : "com.worksap.nlp.sudachi.InhibitConnectionPlugin",
 *     "inhibitedPair" : [ [ 0, 233 ], [435, 332] ]
 *   }
 * }
 * </pre>
 *
 * {@code inhibitPair} is a list of lists of two numbers. At each pair, the
 * first number is right-ID of the left node and the second is left-ID of the
 * right node in a connection.
 */
class InhibitConnectionPlugin extends EditConnectionCostPlugin {

    List<List<Integer>> inhibitedPairs;

    @Override
    public void setUp(Grammar grammar) {
        inhibitedPairs = settings.getIntListList("inhibitedPair");
    }

    @Override
    public void edit(Grammar grammar) {
        for (List<Integer> pair : inhibitedPairs) {
            if (pair.size() < 2) {
                continue;
            }
            inhibitConnection(grammar, pair.get(0).shortValue(), pair.get(1).shortValue());
        }
    }
}
