/*
 * Copyright (c) 2017-2022 Works Applications Co., Ltd.
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
import java.nio.file.Paths;

/**
 * Build a {@link Dictionary} instance from a dictionary file.
 */
public class DictionaryFactory {

    /**
     * Creates {@code Dictionary} with default configuration, read from
     * classpath-based sudachi.json file.
     *
     * @return {@link Dictionary}
     * @throws IOException
     *             if reading a file is failed
     */
    public Dictionary create() throws IOException {
        return create(Config.fromClasspath());
    }

    /**
     * Creates {@code Dictionary} from configuration
     *
     * @return {@link Dictionary}
     * @throws IOException
     *             if reading a file is failed
     */
    public Dictionary create(Config config) throws IOException {
        return new JapaneseDictionary(config);
    }

    /**
     * Creates {@code Dictionary} by using configuration from the classpath, merged
     * with the default configuration.
     *
     * @param settings
     *            settings in JSON string
     * @return {@link Dictionary}
     * @throws IOException
     *             if reading a file is failed
     *
     * @deprecated use {@link #create(Config)} overload instead. This overload will
     *             be removed in 1.0.0.
     */
    @Deprecated()
    public Dictionary create(String settings) throws IOException {
        Config defaults = Config.fromClasspath();
        Config passed = Config.fromJsonString(settings, SettingsAnchor.classpath().andThen(SettingsAnchor.none()));
        Config merged = passed.withFallback(defaults);
        return create(merged);
    }

    /**
     * Creates {@code Dictionary} by read a dictionary file.
     *
     * @param path
     *            the base path if "path" is undefined in settings
     * @param settings
     *            settings in JSON string
     * @return {@link Dictionary}
     * @throws IOException
     *             if reading a file is failed
     * @deprecated use {@link #create(Config)} overload instead. This overload will
     *             be removed in 1.0.0.
     */
    @Deprecated
    public Dictionary create(String path, String settings) throws IOException {
        return create(path, settings, false);
    }

    /**
     * Creates {@code Dictionary} by read a dictionary file.
     *
     * @param path
     *            the base path if "path" is undefined in settings
     * @param settings
     *            settings in JSON string. If null, default settings are used
     *            instead.
     * @param mergeSettings
     *            if true, settings is merged with the default settings. if false,
     *            returns the same result as {@link #create(String,String)
     *            create(path, settings)}
     * @return {@link Dictionary}
     * @throws IOException
     *             if reading a file is failed
     * @deprecated use {@link #create(Config)} overload instead. This overload will
     *             be removed in 1.0.0.
     */
    @Deprecated
    public Dictionary create(String path, String settings, boolean mergeSettings) throws IOException {
        Config config;
        SettingsAnchor defaultAnchor = SettingsAnchor.classpath();
        if (path != null) {
            defaultAnchor = SettingsAnchor.filesystem(Paths.get(path)).andThen(defaultAnchor);
        }
        Config defaultConfig = Config
                .fromSettings(Settings.fromClasspath(getClass().getResource("/sudachi.json"), defaultAnchor));
        if (settings == null) {
            config = defaultConfig;
        } else {
            config = Config.fromSettings(Settings.parse(settings, defaultAnchor));
            if (mergeSettings) {
                config = config.withFallback(Config.fromClasspath());
            }
        }

        return create(config);
    }
}
