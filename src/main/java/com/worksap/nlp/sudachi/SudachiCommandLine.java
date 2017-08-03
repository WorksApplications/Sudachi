package com.worksap.nlp.sudachi;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;

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

    static void run(Tokenizer tokenizer, Tokenizer.SplitMode mode,
                    InputStream input, PrintStream output)
        throws IOException {

        try (InputStreamReader inputReader = new InputStreamReader(input);
             BufferedReader reader = new BufferedReader(inputReader)) {

            while (true) {
                String line = reader.readLine();
                if (line == null) {
                    break;
                }
                for (Morpheme m : tokenizer.tokenize(mode, line)) {
                    output.print(m.surface());
                    output.print("\t");
                    output.print(String.join(",", m.partOfSpeech()));
                    output.print("\t");
                    output.println(m.normalizedForm());
                }
                output.println("EOS");
            }
        }
    }

    public static void main(String[] args) throws IOException {
        Tokenizer.SplitMode mode = Tokenizer.SplitMode.C;
        String settings = null;
        PrintStream output = System.out;
        boolean isEnableDump = false;

        int i = 0;
        for (i = 0; i < args.length; i++) {
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
            } else if (args[i].equals("-o") && i + 1 < args.length) {
                output = new PrintStream(args[++i]);
            } else if (args[i].equals("-d")) {
                isEnableDump = true;
            } else if (args[i].equals("-h")) {
                System.err.println("usage: SudachiCommandLine [-r file] [-m A|B|C] [-o file] [-d] [file ...]");
                System.err.println("\t-r file\tread settings from file");
                System.err.println("\t-m mode\tmode of splitting");
                System.err.println("\t-o file\toutput to file");
                System.err.println("\t-d\tdebug mode");
                return;
            } else {
                break;
            }
        }

        if (settings == null) {
            try (InputStream input
                 = SudachiCommandLine.class
                 .getResourceAsStream("/sudachi.json")) {
                settings = readAll(input);
            }
        }

        Dictionary dict = new DictionaryFactory().create(settings);
        Tokenizer tokenizer = dict.create();
        if (isEnableDump) {
            tokenizer.setDumpOutput(output);
        }

        if (i < args.length) {
            for ( ; i < args.length; i++) {
                try (FileInputStream input = new FileInputStream(args[i])) {
                    run(tokenizer, mode, input, output);
                }
            }
        } else {
            run(tokenizer, mode, System.in, output);
        }
        output.close();
    }
}
