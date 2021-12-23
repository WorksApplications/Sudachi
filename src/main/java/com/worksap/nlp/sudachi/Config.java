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

    public static Config empty() {
        return new Config();
    }

    public static Config fromClasspath() throws IOException {
        return fromClasspath("sudachi.json");
    }

    public static Config fromClasspath(String name) throws IOException {
        return fromClasspath(Config.class.getClassLoader(), name);
    }

    public static Config fromClasspath(ClassLoader loader, String name) throws IOException {
        return fromClasspath(loader.getResource(name));
    }

    public static Config fromClasspath(URL resource) throws IOException {
        String data = StringUtil.readFully(resource);
        return fromJsonString(data, Settings.PathResolver.classPath());
    }

    public static Config fromFile(Path path) throws IOException {
        String data = StringUtil.readFully(path);
        return fromJsonString(data, Settings.PathResolver.fileSystem(path.getParent()));
    }

    public static Config fromJsonString(String json, Settings.PathResolver anchor) {
        return fromSettings(Settings.parseSettings(json, anchor));
    }

    public static Config fromSettings(Settings obj) {
        return Config.empty().mergeSettings(obj);
    }

    public static Config fromClasspathMerged(String name, MergeMode mode) throws IOException {
        return fromClasspathMerged(Config.class.getClassLoader(), name, mode);
    }

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
            if (mode == MergeMode.REPLACE) {
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
            if (mode == MergeMode.REPLACE) {
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

    public Config systemDictionary(Path path) {
        systemDictionary = new Resource.Filesystem<>(path);
        return this;
    }

    public Config systemDictionary(URL url) {
        systemDictionary = new Resource.Classpath<>(url);
        return this;
    }

    public Config systemDictionary(BinaryDictionary dic) {
        if (!dic.getDictionaryHeader().isSystemDictionary()) {
            throw new IllegalArgumentException("built dictionary must be system");
        }
        systemDictionary = new Resource.Ready<>(dic);
        return this;
    }

    public Config clearUserDictionaries() {
        if (userDictionary == null) {
            userDictionary = new ArrayList<>();
        } else {
            userDictionary.clear();
        }
        return this;
    }

    public Config addUserDictionary(Path path) {
        if (userDictionary == null) {
            userDictionary = new ArrayList<>();
        }
        userDictionary.add(new Resource.Filesystem<>(path));
        return this;
    }

    public Config addUserDictionary(URL url) {
        if (userDictionary == null) {
            userDictionary = new ArrayList<>();
        }
        userDictionary.add(new Resource.Classpath<>(url));
        return this;
    }

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

    public Config characterDefinition(Path path) {
        this.characterDefinition = new Resource.Filesystem<>(path);
        return this;
    }

    public Config characterDefinition(URL url) {
        this.characterDefinition = new Resource.Classpath<>(url);
        return this;
    }

    public Config characterDefinition(CharacterCategory obj) {
        this.characterDefinition = new Resource.Ready<>(obj);
        return this;
    }

    public Config allowEmptyMorpheme(boolean allow) {
        this.allowEmptyMorpheme = allow;
        return this;
    }

    public <T extends EditConnectionCostPlugin> PluginConf<EditConnectionCostPlugin> addEditConnectionCostPlugin(
            Class<T> clz) {
        @SuppressWarnings("unchecked") // Java Type system is bad :/
        PluginConf<EditConnectionCostPlugin> conf = (PluginConf<EditConnectionCostPlugin>) PluginConf.make(clz);
        if (editConnectionCost == null) {
            editConnectionCost = new ArrayList<>();
        }
        editConnectionCost.add(conf);
        return conf;
    }

    public Resource<BinaryDictionary> getSystemDictionary() {
        return systemDictionary;
    }

    public List<Resource<BinaryDictionary>> getUserDictionaries() {
        return userDictionary == null ? Collections.emptyList() : Collections.unmodifiableList(userDictionary);
    }

    public Resource<CharacterCategory> getCharacterDefinition() {
        return characterDefinition;
    }

    public List<PluginConf<EditConnectionCostPlugin>> getEditConnectionCostPlugins() {
        return editConnectionCost == null ? Collections.emptyList() : Collections.unmodifiableList(editConnectionCost);
    }

    public List<PluginConf<InputTextPlugin>> getInputTextPlugins() {
        return inputText == null ? Collections.emptyList() : Collections.unmodifiableList(inputText);
    }

    public List<PluginConf<OovProviderPlugin>> getOovProviderPlugins() {
        return oovProviders == null ? Collections.emptyList() : Collections.unmodifiableList(oovProviders);
    }

    public List<PluginConf<PathRewritePlugin>> getPathRewritePlugins() {
        return pathRewrite == null ? Collections.emptyList() : Collections.unmodifiableList(pathRewrite);
    }

    public boolean isAllowEmptyMorpheme() {
        return allowEmptyMorpheme == null || allowEmptyMorpheme;
    }

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

    public enum MergeMode {
        APPEND, REPLACE
    }

    @FunctionalInterface
    public interface IOFunction<T, R> {
        R apply(T arg) throws IOException;
    }

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

        public static <T extends Plugin> PluginConf<T> make(Class<T> clz) {
            return new PluginConf<>(clz.getName(), Settings.empty(), clz);
        }

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

        public PluginConf<T> add(String key, String value) {
            JsonObject obj = Json.createObjectBuilder().add(key, value).build();
            Settings merged = internal.merge(new Settings(obj, Settings.NOOP_RESOLVER));
            return new PluginConf<>(clazzName, merged, parent);
        }
    }

    public static abstract class Resource<T> {
        public T consume(IOFunction<Resource<T>, T> creator) throws IOException {
            return creator.apply(this);
        }

        public InputStream asInputStream() throws IOException {
            throw new UnsupportedOperationException();
        }

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
                throw new UnsupportedOperationException();
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
