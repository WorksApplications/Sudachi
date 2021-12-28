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

import com.worksap.nlp.sudachi.dictionary.BinaryDictionary;
import com.worksap.nlp.sudachi.dictionary.CharacterCategory;

import javax.json.Json;
import javax.json.JsonObject;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Typed configuration for Sudachi. Should be created by static methods
 * ({@link #empty()} or {@code from*()}).
 *
 * There are two main configuration modes: parsing json configuration file and
 * specifying resources in code. Resources can be specified from classpath,
 * filesystem or be pre-created.
 *
 * Resource paths inside the json configuration files will be resolved relative
 * to the configuration file. If the file was in classpath, they will be looked
 * up in classpath or relative to the working directory. If the file was in
 * filesystem, they will be resolved relative to the configuration file only.
 *
 * @see Settings Settings: untyped configuration parsed from json file
 * @see #empty()
 * @see #fromClasspath()
 * @see #fromFile(Path)
 */
public class Config {
    private Resource<BinaryDictionary> systemDictionary;
    private List<Resource<BinaryDictionary>> userDictionary;
    private Resource<CharacterCategory> characterDefinition;
    private List<PluginConf<EditConnectionCostPlugin>> editConnectionCost;
    private List<PluginConf<InputTextPlugin>> inputText;
    private List<PluginConf<OovProviderPlugin>> oovProviders;
    private List<PluginConf<PathRewritePlugin>> pathRewrite;
    private Boolean allowEmptyMorpheme;

    private Config() {
    }

    /**
     * Creates an empty configuration. Useful for building configuration manually
     * instead of loading it from a file.
     * 
     * @return empty Config object
     */
    public static Config empty() {
        return new Config();
    }

    /**
     * Loads configuration from the first instance of {@code sudachi.json} file
     * loaded from classpath.
     * 
     * @return Config object
     * @throws IOException
     *             when IO fails
     */
    public static Config fromClasspath() throws IOException {
        return fromClasspath("sudachi.json");
    }

    /**
     * Loads configuration from the first instance of the json file loaded from
     * classpath. File is loaded by the classloader of the {@code Config} class.
     *
     * @param name
     *            json file name
     * @return Config object
     * @throws IOException
     *             when IO fails
     * @see Settings#fromClasspath(URL, SettingsAnchor)
     */
    public static Config fromClasspath(String name) throws IOException {
        return fromClasspath(name, Config.class.getClassLoader());
    }

    /**
     * Loads configuration from the first instance of the json file loaded from
     * classpath. File is loaded by the provided classloader.
     *
     * @param name
     *            json file name
     * @param loader
     *            classloader to get the resource
     * @return parsed Config
     * @throws IOException
     *             when IO fails
     * @see Settings#fromClasspath(URL, SettingsAnchor)
     */
    public static Config fromClasspath(String name, ClassLoader loader) throws IOException {
        return fromClasspath(loader.getResource(name), loader);
    }

    /**
     * Loads the explicit file from the classpath. Additional files will be loaded
     * by Config classloader.
     *
     * @param resource
     *            URL of the classpath resource
     * @return parsed Config
     * @throws IOException
     *             when IO fails
     */
    public static Config fromClasspath(URL resource) throws IOException {
        return fromClasspath(resource, Config.class.getClassLoader());
    }

    /**
     * Loads the explicit file from the classpath with the provided classloader.
     * 
     * @param resource
     *            URL of the classpath resource
     * @param loader
     *            classloader to load the resource
     * @return parsed Config
     * @throws IOException
     *             when IO fails
     */
    public static Config fromClasspath(URL resource, ClassLoader loader) throws IOException {
        Settings settings = Settings.resolvedBy(SettingsAnchor.classpath(loader)).merge(resource);
        return fromSettings(settings);
    }

    /**
     * Loads the config from the filesystem.
     * 
     * @param path
     *            {@link Path} to the config file
     * @return parsed Config
     * @throws IOException
     *             when IO fails
     */
    public static Config fromFile(Path path) throws IOException {
        return fromSettings(Settings.fromFile(path));
    }

    /**
     * Parses the config fom the provided json string
     * 
     * @param json
     *            configuration as json string
     * @param anchor
     *            how to resolve paths
     * @return parsed Config
     */
    public static Config fromJsonString(String json, SettingsAnchor anchor) {
        return fromSettings(Settings.parse(json, anchor));
    }

    /**
     * Converts untyped {@link Settings} to typed Config
     * 
     * @param obj
     *            untyped configuration
     * @return parsed Config
     */
    public static Config fromSettings(Settings obj) {
        return Config.empty().mergeSettings(obj);
    }

    /**
     * Parses all config files with the specified name in the classpath, merging
     * them. Files are loaded with Config classloader.
     * 
     * @param name
     *            classpath resource name
     * @param mode
     *            how to merge Config objects
     * @return merged Config
     * @throws IOException
     *             when IO fails
     * @see #merge(Config, MergeMode)
     */
    public static Config fromClasspathMerged(String name, MergeMode mode) throws IOException {
        return fromClasspathMerged(Config.class.getClassLoader(), name, mode);
    }

    /**
     * Parses all config files with the specified name in the classpath, merging
     * them. Files are loaded with the provided classloader.
     * 
     * @param classLoader
     *            it will be used to load resources
     * @param name
     *            classpath resource name
     * @param mode
     *            how to merge Config objects
     * @return merged Config
     * @throws IOException
     *             when IO fails
     * @see #merge(Config, MergeMode)
     */
    public static Config fromClasspathMerged(ClassLoader classLoader, String name, MergeMode mode) throws IOException {
        Enumeration<URL> resources = classLoader.getResources(name);
        Config result = Config.empty();
        long count = 0;
        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            result = result.merge(Config.fromClasspath(resource), mode);
            count += 1;
        }
        if (count == 0) {
            throw new IllegalArgumentException(String.format("couldn't find any file %s in classpath", name));
        }
        return result;
    }

    private static <T> List<T> mergeList(MergeMode mode, List<T> dest, List<T> src) {
        if (src != null) {
            if (mode == MergeMode.REPLACE || dest == null) {
                return src;
            } else {
                for (T newItem : src) {
                    if (!dest.contains(newItem)) {
                        dest.add(newItem);
                    }
                }
            }
        }
        return dest;
    }

    private static <T extends Plugin> List<PluginConf<T>> mergePluginList(MergeMode mode, List<PluginConf<T>> dest,
            List<PluginConf<T>> src) {
        if (src != null) {
            if (mode == MergeMode.REPLACE || dest == null) {
                return src;
            } else {
                for (PluginConf<T> newItem : src) {
                    Optional<PluginConf<T>> first = dest.stream()
                            .filter(p -> Objects.equals(p.clazzName, newItem.clazzName)).findFirst();
                    if (first.isPresent()) {
                        PluginConf<T> pconf = first.get();
                        pconf.internal = pconf.internal.merge(newItem.internal);
                    } else {
                        dest.add(newItem);
                    }
                }
            }
        }
        return dest;
    }

    private static <T> T mergeOne(T dest, T src) {
        if (src != null) {
            return src;
        }
        return dest;
    }

    private Config mergeSettings(Settings settings) {

        systemDictionary = settings.getResource("systemDict");
        characterDefinition = settings.getResource("characterDefinitionFile");
        userDictionary = settings.getResourceList("userDict");
        editConnectionCost = settings.getPlugins("editConnectionCostPlugin", EditConnectionCostPlugin.class);
        inputText = settings.getPlugins("inputTextPlugin", InputTextPlugin.class);
        oovProviders = settings.getPlugins("oovProviderPlugin", OovProviderPlugin.class);
        pathRewrite = settings.getPlugins("pathRewritePlugin", PathRewritePlugin.class);
        allowEmptyMorpheme = settings.getBoolean("allowEmptyMorpheme", null);

        return this;
    }

    /**
     * Set system dictionary to a filesystem one. The dictionary itself will not be
     * loaded, nor its existence will be checked.
     * 
     * @param path
     *            Path to the resource
     * @return modified Config
     */
    public Config systemDictionary(Path path) {
        systemDictionary = new Resource.Filesystem<>(path);
        return this;
    }

    /**
     * Set system dictionary to a classpath one. The dictionary itself will not be
     * loaded, nor its existence will be checked.
     * 
     * @param url
     *            URL to the resource
     * @return modified Config
     */
    public Config systemDictionary(URL url) {
        systemDictionary = new Resource.Classpath<>(url);
        return this;
    }

    /**
     * Set system dictionary to a prebuilt one
     * 
     * @param dic
     *            prebuilt System {@link BinaryDictionary}
     * @return modified Config
     */
    public Config systemDictionary(BinaryDictionary dic) {
        if (!dic.getDictionaryHeader().isSystemDictionary()) {
            throw new IllegalArgumentException("built dictionary must be system");
        }
        systemDictionary = new Resource.Ready<>(dic);
        return this;
    }

    /**
     * Makes the current list of user dictionaries empty.
     * 
     * @return modified Config
     */
    public Config clearUserDictionaries() {
        if (userDictionary == null) {
            userDictionary = new ArrayList<>();
        } else {
            userDictionary.clear();
        }
        return this;
    }

    /**
     * Adds a user dictionary from filesystem. The dictionary itself will not be
     * loaded, nor the file existence will be checked.
     * 
     * @param path
     *            Path to the dictionary file
     * @return modified Config
     */
    public Config addUserDictionary(Path path) {
        if (userDictionary == null) {
            userDictionary = new ArrayList<>();
        }
        userDictionary.add(new Resource.Filesystem<>(path));
        return this;
    }

    /**
     * Adds a user dictionary from classpath. The dictionary itself will not be
     * loaded, nor its existence will be checked.
     * 
     * @param url
     *            URL of the classpath resource
     * @return modified Config
     */
    public Config addUserDictionary(URL url) {
        if (userDictionary == null) {
            userDictionary = new ArrayList<>();
        }
        userDictionary.add(new Resource.Classpath<>(url));
        return this;
    }

    /**
     * Adds a prebuilt user dictionary.
     * 
     * @param dic
     *            prebuilt user {@link BinaryDictionary}
     * @return modified Config
     */
    public Config addUserDictionary(BinaryDictionary dic) {
        if (!dic.getDictionaryHeader().isUserDictionary()) {
            throw new IllegalArgumentException("built dictionary must be user");
        }
        if (userDictionary == null) {
            userDictionary = new ArrayList<>();
        }
        userDictionary.add(new Resource.Ready<>(dic));
        return this;
    }

    /**
     * Sets the character definition to a filesystem file.
     * 
     * @param path
     *            Path to the file.
     * @return modified Config
     */
    public Config characterDefinition(Path path) {
        this.characterDefinition = new Resource.Filesystem<>(path);
        return this;
    }

    /**
     * Sets the character definition file to a classpath resource.
     * 
     * @param url
     *            URL to the classpath resource
     * @return modified Config
     */
    public Config characterDefinition(URL url) {
        this.characterDefinition = new Resource.Classpath<>(url);
        return this;
    }

    /**
     * Sets the character definition file to a prebuilt object
     * 
     * @param obj
     *            prebuilt {@link CharacterCategory}
     * @return modified Config
     */
    public Config characterDefinition(CharacterCategory obj) {
        this.characterDefinition = new Resource.Ready<>(obj);
        return this;
    }

    /**
     * Sets whether empty morphemes are allowed
     * 
     * @param allow
     *            whether to allow empty morphemes
     * @return modified Config
     */
    public Config allowEmptyMorpheme(boolean allow) {
        this.allowEmptyMorpheme = allow;
        return this;
    }

    /**
     * Adds one EditConnectionCostPlugin configuration
     * 
     * @param clz
     *            plugin class
     * @param <T>
     *            type of the plugin
     * @return modified Config
     */
    public <T extends EditConnectionCostPlugin> PluginConf<EditConnectionCostPlugin> addEditConnectionCostPlugin(
            Class<T> clz) {
        @SuppressWarnings("unchecked") // Use site variance is bad :/
        PluginConf<EditConnectionCostPlugin> conf = (PluginConf<EditConnectionCostPlugin>) PluginConf.make(clz);
        if (editConnectionCost == null) {
            editConnectionCost = new ArrayList<>();
        }
        editConnectionCost.add(conf);
        return conf;
    }

    /**
     * Adds one InputTextPlugin configuration
     * 
     * @param clz
     *            plugin class
     * @param <T>
     *            type of the plugin
     * @return modified Config
     */
    public <T extends InputTextPlugin> PluginConf<InputTextPlugin> addInputTextPlugin(Class<T> clz) {
        @SuppressWarnings("unchecked") // Use site variance is bad :/
        PluginConf<InputTextPlugin> conf = (PluginConf<InputTextPlugin>) PluginConf.make(clz);
        if (inputText == null) {
            inputText = new ArrayList<>();
        }
        inputText.add(conf);
        return conf;
    }

    /**
     * Adds one OovProviderPlugin configuration
     * 
     * @param clz
     *            plugin class
     * @param <T>
     *            type of the plugin
     * @return modified Config
     */
    public <T extends OovProviderPlugin> PluginConf<OovProviderPlugin> addOovProviderPlugin(Class<T> clz) {
        @SuppressWarnings("unchecked") // Use site variance is bad :/
        PluginConf<OovProviderPlugin> conf = (PluginConf<OovProviderPlugin>) PluginConf.make(clz);
        if (oovProviders == null) {
            oovProviders = new ArrayList<>();
        }
        oovProviders.add(conf);
        return conf;
    }

    /**
     *
     * @return System dictionary as Resource
     */
    public Resource<BinaryDictionary> getSystemDictionary() {
        return systemDictionary;
    }

    /**
     *
     * @return User dictionaries as a List of Resource
     */
    public List<Resource<BinaryDictionary>> getUserDictionaries() {
        return userDictionary == null ? Collections.emptyList() : Collections.unmodifiableList(userDictionary);
    }

    /**
     *
     * @return Character definition as resource
     */
    public Resource<CharacterCategory> getCharacterDefinition() {
        return characterDefinition;
    }

    /**
     *
     * @return list of EditConnectionCostPlugin configuration
     */
    public List<PluginConf<EditConnectionCostPlugin>> getEditConnectionCostPlugins() {
        return editConnectionCost == null ? Collections.emptyList() : Collections.unmodifiableList(editConnectionCost);
    }

    /**
     *
     * @return list of InputTextPlugin configuration
     */
    public List<PluginConf<InputTextPlugin>> getInputTextPlugins() {
        return inputText == null ? Collections.emptyList() : Collections.unmodifiableList(inputText);
    }

    /**
     *
     * @return list of OovProviderPlugin configuration
     */
    public List<PluginConf<OovProviderPlugin>> getOovProviderPlugins() {
        return oovProviders == null ? Collections.emptyList() : Collections.unmodifiableList(oovProviders);
    }

    /**
     *
     * @return list of PathRewritePlugin configuration
     */
    public List<PluginConf<PathRewritePlugin>> getPathRewritePlugins() {
        return pathRewrite == null ? Collections.emptyList() : Collections.unmodifiableList(pathRewrite);
    }

    /**
     *
     * @return whether empty morphemes are allowed
     */
    public boolean isAllowEmptyMorpheme() {
        return allowEmptyMorpheme == null || allowEmptyMorpheme;
    }

    /**
     * Merges this Config with another Config. Compared to
     * {@link Settings#merge(Settings)}, merging is done for already resolved
     * Configs and has no path resolution complexity.
     *
     * Generally, non-empty properties of the provided config will replace
     * properties of the current Config. For lists, the behavior depends on
     * mergeMode parameter. When using {@code MergeMode.REPLACE}, the behavior is
     * the same as with scalar fields. When using {@code MergeMode.APPEND}, new
     * values of the provided config will be appended to the current config.
     *
     * Prefer merging Configs over merging Settings.
     *
     * Can also be used to create a copy of a config as
     * {@code Config.empty().merge(config)}.
     *
     * @param other
     *            Config to merge with the current one
     * @param mergeMode
     *            how to merge lists (plugins, user dictionaries)
     * @return modified Config
     */
    public Config merge(Config other, MergeMode mergeMode) {
        systemDictionary = mergeOne(systemDictionary, other.systemDictionary);
        userDictionary = mergeList(mergeMode, userDictionary, other.userDictionary);
        characterDefinition = mergeOne(characterDefinition, other.characterDefinition);
        editConnectionCost = mergePluginList(mergeMode, editConnectionCost, other.editConnectionCost);
        inputText = mergePluginList(mergeMode, inputText, other.inputText);
        oovProviders = mergePluginList(mergeMode, oovProviders, other.oovProviders);
        pathRewrite = mergePluginList(mergeMode, pathRewrite, other.pathRewrite);
        allowEmptyMorpheme = mergeOne(allowEmptyMorpheme, other.allowEmptyMorpheme);
        return this;
    }

    /**
     * Specifies mode for Config merging
     */
    public enum MergeMode {
        /**
         * List contents will be appended
         */
        APPEND,
        /**
         * Present list will replace existing lists
         */
        REPLACE
    }

    @FunctionalInterface
    public interface IOFunction<T, R> {
        R apply(T arg) throws IOException;
    }

    /**
     * Configuration for Sudachi Plugin.
     * 
     * @param <T>
     */
    public static class PluginConf<T extends Plugin> {
        String clazzName;
        Settings internal;
        Class<T> parent;

        private PluginConf() {
        }

        PluginConf(String clazzName, Settings internal, Class<T> parent) {
            this.clazzName = clazzName;
            this.internal = internal;
            this.parent = parent;
        }

        /**
         * Create a new empty configuration for a plugin
         * 
         * @param clz
         *            plugin class
         * @param <T>
         *            plugin type
         * @return plugin configuration object
         */
        public static <T extends Plugin> PluginConf<T> make(Class<T> clz) {
            return new PluginConf<>(clz.getName(), Settings.empty(), clz);
        }

        /**
         * Create the plugin instance
         * 
         * @return plugin instance
         * @throws IllegalArgumentException
         *             when instantiation fails
         */
        @SuppressWarnings("unchecked")
        public T instantiate() {
            Class<T> clz;
            try {
                clz = (Class<T>) Class.forName(clazzName);
            } catch (ClassNotFoundException e) {
                throw new IllegalArgumentException("non-existent plugin class", e);
            }
            if (!parent.isAssignableFrom(clz)) {
                throw new IllegalArgumentException(String.format("plugin %s did not have correct parent, expected %s",
                        clazzName, parent.getName()));
            }

            T result;
            try {
                Constructor<T> constructor = clz.getDeclaredConstructor();
                result = constructor.newInstance();
            } catch (InstantiationException | IllegalAccessException | NoSuchMethodException
                    | InvocationTargetException e) {
                throw new IllegalArgumentException(e);
            }
            result.setSettings(internal);
            return result;
        }

        /**
         * Add untyped key-value configuration to the plugin
         * 
         * @param key
         *            setting key
         * @param value
         *            setting value
         * @return modified plugin
         */
        public PluginConf<T> add(String key, String value) {
            JsonObject obj = Json.createObjectBuilder().add(key, value).build();
            Settings merged = internal.merge(new Settings(obj, SettingsAnchor.none()));
            return new PluginConf<>(clazzName, merged, parent);
        }
    }

    /**
     * A container for the resource, allowing to combine lazy loading with providing
     * prebuilt resources
     * 
     * @param <T>
     *            resource type of the built resource
     */
    public static abstract class Resource<T> {
        /**
         * Create a real resource instance. File loading should be done inside the
         * creator function.
         * 
         * @param creator
         *            creator function
         * @return created resource
         * @throws IOException
         *             when IO fails
         */
        public T consume(IOFunction<Resource<T>, T> creator) throws IOException {
            return creator.apply(this);
        }

        /**
         * Open this resource as readable InputStream. User should close it when the
         * reading is done.
         *
         * @return readable InputStream.
         * @throws IOException
         *             when IO fails
         */
        public InputStream asInputStream() throws IOException {
            throw new UnsupportedOperationException();
        }

        /**
         * Get view of this resource as a ByteBuffer. When it is possible, the data will
         * be memory mapped, if it is not possible, it will be fully read into the
         * memory. Will not work for files more than 2^31 bytes (2 GB) in size.
         *
         * @return ByteBuffer containing the whole contents of the file
         * @throws IOException
         *             when IO fails
         */
        public ByteBuffer asByteBuffer() throws IOException {
            throw new UnsupportedOperationException();
        }

        /**
         * Returns internal representation (for tests)
         *
         * @return internal representation
         */
        Object repr() {
            return null;
        }

        /**
         * Filesystem-backed resource
         * 
         * @param <T>
         *            resource
         */
        public static class Filesystem<T> extends Resource<T> {
            private final Path path;

            Filesystem(Path path) {
                super();
                this.path = path;
            }

            @Override
            public InputStream asInputStream() throws IOException {
                return Files.newInputStream(path);
            }

            @Override
            public ByteBuffer asByteBuffer() throws IOException {
                return MMap.map(path.toString());
            }

            @Override
            public String toString() {
                return path.toString();
            }

            @Override
            Object repr() {
                return path;
            }
        }

        /**
         * Resource which is in Java classpath.
         * 
         * @param <T>
         */
        public static class Classpath<T> extends Resource<T> {
            private final URL url;

            Classpath(URL url) {
                this.url = url;
            }

            @Override
            public InputStream asInputStream() throws IOException {
                return url.openStream();
            }

            @Override
            public ByteBuffer asByteBuffer() throws IOException {
                if (Objects.equals(url.getProtocol(), "file")) {
                    return MMap.map(url.getPath());
                }
                return StringUtil.readAllBytes(url);
            }

            @Override
            public String toString() {
                return url.toString();
            }

            @Override
            Object repr() {
                return url;
            }
        }

        /**
         * Prebuilt resource.
         * 
         * @param <T>
         */
        public static class Ready<T> extends Resource<T> {
            private final T object;

            public Ready(T object) {
                this.object = object;
            }

            @Override
            public T consume(IOFunction<Resource<T>, T> creator) {
                return object;
            }

            @Override
            Object repr() {
                return object;
            }
        }
    }
}
