package jp.co.worksap.nlp.sudachi;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

class MockMorphemeArray {

    private String[] morphemes;
    private int offset;

    MockMorphemeArray(String[] morphs) {
	morphemes = morphs;
	offset = 0;
    }

    void setOffset(int offset) { this.offset = offset; }

    int size() { return morphemes.length; }

    Morpheme get(int index) throws IndexOutOfBoundsException {
	if (index < 0 || index >= size()) {
	    throw new IndexOutOfBoundsException("Index: " + index);
	}
	return new MockMorpheme(this, index);
    }

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
	return POSList[index % 3];
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
}
