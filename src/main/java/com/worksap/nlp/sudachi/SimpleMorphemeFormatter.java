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
import java.util.Arrays;

/**
 * Provides a formatter for {@link Morpheme}
 *
 * <p>
 * The following is an example of settings.
 * 
 * <pre>
 * {@code
 *   {
 *     "class"   : "com.worksap.nlp.sudachi.SimpleFormatter",
 *     "delimiter"  : "\n",
 *     "eos" : "\nEOS\n",
 *     "columnDelimiter" : "\t"
 *   }
 * }
 * </pre>
 *
 * {@code delimiter} is the delimiter of the morphemes. {@code eos} is printed
 * at the position of EOS. {@code columnDelimiter} is the delimiter of the
 * fields.
 */
public class SimpleMorphemeFormatter extends MorphemeFormatterPlugin {

    protected String columnDelimiter;

    @Override
    public void setUp() throws IOException {
        super.setUp();
        columnDelimiter = settings.getString("columnDelimiter", "\t");
    }

    @Override
    public String formatMorpheme(Morpheme morpheme) {
        String output = morpheme.surface() + columnDelimiter + String.join(",", morpheme.partOfSpeech())
                + columnDelimiter + morpheme.normalizedForm();
        if (showDetails) {
            output += columnDelimiter + morpheme.dictionaryForm() + columnDelimiter + morpheme.readingForm()
                    + columnDelimiter + morpheme.getDictionaryId() + columnDelimiter
                    + Arrays.toString(morpheme.getSynonymGroupIds()) + columnDelimiter
                    + ((morpheme.isOOV()) ? "(OOV)" : "");
        }
        return output;
    }
}
