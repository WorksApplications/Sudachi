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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Test;

import com.worksap.nlp.sudachi.dictionary.Grammar;

public class InhibitConnectionPluginTest {

    @Test
    public void edit() {
        short left = 0;
        short right = 0;
        MockGrammar grammar = new MockGrammar();
        InhibitConnectionPlugin plugin = new InhibitConnectionPlugin();
        plugin.inhibitedPairs = Collections.singletonList(Arrays.asList((int) left, (int) right));
        plugin.edit(grammar);
        assertThat(grammar.getConnectCost(left, right), is(Grammar.INHIBITED_CONNECTION));
    }

}