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
import org.apache.lucene.analysis.miscellaneous.SetKeywordMarkerFilter;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.KeywordAttribute;

import com.worksap.nlp.lucene.sudachi.ja.tokenattribute.BaseFormAttribute;

/**
 * Replaces term text with the {@link BaseFormAttribute}.
 * <p>
 * This acts as a lemmatizer for verbs and adjectives.
 * <p>
 * To prevent terms from being stemmed use an instance of
 * {@link SetKeywordMarkerFilter} or a custom {@link TokenFilter} that sets the
 * {@link KeywordAttribute} before this {@link TokenStream}.
 * </p>
 */
public final class SudachiBaseFormFilter extends TokenFilter {
    private final CharTermAttribute termAtt;
    private final BaseFormAttribute basicFormAtt;
    private final KeywordAttribute keywordAtt;

    public SudachiBaseFormFilter(TokenStream input) {
        super(input);
        termAtt = addAttribute(CharTermAttribute.class);
        basicFormAtt = addAttribute(BaseFormAttribute.class);
        keywordAtt = addAttribute(KeywordAttribute.class);
    }

    @Override
    public boolean incrementToken() throws IOException {
        if (input.incrementToken()) {
            if (!keywordAtt.isKeyword()) {
                String baseForm = basicFormAtt.getBaseForm();
                if (baseForm != null) {
                    termAtt.setEmpty().append(baseForm);
                }
            }
            return true;
        } else {
            return false;
        }
    }
}
