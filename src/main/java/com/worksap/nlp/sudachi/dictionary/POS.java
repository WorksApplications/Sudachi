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

import java.util.AbstractList;
import java.util.Arrays;

/**
 * Part-of-Speech
 * <p>
 * Sudachi POS are 6-component and consist of: 4 layers of POS tags, conjugation
 * type, conjugation form.
 */
public final class POS extends AbstractList<String> {
    public final static int DEPTH = 6;
    public final static int MAX_COMPONENT_LENGTH = 127;
    private final String[] elems;

    /**
     * @param elems
     *            non-null string array of exactly six elements
     */
    public POS(String... elems) {
        if (elems == null) {
            throw new IllegalArgumentException("pos must not be null");
        }
        if (elems.length != DEPTH) {
            throw new IllegalArgumentException("pos must have exactly six elements");
        }
        for (String e : elems) {
            if (e == null) {
                throw new IllegalArgumentException("POS components can't be null");
            }

            if (e.length() > MAX_COMPONENT_LENGTH) {
                throw new IllegalArgumentException(
                        String.format("POS component had length (%d) > %d: %s", e.length(), MAX_COMPONENT_LENGTH, e));
            }
        }
        this.elems = elems;
    }

    @Override
    public String get(int i) {
        return elems[i];
    }

    @Override
    public int size() {
        return DEPTH;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o instanceof POS) {
            POS strings = (POS) o;
            return Arrays.equals(elems, strings.elems);
        }
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        int result = 0xfeed;
        result = 31 * result + Arrays.hashCode(elems);
        return result;
    }

    @Override
    public String toString() {
        return String.join(",", elems);
    }
}
