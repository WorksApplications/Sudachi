/*
 * Copyright (c) 2017 Works Applications Co., Ltd.
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

import java.util.Set;

import com.worksap.nlp.sudachi.dictionary.CategoryType;

/**
 * An immutable sequence of characters.
 * This class has the original input text and the modified text
 * each character of which is mapped to the original one.
 * In the methods of this class the index is not one of characters or
 * code points. The meaning of the index is implementation dependent,
 * but users do not have to worry about it.
 */
public interface InputText<E> {
    
    /**
     * Returns the modified text.
     * This text is used in the tokenizing.
     *
     * @return the modified text
     */
    public String getText();
    
    /**
     * Returns the original input text before all of the replacements
     * in {@link InputTextBuilder#replace}.
     *
     * @return the original input text.
     */
    public String getOriginalText();
    
    /**
     * Returns the substring of the modified text.
     * The substring begins at the specified {@code begin} and
     * extends to the character at index {@code end - 1}.
     *
     * @param begin the beginning index
     * @param end the ending index
     * @return the new string
     * @throws IndexOutOfBoundsException if {@code begin} or {@code end}
     *         are negative, greater than the length of the sequence,
     *         or {@code begin} is greater than {@code end}
     */
    public String getSubstring(int begin, int end);
    
    /**
     * Returns the index of the original text mapped to
     * the character at the specified {@code index} in the modified text.
     *
     * @param index the index of the modified text
     * @return the index of original text
     * @throws IndexOutOfBoundsException if {@code index}
     *         are negative, greater than the length of the sequence
     */
    public int getOriginalIndex(int index);
    
    /**
     * Returns the set of category types of the character
     * at the specified {@code index} in the modified text.
     *
     * @param index the index of the modified text
     * @return the set of the character category types
     * @throws IndexOutOfBoundsException if {@code index}
     *         are negative, greater than the length of the sequence
     */
    public Set<CategoryType> getCharCategoryTypes(int index);

    /**
     * Returns the intersection of the sets of category types
     * of each characters in the specified substring.
     * The substring begins at the specified {@code begin} and
     * extends to the character at index {@code end - 1}.
     *
     * @param begin the beginning index
     * @param end the ending index
     * @return the set of the character category types
     * @throws IndexOutOfBoundsException if {@code begin} or {@code end}
     *         are negative, greater than the length of the sequence,
     *         or {@code begin} is greater than {@code end}
     */
    public Set<CategoryType> getCharCategoryTypes(int begin, int end);
    
    /**
     * Returns the longest length of the substring all the character
     * of which share the same character category types.
     * The substring begins at the specified {@code index}.
     *
     * @param index the beginning index
     * @return the length of the substring has the same character
     *         category types
     * @throws IndexOutOfBoundsException if {@code index}
     *         are negative, greater than the length of the sequence
     */
    public int getCharCategoryContinuousLength(int index);
    
    /**
     * Returns the length of offset from the given {@code index}
     * by {@code codePointOffset} code points.
     * Unpaired surrogates within the text range given by
     * {@code index} and {@code codePointOffset} count as
     * one code point each.
     *
     * @param index the index to be offset
     * @param codePointOffset the offset in code points
     * @return the length of offset
     * @exception IndexOutOfBoundsException if {@code index}
     *   is negative or larger then the length of this sequence,
     *   or if {@code codePointOffset} is positive and the subsequence
     *   starting with {@code index} has fewer than
     *   {@code codePointOffset} code points,
     *   or if {@code codePointOffset} is negative and the subsequence
     *   before {@code index} has fewer than the absolute value of
     *   {@code codePointOffset} code points.
     */
    public int getCodePointsOffsetLength(int index, int codePointOffset);

    public boolean canBow(int index);
}
