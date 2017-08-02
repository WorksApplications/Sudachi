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
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonString;
import javax.json.JsonStructure;
import javax.json.JsonValue;
import javax.json.stream.JsonParsingException;

public class Settings {

    JsonObject root;

    Settings(JsonObject root) {
        this.root = root;
    }

    static Settings parseSettings(String json) {
        try (JsonReader reader = Json.createReader(new StringReader(json))) {
            JsonStructure root = reader.read();
            if (root instanceof JsonObject) {
                return new Settings((JsonObject)root);
            } else {
                throw new IllegalArgumentException("root must be a object");
            }
        } catch (JsonParsingException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public String getString(String setting) {
        return root.getString(setting, null);
    }

    public String getString(String setting, String defaultValue) {
        return root.getString(setting, defaultValue);
    }

    public List<String> getStringList(String setting) {
        JsonArray array = root.getJsonArray(setting);
        if (array == null) {
            return Collections.emptyList();
        }
        List<String> result = new ArrayList<>(array.size());
        for (int i = 0; i < array.size(); i++) {
            result.add(array.getString(i));
        }
        return result;
    }

    public int getInt(String setting) {
        return root.getInt(setting, 0);
    }

    <E extends JsonValue> List<E> getList(String setting, Class<E> clazz) {
        JsonArray array = root.getJsonArray(setting);
        if (array == null) {
            return Collections.emptyList();
        }
        return array.getValuesAs(clazz);
    }

    String getSystemDictPath() {
        String systemDict = getString("systemDict");
        if (systemDict == null) {
            throw new IllegalArgumentException("system dictionary is not specified");
        }

        String path = getString("path");
        return (isAbsolutePath(systemDict) || path == null) ? systemDict
            : Paths.get(path, systemDict).toString();
    }

    List<String> getUserDictPath() {
        List<String> userDict = getStringList("userDict");
        if (userDict.isEmpty()) {
            return Collections.emptyList();
        }

        String path = getString("path");
        return userDict.stream()
            .map(u -> (isAbsolutePath(u) || path == null) ? u
                 : Paths.get(path, u).toString())
            .collect(Collectors.toList());
    }
    
    String getCharacterDefinitionFilePath() {
        String charDefinitionFile = getString("characterDefinitionFile");
        if (charDefinitionFile == null) {
            return null;
        }

        String path = getString("path");
        return (isAbsolutePath(charDefinitionFile) || path == null) ? charDefinitionFile
            : Paths.get(path, charDefinitionFile).toString();
    }


    private <E extends Plugin> List<E> getPlugin(String setting) {
        List<JsonObject> list = getList(setting, JsonObject.class);
        if (list.isEmpty()) {
            return Collections.emptyList();
        }

        List<E> result = new ArrayList<>(list.size());
        for (int i = 0; i < list.size(); i++) {
            Object o;
            String classname = list.get(i).getString("class");
            try {
                o = this.getClass().getClassLoader()
                    .loadClass(classname).newInstance();
            } catch (ClassNotFoundException | InstantiationException
                     | IllegalAccessException e) {
                throw new IllegalArgumentException(e);
            }
            if (!(o instanceof Plugin)) {
                throw new IllegalArgumentException(classname + "is not plugin");
            }
            @SuppressWarnings("unchecked") E plugin = (E)o;
            plugin.setSettings(new Settings(list.get(i)));
            result.add(plugin);
        }
        return result;
    }

    List<InputTextPlugin> getInputTextPlugin() {
        return getPlugin("inputTextPlugin");
    }

    List<OovProviderPlugin> getOovProviderPlugin() {
        return getPlugin("oovProviderPlugin");
    }

    List<PathRewritePlugin> getPathRewritePlugin() {
        return getPlugin("pathRewritePlugin");
    }

    private boolean isAbsolutePath(String path) {
        File file = new File(path);
        return file.isAbsolute();
    }
}
