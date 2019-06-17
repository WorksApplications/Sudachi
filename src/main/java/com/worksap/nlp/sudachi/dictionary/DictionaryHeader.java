/*
 * Copyright (c) 2019 Works Applications Co., Ltd.
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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

/**
 * A header of a dictionary file.
 */
public class DictionaryHeader {

    private final long version;
    private final long createTime;
    private final String description;

    private static final int DESCRIPTION_SIZE = 256;
    private static final int STORAGE_SIZE = 8 + 8 + DESCRIPTION_SIZE;

    DictionaryHeader(long version, long createTime, String description) {
        this.version = version;
        this.createTime = createTime;
        this.description = description;
    }

    public DictionaryHeader(ByteBuffer input, int offset) {
        version = input.getLong(offset);
        offset += 8;
        createTime = input.getLong(offset);
        offset += 8;
        byte[] byteDescription = new byte[DESCRIPTION_SIZE];
        int length;
        for (length = 0; length < DESCRIPTION_SIZE; length++) {
            byteDescription[length] = input.get(offset++);
            if (byteDescription[length] == 0) {
                break;
            }
        }
        description = new String(byteDescription, 0, length, StandardCharsets.UTF_8);
    }

    public int storageSize() {
        return STORAGE_SIZE;
    }

    byte[] toByte() {
        byte[] output = new byte[STORAGE_SIZE];
        ByteBuffer buffer = ByteBuffer.wrap(output);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.putLong(version);
        buffer.putLong(createTime);
        byte[] byteDescription = description.getBytes(StandardCharsets.UTF_8);
        if (byteDescription.length > DESCRIPTION_SIZE) {
            throw new IllegalArgumentException("description is too long");
        }
        buffer.put(byteDescription);

        return output;
    }

    /**
     * Returns the version of the dictionary.
     *
     * The version is {@link DictionaryVersion#SYSTEM_DICT_VERSION} or
     * {@code DictionaryVersion#USER_DICT_VERSION_*}. If the file is not a dictionary,
     * returns an other value.
     * 
     * @return the version of the dictionary
     */
    public long getVersion() {
        return version;
    }

    /**
     * Returns the epoch seconds at which the dictionary is created.
     *
     * @return the epoch seconds
     */
    public long getCreateTime() {
        return createTime;
    }

    /**
     * Returns the description of the dictionary which is specified at creating.
     *
     * @return the description of the dictionary
     */
    public String getDescription() {
        return description;
    }
}
