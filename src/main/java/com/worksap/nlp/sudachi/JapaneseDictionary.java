package com.worksap.nlp.sudachi;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.Collections;
import java.util.List;

import com.worksap.nlp.sudachi.dictionary.DoubleArrayLexicon;
import com.worksap.nlp.sudachi.dictionary.Grammar;
import com.worksap.nlp.sudachi.dictionary.GrammarImpl;
import com.worksap.nlp.sudachi.dictionary.Lexicon;

public class JapaneseDictionary implements Dictionary {

    Grammar grammar;
    Lexicon lexicon;
    List<InputTextPlugin> inputTextPlugins;
    List<WordLookingUpPlugin> wordLookingUpPlugins;

    JapaneseDictionary() throws IOException {
        FileInputStream istream = new FileInputStream("system.dic");
        FileChannel inputFile = istream.getChannel();
        ByteBuffer bytes
            = inputFile.map(FileChannel.MapMode.READ_ONLY, 0, inputFile.size());
        inputFile.close();
        bytes.order(ByteOrder.LITTLE_ENDIAN);

        GrammarImpl grammar = new GrammarImpl(bytes, 0);
        this.grammar = grammar;
        lexicon = new DoubleArrayLexicon(bytes, grammar.storageSize());

        inputTextPlugins = Collections.emptyList();

        WordLookingUpPlugin mecab
            = new MeCabWordLookingUpPlugin(grammar,
                                           new FileInputStream("char.def"),
                                           new FileInputStream("unk.def"));
        wordLookingUpPlugins = Collections.singletonList(mecab);
    }

    @Override
    public void close() {
        grammar = null;
        lexicon = null;
    }

    @Override
    public Tokenizer create() {
        return new JapaneseTokenizer(grammar, lexicon,
                                     inputTextPlugins, wordLookingUpPlugins);
    }
}
