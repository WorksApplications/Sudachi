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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.util.ResourceLoader;
import org.apache.lucene.analysis.util.ResourceLoaderAware;
import org.apache.lucene.analysis.util.TokenFilterFactory;

public class SudachiPartOfSpeechStopFilterFactory extends TokenFilterFactory
        implements ResourceLoaderAware {
    private final String stopTagFiles;
    private Set<String> stopTags;

    public SudachiPartOfSpeechStopFilterFactory(Map<String, String> args) {
        super(args);
        stopTagFiles = get(args, "tags");
        if (args.containsKey("enablePositionIncrements")) {
            throw new IllegalArgumentException(
                    "enablePositionIncrements is not a valid option as of Lucene 5.0");
        }
        if (!args.isEmpty()) {
            throw new IllegalArgumentException("Unknown parameters: " + args);
        }
    }

    public void inform(ResourceLoader loader) throws IOException {
        stopTags = null;
        CharArraySet cas = getWordSet(loader, stopTagFiles, false);
        if (cas != null) {
            stopTags = new HashSet<String>();
            for (Object element : cas) {
                char chars[] = (char[]) element;
                stopTags.add(new String(chars));
            }
        }
    }

    @Override
    public TokenStream create(TokenStream stream) {
        // if stoptags is null, it means the file is empty
        if (stopTags != null) {
            return new SudachiPartOfSpeechStopFilter(stream, stopTags);
        } else {
            return stream;
        }
    }
}
