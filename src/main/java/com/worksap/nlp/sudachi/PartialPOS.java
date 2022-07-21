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

package com.worksap.nlp.sudachi;

import com.worksap.nlp.sudachi.dictionary.POS;

import java.util.AbstractList;
import java.util.Arrays;
import java.util.List;

public class PartialPOS extends AbstractList<String> {
    private final List<String> data;

    public PartialPOS(List<String> data) {
        if (data.size() == 0) {
            throw new IllegalArgumentException("Partial POS must have at least 1 component");
        }
        if (data.size() > POS.DEPTH) {
            throw new IllegalArgumentException("Partial POS can have at most 6 components, was " + data);
        }
        for (String component : data) {
            if (component != null && component.length() > POS.MAX_COMPONENT_LENGTH) {
                throw new IllegalArgumentException("Component length can't be more than " + POS.MAX_COMPONENT_LENGTH
                        + ", was " + component.length() + ":" + component);
            }
        }
        this.data = data;
    }

    public PartialPOS(String... data) {
        this(Arrays.asList(data));
    }

    @Override
    public String get(int index) {
        return data.get(index);
    }

    @Override
    public int size() {
        return data.size();
    }

    boolean matches(POS pos) {
        for (int level = 0; level < data.size(); ++level) {
            String s = data.get(level);
            if (s == null) {
                continue;
            }
            if (!s.equals(pos.get(level))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        return String.join(",", data);
    }

    public static PartialPOS of(String... parts) {
        return new PartialPOS(parts);
    }
}
