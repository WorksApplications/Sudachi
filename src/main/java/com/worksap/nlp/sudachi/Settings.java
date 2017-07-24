package com.worksap.nlp.sudachi;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class Settings {
    public String path;
    public String systemDict;
    public ArrayList<String> userDict;
    public ArrayList<InputTextPlugin> inputTextPlugin;
    public ArrayList<WordLookingUpPlugin> wordLookingUpPlugin;

    String getSystemDictPath() {
        if (systemDict == null) {
            throw new RuntimeException("system dictionary is not specified");
        }

        return (isAbsolutePath(systemDict) || path == null) ? systemDict
            : Paths.get(path, systemDict).toString();
    }

    List<String> getUserDictPath() {
        if (userDict == null) {
            return Collections.emptyList();
        }

        return userDict.stream()
            .map(u -> (isAbsolutePath(u) || path == null) ? u
                 : Paths.get(path, u).toString())
            .collect(Collectors.toList());
    }

    List<InputTextPlugin> getInputTextPlugin() {
        return (inputTextPlugin != null) ? inputTextPlugin
            : Collections.emptyList();
    }

    List<WordLookingUpPlugin> getWordLookingUpPlugin() {
        return (wordLookingUpPlugin != null) ? wordLookingUpPlugin
            : Collections.emptyList();
    }

    boolean isAbsolutePath(String path) {
        File file = new File(path);
        return file.isAbsolute();
    }
}
