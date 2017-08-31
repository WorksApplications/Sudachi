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

import java.util.List;
import java.util.Set;

import org.apache.lucene.analysis.FilteringTokenFilter;
import org.apache.lucene.analysis.TokenStream;

import com.worksap.nlp.lucene.sudachi.ja.tokenattribute.PartOfSpeechAttribute;

/**
 * Removes tokens that match a set of part-of-speech tags.
 */
public final class SudachiPartOfSpeechStopFilter extends FilteringTokenFilter {
    private final Set<String> stopTags;
    private final PartOfSpeechAttribute posAtt;

    /**
     * Create a new {@link SudachiPartOfSpeechStopFilter}.
     * 
     * @param input
     *            the {@link TokenStream} to consume
     * @param stopTags
     *            the part-of-speech tags that should be removed
     */
    public SudachiPartOfSpeechStopFilter(TokenStream input, Set<String> stopTags) {
        super(input);
        this.stopTags = stopTags;
        posAtt = addAttribute(PartOfSpeechAttribute.class);
    }

    @Override
    protected boolean accept() {
        final List<String> posList = posAtt.getPartOfSpeechForArray();
        if (posList == null || posList.size() == 0) {
            return true;
        }
        for (String pos : posList) {
            if (stopTags.contains(pos)) {
                return false;
            }
        }
        return true;
    }
}
