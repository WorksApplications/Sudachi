package com.worksap.nlp.sudachi.dictionary.build;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Lookup2 {
    public interface Entry {
        int pointer();
        boolean matches(short posId, String reading);
        String headword();
    }

    public Lookup2(List<Entry> entries) {
        this.entries = entries;
        HashMap<String, List<Entry>> result = new HashMap<>(entries.size() * 4 / 3);
        for (Entry e : entries) {
            List<Entry> sublist = result.computeIfAbsent(e.headword(), x -> new ArrayList<>());
            sublist.add(e);
        }
        bySurface = result;
    }

    private final List<Entry> entries;
    private final Map<String, List<Entry>> bySurface;

    public Entry byIndex(int index) {
        return entries.get(index);
    }

    public List<Entry> byHeadword(String headword) {
        return bySurface.get(headword);
    }
}
