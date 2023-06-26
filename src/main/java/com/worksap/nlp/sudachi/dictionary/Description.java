/*
 * Copyright (c) 2022 Works Applications Co., Ltd.
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

package com.worksap.nlp.sudachi.dictionary;

import com.worksap.nlp.sudachi.dictionary.build.BufWriter;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/**
 * Description of the dictionary blocks, in-memory representation.
 * Basically, an extended version of the dictionary header.
 */
public class Description {
    private Instant creationTime = Instant.now();
    private String comment = "";
    private String signature = defaultSignature(creationTime);
    private String reference = "";
    private List<Block> blocks = new ArrayList<>();
    private long flags;
    private int numTotalEntries;
    private int numIndexedEntries;

    /**
     * Return a slice of the full dictionary with the provided name
     * @param full ByteBuffer which represents the whole dictionary loaded into memory
     * @param part name of the required part
     * @return slice of the ByteBuffer
     * @throws IllegalArgumentException if the part with the provided name was not found
     */
    public ByteBuffer slice(ByteBuffer full, String part) {
        ByteBuffer slice = sliceOrNull(full, part);
        if (slice == null) {
            throw new IllegalArgumentException("Dictionary did not contain part with name=" + part);
        }
        return slice;
    }

    /**
     * Return a slice of the full dictionary with the provided name
     * @param full ByteBuffer which represents the whole dictionary loaded into memory
     * @param part name of the required part
     * @return slice of the ByteBuffer or null if not found
     */
    public ByteBuffer sliceOrNull(ByteBuffer full, String part) {
        for (Block b: blocks) {
            if (b.name.equals(part)) {
                int start = (int)b.start;
                int end = (int)(b.start + b.size);
                int position = full.position();
                int limit = full.limit();
                full.position(start);
                full.limit(end);
                ByteBuffer slice = full.slice();
                full.position(position);
                full.limit(limit);
                slice.order(ByteOrder.LITTLE_ENDIAN);
                return slice;
            }
        }
        return null;
    }

    public boolean isSystemDictionary() {
        return reference.isEmpty();
    }

    public boolean isUserDictionary() {
        return !reference.isEmpty();
    }

    public long getNumTotalEntries() {
        return numTotalEntries;
    }

    public static class Block {
        private final String name;
        private final long start;
        private final long size;

        public Block(String name, long start, long size) {
            this.name = name;
            this.start = start;
            this.size = size;
        }

        public String getName() {
            return name;
        }

        public long getStart() {
            return start;
        }

        public long getSize() {
            return size;
        }
    }

    public static Description load(SeekableByteChannel channel) throws IOException {
        ByteBuffer buf = ByteBuffer.allocate(4096);
        buf.order(ByteOrder.LITTLE_ENDIAN);
        if (channel.read(buf) == -1) {
            throw new IllegalArgumentException("end of channel");
        }
        buf.flip();
        return load(buf);
    }

    public static Description load(ByteBuffer raw) {
        checkLegacyDictionaryFormat(raw);
        checkMagic(raw);
        long version = raw.getLong();
        if (version == 1) {
            return loadV1(raw);
        } else {
            throw new IllegalArgumentException(String.format("invalid version %d, corrupted dictionary", version));
        }
    }

    private static Description loadV1(ByteBuffer raw) {
        Description desc = new Description();
        BufReader reader = new BufReader(raw);
        desc.creationTime = Instant.ofEpochSecond(reader.readLong());
        desc.flags = reader.readLong();
        desc.comment = reader.readUtf8String();
        desc.signature = reader.readUtf8String();
        desc.reference = reader.readUtf8String();
        desc.numIndexedEntries = reader.readVarint32();
        desc.numTotalEntries = reader.readVarint32();
        int length = reader.readVarint32();
        for (int i = 0; i < length; ++i) {
            Block b = new Block(reader.readUtf8String(), reader.readVarint64(), reader.readVarint64());
            desc.blocks.add(b);
        }

        return desc;
    }

    public void save(SeekableByteChannel channel) throws IOException {
        ByteBuffer buff = ByteBuffer.allocate(4096);
        buff.order(ByteOrder.LITTLE_ENDIAN);
        buff.put(MAGIC_BYTES);
        BufWriter writer = new BufWriter(buff);
        writer.putLong(1); // version
        writer.putLong(creationTime.getEpochSecond());
        writer.putLong(flags);
        writer.putStringUtf8(comment);
        writer.putStringUtf8(signature);
        writer.putStringUtf8(reference);
        writer.putVarint32(numIndexedEntries);
        writer.putVarint32(numTotalEntries);
        int length = blocks.size();
        writer.putVarint32(length);
        for (Block b : blocks) {
            writer.putStringUtf8(b.name);
            writer.putVarint64(b.start);
            writer.putVarint64(b.size);
        }

        long pos = channel.position();
        channel.position(0);
        buff.flip();
        channel.write(buff);
        channel.position(pos);
    }

    private final static byte[] MAGIC_BYTES = "SudachiBinaryDic".getBytes(StandardCharsets.UTF_8);

    private static void checkMagic(ByteBuffer raw) {
        assert MAGIC_BYTES.length == 16;
        byte[] expected = new byte[MAGIC_BYTES.length];
        raw.get(expected);
        for (int i = 0; i < expected.length; i++) {
            if (MAGIC_BYTES[i] != expected[i]) {
                throw new IllegalArgumentException("invalid magic string, dictionary is corrupted");
            }
        }
    }

    private static void checkLegacyDictionaryFormat(ByteBuffer raw) {
        long version = raw.getLong(0);
        if (DictionaryVersion.isSystemDictionary(version)) {
            throw new IllegalArgumentException("passed dictionary is a legacy system dictionary, please rebuild it");
        }
        if (DictionaryVersion.isUserDictionary(version)) {
            throw new IllegalArgumentException("passed dictionary is a legacy user dictionary, please rebuild it");
        }
    }

    private String defaultSignature(Instant date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss", Locale.US);
        return String.format("%s-%08x", formatter.format(LocalDateTime.ofInstant(date, ZoneId.systemDefault())), new Random().nextLong());
    }

    public Instant getCreationTime() {
        return creationTime;
    }

    public void setCompilationTime(Instant creationTime) {
        this.creationTime = creationTime;
    }

    @Deprecated
    public String getDescription() { return getComment(); }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public List<Block> getBlocks() {
        return blocks;
    }

    public void setBlocks(List<Block> blocks) {
        this.blocks = blocks;
    }

    public void setRuntimeCosts(boolean val) {
        long x = val ? 1 : 0;
        flags = (flags & ~0x1L) | x;
    }

    public boolean isRuntimeCosts() {
        return (flags & 0x1L) != 0;
    }

    public void setNumberOfEntries(int indexed, int total) {
        this.numIndexedEntries = indexed;
        this.numTotalEntries = total;
    }
}
