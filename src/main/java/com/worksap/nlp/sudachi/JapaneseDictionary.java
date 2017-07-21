package com.worksap.nlp.sudachi;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.worksap.nlp.sudachi.dictionary.DoubleArrayLexicon;
import com.worksap.nlp.sudachi.dictionary.Grammar;
import com.worksap.nlp.sudachi.dictionary.GrammarImpl;
import com.worksap.nlp.sudachi.dictionary.Lexicon;

public class JapaneseDictionary implements Dictionary {

    Grammar grammar;
    Lexicon lexicon;
    List<InputTextPlugin> inputTextPlugins;
    List<WordLookingUpPlugin> wordLookingUpPlugins;

    JapaneseDictionary(String jsonString) throws IOException {
        Settings settings = parseSettings(jsonString);
        readSystemDictionary(settings.getSystemDictPath());
        inputTextPlugins = settings.getInputTextPlugin();
        wordLookingUpPlugins = settings.getWordLookingUpPlugin();
        // ToDo: set fallback OOV provider
        for (WordLookingUpPlugin p : wordLookingUpPlugins) {
            p.setUp(grammar);
        }
    }

    Settings parseSettings(String settings) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.OBJECT_AND_NON_CONCRETE);
        return mapper.readValue(settings, Settings.class);
    }

    void readSystemDictionary(String filename) throws IOException {
        ByteBuffer bytes;
        try (FileInputStream istream = new FileInputStream(filename);
             FileChannel inputFile = istream.getChannel()) {
            bytes = inputFile.map(FileChannel.MapMode.READ_ONLY, 0,
                                  inputFile.size());
            bytes.order(ByteOrder.LITTLE_ENDIAN);
        }

        GrammarImpl grammar = new GrammarImpl(bytes, 0);
        this.grammar = grammar;
        lexicon = new DoubleArrayLexicon(bytes, grammar.storageSize());
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
