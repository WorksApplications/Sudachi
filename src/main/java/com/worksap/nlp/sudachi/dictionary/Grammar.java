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

import java.util.List;

/**
 * The parameters and grammatical informations.
 */
public interface Grammar {

    /**
     * Returns the number of types of part-of-speech.
     *
     * The IDs of part-of-speech are within the range of 0 to
     * {@code getPartOfSpeechSize() - 1}.
     *
     * @return the number of types of part-of-speech
     */
    public int getPartOfSpeechSize();

    /**
     * Returns the array of strings of part-of-speech name.
     *
     * The name is divided into layers.
     *
     * @param posId
     *            the ID of the part-of-speech
     * @return the list of strings of part-of-speech name
     * @throws IndexOutOfBoundsException
     *             if {@code posId} is out of the range
     */
    public List<String> getPartOfSpeechString(short posId);

    /**
     * Returns the the ID corresponding to the part-of-speech name.
     *
     * <p>
     * If there is not such the part-of-speech name, -1 is returned.
     *
     * @param pos
     *            the list of string of part-of-speech name
     * @return the ID corresponding to the part-of-speech name, or -1 without
     *         corresponding one.
     */
    public short getPartOfSpeechId(List<String> pos);

    /**
     * Returns the cost of the specified connection.
     *
     * <p>
     * When the Id is out of the range, the behavior is undefined.
     *
     * @param leftId
     *            the left-ID of the connection
     * @param rightId
     *            the right-ID of the connection
     * @return the cost of the connection
     */
    public short getConnectCost(short leftId, short rightId);

    /**
     * Set the connection costs.
     *
     * <p>
     * When the Id is out of the range, the behavior is undefined.
     *
     * @param leftId
     *            the left-ID of the connection
     * @param rightId
     *            the right-ID of the connection
     * @param cost
     *            the cost of the connection
     */
    public void setConnectCost(short leftId, short rightId, short cost);

    /**
     * Returns the parameter of the beginning of sentence.
     *
     * <p>
     * The following are the parameters.
     * 
     * <pre>
     * {@code { left-ID, rightID, cost } }
     * </pre>
     *
     * @return the parameter of the beginning of sentence
     */
    public short[] getBOSParameter();

    /**
     * Returns the parameter of the end of sentence.
     *
     * <p>
     * The following are the parameters.
     * 
     * <pre>
     * {@code { left-ID, rightID, cost } }
     * </pre>
     *
     * @return the parameter of the end of sentence
     */
    public short[] getEOSParameter();

    public CharacterCategory getCharacterCategory();

    public void setCharacterCategory(CharacterCategory charCategory);

    /** the cost of inhibited connections */
    public static final short INHIBITED_CONNECTION = Short.MAX_VALUE;
}
