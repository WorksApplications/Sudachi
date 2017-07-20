package com.worksap.nlp.sudachi;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class SudachiCommandLine {

    public static void main(String[] args) throws IOException {
        Tokenizer.SplitMode mode = Tokenizer.SplitMode.C;
        if (args.length > 0) {
            switch (args[0]) {
            case "A":
                mode = Tokenizer.SplitMode.A;
                break;
            case "B":
                mode = Tokenizer.SplitMode.B;
                break;
            default:
                mode = Tokenizer.SplitMode.C;
                break;
            }
        }

        BufferedReader reader
            = new BufferedReader(new InputStreamReader(System.in));

        JapaneseDictionary dict = new JapaneseDictionary();
        Tokenizer tokenizer = dict.create();

        while (true) {
            String line = reader.readLine();
            if (line == null) {
                break;
            }
            for (Morpheme m : tokenizer.tokenize(mode, line)) {
                System.out.print(m.surface() + "\t");
                System.out.print(String.join(",", m.partOfSpeech()) + "\t");
                System.out.print(m.normalizedForm() + "\t");
                System.out.println(m.isOOV() ? "*" : "");
            }
            System.out.println("EOS");
        }
    }
}
