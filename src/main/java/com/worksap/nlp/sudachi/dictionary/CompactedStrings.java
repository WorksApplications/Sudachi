/*
 * Copyright (c) 2024 Works Applications Co., Ltd.
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

import java.nio.CharBuffer;

public class CompactedStrings {
    private final CharBuffer chars;

    public CompactedStrings(CharBuffer chars) {
        this.chars = chars;
    }

    public CharSequence sequence(int pointer) {
        CharBuffer dup = chars.duplicate();
        StringPtr ptr = StringPtr.decode(pointer);
        dup.position(ptr.getOffset());
        dup.limit(ptr.getOffset() + ptr.getLength());
        return dup;
    }

    public String string(int pointer) {
        return sequence(pointer).toString();
    }
}
