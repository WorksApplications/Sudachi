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

import com.worksap.nlp.sudachi.dictionary.BinaryDictionary;
import com.worksap.nlp.sudachi.dictionary.CharacterCategory;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
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
 * <p>
 * There are two main configuration modes: parsing json configuration file and
 * specifying resources in code. Resources can be specified from classpath,
 * filesystem or be pre-created.
 * <p>
 * {@link URL}s for fromClasspath methods should be provided from
 * {@link Class#getResource(String)} or {@link ClassLoader#getResource(String)}
 * methods.
 * <p>
 * Resource paths inside the json configuration files will be resolved relative
 * to the configuration file. If the file was in classpath, they will be looked
 * up in classpath or relative to the working directory. If the file was in
 * filesystem, they will be resolved relative to the configuration file only.
 *
 * @see Settings Settings: untyped configuration parsed from json file
 * @see PathAnchor
 * @see #empty()
 * @see #defaultConfig()
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
     * loaded from classpath. In-config paths will be resolved only with respect to
     * classpath.
     *
     * @return Config object
     * @throws IOException
     *             when IO fails
     */
    public static Config defaultConfig() throws IOException {
        return defaultConfig(PathAnchor.classpath());
    }

    /**
     * Loads configuration from the first instance of {@code sudachi.json} in the
     * classpath. In-config paths will be resolved with respect to the provided
     * anchor.
     *
     * @param anchor
     *            resolve paths with respect to this anchor
     * @return Config object
     * @throws IOException
     *             when IO fails
     */
    public static Config defaultConfig(PathAnchor anchor) throws IOException {
        return fromClasspath("sudachi.json", anchor);
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
     * @see Settings#fromClasspath(URL, PathAnchor)
     */
    public static Config fromClasspath(String name) throws IOException {
        return fromClasspath(name, PathAnchor.classpath());
    }

    public static Config fromClasspath(String name, PathAnchor anchor) throws IOException {
        ClassLoader loader = Config.class.getClassLoader();
        PathAnchor newAnchor = anchor.andThen(PathAnchor.classpath(loader));
        URL resource = loader.getResource(name);
        if (resource == null) {
            throw new IllegalArgumentException("failed to find resource in classpath: " + name);
        }
        return fromClasspath(resource, newAnchor);
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
        return fromClasspath(resource, PathAnchor.classpath());
    }

    public static Config fromClasspath(URL resource, PathAnchor anchor) throws IOException {
        Settings settings = Settings.fromClasspath(resource, anchor);
        return fromSettings(settings);
    }

    /**
     * Loads the config from the filesystem. Paths in the config will NOT be
     * resolved with respect to classpath.
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
     * Loads the config from the filesystem. Paths in the config will be resolved
     * with respect to the provided file.
     *
     * @param path
     *            {@link Path} to the config file
     * @param anchor
     *            anchor for resolution
     * @return parsed Config
     * @throws IOException
     *             when IO fails
     */
    public static Config fromFile(Path path, PathAnchor anchor) throws IOException {
        return fromSettings(Settings.fromFile(path, anchor));
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
    public static Config fromJsonString(String json, PathAnchor anchor) {
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
        return Config.empty().fallbackSettings(obj);
    }

    /**
     * Parses all config files with the specified name in the classpath, merging
     * them. Files are loaded with Config classloader.
     *
     * @param name
     *            classpath resource name
     * @return merged Config
     * @throws IOException
     *             when IO fails
     * @see #withFallback(Config)
     */
    public static Config fromClasspathMerged(String name) throws IOException {
        return fromClasspathMerged(Config.class.getClassLoader(), name);
    }

    /**
     * Parses all config files with the specified name in the classpath, merging
     * them. Files are loaded with the provided classloader.
     *
     * @param classLoader
     *            it will be used to load resources
     * @param name
     *            classpath resource name
     * @return merged Config
     * @throws IOException
     *             when IO fails
     * @see #withFallback(Config)
     */
    public static Config fromClasspathMerged(ClassLoader classLoader, String name) throws IOException {
        Enumeration<URL> resources = classLoader.getResources(name);
        Config result = Config.empty();
        long count = 0;
        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            result = result.withFallback(Config.fromClasspath(resource));
            count += 1;
        }
        if (count == 0) {
            throw new IllegalArgumentException(String.format("couldn't find any file %s in classpath", name));
        }
        return result;
    }

    private static <T> List<T> mergeList(List<T> self, List<T> other) {
        if (self == null) {
            return other;
        }
        return self;
    }

    private static <T extends Plugin> List<PluginConf<T>> mergePluginList(List<PluginConf<T>> self,
            List<PluginConf<T>> other) {
        if (other != null) {
            if (self == null) {
                return other;
            }
            for (PluginConf<T> selfConf : self) {
                Optional<PluginConf<T>> first = other.stream()
                        .filter(p -> Objects.equals(p.clazzName, selfConf.clazzName)).findFirst();
                if (first.isPresent()) {
                    PluginConf<T> otherConf = first.get();
                    selfConf.internal = selfConf.internal.withFallback(otherConf.internal);
                }
            }
        }
        return self;
    }

    private static <T> T mergeOne(T self, T other) {
        if (self == null) {
            return other;
        }
        return self;
    }

    private Config fallbackSettings(Settings settings) {

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
        userDictionary.add(Resource.ready(dic));
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
        this.characterDefinition = Resource.ready(obj);
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
     * @return System dictionary as Resource
     */
    public Resource<BinaryDictionary> getSystemDictionary() {
        return systemDictionary;
    }

    /**
     * @return User dictionaries as a List of Resource
     */
    public List<Resource<BinaryDictionary>> getUserDictionaries() {
        return userDictionary == null ? Collections.emptyList() : Collections.unmodifiableList(userDictionary);
    }

    /**
     * @return Character definition as resource
     */
    public Resource<CharacterCategory> getCharacterDefinition() {
        return characterDefinition;
    }

    /**
     * @return list of EditConnectionCostPlugin configuration
     */
    public List<PluginConf<EditConnectionCostPlugin>> getEditConnectionCostPlugins() {
        return editConnectionCost == null ? Collections.emptyList() : Collections.unmodifiableList(editConnectionCost);
    }

    /**
     * @return list of InputTextPlugin configuration
     */
    public List<PluginConf<InputTextPlugin>> getInputTextPlugins() {
        return inputText == null ? Collections.emptyList() : Collections.unmodifiableList(inputText);
    }

    /**
     * @return list of OovProviderPlugin configuration
     */
    public List<PluginConf<OovProviderPlugin>> getOovProviderPlugins() {
        return oovProviders == null ? Collections.emptyList() : Collections.unmodifiableList(oovProviders);
    }

    /**
     * @return list of PathRewritePlugin configuration
     */
    public List<PluginConf<PathRewritePlugin>> getPathRewritePlugins() {
        return pathRewrite == null ? Collections.emptyList() : Collections.unmodifiableList(pathRewrite);
    }

    /**
     * @return whether empty morphemes are allowed
     */
    public boolean isAllowEmptyMorpheme() {
        return allowEmptyMorpheme == null || allowEmptyMorpheme;
    }

    /**
     * Merges this Config with another Config. Compared to
     * {@link Settings#withFallback(Settings)}, merging is done for already resolved
     * Configs and has no path resolution complexity.
     * <p>
     * Values of the current config will be preferred over values of the underlying
     * config. Plugin configurations are always overridden. Prefer merging Configs
     * over merging Settings.
     * <p>
     * Can also be used to create a copy of a config as
     * {@code Config.empty().withFallback(config)}.
     *
     * @param other
     *            Config to merge with the current one
     * @return modified Config
     */
    public Config withFallback(Config other) {
        systemDictionary = mergeOne(systemDictionary, other.systemDictionary);
        userDictionary = mergeList(userDictionary, other.userDictionary);
        characterDefinition = mergeOne(characterDefinition, other.characterDefinition);
        editConnectionCost = mergePluginList(editConnectionCost, other.editConnectionCost);
        inputText = mergePluginList(inputText, other.inputText);
        oovProviders = mergePluginList(oovProviders, other.oovProviders);
        pathRewrite = mergePluginList(pathRewrite, other.pathRewrite);
        allowEmptyMorpheme = mergeOne(allowEmptyMorpheme, other.allowEmptyMorpheme);
        return this;
    }

    @FunctionalInterface
    public interface IOFunction<T, R> {
        R apply(T arg) throws IOException;
    }

    /**
     * Configuration for Sudachi Plugin.
     *
     * @param <T>
     *            resulting resource type
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
         * Add a string value to the plugin configuration
         *
         * @param key
         *            setting key
         * @param value
         *            setting value
         * @return current plugin instance
         */
        public PluginConf<T> add(String key, String value) {
            JsonObject obj = Json.createObjectBuilder().add(key, value).build();
            internal = new Settings(obj, PathAnchor.none()).withFallback(internal);
            return this;
        }

        /**
         * Add an int value to the plugin configuration
         *
         * @param key
         *            setting key
         * @param value
         *            setting value
         * @return current plugin instance
         */
        public PluginConf<T> add(String key, int value) {
            JsonObject obj = Json.createObjectBuilder().add(key, value).build();
            internal = new Settings(obj, PathAnchor.none()).withFallback(internal);
            return this;
        }

        /**
         * Add a string list value to the plugin configuration
         *
         * @param key
         *            setting key
         * @param values
         *            setting value as a string array
         * @return current plugin instance
         */
        public PluginConf<T> addList(String key, String... values) {
            JsonArrayBuilder builder = Json.createArrayBuilder(Arrays.asList(values));
            JsonObject obj = Json.createObjectBuilder().add(key, builder).build();
            internal = new Settings(obj, PathAnchor.none()).withFallback(internal);
            return this;
        }

        @Override
        public String toString() {
            return String.format("Plugin (%s) class: %s", parent.getSimpleName(), clazzName);
        }
    }

    /**
     * A container for the resource, allowing to combine lazy loading with providing
     * prebuilt resources. Use {@link PathAnchor} to create resources.
     *
     * @param <T>
     *            resource type of the built resource
     *
     * @see PathAnchor#filesystem(Path)
     * @see PathAnchor#toResource(Path)
     * @see PathAnchor#resource(String)
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
         *            resulting resource type
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
         *            resulting resource type
         */
        static class Ready<T> extends Resource<T> {
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

        public static class NotFound<T> extends Resource<T> {
            private final Path path;
            private final PathAnchor anchor;

            public NotFound(Path path, PathAnchor anchor) {
                this.path = path;
                this.anchor = anchor;
            }

            @Override
            public T consume(IOFunction<Resource<T>, T> creator) throws IOException {
                throw makeException();
            }

            @Override
            public InputStream asInputStream() {
                throw makeException();
            }

            @Override
            public ByteBuffer asByteBuffer() {
                throw makeException();
            }

            @Override
            Object repr() {
                return path;
            }

            private IllegalArgumentException makeException() {
                String sb = "Failed to resolve file: " + path.toString() + "\n" + "Tried roots: " + anchor;
                return new IllegalArgumentException(sb);
            }
        }

        /**
         * Create a resource wrapper for a prebuilt resource
         * 
         * @param object
         *            prebuilt resource
         * @return wrapper
         * @param <T>
         *            type of the prebuilt resource
         */
        public static <T> Resource<T> ready(T object) {
            return new Ready<>(object);
        }
    }
}
