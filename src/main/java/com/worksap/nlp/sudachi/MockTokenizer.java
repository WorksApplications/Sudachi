package com.worksap.nlp.sudachi;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class MockTokenizer implements Tokenizer {

    private static Pattern bPattern
        = Pattern.compile("\\p{IsHiragana}|\\p{IsKatakana}+|\\p{InCjkUnifiedIdeographs}+|[^\\p{IsHiragana}\\p{IsKatakana}\\p{InCjkUnifiedIdeographs}]+");
    private static Pattern cPattern
        = Pattern.compile("\\p{IsHiragana}+|\\p{IsKatakana}+|\\p{InCjkUnifiedIdeographs}+|[^\\p{IsHiragana}\\p{IsKatakana}\\p{InCjkUnifiedIdeographs}]+");

    MockTokenizer(Dictionary dictionary) {}

    @Override
    public List<Morpheme> tokenize(String text) {
        return tokenize(SplitMode.C, text);
    }

    @Override
    public List<Morpheme> tokenize(SplitMode mode, String text) {
        String[] splitted = split(mode, text);

        MockMorphemeArray array = new MockMorphemeArray(splitted);
        return array;
    }

    static String[] split(SplitMode mode, String text) {
        if (mode == SplitMode.A)
            return text.split("");

        Pattern p = (mode == SplitMode.B) ? bPattern : cPattern;
        Matcher m = p.matcher(text);

        ArrayList<String> splitted = new ArrayList<>();
        while (m.find()) {
            splitted.add(m.group());
        }
        return splitted.toArray(new String[splitted.size()]);
    }
}
