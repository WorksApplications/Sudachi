/*
 * Copyright (c) 2020 Works Applications Co., Ltd.
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

/**
 * A mutable sequence of characters. This class is used in
 * {@link InputTextPlugin#rewrite} to modify the input text.
 */
public interface InputTextBuilder {

    /**
     * Replaces the characters in a substring of this sequence with characters in
     * the specified {@code String}. The substring begins at the specified
     * {@code begin} and extends to the character at index {@code end - 1} or to the
     * end of sequence if no such character exists.
     *
     * @param begin
     *            the beginning index
     * @param end
     *            the ending index
     * @param str
     *            the replacement string
     * @throws StringIndexOutOfBoundsException
     *             if {@code begin} is negative, greater than the length of the
     *             sequence, or greater than {@code end}.
     */
    public void replace(int begin, int end, String str);

    /**
     * Returns the sequence before all of the replacements.
     *
     * @return the sequence before all of the replacements
     */
    public String getOriginalText();

    /**
     * Returns the sequence as {@link String}.
     *
     * @return the sequence as {@link String}
     */
    public String getText();

    /**
     * Returns the immutable sequence of characters.
     *
     * @return the immutable sequence of characters
     */
    public InputText build();
}
