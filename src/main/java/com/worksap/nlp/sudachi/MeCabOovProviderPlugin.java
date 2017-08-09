package com.worksap.nlp.sudachi;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.worksap.nlp.sudachi.dictionary.CategoryType;
import com.worksap.nlp.sudachi.dictionary.Grammar;
import com.worksap.nlp.sudachi.dictionary.WordInfo;

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

    Map<CategoryType, CategoryInfo> categories = new HashMap<>();
    Map<CategoryType, List<OOV>> oovList = new HashMap<>();

    @Override
    public void setUp(Grammar grammar) throws IOException {
        readCharacterProperty(settings.getPath("charDef"));
        readOOV(settings.getPath("unkDef"), grammar);
    }

    @Override
    public List<LatticeNode> provideOOV(InputText<?> inputText, int offset, boolean hasOtherWords) {
        List<LatticeNode> nodes = new ArrayList<>();
        int length = inputText.getCharCategoryContinuousLength(offset);
        if (length > 0) {
            for (CategoryType type : inputText.getCharCategoryTypes(offset)) {
                CategoryInfo cinfo = categories.get(type);
                int llength = length;
                List<OOV> oovs = oovList.get(cinfo.type);
                if (cinfo.isGroup &&
                    (cinfo.isInvoke || !hasOtherWords)) {
                    String s = inputText.getSubstring(offset, offset + length);
                    for (OOV oov : oovs) {
                        nodes.add(getOOVNode(s, oov, length));
                    }
                    llength -= 1;
                }
                if (cinfo.isInvoke || !hasOtherWords) {
                    for (int i = 1; i <= cinfo.length; i++) {
                        int sublength = inputText.getCodePointsOffsetLength(offset, i);
                        if (offset + sublength > llength) {
                            break;
                        }
                        String s = inputText.getSubstring(offset, offset + sublength);
                        for (OOV oov : oovs) {
                            nodes.add(getOOVNode(s, oov, sublength));
                        }
                    }
                }
            }
        }
        return nodes;
    }

    LatticeNode getOOVNode(String text, OOV oov, int length) {
        LatticeNode node = createNode();
        node.setParameter(oov.leftId, oov.rightId, oov.cost);
        WordInfo info
            = new WordInfo(text, (short)length, oov.posId, text, text, "");
        node.setWordInfo(info);
        return node;
    }

    void readCharacterProperty(String charDef) throws IOException {
        try (FileInputStream input = new FileInputStream(charDef);
             LineNumberReader reader
             = new LineNumberReader(new InputStreamReader(input))) {
            while (true) {
                String line = reader.readLine();
                if (line == null) {
                    break;
                }
                if (line.matches("\\s*") || line.startsWith("#")) {
                    continue;
                }
                String[] cols = line.split("\\s+");
                if (cols.length < 2) {
                    throw new RuntimeException("invalid format at line " +
                                               reader.getLineNumber());
                }
                if (cols[0].startsWith("0x")) {
                    continue;
                }
                CategoryType type = CategoryType.valueOf(cols[0]);
                if (type == null) {
                    throw new RuntimeException(cols[0] + " is invalid type at line "
                                               + reader.getLineNumber());
                }
                if (categories.containsKey(type)) {
                    throw new RuntimeException(cols[0] + " is already defined at line "
                                               + reader.getLineNumber());
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

    void readOOV(String unkDef, Grammar grammar) throws IOException {
        try (FileInputStream input = new FileInputStream(unkDef);
             LineNumberReader reader
             = new LineNumberReader(new InputStreamReader(input))) {
            while (true) {
                String line = reader.readLine();
                if (line == null) {
                    break;
                }
                String[] cols = line.split(",");
                if (cols.length < 10) {
                    throw new RuntimeException("invalid format at line " +
                                               reader.getLineNumber());
                }
                CategoryType type = CategoryType.valueOf(cols[0]);
                if (type == null) {
                    throw new RuntimeException(cols[0] + " is invalid type at line "
                                               + reader.getLineNumber());
                }
                if (!categories.containsKey(type)) {
                    throw new RuntimeException(cols[0] + " is undefined at line "
                                               + reader.getLineNumber());
                }

                OOV oov = new OOV();
                oov.leftId = Short.parseShort(cols[1]);
                oov.rightId = Short.parseShort(cols[2]);
                oov.cost = Short.parseShort(cols[3]);
                oov.posId
                    = grammar.getPartOfSpeechId(cols[4], cols[5], cols[6],
                                                cols[7], cols[8], cols[9]);

                if (!oovList.containsKey(type)) {
                    oovList.put(type, new ArrayList<OOV>());
                }
                oovList.get(type).add(oov);
            }
        }
    }
}
