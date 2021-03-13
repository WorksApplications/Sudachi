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
 * Categories of characters.
 *
 * These categories are used in the
 * {@link com.worksap.nlp.sudachi.OovProviderPlugin} and
 * {@link com.worksap.nlp.sudachi.PathRewritePlugin}.
 *
 * <p>
 * You can defined the range of each category in the file which specified
 * "characterDefinitionFile" of the settings.
 */
public enum CategoryType {
    /** The fall back category. */
    DEFAULT(1),
    /** White spaces. */
    SPACE(1 << 1),
    /** CJKV ideographic characters. */
    KANJI(1 << 2),
    /** Symbols. */
    SYMBOL(1 << 3),
    /** Numerical characters. */
    NUMERIC(1 << 4),
    /** Latin alphabets. */
    ALPHA(1 << 5),
    /** Hiragana characters. */
    HIRAGANA(1 << 6),
    /** Katakana characters. */
    KATAKANA(1 << 7),
    /** Kanji numeric characters. */
    KANJINUMERIC(1 << 8),
    /** Greek alphabets. */
    GREEK(1 << 9),
    /** Cyrillic alphabets. */
    CYRILLIC(1 << 10),
    /** User defined category. */
    USER1(1 << 11),
    /** User defined category. */
    USER2(1 << 12),
    /** User defined category. */
    USER3(1 << 13),
    /** User defined category. */
    USER4(1 << 14),
    /** Characters that cannot be the beginning of word */
    NOOOVBOW(1 << 15);

    private final int id;

    private CategoryType(int id) {
        this.id = id;
    }

    /**
     * Returns the integer ID number of the category.
     *
     * @return the ID number of the category
     */
    public int getId() {
        return id;
    }

    /**
     * Returns the category to which the specified ID is mapped, or {@code null} if
     * there is no associated category.
     *
     * @param id
     *            the ID number of category
     * @return the category to which the specified ID is mapped, or {@code null} if
     *         there is no associated category.
     */
    public static CategoryType getType(int id) {
        for (CategoryType type : CategoryType.values()) {
            if (type.getId() == id) {
                return type;
            }
        }
        return null;
    }
}
