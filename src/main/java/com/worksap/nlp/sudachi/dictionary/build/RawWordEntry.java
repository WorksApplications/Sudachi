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

import com.worksap.nlp.sudachi.dictionary.StringPtr;
import com.worksap.nlp.sudachi.dictionary.WordInfoList;
import com.worksap.nlp.sudachi.dictionary.Ints;

import java.util.List;
import java.util.Objects;
import java.util.ArrayList;

/**
 * Raw word info entry parsed from the lexicon csv.
 */
@SuppressWarnings("jol")
public class RawWordEntry implements Lookup2.Entry {
    int pointer; // wordid, compressed offset of this entry in the lexicon.WordEntries
    String headword;
    String reading;
    WordRef normalizedForm;
    WordRef dictionaryForm;
    List<WordRef> aUnitSplit;
    List<WordRef> bUnitSplit;
    List<WordRef> cUnitSplit;
    List<WordRef> wordStructure;
    Ints synonymGroups;
    String userData;
    String mode;
    short leftId;
    short rightId;
    short cost;
    short posId;
    int sourceLine;
    String sourceName;

    /**
     * Compute expected size of word entry when put in the binary dictionary. This
     * function additionally validates length of split entries.
     * 
     * @return expected binary size of this entry, in bytes, will be always >=32
     */
    public int computeExpectedSize() {
        int size = 32;

        size += cUnitSplit.size() * 4;
        size += (bUnitSplit.equals(cUnitSplit)) ? 0 : bUnitSplit.size() * 4;
        size += (aUnitSplit.equals(bUnitSplit)) ? 0 : aUnitSplit.size() * 4;
        size += (wordStructure.equals(aUnitSplit)) ? 0 : wordStructure.size() * 4;
        size += synonymGroups.length() * 4;
        if (userData.length() != 0) {
            size += 2 + userData.length() * 2;
        }

        size = Align.align(size, WordInfoList.OFFSET_ALIGNMENT);
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

    /** check if sudachi dictionary can handle this entry */
    public void validate() {
        checkString(headword, "headword");
        checkString(reading, "reading");
    }

    /**
     * add necessary strings into the string storage.
     * 
     * @param strings
     *            storage to publish strings.
     */
    public void publishStrings(StringStorage strings) {
        strings.add(headword);
        strings.add(reading);
        if (normalizedForm instanceof WordRef.Headword) {
            WordRef.Headword normalized = (WordRef.Headword) normalizedForm;
            strings.add(normalized.getHeadword());
        }
    }

    public static RawWordEntry makeEmpty() {
        RawWordEntry entry = new RawWordEntry();
        entry.headword = "";
        entry.reading = "";
        // entry.normalizedForm
        // entry.dictionaryForm
        entry.aUnitSplit = new ArrayList<>();
        entry.bUnitSplit = new ArrayList<>();
        entry.cUnitSplit = new ArrayList<>();
        entry.wordStructure = new ArrayList<>();
        entry.synonymGroups = Ints.wrap(Ints.EMPTY_ARRAY);
        entry.userData = "";
        entry.mode = "A";
        entry.leftId = -1;
        entry.rightId = -1;
        entry.cost = Short.MAX_VALUE;
        entry.posId = 0;
        return entry;
    }
}
