/*
 *  Copyright (c) 2017 Works Applications Co., Ltd.
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

package com.worksap.nlp.lucene.sudachi.ja;

import java.io.IOException;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.ja.util.ToStringUtil;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import com.worksap.nlp.lucene.sudachi.ja.tokenattribute.ReadingAttribute;

public final class SudachiReadingFormFilter extends TokenFilter {
    private final CharTermAttribute termAttr;
    private final ReadingAttribute readingAttr;

    private StringBuilder buffer = new StringBuilder();
    private boolean useRomaji;

    public SudachiReadingFormFilter(TokenStream input, boolean useRomaji) {
        super(input);
        this.useRomaji = useRomaji;
        termAttr = addAttribute(CharTermAttribute.class);
        readingAttr = addAttribute(ReadingAttribute.class);
    }

    public SudachiReadingFormFilter(TokenStream input) {
        this(input, false);
    }

    @Override
    public boolean incrementToken() throws IOException {
        if (input.incrementToken()) {
            String reading = readingAttr.getReading();

            if (useRomaji) {
                if (reading == null) {
                    buffer.setLength(0);
                    ToStringUtil.getRomanization(buffer, termAttr);
                    termAttr.setEmpty().append(buffer);
                } else {
                    ToStringUtil.getRomanization(termAttr.setEmpty(), reading);
                }
            } else {
                if (reading != null) {
                    termAttr.setEmpty().append(reading);
                }
            }
            return true;
        } else {
            return false;
        }
    }
}
