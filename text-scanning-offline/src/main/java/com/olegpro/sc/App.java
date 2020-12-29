package com.olegpro.sc;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class App {
    /**
     * This entry point expects the name of file to process as the first argument.
     * It can be refactored to take in many files or a folder.
     * For simplicity, leaving it to a single file for now.
     * */
    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();
        try {
            if (null == args || args.length < 1) {
                System.out.println("Please provide the name of the file as the first argument.");
                System.exit(1);
            }
            String inputFilePath = args[0];
            Set<String> argsSet = Arrays.stream(args).collect(Collectors.toSet());
            boolean parallel = argsSet.contains("parallel");
            boolean delay = argsSet.contains("delay");
            boolean fuzzy = argsSet.contains("fuzzy");

            DigitalNumberScanner digitalNumberScanner = new DigitalNumberScanner();
            digitalNumberScanner.init();
            digitalNumberScanner.delayArtificially = delay;
            digitalNumberScanner.fuzzyMatchingMode = fuzzy;
            if (parallel) {
                digitalNumberScanner.scanFileParallel(inputFilePath);
            } else {
                digitalNumberScanner.scanFile(inputFilePath);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.print(String.format("Completed in %d ms. %n", System.currentTimeMillis() - startTime));
    }
}
