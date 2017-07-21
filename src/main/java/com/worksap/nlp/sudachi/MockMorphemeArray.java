package com.worksap.nlp.sudachi;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.AbstractList;
import java.util.List;

class MockMorphemeArray extends AbstractList<Morpheme> {

    private String[] morphemes;
    private int offset;
    private int listOffset;

    @Override
    public int size() { return morphemes.length; }

    @Override
    public boolean contains(Object o) {
        return indexOf(o) >= 0;
    }

    @Override
    public int indexOf(Object o) {
        if (o == null || o instanceof Morpheme) {
            return -1;
        } else {
            Morpheme m = (Morpheme)o;
            for (int i = 0; i < morphemes.length; i++) {
                if (elementEquals(i, m)) {
                    return i;
                }
            }
        }
        return -1;
    }

    @Override
    public int lastIndexOf(Object o) {
        if (o == null || o instanceof Morpheme) {
            return -1;
        } else {
            Morpheme m = (Morpheme)o;
            for (int i = morphemes.length - 1; i >= 0; i--) {
                if (elementEquals(i, m)) {
                    return i;
                }
            }
        }
        return -1;
    }

    @Override
    public Morpheme get(int index) throws IndexOutOfBoundsException {
        if (index < 0 || index >= size()) {
            throw new IndexOutOfBoundsException("Index: " + index);
        }
        return new MockMorpheme(this, index);
    }

    @Override
    public List<Morpheme> subList(int fromIndex, int toIndex) {
        if (fromIndex < 0)
            throw new IndexOutOfBoundsException("fromIndex = " + fromIndex);
        if (toIndex > morphemes.length)
            throw new IndexOutOfBoundsException("toIndex = " + toIndex);
        if (fromIndex > toIndex)
            throw new IllegalArgumentException("fromIndex(" + fromIndex +
                                               ") > toIndex(" + toIndex + ")");

        String[] subMorphs = Arrays.copyOfRange(morphemes, fromIndex, toIndex);
        MockMorphemeArray sublist = new MockMorphemeArray(subMorphs);
        sublist.listOffset = listOffset + fromIndex;
        sublist.setOffset(this.offset);
        return sublist;
    }

    MockMorphemeArray(String[] morphs) {
        morphemes = morphs;
        offset = 0;
    }

    boolean elementEquals(int index, Morpheme m) {
        return getSurface(index).equals(m.surface()) &&
            equals(getPartOfSpeech(index), m.partOfSpeech()) &&
            getDictionaryForm(index).equals(m.dictionaryForm());
    }

    void setOffset(int offset) { this.offset = offset; }

    int getBegin(int index) {
        int b = offset;
        for (int i = 0; i < index; i++) {
            b += morphemes[i].length();
        }
        return b;
    }

    int getEnd(int index) {
        int e = offset;
        for (int i = 0; i <= index; i++) {
            e += morphemes[i].length();
        }
        return e;
    }
    
    String getSurface(int index) {
        return morphemes[index];
    }

    String[] getPartOfSpeech(int index) {
        String[][] POSList = {
            { "名詞", "普通名詞" ,"一般", "*", "*", "*", },
            { "動詞", "一般", "*", "*", "五段-サ行", "連用形-一般", },
            { "名詞", "固有名詞", "地名", "一般", "*", "*", },
        };
        return POSList[(index + listOffset) % 3];
    }

    String getDictionaryForm(int index) {
        if (getPartOfSpeech(index)[0].equals("動詞")) {
            return getSurface(index) + "る";
        } else {
            return getSurface(index);
        }
    }

    String getNormalizedForm(int index) {
        return getSurface(index);
    }

    String getReading(int index) {
        String s = getSurface(index);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            sb.append('ア');
        }
        return sb.toString();
    }

    List<Morpheme> split(int index, Tokenizer.SplitMode mode) {
        String s = getSurface(index);
        String[] splitted = MockTokenizer.split(mode, s);
        MockMorphemeArray array = new MockMorphemeArray(splitted);
        array.setOffset(getBegin(index));
        int size = array.size();
        List<Morpheme> morphs = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            morphs.add(new MockMorpheme(array, i));
        }
        return morphs;
    }

    boolean isOOV(int index) {
        return false;
    }

    static boolean equals(String[] s1, String[] s2) {
        if (s1.length != s2.length) {
            return false;
        }
        for (int i = 0; i < s1.length; i++) {
            if (!s1[i].equals(s2[i])) {
                return false;
            }
        }
        return true;
    }
}
