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

package com.worksap.nlp.elasticsearch.sudachi.index;

import java.io.IOException;
import java.util.Set;

import org.apache.lucene.analysis.CharArraySet;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.IndexSettings;
import org.elasticsearch.index.analysis.AbstractIndexAnalyzerProvider;
import org.elasticsearch.index.analysis.Analysis;

import com.worksap.nlp.lucene.sudachi.ja.SudachiAnalyzer;
import com.worksap.nlp.lucene.sudachi.ja.SudachiTokenizer;

public class SudachiAnalyzerProvider extends
        AbstractIndexAnalyzerProvider<SudachiAnalyzer> {

    private final SudachiAnalyzer analyzer;

    public SudachiAnalyzerProvider(IndexSettings indexSettings,
            Environment env, String name, Settings settings) throws IOException {
        super(indexSettings, name, settings);
        final Set<?> stopWords = Analysis.parseStopWords(env, settings,
                SudachiAnalyzer.getDefaultStopSet());
        final SudachiTokenizer.Mode mode = SudachiTokenizerFactory
                .getMode(settings);
        final String resourcesPath = new SudachiPathResolver(env.configFile()
                .toString(), settings.get("resources_path"))
                .resolvePathForDirectory();
        final String settingsPath = new SudachiSettingsReader(env.configFile()
                .toString(), settings.get("settings_path")).read();
        analyzer = new SudachiAnalyzer(mode, resourcesPath, settingsPath,
                CharArraySet.copy(stopWords),
                SudachiAnalyzer.getDefaultStopTags());
    }

    @Override
    public SudachiAnalyzer get() {
        return this.analyzer;
    }

}
