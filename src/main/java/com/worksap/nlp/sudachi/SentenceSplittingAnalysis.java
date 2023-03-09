/*
 * Copyright (c) 2023 Works Applications Co., Ltd.
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

import com.worksap.nlp.sudachi.dictionary.LexiconSet;
import com.worksap.nlp.sudachi.sentdetect.SentenceDetector;

import java.util.ArrayList;
import java.util.Iterator;

/*internal*/ class SentenceSplittingAnalysis implements SentenceDetector.NonBreakCheker {
    private final SentenceDetector detector = new SentenceDetector();

    private final Tokenizer.SplitMode mode;
    private final JapaneseTokenizer tokenizer;
    final ArrayList<MorphemeList> result = new ArrayList<>();

    SentenceSplittingAnalysis(Tokenizer.SplitMode mode, JapaneseTokenizer tokenizer) {
        this.mode = mode;
        this.tokenizer = tokenizer;
    }

    UTF8InputText input;
    int bos;

    int tokenizeBuffer(CharSequence buffer) {
        UTF8InputText input = tokenizer.buildInputText(buffer);
        String normalized = input.getText();
        this.input = input;

        int bos = 0;
        int length;

        this.bos = bos;
        while ((length = detector.getEos(normalized, this)) > 0) {
            int eos = bos + length;
            if (eos < normalized.length()) {
                eos = input.getNextInOriginal(eos - 1);
                length = eos - bos;
            }
            UTF8InputText sentence = input.slice(bos, eos);
            result.add(tokenizer.tokenizeSentence(mode, sentence));
            normalized = normalized.substring(length);
            bos = eos;
            this.bos = bos;
        }

        // buffer is full, need to clean it up
        if (length < 0 && buffer.length() == -length) {
            result.add(tokenizer.tokenizeSentence(mode, input));
            return -length;
        }

        return length;
    }

    int bosPosition() {
        return input.textIndexToOriginalTextIndex(bos);
    }

    @Override
    public boolean hasNonBreakWord(int length) {
        UTF8InputText inp = input;
        int byteEOS = inp.getCodePointsOffsetLength(0, bos + length);
        byte[] bytes = inp.getByteText();
        LexiconSet lexicon = tokenizer.lexicon;
        for (int i = Math.max(0, byteEOS - 64); i < byteEOS; i++) {
            Iterator<int[]> iterator = lexicon.lookup(bytes, i);
            while (iterator.hasNext()) {
                int[] r = iterator.next();
                int l = r[1];
                if (l > byteEOS || (l == byteEOS && bos + length - inp.modifiedOffset(i) > 1)) {
                    return true;
                }
            }
        }
        return false;
    }
}
