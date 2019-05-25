/*
 * Copyright (c) 2017 Works Applications Co., Ltd.
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

import java.io.File;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonStructure;
import javax.json.JsonValue;
import javax.json.stream.JsonParsingException;

/**
 * A structure of settings.
 *
 * This class reads a settings written in JSON.
 *
 * <p>The following is an example of settings.
 * <pre>{@code
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
 * }</pre>
 *
 * <p>{@code path} is a reserved key. Its value is used in {@link getPath}
 * as the base path to make an absolute path from a relative path.
 */
public class Settings {

    JsonObject root;
    String basePath;

    Settings(JsonObject root, String basePath) {
        this.root = root;
        this.basePath = basePath;
    }

    /**
     * Read a settings from a JSON string.
     *
     * The root level of JSON must be a Object.
     *
     * @param path the base path if "path" is undefined in {@code json}
     * @param json JSON string
     * @return a structure of settings
     * @throws IllegalArgumentException if the parsing is failed
     */
    public static Settings parseSettings(String path, String json) {
        try (JsonReader reader = Json.createReader(new StringReader(json))) {
            JsonStructure rootStr = reader.read();
            if (rootStr instanceof JsonObject) {
                JsonObject root = (JsonObject)rootStr;
                String basePath = root.getString("path", path);
                return new Settings(root, basePath);
            } else {
                throw new IllegalArgumentException("root must be an object");
            }
        } catch (JsonParsingException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Returns the value as the string to which the specified key is mapped,
     * or {@code null} if this settings contains no mapping for the key.
     *
     * @param setting the key
     * @return the value or {@code null} if this settings has no mapping
     * @throws IllegalArgumentException if the value is not a string
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
     * Returns the value as the string to which the specified key is mapped,
     * or {@code defaultValue} if this settings contains
     * no mapping for the key.
     *
     * @param setting the key
     * @param defaultValue the default mapping of the key
     * @return the value or {@code defaultValue} if this settings
     *         has no mapping
     * @throws IllegalArgumentException if the value is not a string
     */
    public String getString(String setting, String defaultValue) {
        return root.getString(setting, defaultValue);
    }

    /**
     * Returns the value as the list of strings to which the specified
     * key is mapped, or an empty list if this settings contains
     * no mapping for the key.
     *
     * @param setting the key
     * @return the value or a empty list if this settings has no mapping
     * @throws IllegalArgumentException if the value is not an array of strings
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
     * Returns the value as the integer to which the specified key is mapped,
     * or 0 if this settings contains no mapping for the key.
     *
     * @param setting the key
     * @return the value or 0 if this settings has no mapping
     * @throws IllegalArgumentException if the value is not an integer
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
     * Returns the value as the string to which the specified key is mapped,
     * or {@code defaultValue} if this settings contains
     * no mapping for the key.
     *
     * @param setting the key
     * @param defaultValue the default mapping of the key
     * @return the value or {@code defaultValue} if this settings
     *         has no mapping
     * @throws IllegalArgumentException if the value is not a string
     */
    public int getInt(String setting, int defaultValue) {
        try {
            return root.getInt(setting, defaultValue);
        } catch (ClassCastException e) {
            throw new IllegalArgumentException(setting + " is not a number", e);
        }
    }

    /**
     * Returns the value as the list of integers to which the specified
     * key is mapped, or an empty list if this settings contains
     * no mapping for the key.
     *
     * @param setting the key
     * @return the value or a empty list if this settings has no mapping
     * @throws IllegalArgumentException if the value is not an array
     *         of integers
     */
    public List<Integer> getIntList(String setting) {
        try {
            return getList(setting, JsonNumber.class).stream()
                .map(JsonNumber::intValue).collect(Collectors.toList());
        } catch (ClassCastException e) {
            throw new IllegalArgumentException(setting + " is not an array of numbers", e);
        }
    }

    /**
     * Returns the value as the list of lists of integers to which
     * the specified key is mapped, or an empty list if this settings contains
     * no mapping for the key.
     *
     * @param setting the key
     * @return the value or an empty list if this settings has no mapping
     * @throws IllegalArgumentException if the value is not an array of arrays
     *         of integers
     */
    public List<List<Integer>> getIntListList(String setting) {
        try {
            return getList(setting, JsonArray.class).stream()
                .map(a -> a.getValuesAs(JsonNumber.class).stream()
                     .map(JsonNumber::intValue).collect(Collectors.toList()))
                .collect(Collectors.toList());
        } catch (ClassCastException e) {
            throw new IllegalArgumentException(setting + " is not an array of arrays of numbers", e);
        }
    }

    /**
     * Returns the value as the file path to which the specified key is mapped,
     * or {@code null} if this settings contains no mapping for the key.
     *
     * <p>If {@code "path"} is specified in the root object and
     * the value is not an absolute path, this method joins them
     * using the {@link java.nio.file.FileSystem#getSeparator name-separator}
     * as the separator.
     *
     * @param setting the key
     * @return the value or {@code null} if this settings has no mapping
     * @throws IllegalArgumentException if the value is not a string
     */
    public String getPath(String setting) {
        String path = getString(setting);
        return (path == null || isAbsolutePath(path) || basePath == null) ?
            path : Paths.get(basePath, path).toString();
    }

    /**
     * Returns the value as the list of file paths to which the specified
     * key is mapped, or an empty list if this settings contains
     * no mapping for the key.
     *
     * <p>If {@code "path"} is specified in the root object and
     * the file path is not an absolute path, this method joins them
     * using the {@link java.nio.file.FileSystem#getSeparator name-separator}
     * as the separator.
     *
     * @param setting the key
     * @return the value or an empty list if this settings has no mapping
     * @throws IllegalArgumentException if the value is not an array of strings
     */
    public List<String> getPathList(String setting) {
        List<String> list = getStringList(setting);
        if (list.isEmpty()) {
            return Collections.emptyList();
        }
        return list.stream()
            .map(p -> (isAbsolutePath(p) || basePath == null) ? p
                 : Paths.get(basePath, p).toString())
            .collect(Collectors.toList());
    }

    /**
     * Returns the value as the boolean to which the specified key is mapped,
     * or {@code defaultValue} if this settings contains
     * no mapping for the key.
     *
     * @param setting the key
     * @param defaultValue the default mapping of the key
     * @return the value or {@code defaultValue} if this settings
     *         has no mapping
     * @throws IllegalArgumentException if the value is not a boolean
     */
    public boolean getBoolean(String setting, boolean defaultValue) {
        return root.getBoolean(setting, defaultValue);
    }

    <E extends JsonValue> List<E> getList(String setting, Class<E> clazz) {
        JsonArray array = root.getJsonArray(setting);
        if (array == null) {
            return Collections.emptyList();
        }
        return array.getValuesAs(clazz);
    }

    <E extends Plugin> List<E> getPluginList(String setting) {
        List<JsonObject> list;
        try {
            list = getList(setting, JsonObject.class);
        } catch (ClassCastException e) {
            throw new IllegalArgumentException(setting + " is not a array of object", e);
        }
        if (list.isEmpty()) {
            return Collections.emptyList();
        }

        List<E> result = new ArrayList<>(list.size());
        for (int i = 0; i < list.size(); i++) {
            Object o;
            String classname;

            try {
                classname = list.get(i).getString("class");
            } catch (NullPointerException e) {
                throw new IllegalArgumentException(setting + " has a member without a \"class\"", e);
            } catch (ClassCastException e) {
                throw new IllegalArgumentException(setting + " has a member with invalid \"class\"", e);
            }

            try {
                o = this.getClass().getClassLoader()
                    .loadClass(classname).getDeclaredConstructor().newInstance();
            } catch (ClassNotFoundException
                     | NoSuchMethodException | SecurityException
                     | InstantiationException | IllegalAccessException
                     | IllegalArgumentException | InvocationTargetException e) {
                throw new IllegalArgumentException(classname + " in " + setting + " cannot be initialized", e);
            }

            if (!(o instanceof Plugin)) {
                throw new IllegalArgumentException(classname + " in " + setting + " is not a plugin");
            }

            @SuppressWarnings("unchecked") E plugin = (E)o;
            plugin.setSettings(new Settings(list.get(i), basePath));
            result.add(plugin);
        }
        return result;
    }

    private boolean isAbsolutePath(String path) {
        File file = new File(path);
        return file.isAbsolute();
    }
}
