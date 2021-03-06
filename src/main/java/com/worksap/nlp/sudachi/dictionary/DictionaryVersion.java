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

package com.worksap.nlp.sudachi.dictionary;

/**
 * Versions of dictionaries.
 */
public class DictionaryVersion {

    private DictionaryVersion() {
    }

    /** the first version of system dictionries */
    public static final long SYSTEM_DICT_VERSION_1 = 0x7366d3f18bd111e7L;

    /** the second version of system dictionries */
    public static final long SYSTEM_DICT_VERSION_2 = 0xce9f011a92394434L;

    /** the first version of user dictionries */
    public static final long USER_DICT_VERSION_1 = 0xa50f31188bd211e7L;

    /** the second version of user dictionries */
    public static final long USER_DICT_VERSION_2 = 0x9fdeb5a90168d868L;

    /** the third version of user dictionries */
    public static final long USER_DICT_VERSION_3 = 0xca9811756ff64fb0L;

    public static boolean isSystemDictionary(long version) {
        return version == SYSTEM_DICT_VERSION_1 || version == SYSTEM_DICT_VERSION_2;
    }

    public static boolean isUserDictionary(long version) {
        return version == USER_DICT_VERSION_1 || version == USER_DICT_VERSION_2 || version == USER_DICT_VERSION_3;
    }

    static boolean hasGrammar(long version) {
        return isSystemDictionary(version) || version == USER_DICT_VERSION_2 || version == USER_DICT_VERSION_3;
    }

    static boolean hasSynonymGroupIds(long version) {
        return version == SYSTEM_DICT_VERSION_2 || version == USER_DICT_VERSION_3;
    }
}
