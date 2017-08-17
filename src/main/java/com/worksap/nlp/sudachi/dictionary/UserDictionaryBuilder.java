package com.worksap.nlp.sudachi.dictionary;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A user dictionary building tool. This class provide the converter
 * from the source file in the CSV format to the binary format.
 */
public class UserDictionaryBuilder extends DictionaryBuilder {

    Grammar grammar;

    UserDictionaryBuilder(Grammar grammar) {
        super();
        this.grammar = grammar;
    }

    void build(FileInputStream lexiconInput, FileOutputStream output)
        throws IOException {
        buildLexicon(lexiconInput);
        lexiconInput.close();

        FileChannel outputChannel = output.getChannel();
        writeLexicon(outputChannel);
        outputChannel.close();
    }

    @Override
    void buildLexicon(FileInputStream lexiconInput) throws IOException {
        LineNumberReader reader
            = new LineNumberReader(new InputStreamReader(lexiconInput));

        int wordId = 0;
        for (; ; wordId++) {
            String line = reader.readLine();
            if (line == null) {
                break;
            }

            String[] cols = line.split(",");
            if (cols.length != NUMBER_OF_COLUMNS) {
                System.err.println("Error: invalid format at line "
                                   + reader.getLineNumber());
                continue;
            }
            for (int i = 0; i < cols.length; i++) {
                cols[i] = decode(cols[i]);
            }

            if (cols[0].length() == 0) {
                System.err.println("Error: headword is empty at line "
                                   + reader.getLineNumber());
                continue;
            }
            if (!cols[1].equals("-1")) {
                // headword
                byte[] key = cols[0].getBytes(StandardCharsets.UTF_8);
                if (!trieKeys.containsKey(key)) {
                    trieKeys.put(key, new ArrayList<Integer>());
                }
                trieKeys.get(key).add(wordId);
                // left-id, right-id, cost
            }
            params.add(new Short[] { Short.parseShort(cols[1]),
                                     Short.parseShort(cols[2]),
                                     Short.parseShort(cols[3]) });

            List<String> pos = Arrays.asList(cols[5], cols[6], cols[7],
                                             cols[8], cols[9], cols[10]);
            short posId = grammar.getPartOfSpeechId(pos);
            if (posId < 0) {
                System.err.println("Error: Part of speech is wrong at line  "
                                   + reader.getLineNumber());
            }

            WordInfo info
                = new WordInfo(cols[4], // headword
                               (short)cols[0].getBytes(StandardCharsets.UTF_8).length,
                               posId,
                               cols[12], // normalizedForm
                               (cols[13].equals("*") ? -1 :Integer.parseInt(cols[13])), // dictionaryFormWordId
                               "", // dummy
                               cols[11], // readingForm
                               parseSplitInfo(cols[15]), // aUnitSplit
                               parseSplitInfo(cols[16]), // bUnitSplit
                               parseSplitInfo(cols[17]) // wordStructure
                               );
            wordInfos.add(info);
        }
        wordSize = wordId;
    }

    /**
     * Builds the user dictionary.
     *
     * This tool requires three arguments.
     * <ol start="0">
     * <li>the path of the system dictionary</li>
     * <li>the path of the source file in the CSV format</li>
     * <li>the path of the output file</li>
     * </ol>
     * @param args the input filename, the connection matrix file,
     * and the output filename
     * @throws IOException if IO or parsing is failed
     */
    public static void main(String[] args) throws IOException {
        if (args.length != 3) {
            System.err.println("usage: UserDictionaryBuilder system.dic input.csv output.dic");
            return;
        }

        Grammar grammar;
        try (FileInputStream istream = new FileInputStream(args[0]);
             FileChannel inputFile = istream.getChannel()) {
            ByteBuffer bytes;
            bytes = inputFile.map(FileChannel.MapMode.READ_ONLY, 0,
                                  inputFile.size());
            bytes.order(ByteOrder.LITTLE_ENDIAN);
            grammar = new GrammarImpl(bytes, 0);
        }

        try (FileInputStream lexiconInput = new FileInputStream(args[1]);
             FileOutputStream output = new FileOutputStream(args[2])) {
            UserDictionaryBuilder builder = new UserDictionaryBuilder(grammar);
            builder.build(lexiconInput, output);
        }
    }
}
