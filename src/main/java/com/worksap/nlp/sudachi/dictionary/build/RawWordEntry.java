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

package com.worksap.nlp.sudachi.dictionary.build;

import com.worksap.nlp.sudachi.StringUtil;
import com.worksap.nlp.sudachi.dictionary.StringPtr;
import com.worksap.nlp.sudachi.dictionary.WordInfo;

import java.util.List;
import java.util.Objects;

@SuppressWarnings("jol")
public class RawWordEntry implements Lookup2.Entry {
    WordInfo wordInfo;
    int pointer;
    String headword;
    String reading;
    WordRef normalizedForm;
    WordRef dictionaryForm;
    String aUnitSplitString;
    String bUnitSplitString;
    String cUnitSplitString;
    String wordStructureString;
    String synonymGroups;
    String userData;
    String mode;
    short leftId;
    short rightId;
    short cost;
    short posId;
    int sourceLine;
    String sourceName;

    private int countRefs(String data, String prev) {
        if (data == null || data.isEmpty() || "*".equals(data) || data.equals(prev)) {
            return 0;
        }
        int nsplits = StringUtil.count(data, '/');
        if (nsplits >= CsvLexicon.ARRAY_MAX_LENGTH) {
            throw new CsvFieldException("maximum number of splits were exceeded");
        }
        return nsplits + 1;
    }

    /**
     * Compute expected size of word entry when put in the binary dictionary. This
     * function additionally validates length of split entries.
     * 
     * @return expected binary size of this entry, in bytes, will be always >=32
     */
    public int computeExpectedSize() {
        int size = 32;

        size += countRefs(cUnitSplitString, "") * 4;
        size += countRefs(bUnitSplitString, cUnitSplitString) * 4;
        size += countRefs(aUnitSplitString, bUnitSplitString) * 4;
        size += countRefs(wordStructureString, aUnitSplitString) * 4;
        size += countRefs(synonymGroups, "") * 4;
        if (userData.length() != 0) {
            size += 2 + userData.length() * 2;
        }

        size = Align.align(size, 8);
        return size;
    }

    /**
     * Entries with negative leftId are not indexed
     * 
     * @return true if the word should be present in the trie index
     */
    public boolean shouldBeIndexed() {
        return leftId >= 0;
    }

    @Override
    public int pointer() {
        return pointer;
    }

    @Override
    public boolean matches(short posId, String reading) {
        return this.posId == posId && Objects.equals(this.reading, reading);
    }

    @Override
    public String headword() {
        return headword;
    }

    private void checkString(String value, String name) {
        if (value.length() > StringPtr.MAX_LENGTH) {
            throw new CsvFieldException(
                    String.format("field %s had value which exceeded the maximum length %d (actual length: %d)", name,
                            StringPtr.MAX_LENGTH, value.length()));
        }
    }

    public void validate() {
        checkString(headword, "headword");
        checkString(reading, "reading");
    }

    public void publishStrings(StringStorage strings) {
        strings.add(headword);
        strings.add(reading);
        if (normalizedForm instanceof WordRef.Headword) {
            WordRef.Headword normalized = (WordRef.Headword) normalizedForm;
            strings.add(normalized.getHeadword());
        }
    }

    public int addPhantomEntries(List<RawWordEntry> list, Lookup2 lookup) {
        if (normalizedForm instanceof WordRef.Headword) {
            WordRef.Headword ref = (WordRef.Headword) normalizedForm;
            if (lookup.byHeadword(ref.getHeadword()) != null) {
                return 0;
            }
            RawWordEntry copy = new RawWordEntry();
            copy.headword = ref.getHeadword();
            copy.reading = copy.headword;
            copy.userData = "";
            copy.leftId = -1;
            copy.rightId = -1;
            copy.cost = Short.MAX_VALUE;
            copy.mode = "A";
            copy.posId = posId;
            RawWordEntry last = list.get(list.size() - 1);
            copy.pointer = RawLexicon.pointer(last.pointer * 8L + last.computeExpectedSize());
            list.add(copy);
            lookup.add(copy);
            return 1;
        } else {
            return 0;
        }
    }
}
