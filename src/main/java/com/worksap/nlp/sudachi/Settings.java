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

import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.json.JsonString;
import javax.json.JsonStructure;
import javax.json.JsonValue;
import javax.json.stream.JsonParsingException;

/**
 * A structure of settings.
 * <p>
 * This class reads a settings written in JSON.
 *
 * <p>
 * The following is an example of settings.
 *
 * <pre>
 * {@code
 *   {
 *     "path" : "/usr/local/share/sudachi",
 *     "systemDict" : "system.dic",
 *     "characterDefinitionFile" : "char.def",
 *     "inputTextPlugin" : [
 *       { "class" : "com.worksap.nlp.sudachi.DefaultInputTextPlugin" }
 *     ],
 *     "oovProviderPlugin" : [
 *       {
 *         "class" : "com.worksap.nlp.sudachi.MeCabOovProviderPlugin",
 *         "charDef" : "char.def",
 *         "unkDef" : "unk.def"
 *       },
 *       {
 *         "class" : "com.worksap.nlp.sudachi.SimpleOovProviderPlugin",
 *         "oovPOSStrings" : [ "補助記号", "一般", "*", "*", "*", "*" ],
 *         "leftId" : 5968,
 *         "rightId" : 5968,
 *         "cost" : 3857
 *       }
 *     ]
 *   }
 * }
 * </pre>
 *
 * <p>
 * {@code path} is a reserved key. Its value is used in {@link #getPath} as the
 * base path to make an absolute path from a relative path.
 */
public class Settings {
    static final PathResolver NOOP_RESOLVER = new PathResolver.Noop();
    JsonObject root;
    PathResolver base;

    Settings(JsonObject root, String basePath) {
        this.root = root;
        if (basePath == null) {
            base = NOOP_RESOLVER;
        } else {
            base = PathResolver.fileSystem(Paths.get(basePath));
        }
    }

    Settings(JsonObject root, PathResolver base) {
        this.root = root;
        this.base = base;
    }

    public static Settings empty() {
        return new Settings(JsonObject.EMPTY_JSON_OBJECT, NOOP_RESOLVER);
    }

    /**
     * Read a settings from a JSON string.
     * <p>
     * The root level of JSON must be a Object.
     *
     * @param path
     *            the base path if "path" is undefined in {@code json}
     * @param json
     *            JSON string
     * @return a structure of settings
     * @throws IllegalArgumentException
     *             if the parsing is failed
     * @deprecated use PathResolver overload, will be removed in 1.0.0
     */
    @Deprecated
    public static Settings parseSettings(String path, String json) {
        try (JsonReader reader = Json.createReader(new StringReader(json))) {
            JsonStructure rootStr = reader.read();
            if (rootStr instanceof JsonObject) {
                JsonObject root = (JsonObject) rootStr;
                String basePath = root.getString("path", path);
                return new Settings(root, basePath);
            } else {
                throw new IllegalArgumentException("root must be an object");
            }
        } catch (JsonParsingException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static Settings parseSettings(String json, PathResolver resolver) {
        try (JsonReader reader = Json.createReader(new StringReader(json))) {
            JsonStructure rootStr = reader.read();
            if (rootStr instanceof JsonObject) {
                JsonObject root = (JsonObject) rootStr;
                String basePath = root.getString("path", null);
                if (basePath == null) {
                    if (resolver == null) {
                        resolver = NOOP_RESOLVER;
                    }
                    return new Settings(root, resolver);
                } else {
                    PathResolver pathResolver = PathResolver.fileSystem(Paths.get(basePath));
                    return new Settings(root, pathResolver);
                }
            } else {
                throw new IllegalArgumentException("root must be an object");
            }
        } catch (JsonParsingException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static Settings fromFile(Path path, PathResolver resolver) throws IOException {
        return parseSettings(StringUtil.readFully(path), resolver);
    }

    public static Settings fromClasspath(URL url, PathResolver resolver) throws IOException {
        return parseSettings(StringUtil.readFully(url), resolver);
    }

    /**
     * Returns the value as the string to which the specified key is mapped, or
     * {@code null} if this settings contains no mapping for the key.
     *
     * @param setting
     *            the key
     * @return the value or {@code null} if this settings has no mapping
     * @throws IllegalArgumentException
     *             if the value is not a string
     */
    public String getString(String setting) {
        try {
            return root.getString(setting);
        } catch (NullPointerException e) {
            return null;
        } catch (ClassCastException e) {
            throw new IllegalArgumentException(setting + " is not a string", e);
        }
    }

    /**
     * Returns the value as the string to which the specified key is mapped, or
     * {@code defaultValue} if this settings contains no mapping for the key.
     *
     * @param setting
     *            the key
     * @param defaultValue
     *            the default mapping of the key
     * @return the value or {@code defaultValue} if this settings has no mapping
     * @throws IllegalArgumentException
     *             if the value is not a string
     */
    public String getString(String setting, String defaultValue) {
        return root.getString(setting, defaultValue);
    }

    /**
     * Returns the value as the list of strings to which the specified key is
     * mapped, or an empty list if this settings contains no mapping for the key.
     *
     * @param setting
     *            the key
     * @return the value or a empty list if this settings has no mapping
     * @throws IllegalArgumentException
     *             if the value is not an array of strings
     */
    public List<String> getStringList(String setting) {
        try {
            JsonArray array = root.getJsonArray(setting);
            if (array == null) {
                return Collections.emptyList();
            }
            List<String> result = new ArrayList<>(array.size());
            for (int i = 0; i < array.size(); i++) {
                result.add(array.getString(i));
            }
            return result;
        } catch (ClassCastException e) {
            throw new IllegalArgumentException(setting + " is not an array of strings", e);
        }
    }

    /**
     * Returns the value as the integer to which the specified key is mapped, or 0
     * if this settings contains no mapping for the key.
     *
     * @param setting
     *            the key
     * @return the value or 0 if this settings has no mapping
     * @throws IllegalArgumentException
     *             if the value is not an integer
     */
    public int getInt(String setting) {
        try {
            return root.getInt(setting);
        } catch (NullPointerException e) {
            return 0;
        } catch (ClassCastException e) {
            throw new IllegalArgumentException(setting + " is not a number", e);
        }
    }

    /**
     * Returns the value as the string to which the specified key is mapped, or
     * {@code defaultValue} if this settings contains no mapping for the key.
     *
     * @param setting
     *            the key
     * @param defaultValue
     *            the default mapping of the key
     * @return the value or {@code defaultValue} if this settings has no mapping
     * @throws IllegalArgumentException
     *             if the value is not a string
     */
    public int getInt(String setting, int defaultValue) {
        try {
            return root.getInt(setting, defaultValue);
        } catch (ClassCastException e) {
            throw new IllegalArgumentException(setting + " is not a number", e);
        }
    }

    /**
     * Returns the value as the list of integers to which the specified key is
     * mapped, or an empty list if this settings contains no mapping for the key.
     *
     * @param setting
     *            the key
     * @return the value or a empty list if this settings has no mapping
     * @throws IllegalArgumentException
     *             if the value is not an array of integers
     */
    public List<Integer> getIntList(String setting) {
        try {
            return getList(setting, JsonNumber.class).stream().map(JsonNumber::intValue).collect(Collectors.toList());
        } catch (ClassCastException e) {
            throw new IllegalArgumentException(setting + " is not an array of numbers", e);
        }
    }

    /**
     * Returns the value as the list of lists of integers to which the specified key
     * is mapped, or an empty list if this settings contains no mapping for the key.
     *
     * @param setting
     *            the key
     * @return the value or an empty list if this settings has no mapping
     * @throws IllegalArgumentException
     *             if the value is not an array of arrays of integers
     */
    public List<List<Integer>> getIntListList(String setting) {
        try {
            return getList(setting, JsonArray.class).stream().map(a -> a.getValuesAs(JsonNumber.class).stream()
                    .map(JsonNumber::intValue).collect(Collectors.toList())).collect(Collectors.toList());
        } catch (ClassCastException e) {
            throw new IllegalArgumentException(setting + " is not an array of arrays of numbers", e);
        }
    }

    /**
     * Returns the value as the file path to which the specified key is mapped, or
     * {@code null} if this settings contains no mapping for the key.
     *
     * <p>
     * If {@code "path"} is specified in the root object and the value is not an
     * absolute path, this method joins them using the
     * {@link java.nio.file.FileSystem#getSeparator name-separator} as the
     * separator.
     *
     * @param setting
     *            the key
     * @return the value or {@code null} if this settings has no mapping
     * @throws IllegalArgumentException
     *             if the value is not a string
     */
    public String getPath(String setting) {
        Path resource = getPathObject(setting);
        if (resource == null) {
            return null;
        }
        return resource.toString();
    }

    /**
     * Returns the setting value as the file path, or {@code null} if there is no
     * corresponding setting
     *
     * @param setting
     *            the key
     * @return the value or {@code null}
     * @throws IllegalArgumentException
     *             if the value is not a string
     */
    public Path getPathObject(String setting) {
        String path = getString(setting);
        if (path == null) {
            return null;
        }
        return base.resolve(path);
    }

    /**
     * Get value for setting as {@code Config.Resource}
     *
     * @param setting
     *            key name
     * @return resource corresponding to the key
     */
    public <T> Config.Resource<T> getResource(String setting) {
        String path = getString(setting);
        if (path == null) {
            return null;
        }
        return extractResource(path);
    }

    private <T> Config.Resource<T> extractResource(String path) {
        Path obj = base.resolve(path);
        if (base.anchorType() == AnchorType.FILESYSTEM) {
            return new Config.Resource.Filesystem<>(obj);
        } else {
            URL url = getClass().getClassLoader().getResource(obj.toString());
            return new Config.Resource.Classpath<>(url);
        }
    }

    public <T> List<Config.Resource<T>> getResourceList(String setting) {
        List<String> list = getStringList(setting);
        if (list == null) {
            return null;
        }
        return list.stream().map(this::<T>extractResource).collect(Collectors.toList());
    }

    /**
     * Returns the value as the list of file paths to which the specified key is
     * mapped, or an empty list if this settings contains no mapping for the key.
     *
     * <p>
     * If {@code "path"} is specified in the root object and the file path is not an
     * absolute path, this method joins them using the
     * {@link java.nio.file.FileSystem#getSeparator name-separator} as the
     * separator.
     *
     * @param setting
     *            the key
     * @return the value or an empty list if this settings has no mapping
     * @throws IllegalArgumentException
     *             if the value is not an array of strings
     */
    public List<String> getPathList(String setting) {
        List<String> list = getStringList(setting);
        if (list.isEmpty()) {
            return Collections.emptyList();
        }
        return list.stream().map(p -> base.resolve(p).toString()).collect(Collectors.toList());
    }

    /**
     * Returns the value as the boolean to which the specified key is mapped, or
     * {@code defaultValue} if this settings contains no mapping for the key.
     *
     * @param setting
     *            the key
     * @param defaultValue
     *            the default mapping of the key
     * @return the value or {@code defaultValue} if this settings has no mapping
     */
    public Boolean getBoolean(String setting, Boolean defaultValue) {
        try {
            return root.getBoolean(setting);
        } catch (NullPointerException | ClassCastException e) {
            return defaultValue;
        }
    }

    <E extends JsonValue> List<E> getList(String setting, Class<E> clazz) {
        JsonArray array = root.getJsonArray(setting);
        if (array == null) {
            return Collections.emptyList();
        }
        return array.getValuesAs(clazz);
    }

    <P extends Plugin> List<Config.PluginConf<P>> getPlugins(String name, Class<P> cls) {
        JsonArray array = root.getJsonArray(name);
        if (array == null) {
            // must be mutable
            return null;
        }

        ArrayList<Config.PluginConf<P>> result = new ArrayList<>();

        for (JsonValue key : array) {
            JsonObject inner = key.asJsonObject();
            String className = inner.getString("class");
            if (className == null) {
                throw new IllegalArgumentException(String.format("subobject for %s didn't have class key", name));
            }
            result.add(new Config.PluginConf<>(className, new Settings(inner, base), cls));
        }
        return result;
    }

    Settings merge(Settings settings) {
        JsonObjectBuilder newRoot = Json.createObjectBuilder();
        for (Map.Entry<String, JsonValue> thisEntry : this.root.entrySet()) {
            String thisKey = thisEntry.getKey();
            JsonValue thisValue = thisEntry.getValue();
            if (settings.root.containsKey(thisKey)) {
                JsonValue value = settings.root.get(thisKey);
                if (thisValue instanceof JsonString || thisValue instanceof JsonNumber
                        || thisValue instanceof JsonObject) {
                    newRoot.add(thisKey, value);
                } else if (thisValue instanceof JsonArray) {
                    newRoot.add(thisKey, mergeArray(thisValue, value));
                }
            } else {
                newRoot.add(thisKey, thisValue);
            }
        }
        for (Map.Entry<String, JsonValue> entry : settings.root.entrySet()) {
            if (!this.root.containsKey(entry.getKey())) {
                newRoot.add(entry.getKey(), entry.getValue());
            }
        }
        return new Settings(newRoot.build(), base);
    }

    private JsonArray mergeArray(JsonValue thisValue, JsonValue value) {
        if (value instanceof JsonArray && ((JsonArray) value).isEmpty()) {
            return (JsonArray) value;
        }
        JsonArray thisList = (JsonArray) thisValue;
        JsonArrayBuilder newList = Json.createArrayBuilder();
        Map<Integer, JsonValue> replaceItems = new HashMap<>();

        for (JsonValue item : (JsonArray) value) {
            boolean isReplaced = false;
            if (item instanceof JsonObject && ((JsonObject) item).containsKey("class")) {
                JsonValue className = ((JsonObject) item).get("class");
                for (int i = 0; i < thisList.size(); i++) {
                    JsonValue thisItem = thisList.get(i);
                    if (thisItem instanceof JsonObject && ((JsonObject) thisItem).get("class").equals(className)) {
                        replaceItems.put(i, item);
                        isReplaced = true;
                    }
                }
            }
            if (!isReplaced) {
                newList.add(item);
            }
        }
        for (int i = 0; i < thisList.size(); i++) {
            JsonValue item = replaceItems.get(i);
            newList.add(item == null ? thisList.get(i) : item);
        }
        return newList.build();
    }

    public enum AnchorType {
        CLASSPATH, FILESYSTEM
    }

    public abstract static class PathResolver {
        public static PathResolver classPath() {
            return new BaseResolver(Paths.get(""), AnchorType.CLASSPATH);
        }

        public static PathResolver classPath(Class<?> clz) {
            String name = clz.getName();
            String path = name.replaceAll("\\.", "/");
            return new BaseResolver(Paths.get(path), AnchorType.CLASSPATH);
        }

        public static PathResolver fileSystem(Path p) {
            return new BaseResolver(p, AnchorType.FILESYSTEM);
        }

        Path resolve(String part) {
            return Paths.get(part);
        }

        AnchorType anchorType() {
            return AnchorType.FILESYSTEM;
        }

        static class BaseResolver extends PathResolver {
            private final Path base;
            private final AnchorType anchorType;

            public BaseResolver(Path base, AnchorType anchorType) {
                this.base = base;
                this.anchorType = anchorType;
            }

            @Override
            Path resolve(String part) {
                return base.resolve(part);
            }

            @Override
            AnchorType anchorType() {
                return this.anchorType;
            }
        }

        static private class Noop extends PathResolver {
        }
    }
}
