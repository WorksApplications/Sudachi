package com.worksap.nlp.sudachi.dictionary.build;

import com.worksap.nlp.sudachi.dictionary.CSVParser;
import com.worksap.nlp.sudachi.dictionary.StringPtr;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.channels.SeekableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;

public class UniqueStrings {
    private final HashMap<String, Item> strings = new HashMap<>();
    private final HashMap<String, Item> candidates = new HashMap<>();
    private final WordLayout layout = new WordLayout();

    void add(String data) {
        strings.put(data, null);
    }

    void compile() {
        candidates.clear();
        candidates.put("", new Item("", 0, 0));
        List<String> collect = new ArrayList<>(strings.keySet());
        collect.sort(Comparator.comparingInt(String::length).reversed().thenComparing(String::compareTo));
        for (String str: collect) {
            strings.put(str, process(str));
        }
        candidates.clear();
    }

    private Item process(String str) {
        Item present = candidates.get(str);
        if (present != null) {
            return present;
        }

        int length = str.length();
        int[] offsets = new int[length + 1];
        int numOffsets = computeOffsets(str, offsets);

        StringPtr ptr = layout.add(str, 0, length);
        Item full = new Item(str, 0, length);
        full.root = full;
        full.ptr = ptr;
        candidates.put(str, full);

        for (int i = 0; i < numOffsets; ++i) {
            int start = offsets[i];
            for (int j = i + 1; j <= numOffsets; ++j) {
                int end = offsets[j];
                String sub = str.substring(start, end);
                // Create a possible substring only if
                // 1. It does not exist yet
                // 2. Can form a valid pointer to it
                if (!candidates.containsKey(sub) && ptr.isSubseqValid(start, end)) {
                    Item item = new Item(str, start, end);
                    item.root = full;
                    candidates.put(sub, item);
                }
            }
        }

        return full;
    }

    private int computeOffsets(String str, int[] offsets) {
        int count = 0;
        int len = str.length();
        for (int i = 0; i < len; ++i) {
            char ch = str.charAt(i);
            if (Character.isLowSurrogate(ch)) {
                if (i + 1 < len && Character.isHighSurrogate(str.charAt(i + 1))) {
                    i += 1;
                }
            }
            offsets[count] = i;
            count += 1;
        }
        offsets[count] = len;
        return count;
    }

    public HashMap<String, Item> getStrings() {
        return strings;
    }

    public void writeCompact(WritableByteChannel channel) throws IOException {
        layout.write(channel);
    }

    public void writeLengthPrefixedCompact(SeekableByteChannel channel) throws IOException {
        DicBuffer buf = new DicBuffer(64 * 1024);
        for (Map.Entry<String, Item> item: strings.entrySet()) {
            Item value = item.getValue();
            String sub = value.data.substring(value.start, value.end);
            if (buf.wontFit(sub.length() * 2)) {
                buf.consume(channel::write);
            }
            buf.put(sub);
        }
        buf.consume(channel::write);
    }

    public static class Item {
        private final String data;
        private final int start;
        private final int end;
        private Item root;
        private StringPtr ptr;

        public Item(String data, int start, int end) {
            this.data = data;
            this.start = start;
            this.end = end;
        }

        public String getData() {
            return data;
        }

        public int getStart() {
            return start;
        }

        public int getEnd() {
            return end;
        }

        public int getLength() {
            return end - start;
        }
    }


    public static void main(String[] args) throws IOException {
        UniqueStrings strings = new UniqueStrings();
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(args[0]))) {
            CSVParser parser = new CSVParser(reader);
            List<String> record;
            while ((record = parser.getNextRecord()) != null) {
                strings.add(record.get(0));
                strings.add(record.get(4));
                strings.add(record.get(11));
                strings.add(record.get(12));
            }
        }
        strings.compile();

        Path fullName = Paths.get(args[1] + ".lpf");
        try (SeekableByteChannel chan = Files.newByteChannel(fullName, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
            strings.writeLengthPrefixedCompact(chan);
        }

        Path compactName = Paths.get(args[1] + ".cmp");
        try (SeekableByteChannel chan = Files.newByteChannel(compactName, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
            strings.writeCompact(chan);
        }
        System.out.printf("wasted bytes=%d, slots=%d%n", strings.layout.wastedBytes(), strings.layout.numSlots());
    }
}
