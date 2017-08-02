package com.worksap.nlp.sudachi;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.List;

import com.worksap.nlp.sudachi.dictionary.CharacterCategory;
import com.worksap.nlp.sudachi.dictionary.DoubleArrayLexicon;
import com.worksap.nlp.sudachi.dictionary.Grammar;
import com.worksap.nlp.sudachi.dictionary.GrammarImpl;
import com.worksap.nlp.sudachi.dictionary.LexiconSet;

class JapaneseDictionary implements Dictionary {

    Grammar grammar;
    LexiconSet lexicon;
    List<InputTextPlugin> inputTextPlugins;
    List<OovProviderPlugin> oovProviderPlugins;
    List<PathRewritePlugin> pathRewritePlugins;

    JapaneseDictionary(String jsonString) throws IOException {
        Settings settings = Settings.parseSettings(jsonString);

        readSystemDictionary(settings.getSystemDictPath());
        
        readCharacterDefinition(settings.getCharacterDefinitionFilePath());

        inputTextPlugins = settings.getInputTextPlugin();
        oovProviderPlugins = settings.getOovProviderPlugin();
        if (oovProviderPlugins.isEmpty()) {
            throw new IllegalArgumentException("no OOV provider");
        }
        for (OovProviderPlugin p : oovProviderPlugins) {
            p.setUp(grammar);
        }
        pathRewritePlugins = settings.getPathRewritePlugin();

        for (String filename : settings.getUserDictPath()) {
            readUserDictionary(filename);
        }
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
        lexicon = new LexiconSet(new DoubleArrayLexicon(bytes, grammar.storageSize()));
    }

    void readUserDictionary(String filename) throws IOException {
        ByteBuffer bytes;
        try (FileInputStream input = new FileInputStream(filename);
             FileChannel inputFile = input.getChannel()) {
            bytes = inputFile.map(FileChannel.MapMode.READ_ONLY, 0,
                                  inputFile.size());
            bytes.order(ByteOrder.LITTLE_ENDIAN);
        }

        DoubleArrayLexicon userLexicon = new DoubleArrayLexicon(bytes, 0);
        Tokenizer tokenizer = create();
        userLexicon.calculateCost(tokenizer);
        lexicon.add(userLexicon);
    }
    
    void readCharacterDefinition(String filename) throws IOException {
        if (grammar == null) {
            return;
        }
        CharacterCategory charCategory = new CharacterCategory();
        charCategory.readCharacterDefinition(filename);
        grammar.setCharacterCategory(charCategory);
    }

    @Override
    public void close() {
        grammar = null;
        lexicon = null;
    }

    @Override
    public Tokenizer create() {
        return new JapaneseTokenizer(grammar, lexicon,
                                     inputTextPlugins, oovProviderPlugins,
                                     pathRewritePlugins);
    }
}
