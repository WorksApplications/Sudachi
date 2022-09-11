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

import com.worksap.nlp.sudachi.dictionary.CategoryType;
import com.worksap.nlp.sudachi.dictionary.Grammar;
import com.worksap.nlp.sudachi.dictionary.POS;
import com.worksap.nlp.sudachi.dictionary.WordInfo;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Provides the OOVs in the same way as MeCab.
 *
 * <p>
 * The following is an example of settings.
 * 
 * <pre>
 * {@code
 *   {
 *     "class"   : "com.worksap.nlp.sudachi.MeCabOovProviderPlugin",
 *     "charDef" : "char.def",
 *     "unkDef"  : "unk.def"
 * }
 * }
 * </pre>
 *
 * {@code charDef} is the file path of the definition of OOV insertion behavior.
 * {@code unkDef} is the file path of the definition of OOV informations.
 *
 * These files are compatible with MeCab. But the definitions of character
 * categories in {@code charDef} are ignored and this plugin uses the ones
 * {@code characterDefinitionFile} in the settings.
 */
class MeCabOovProviderPlugin extends OovProviderPlugin {

    static class CategoryInfo {
        CategoryType type;
        boolean isInvoke;
        boolean isGroup;
        int length;
    }

    static class OOV {
        short leftId;
        short rightId;
        short cost;
        short posId;
    }

    Map<CategoryType, CategoryInfo> categories = new EnumMap<>(CategoryType.class);
    Map<CategoryType, List<OOV>> oovList = new EnumMap<>(CategoryType.class);

    @Override
    public void setUp(Grammar grammar) throws IOException {
        Config.Resource<Object> charDef = settings.getResource("charDef");
        readCharacterProperty(charDef);
        Config.Resource<Object> unkDef = settings.getResource("unkDef");
        readOOV(unkDef, grammar, settings.getString(USER_POS, USER_POS_FORBID));
    }

    @Override
    public int provideOOV(InputText inputText, int offset, long otherWords, List<LatticeNodeImpl> nodes) {
        int length = inputText.getCharCategoryContinuousLength(offset);
        int added = 0;
        if (length > 0) {
            for (CategoryType type : inputText.getCharCategoryTypes(offset)) {
                CategoryInfo cinfo = categories.get(type);
                if (cinfo == null) {
                    continue;
                }
                int llength = length;
                List<OOV> oovs = oovList.get(cinfo.type);
                if (oovs == null) {
                    continue;
                }
                if (cinfo.isGroup && (cinfo.isInvoke || otherWords == 0)) {
                    String s = inputText.getSubstring(offset, offset + length);
                    for (OOV oov : oovs) {
                        nodes.add(getOOVNode(s, oov, length));
                        added += 1;
                    }
                    llength -= 1;
                }
                if (cinfo.isInvoke || otherWords == 0) {
                    for (int i = 1; i <= cinfo.length; i++) {
                        int sublength = inputText.getCodePointsOffsetLength(offset, i);
                        if (sublength > llength) {
                            break;
                        }
                        String s = inputText.getSubstring(offset, offset + sublength);
                        for (OOV oov : oovs) {
                            nodes.add(getOOVNode(s, oov, sublength));
                            added += 1;
                        }
                    }
                }
            }
        }
        return added;
    }

    LatticeNodeImpl getOOVNode(String text, OOV oov, int length) {
        LatticeNodeImpl node = createNode();
        node.setParameter(oov.leftId, oov.rightId, oov.cost);
        WordInfo info = new WordInfo(text, (short) length, oov.posId, text, text, "");
        node.setWordInfo(info);
        return node;
    }

    private static final Pattern PATTERN_SPACES = Pattern.compile("\\s+");
    private static final Pattern PATTERN_EMPTY_OR_SPACES = Pattern.compile("\\s*");

    <T> void readCharacterProperty(Config.Resource<T> charDef) throws IOException {
        if (charDef == null) {
            charDef = settings.base.toResource(Paths.get("char.def"));
        }
        try (InputStream input = charDef.asInputStream();
                InputStreamReader isReader = new InputStreamReader(input, StandardCharsets.UTF_8);
                LineNumberReader reader = new LineNumberReader(isReader)) {
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                if (line.startsWith("#") || line.startsWith("0x") || PATTERN_EMPTY_OR_SPACES.matcher(line).matches()) {
                    continue;
                }
                String[] cols = PATTERN_SPACES.split(line);
                if (cols.length < 4) {
                    throw new IllegalArgumentException("invalid format at line " + reader.getLineNumber());
                }
                CategoryType type;
                try {
                    type = CategoryType.valueOf(cols[0]);
                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException(cols[0] + " is invalid type at line " + reader.getLineNumber(),
                            e);
                }
                if (categories.containsKey(type)) {
                    throw new IllegalArgumentException(
                            cols[0] + " is already defined at line " + reader.getLineNumber());
                }
                CategoryInfo info = new CategoryInfo();
                info.type = type;
                info.isInvoke = (!cols[1].equals("0"));
                info.isGroup = (!cols[2].equals("0"));
                info.length = Integer.parseInt(cols[3]);
                categories.put(type, info);
            }
        }
    }

    <T> void readOOV(Config.Resource<T> unkDef, Grammar grammar, String userPosMode) throws IOException {
        if (unkDef == null) {
            unkDef = settings.base.toResource(Paths.get("unk.def"));
        }
        try (InputStream input = unkDef.asInputStream();
                InputStreamReader isReader = new InputStreamReader(input, StandardCharsets.UTF_8);
                LineNumberReader reader = new LineNumberReader(isReader)) {
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                String[] cols = line.split(",");
                if (cols.length < 10) {
                    throw new IllegalArgumentException("invalid format at line " + reader.getLineNumber());
                }
                CategoryType type;
                try {
                    type = CategoryType.valueOf(cols[0]);
                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException(cols[0] + " is invalid type at line " + reader.getLineNumber(),
                            e);
                }
                if (!categories.containsKey(type)) {
                    throw new IllegalArgumentException(cols[0] + " is undefined at line " + reader.getLineNumber());
                }

                OOV oov = new OOV();
                oov.leftId = Short.parseShort(cols[1]);
                oov.rightId = Short.parseShort(cols[2]);
                oov.cost = Short.parseShort(cols[3]);
                POS pos = new POS(cols[4], cols[5], cols[6], cols[7], cols[8], cols[9]);
                oov.posId = posIdOf(grammar, pos, userPosMode);

                oovList.computeIfAbsent(type, t -> new ArrayList<>()).add(oov);
            }
        }
    }
}
