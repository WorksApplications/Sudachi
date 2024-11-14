package com.worksap.nlp.sudachi.benchmark;

import java.io.BufferedReader;
import java.io.Console;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.InterruptedException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.worksap.nlp.sudachi.Config;
import com.worksap.nlp.sudachi.Dictionary;
import com.worksap.nlp.sudachi.DictionaryFactory;
import com.worksap.nlp.sudachi.MorphemeList;
import com.worksap.nlp.sudachi.Morpheme;
import com.worksap.nlp.sudachi.PathAnchor;
import com.worksap.nlp.sudachi.Settings;
import com.worksap.nlp.sudachi.SudachiCommandLine;
import com.worksap.nlp.sudachi.Tokenizer;

public class TokenizeMultiThread {
    private int numThread;

    private TokenizeMultiThread(int numThread) {
        this.numThread = numThread;
    }

    static void printUsage() {
        Console console = System.console();
        console.printf("usage: TokenizeMultiThread [-p numThread] [--systemDict file] file");
        console.printf("\t-p numThread\thow many threads to create tokenize task\n");
        console.printf("\t--systemDict file\tpath to a system dictionary (overrides everything)\n");
    }

    void tokenize_multithread(PrintStream output, Dictionary dict, String filepath)
            throws IOException, InterruptedException {
        ExecutorService exs = Executors.newFixedThreadPool(numThread);
        for (int i = 0; i < numThread; i++) {
            exs.submit(new Task(i, dict.create(), output, filepath));
        }
        exs.shutdown();

        PrintStream err = System.err;
        if (!exs.awaitTermination(30, TimeUnit.MINUTES)) {
            err.printf("terminate by timeout\n");
        }
    }

    class Task implements Callable<Void> {
        int id;
        Tokenizer tok;
        PrintStream output;
        String filepath;

        Task(int id, Tokenizer tok, PrintStream output, String filepath) {
            this.id = id;
            this.tok = tok;
            this.output = output;
            this.filepath = filepath;
        }

        @Override
        public Void call() throws IOException {
            try (
                    FileInputStream input = new FileInputStream(filepath);
                    InputStreamReader inputReader = new InputStreamReader(input, StandardCharsets.UTF_8);
                    BufferedReader reader = new BufferedReader(inputReader);) {
                String line;
                while ((line = reader.readLine()) != null) {
                    MorphemeList ms = tok.tokenize(line);
                    output.println(
                            String.join(" ", ms.stream().map(Morpheme::surface).collect(Collectors.toList())));
                }
            }
            return null;
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        int numThread = 1;
        String filepath;

        PathAnchor anchor = PathAnchor.classpath().andThen(PathAnchor.none());
        Config additional = Config.empty();
        Settings current = Settings.resolvedBy(anchor)
                .read(SudachiCommandLine.class.getClassLoader().getResource("sudachi.json"));

        int i;
        for (i = 0; i < args.length; i++) {
            if (args[i].equals("-h")) {
                printUsage();
                return;
            } else if (args[i].equals("-p") && i + 1 < args.length) {
                numThread = Integer.parseInt(args[++i]);
            } else if (args[i].equals("--systemDict") && i + 1 < args.length) {
                Path resolved = anchor.resolve(args[++i]);
                additional = additional.systemDictionary(resolved);
            } else {
                break;
            }
        }
        if (i >= args.length) {
            System.err.println("target text file is required.");
            return;
        }
        filepath = args[i];

        Config config = additional.withFallback(Config.fromSettings(current));

        try (Dictionary dict = new DictionaryFactory().create(config)) {
            PrintStream output = System.out;
            TokenizeMultiThread runner = new TokenizeMultiThread(numThread);
            runner.tokenize_multithread(output, dict, filepath);
        }
    }
}
