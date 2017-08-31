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

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.StopwordAnalyzerBase;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.StopFilter;
import org.elasticsearch.ElasticsearchException;

import com.worksap.nlp.lucene.sudachi.ja.SudachiTokenizer.Mode;

/**
 * Analyzer for Sudachi that uses morphological analysis.
 * 
 * @see SudachiTokenizer
 */
public class SudachiAnalyzer extends StopwordAnalyzerBase {
    private final Mode mode;
    private final String resourcesPath;
    private final String settings;
    private final Set<String> stoptags;

    public SudachiAnalyzer() {
        this(SudachiTokenizer.DEFAULT_MODE, "", DefaultSudachiSettingsReader(),
                DefaultSetHolder.DEFAULT_STOP_SET,
                DefaultSetHolder.DEFAULT_STOP_TAGS);
    }

    public SudachiAnalyzer(Mode mode, String resourcesPath, String settings,
            CharArraySet stopwords, Set<String> stoptags) {
        super(stopwords);
        this.mode = mode;
        this.resourcesPath = resourcesPath;
        this.settings = settings;
        this.stoptags = stoptags;
    }

    public static CharArraySet getDefaultStopSet() {
        return DefaultSetHolder.DEFAULT_STOP_SET;
    }

    public static Set<String> getDefaultStopTags() {
        return DefaultSetHolder.DEFAULT_STOP_TAGS;
    }

    private static class DefaultSetHolder {
        static final CharArraySet DEFAULT_STOP_SET;
        static final Set<String> DEFAULT_STOP_TAGS;

        static {
            try {
                DEFAULT_STOP_SET = loadStopwordSet(true, SudachiAnalyzer.class,
                        "stopwords.txt", "#");
                final CharArraySet tagset = loadStopwordSet(false,
                        SudachiAnalyzer.class, "stoptags.txt", "#");
                DEFAULT_STOP_TAGS = new HashSet<>();
                for (Object element : tagset) {
                    char chars[] = (char[]) element;
                    DEFAULT_STOP_TAGS.add(new String(chars));
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    static String DefaultSudachiSettingsReader() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                SudachiAnalyzer.class
                        .getResourceAsStream("sudachiSettings.json"), StandardCharsets.UTF_8));) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        } catch (FileNotFoundException e) {
            throw new ElasticsearchException(
                    "Sudachi Settings File not Found.", e);
        } catch (IOException e) {
            throw new ElasticsearchException(
                    "Fail to load Sudachi Settings File.", e);
        }
    }

    @Override
    protected TokenStreamComponents createComponents(String fieldName) {
        Tokenizer tokenizer = null;
        try {
            tokenizer = new SudachiTokenizer(true, mode, resourcesPath, settings);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        TokenStream stream = new SudachiBaseFormFilter(tokenizer);
        stream = new SudachiPartOfSpeechStopFilter(stream, stoptags);
        stream = new StopFilter(stream, stopwords);
        return new TokenStreamComponents(tokenizer, stream);
    }
}
