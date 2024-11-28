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

package com.worksap.nlp.sudachi;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.CharBuffer;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import com.worksap.nlp.sudachi.dictionary.LexiconSet;
import com.worksap.nlp.sudachi.sentdetect.SentenceDetector;

/**
 * Provides lazy sentence split and analysis.
 */
/* internal */ class SentenceSplittingLazyAnalysis implements SentenceDetector.NonBreakCheker, Iterator<MorphemeList> {
    private final SentenceDetector detector = new SentenceDetector();

    private final Tokenizer.SplitMode mode;
    private final JapaneseTokenizer tokenizer;
    private final Readable readable;

    SentenceSplittingLazyAnalysis(Tokenizer.SplitMode mode, JapaneseTokenizer tokenizer, Readable readable) {
        this.mode = mode;
        this.tokenizer = tokenizer;
        this.readable = new IOTools.SurrogateAwareReadable(readable);

        this.buffer = CharBuffer.allocate(SentenceDetector.DEFAULT_LIMIT);
        this.buffer.flip();
        this.input = tokenizer.buildInputText("");
    }

    // input buffer
    private final CharBuffer buffer;
    // preprocessed InputText of the buffer.
    // used to normalize text for the sentence detection.
    private UTF8InputText input;
    // begining-of-sentence index of next sentence in the input
    private int bos = 0;
    // normalized text left. corresponds to `input.getSubstring(bos,
    // input.getText().length())`
    private String normalized = "";

    /** Return bos position in the buffer. */
    private int bosPosition() {
        return input.textIndexToOriginalTextIndex(bos);
    }

    /**
     * Reset the buffer discarding processed text, then read from the input.
     * 
     * @return the number of chars added to the buffer. -1 if input reabable is at
     *         its end.
     */
    private int reloadBuffer() throws IOException {
        buffer.position(bosPosition());
        buffer.compact();
        int nread = readable.read(buffer);
        buffer.flip();

        // align with new buffer state
        input = tokenizer.buildInputText(buffer);
        bos = 0;
        normalized = input.getText();

        return nread;
    }

    @Override
    public boolean hasNext() {
        if (!normalized.isEmpty()) {
            return true;
        }

        int nread;
        try {
            nread = reloadBuffer();
        } catch (IOException e) {
            throw new UncheckedIOException(e.getMessage(), e);
        }

        return !(nread < 0 && !buffer.hasRemaining());
    }

    @Override
    public MorphemeList next() {
        int length = detector.getEos(normalized, this);
        if (length > 0) { // sentence found
            int eos = bos + length;
            if (eos < normalized.length()) {
                eos = input.getNextInOriginal(eos - 1);
                length = eos - bos;
            }
            UTF8InputText sentence = input.slice(bos, eos);
            bos = eos;
            normalized = normalized.substring(length);
            return tokenizer.tokenizeSentence(mode, sentence);
        }

        // buffer is just after reload but no (safe) eos found. need to clean it up.
        // tokenize all text in the buffer.
        if (bos == 0 && length < 0) {
            bos = normalized.length();
            normalized = "";
            return tokenizer.tokenizeSentence(mode, input);
        }

        int nread;
        try {
            nread = reloadBuffer();
        } catch (IOException e) {
            throw new UncheckedIOException(e.getMessage(), e);
        }

        if (nread < 0 && !buffer.hasRemaining()) {
            throw new NoSuchElementException("no texts left to analyze");
        }

        // recursive call with reloaded buffer.
        return next();
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
