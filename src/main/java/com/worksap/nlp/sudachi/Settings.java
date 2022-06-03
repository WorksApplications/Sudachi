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

import javax.json.*;
import javax.json.stream.JsonParsingException;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * An untyped collection of settings. This class handles parsing a JSON object
 * and using its properties as settings.
 *
 * <p>
 * There are multiple settings which can be paths, and paths are resolved using
 * {@link SettingsAnchor}s. Paths can be resolved with respect to classpath or
 * filesystem. SettingsAnchors also can be chained, returning the first existing
 * path.
 * </p>
 *
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
 * <p>
 * {@code path} is a reserved key. It prepends an additional filesystem anchor
 * to the current list of anchors.
 *
 *
 * @see Config
 * @see SettingsAnchor
 */
public class Settings {
    private static final Logger logger = Logger.getLogger(Settings.class.getName());

    JsonObject root;
    SettingsAnchor base;

    Settings(JsonObject root, SettingsAnchor base) {
        this.root = root;
        this.base = base;
    }

    /**
     *
     * @return empty object
     */
    public static Settings empty() {
        return resolvedBy(SettingsAnchor.none());
    }

    /**
     * Returns empty object resolved by the provided {@link SettingsAnchor}
     * 
     * @param resolver
     *            anchor
     * @return Settings object
     */
    public static Settings resolvedBy(SettingsAnchor resolver) {
        return new Settings(JsonObject.EMPTY_JSON_OBJECT, resolver);
    }

    /**
     * Reads the content of the specified file and merges it into the current
     * Settings object. Anchor is not modified.
     *
     * @param file
     *            {@link Path} to the file
     * @return modified Settings object
     * @throws IOException
     *             if IO fails
     * @see #parse(String, SettingsAnchor)
     */
    public Settings read(Path file) throws IOException {
        logger.fine(() -> String.format("reading settings from %s", file));
        String data = StringUtil.readFully(file);
        Settings settings = parse(data, this.base);
        return settings.withFallback(this);
    }

    /**
     * Reads the content of the specified classpath resource and merges it into the
     * current Settings object. Anchor is not modified.
     *
     * @param resource
     *            result of {@link Class#getResource(String)}
     * @return modified Settings object
     * @throws IOException
     *             if IO fails
     * @see #parse(String, SettingsAnchor)
     */
    public Settings read(URL resource) throws IOException {
        logger.fine(() -> String.format("reading settings from %s", resource));
        String data = StringUtil.readFully(resource);
        Settings settings = parse(data, this.base);
        return settings.withFallback(this);
    }

    /**
     * Read a settings from a JSON string.
     * <p>
     *
     * @param path
     *            will add additional {@link SettingsAnchor} to this path if not
     *            {@code null}
     * @param json
     *            JSON string
     * @return Settings object
     * @throws IllegalArgumentException
     *             if the parsing is failed
     * @deprecated use {@link #parse(String, SettingsAnchor)}, this method will be
     *             removed in 1.0.0
     */
    @Deprecated
    public static Settings parseSettings(String path, String json) {
        SettingsAnchor anchor = path == null ? SettingsAnchor.none() : SettingsAnchor.filesystem(Paths.get(path));
        anchor = anchor.andThen(SettingsAnchor.classpath());
        return parse(json, anchor);
    }

    /**
     * Parse a JSON string into a Settings object. String must contain a JSON
     * object. If a JSON contains a {@code path} key, its value will be prepended as
     * an additional filesystem SettingsAnchor.
     *
     * @param json
     *            JSON object as a String
     * @param resolver
     *            paths will be resolved against this {@link SettingsAnchor}
     * @return SettingsObject
     *
     * @see SettingsAnchor#none()
     * @see SettingsAnchor#filesystem(Path)
     * @see SettingsAnchor#classpath()
     */
    public static Settings parse(String json, SettingsAnchor resolver) {
        try (JsonReader reader = Json.createReader(new StringReader(json))) {
            JsonStructure rootStr = reader.read();
            if (rootStr instanceof JsonObject) {
                JsonObject root = (JsonObject) rootStr;
                String basePath = root.getString("path", null);
                if (basePath == null) {
                    if (resolver == null) {
                        resolver = SettingsAnchor.none();
                    }
                    return new Settings(root, resolver);
                } else {
                    SettingsAnchor pathResolver = SettingsAnchor.filesystem(Paths.get(basePath));
                    if (resolver != null) {
                        pathResolver = pathResolver.andThen(resolver);
                    }
                    return new Settings(root, pathResolver);
                }
            } else {
                throw new IllegalArgumentException("root must be an object");
            }
        } catch (JsonParsingException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Parse a Settings from a filesystem-based file. Paths will be resolved
     * relative to the directory of the file.
     *
     * @param path
     *            filesystem path to the settings file
     * @return Settings object
     * @throws IOException
     *             if IO fails
     * @see #parse(String, SettingsAnchor)
     */
    public static Settings fromFile(Path path) throws IOException {
        return fromFile(path, SettingsAnchor.filesystem(path.getParent()));
    }

    /**
     * Parse a Settings from a filesystem-based file. Paths will be resolved by the
     * provided anchor.
     *
     * @param path
     *            filesystem path to the settings file
     * @param resolver
     *            paths will be resolved relative to this anchor
     * @return Settings object
     * @throws IOException
     *             if IO fails
     * @see #parse(String, SettingsAnchor)
     */
    public static Settings fromFile(Path path, SettingsAnchor resolver) throws IOException {
        return resolvedBy(resolver).read(path);
    }

    /**
     * Parse a Settings from a classpath resource
     * 
     * @param url
     *            resource URL
     * @param resolver
     *            paths will be resolved relative to this anchor
     * @return Settings object
     * @throws IOException
     *             if IO fails
     * @see #parse(String, SettingsAnchor)
     */
    public static Settings fromClasspath(URL url, SettingsAnchor resolver) throws IOException {
        return resolvedBy(resolver).read(url);
    }

    /**
     * Returns the value as the string to which the specified key is mapped, or
     * {@code null} if the Settings object contains no mapping for the key.
     *
     * @param setting
     *            the key
     * @return the value or {@code null} if there is no key
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
     * {@code defaultValue} if the Settings object contains no mapping for the key.
     *
     * @param setting
     *            the key
     * @param defaultValue
     *            the default mapping of the key
     * @return the value or {@code defaultValue} if this settings object has no
     *         mapping
     * @throws IllegalArgumentException
     *             if the value is not a string
     */
    public String getString(String setting, String defaultValue) {
        return root.getString(setting, defaultValue);
    }

    /**
     * Returns the value as the list of strings to which the specified key is
     * mapped, or an empty list if the Settings object contains no mapping for the
     * key.
     *
     * @param setting
     *            the key
     * @return the value or an empty list if there is no key
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
     * if the Settings object contains no mapping for the key.
     *
     * @param setting
     *            the key
     * @return the value or 0 if there is no key
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
     * {@code defaultValue} if the Settings object contains no mapping for the key.
     *
     * @param setting
     *            the key
     * @param defaultValue
     *            the default mapping of the key
     * @return the value or {@code defaultValue} if there is no key
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
     * mapped, or an empty list if the Settings object contains no mapping for the
     * key.
     *
     * @param setting
     *            the key
     * @return the value or an empty list if there is no mapping
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
     * is mapped, or an empty list if the Settings contains no mapping for the key.
     *
     * @param setting
     *            the key
     * @return the value or an empty list if the Settings has no key
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
     * Returns resolved path mapped by the key, or {@code null} if the Settings
     * contains no such key. Paths are resolved using {@link SettingsAnchor}.
     *
     * Strongly prefer using {@link #getResource(String)} over this method, because
     * this method can't handle classpath resources.
     *
     * @param setting
     *            the key
     * @return the resolved Path or {@code null} if there was no such key
     * @throws IllegalArgumentException
     *             if the value is not a string
     * @deprecated use {@link #getResource(String)}
     */
    @Deprecated
    public String getPath(String setting) {
        Path resource = getPathObject(setting);
        if (resource == null) {
            return null;
        }
        return resource.toString();
    }

    /**
     * Returns the setting value as the file path, or {@code null} if there is no
     * corresponding setting.
     *
     * Strongly prefer using {@link #getResource(String)} over this method, because
     * this method can't handle classpath resources.
     *
     * @param setting
     *            the key
     * @return the resolved Path or {@code null}
     * @throws IllegalArgumentException
     *             if the value is not a string
     * @deprecated use {@link #getResource(String)}
     */
    @Deprecated
    public Path getPathObject(String setting) {
        String path = getString(setting);
        if (path == null) {
            return null;
        }
        return base.resolve(path);
    }

    /**
     * Get value for setting as {@link com.worksap.nlp.sudachi.Config.Resource}.
     * Original value should be a string, but its value would be resolved with an
     * anchor.
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
        return base.toResource(obj);
    }

    /**
     * Get list of values for key as a List of
     * {@link com.worksap.nlp.sudachi.Config.Resource}. Original values for
     * resources should be strings, but their values would be resolved with an
     * anchor.
     * 
     * @param setting
     *            key name
     * @param <T>
     *            type of resource
     * @return list of resources corresponding to the key
     */
    public <T> List<Config.Resource<T>> getResourceList(String setting) {
        List<String> list = getStringList(setting);
        if (list == null) {
            return null;
        }
        return list.stream().map(this::<T>extractResource).filter(Objects::nonNull).collect(Collectors.toList());
    }

    /**
     * Returns the value as the list of file paths to which the specified key is
     * mapped, or an empty list if the settings object contains no mapping for the
     * key.
     *
     * <p>
     * If {@code "path"} is specified in the root object and the file path is not an
     * absolute path, this method joins them using the
     * {@link java.nio.file.FileSystem#getSeparator name-separator} as the
     * separator.
     *
     * @param setting
     *            the key
     * @return the value or an empty list if this settings object has no mapping
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
     * {@code defaultValue} if this settings object contains no mapping for the key.
     *
     * @param setting
     *            the key
     * @param defaultValue
     *            the default mapping of the key
     * @return the value or {@code defaultValue} if this settings object has no
     *         mapping
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
                throw new IllegalArgumentException(String.format("sub-object for %s didn't have class key", name));
            }
            result.add(new Config.PluginConf<>(className, new Settings(inner, base), cls));
        }
        return result;
    }

    /**
     * Merge another Settings object with this object, returning a new Settings
     * object. Scalar values and arrays of this object will be replaced by values of
     * another object.
     *
     * The current object will not be modified.
     *
     * {@link SettingsAnchor} of the another object will be merged with this one,
     * chaining them using {@link SettingsAnchor#andThen(SettingsAnchor)} method,
     * using the anchor of the passed Settings object before the current anchor.
     *
     * This is advanced API, in most cases Configs should be merged instead.
     *
     * @param settings
     *            another Settings object to merge
     * @return new Settings object, containing merge results
     * @deprecated use {@link Settings#withFallback(Settings)} instead
     */
    @Deprecated
    public Settings merge(Settings settings) {
        return settings.withFallback(this);
    }

    /**
     * Merge another Settings object with this object, returning a new Settings
     * object. Scalar values and arrays of another object will be added to the
     * config if they are not present yet.
     *
     * The current object will not be modified.
     *
     * {@link SettingsAnchor} of the another object will be merged with this one,
     * chaining them using {@link SettingsAnchor#andThen(SettingsAnchor)} method,
     * using the anchor of the passed Settings object after the current anchor.
     *
     * This is advanced API, in most cases Configs should be merged instead.
     *
     * @param other
     *            another Settings object to merge
     * @return new Settings object, containing merge results
     * @see Config#withFallback(Config)
     */
    public Settings withFallback(Settings other) {
        JsonObject merged = mergeObject(other.root, this.root);
        return new Settings(merged, base.andThen(other.base));
    }

    private static JsonObject mergeObject(JsonObject left, JsonObject right) {
        if (left.isEmpty()) {
            return right;
        }
        if (right.isEmpty()) {
            return left;
        }
        JsonObjectBuilder builder = Json.createObjectBuilder(right);
        for (String leftKey : left.keySet()) {
            JsonValue leftValue = left.get(leftKey);
            JsonValue rightValue = right.get(leftKey);
            if (rightValue == null) {
                builder.add(leftKey, leftValue);
            } else if (leftValue instanceof JsonObject && rightValue instanceof JsonObject) {
                builder.add(leftKey, mergeObject((JsonObject) leftValue, (JsonObject) rightValue));
            }
        }
        return builder.build();
    }

}
