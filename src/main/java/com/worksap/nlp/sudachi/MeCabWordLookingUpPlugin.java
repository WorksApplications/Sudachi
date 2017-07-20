package com.worksap.nlp.sudachi;

import java.io.LineNumberReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

import com.worksap.nlp.sudachi.dictionary.Grammar;
import com.worksap.nlp.sudachi.dictionary.WordInfo;

class MeCabWordLookingUpPlugin extends WordLookingUpPlugin {

    static class Range {
        int low;
        int high;
        List<CategoryInfo> categories = new ArrayList<>();

        int containingLength(String text) {
            for (int i = 0; i < text.length();
                 i = text.offsetByCodePoints(i, 1)) {
                int c = text.codePointAt(i);
                if (c < low || c > high) {
                    return i;
                }
            }
            return text.length();
        }
    }

    static class CategoryInfo {
        String name;
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

    Map<String, CategoryInfo> categories = new HashMap<>();
    List<Range> rangeList = new ArrayList<>();
    Map<String, List<OOV>> oovList = new HashMap<>();

    MeCabWordLookingUpPlugin(Grammar grammar,
                              InputStream charDef, InputStream unkDef)
        throws IOException {
        readCharacterProperty(charDef);
        readOOV(unkDef, grammar);
    }

    @Override
    public List<LatticeNode> provideOOV(String text,
                                        List<Integer> otherWordsLength) {
        List<LatticeNode> nodes = new ArrayList<>();
        for (Range r : rangeList) {
            int length = r.containingLength(text);
            if (length == 0) {
                continue;
            }

            for (CategoryInfo cinfo : r.categories) {
                int llength = length;
                List<OOV> oovs = oovList.get(cinfo.name);
                if (cinfo.isGroup &&
                    (cinfo.isInvoke || !otherWordsLength.contains(length))) {
                    String s = text.substring(0, length);
                    for (OOV oov : oovs) {
                        nodes.add(getOOVNode(s, oov));
                    }
                    llength -= 1;
                }
                if (cinfo.length > 0) {
                    int lim = Math.min(cinfo.length, llength);
                    for (int i = 1; i <= lim; i++) {
                        String s = text.substring(0, i);
                        for (OOV oov : oovs) {
                            nodes.add(getOOVNode(s, oov));
                        }
                    }
                }
            }
            break;
        }
        return nodes;
    }

    LatticeNode getOOVNode(String text, OOV oov) {
        LatticeNode node = createNode();
        node.setParameter(oov.leftId, oov.rightId, oov.cost);
        WordInfo info
            = new WordInfo(text, oov.posId, text, text, "");
        node.setWordInfo(info);
        return node;
    }

    void readCharacterProperty(InputStream charDef) throws IOException {
        LineNumberReader reader
            = new LineNumberReader(new InputStreamReader(charDef));
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
                Range range = new Range();
                String[] r = cols[0].split("\\.\\.");
                range.low = range.high = Integer.decode(r[0]);
                if (r.length > 1) {
                    range.high = Integer.decode(r[1]);
                }
                if (range.low > range.high) {
                    throw new RuntimeException("invalid range at line " +
                                               reader.getLineNumber());
                }
                for (int i = 1; i < cols.length; i++) {
                    if (cols[i].startsWith("#")) {
                        break;
                    }
                    if (!categories.containsKey(cols[i])) {
                        throw new RuntimeException(cols[i] + " is undefined at line "
                                                   + reader.getLineNumber());
                    }
                    range.categories.add(categories.get(cols[i]));
                }
                rangeList.add(range);
            } else {
                if (cols.length < 4) {
                    throw new RuntimeException("invalid format at line " +
                                               reader.getLineNumber());
                }
                String key = cols[0];
                if (categories.containsKey(key)) {
                    throw new RuntimeException(cols[0] + " is already defined at line "
                                               + reader.getLineNumber());
                }
                CategoryInfo info = new CategoryInfo();
                info.name = key;
                info.isInvoke = (!cols[1].equals("0"));
                info.isGroup = (!cols[2].equals("0"));
                info.length = Integer.parseInt(cols[3]);
                categories.put(key, info);
            }
            Range defaultRange = new Range();
            defaultRange.low = 0;
            defaultRange.high = Integer.MAX_VALUE;
            defaultRange.categories
                = Collections.singletonList(categories.get("DEFAULT"));
        }
    }

    void readOOV(InputStream unkDef, Grammar grammar) throws IOException {
        LineNumberReader reader
            = new LineNumberReader(new InputStreamReader(unkDef));
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
            if (!categories.containsKey(cols[0])) {
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

            if (!oovList.containsKey(cols[0])) {
                oovList.put(cols[0], new ArrayList<OOV>());
            }
            oovList.get(cols[0]).add(oov);
        }
    }
}
