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

import org.apache.lucene.analysis.Tokenizer;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.IndexSettings;
import org.elasticsearch.index.analysis.AbstractTokenizerFactory;

import com.worksap.nlp.lucene.sudachi.ja.SudachiTokenizer;
import com.worksap.nlp.lucene.sudachi.ja.SudachiTokenizer.Mode;

public class SudachiTokenizerFactory extends AbstractTokenizerFactory {
    private final Mode mode;
    private final boolean discardPunctuation;
    private final String resourcesPath;
    private final String settingsPath;
    

    public SudachiTokenizerFactory(IndexSettings indexSettings,
            Environment env, String name, Settings settings) throws IOException {
        super(indexSettings, name, settings);
        mode = getMode(settings);
        discardPunctuation = settings.getAsBoolean("discard_punctuation", true);
        resourcesPath = new SudachiPathResolver(env.configFile().toString(), 
                settings.get("resources_path")).resolvePathForDirectory();
        settingsPath = new SudachiSettingsReader(env.configFile().toString(), 
                settings.get("settings_path")).read();
    }

    public static SudachiTokenizer.Mode getMode(Settings settings) {
        SudachiTokenizer.Mode mode = SudachiTokenizer.DEFAULT_MODE;
        String modeSetting = settings.get("mode", null);
        if (modeSetting != null) {
            if ("search".equalsIgnoreCase(modeSetting)) {
                mode = SudachiTokenizer.Mode.SEARCH;
            } else if ("normal".equalsIgnoreCase(modeSetting)) {
                mode = SudachiTokenizer.Mode.NORMAL;
            } else if ("extended".equalsIgnoreCase(modeSetting)) {
                mode = SudachiTokenizer.Mode.EXTENDED;
            }
        }
        return mode;
    }

    @Override
    public Tokenizer create() {
        SudachiTokenizer t = null;
        try {
            t = new SudachiTokenizer(discardPunctuation, mode, resourcesPath, settingsPath);
        } catch (IOException e) {
            throw new ElasticsearchException("fail to make SudachiTokenizer", e);
        }
        return t;
    }

}
