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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class Description {

    private Instant creationTime;

    private String comment;

    private String signature;

    private String reference;

    private List<Block> blocks = new ArrayList<>();

    public static class Block {
        private String name;
        private long start;
        private long size;
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
        desc.comment = reader.readUtf8String();
        desc.signature = reader.readUtf8String();
        desc.reference = reader.readUtf8String();
        int length = reader.readVarint32();
        for (int i = 0; i < length; ++i) {
            Block b = new Block();
            b.name = reader.readUtf8String();
            b.start = reader.readVarint64();
            b.size = reader.readVarint64();
            desc.blocks.add(b);
        }

        return desc;
    }

    public final static byte[] MAGIC_BYTES = "SudachiBinaryDic".getBytes(StandardCharsets.UTF_8);

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

    public Instant getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Instant creationTime) {
        this.creationTime = creationTime;
    }

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
}
