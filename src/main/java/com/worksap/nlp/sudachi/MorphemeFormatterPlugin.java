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

import java.io.PrintStream;
import java.util.List;

/**
 * Provides a formatter for {@link Morpheme}
 *
 * <p>
 * The following is an example of settings.
 * 
 * <pre>
 * {@code
 *   {
 *     "class"   : "com.worksap.nlp.sudachi.MorphemeFormatterPlugin",
 *     "delimiter"  : "\n",
 *     "eos" : "\nEOS\n",
  *   }
 * }
 * </pre>
 *
 * {@code delimiter} is the delimiter of the morphemes. {@code eos} is printed
 * at the position of EOS.
 */
public abstract class MorphemeFormatterPlugin extends Plugin {

    String delimiter;
    String eosString;
    boolean showDetails;

    /**
     * Set up the plugin.
     *
     * {@link SudachiCommandLine} calls this method for setting up this plugin.
     */
    public void setUp() {
        delimiter = settings.getString("delimiter", "\n");
        eosString = settings.getString("eos", "\nEOS\n");
        showDetails = false;
    }

    /**
     * Provides a string representation of a morpheme.
     *
     * @param morpheme
     *            the input
     * 
     * @return a string representation of a morpheme.
     */
    public abstract String formatMorpheme(Morpheme morpheme);

    /**
     * Show details.
     *
     * This method is called when the {@code -a} option is specified.
     */
    public void showDetails() {
        showDetails = true;
    }

    void printSentence(List<Morpheme> sentence, PrintStream output) {
        boolean isFirst = true;
        for (Morpheme m : sentence) {
            if (isFirst) {
                isFirst = false;
            } else {
                output.print(delimiter);
            }
            output.print(formatMorpheme(m));
        }
        output.print(eosString);
    }
}
