package com.worksap.nlp.sudachi;

import java.io.File;
import java.io.StringReader;
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
import javax.json.JsonString;
import javax.json.JsonStructure;
import javax.json.JsonValue;
import javax.json.stream.JsonParsingException;

public class Settings {

    JsonObject root;
    String basePath;

    Settings(JsonObject root, String basePath) {
        this.root = root;
        this.basePath = basePath;
    }

    static Settings parseSettings(String json) {
        try (JsonReader reader = Json.createReader(new StringReader(json))) {
            JsonStructure rootStr = reader.read();
            if (rootStr instanceof JsonObject) {
                JsonObject root = (JsonObject)rootStr;
                String basePath = root.getString("path", null);
                return new Settings(root, basePath);
            } else {
                throw new IllegalArgumentException("root must be an object");
            }
        } catch (JsonParsingException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public String getString(String setting) {
        try {
            return root.getString(setting);
        } catch (NullPointerException e) {
            return null;
        } catch (ClassCastException e) {
            throw new IllegalArgumentException(setting + " is not a string", e);
        }
    }

    public String getString(String setting, String defaultValue) {
        return root.getString(setting, defaultValue);
    }

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

    public int getInt(String setting) {
        try {
            return root.getInt(setting);
        } catch (NullPointerException e) {
            return 0;
        } catch (ClassCastException e) {
            throw new IllegalArgumentException(setting + " is not a number", e);
        }
    }

    public List<Integer> getIntList(String setting) {
        try {
            return getList(setting, JsonNumber.class).stream()
                .map(i -> i.intValue()).collect(Collectors.toList());
        } catch (ClassCastException e) {
            throw new IllegalArgumentException(setting + " is not an array of numbers", e);
        }
    }

    public List<List<Integer>> getIntListList(String setting) {
        try {
            return getList(setting, JsonArray.class).stream()
                .map(a -> a.getValuesAs(JsonNumber.class).stream()
                     .map(i -> i.intValue()).collect(Collectors.toList()))
                .collect(Collectors.toList());
        } catch (ClassCastException e) {
            throw new IllegalArgumentException(setting + " is not an array of arrays of numbers", e);
        }
    }

    public String getPath(String setting) {
        String path = getString(setting);
        return (path == null || isAbsolutePath(path) || basePath == null) ?
            path : Paths.get(basePath, path).toString();
    }

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
            throw new IllegalArgumentException(setting + " is not a list of object", e);
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
                    .loadClass(classname).newInstance();
            } catch (ClassNotFoundException | InstantiationException
                     | IllegalAccessException e) {
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
