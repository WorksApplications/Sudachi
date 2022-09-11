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

import java.io.IOException;
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
 *     "class" : "com.worksap.nlp.sudachi.SurfaceFormatter",
 *     "delimiter" : " ",
 *     "eos" : "\n",
 * }
 * }
 * </pre>
 *
 * {@code delimiter} is the delimiter of the morphemes. {@code eos} is printed
 * at the position of EOS.
 */
public class WordSegmentationFormatter extends MorphemeFormatterPlugin {

    @Override
    public void setUp() throws IOException {
        super.setUp();
        delimiter = settings.getString("delimiter", " ");
        eosString = settings.getString("eos", "\n");
    }

    @Override
    public String formatMorpheme(Morpheme morpheme) {
        return morpheme.surface();
    }

    @Override
    void printSentence(List<Morpheme> sentence, PrintStream output) {
        boolean isFirst = true;
        for (Morpheme m : sentence) {
            String morpheme = formatMorpheme(m);
            if (morpheme.equals("")) {
                continue;
            }
            if (morpheme.equals(delimiter)) {
                continue;
            }
            if (isFirst) {
                isFirst = false;
            } else {
                output.print(delimiter);
            }
            output.print(morpheme);
        }
        output.print(eosString);
    }
}
