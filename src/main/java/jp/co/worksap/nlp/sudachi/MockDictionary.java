package jp.co.worksap.nlp.sudachi;

public class MockDictionary implements Dictionary {
    public MockDictionary(String config) {
    }

    @Override
    public void close() {}

    @Override
    public Tokenizer create() {
        return new MockTokenizer(this);
    }
}
