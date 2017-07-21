package com.worksap.nlp.sudachi;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

public class SudachiCommandLine {

    static String readAll(InputStream input) throws IOException {
        BufferedReader reader
            = new BufferedReader(new InputStreamReader(input));
        StringBuilder sb = new StringBuilder();
        while (true) {
            String line = reader.readLine();
            if (line == null) {
                break;
            }
            sb.append(line);
        }
        return sb.toString();
    }

    public static void main(String[] args) throws IOException {
        Tokenizer.SplitMode mode = Tokenizer.SplitMode.C;
        String settings = null;
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-r") && i + 1 < args.length) {
                try (FileInputStream input = new FileInputStream(args[++i])) {
                    settings = readAll(input);
                }
            } else if (args[i].equals("-m") && i + 1 < args.length) {
                switch (args[++i]) {
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
        }

        if (settings == null) {
            try (InputStream input
                 = SudachiCommandLine.class
                 .getResourceAsStream("/sudachi.json")) {
                settings = readAll(input);
            }
        }

        BufferedReader reader
            = new BufferedReader(new InputStreamReader(System.in));

        Dictionary dict = new DictionaryFactory().create(settings);
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
