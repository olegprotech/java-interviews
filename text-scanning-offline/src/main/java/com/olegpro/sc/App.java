package com.olegpro.sc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import java.util.regex.Pattern;

/**
 * About 2h to the point of reading the input files and producing the expected output. No tests, no optimization.
 * Basic validation of the input files.
 * TODO:
 * * Refactor to the proper classes and methods
 * * Clean up the variables naming
 * * Testing
 * * Much more input validation
 * * Infer the sizing from the digit map file... maybe?
 * * Do we have to statically define the amount of digits in the number? Definitely not... this can be inferred by dividing the line length by the digit width.
 */
public class App {
    int digitWidth;
    int digitHeight;
    int numberOfDigits;
    int lineLength = numberOfDigits * digitWidth;
    Map<String, Integer> digitsMap;

    /**
     * This entry point expects the name of file to process as the first argument.
     * It can be refactored to take in many files or a folder.
     * For simplicity, leaving it to a single file for now.
     * */
    public static void main(String[] args) {
        try {
            String inputFilePath = args[0];
            App digitalNumberScanner = new App();
            digitalNumberScanner.init();
            digitalNumberScanner.scanInputFile(inputFilePath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Reads in the application properties as well as the digit symbol definitions.
     * @throws Exception and wraps any specific exception occurring during the initialization into it.
     */
    private void init() throws Exception {
        final Properties properties = new Properties();
        try (
                final InputStream stream =
                        this.getClass().getResourceAsStream("/App.properties")) {
            properties.load(stream);
            digitWidth = Integer.valueOf(properties.getProperty("digitWidth"));
            digitHeight = Integer.valueOf(properties.getProperty("digitHeight"));
            numberOfDigits = Integer.valueOf(properties.getProperty("numberOfDigits"));
            lineLength = numberOfDigits * digitWidth;
            digitsMap = readDigitsMap();
        } catch (IOException e) {
            throw new Exception("Initialisation failed", e);
        }
    }

    private void scanInputFile(String inputFilePath) throws Exception {
        try {
            System.out.printf("Reading %s %n", inputFilePath);
            File inputFile = new File(inputFilePath);
            Scanner inputFileScanner = new Scanner(inputFile);
            inputFileScanner.useDelimiter(Pattern.compile("\\n\\s*\\n"));
            while (inputFileScanner.hasNext()) {
                // Read and validate that the lines conform to expectations. If not, the null is returned and the chunk is skipped.
                String[] lines = readAndValidateNextChunk(inputFileScanner);
                if (lines == null) continue;
                Boolean hadIllegalSymbols = Boolean.FALSE;
                for (int digitNumber = 0; digitNumber < numberOfDigits; digitNumber++) {
                    int offset = digitNumber * digitWidth;
                    String digit = readDigit(lines, offset, digitWidth, digitHeight);
                    String output;
                    if (digitsMap.containsKey(digit)) {
                        output = digitsMap.get(digit).toString();
                    } else {
                        output = "?";
                        hadIllegalSymbols = Boolean.TRUE;
                    }
                    System.out.print(output);
                }
                if (hadIllegalSymbols) {
                    System.out.print("ILL");
                }
                System.out.println();
            }
        } catch (FileNotFoundException e) {
            throw new Exception("Processing failed", e);
        }
    }

    private Map<String, Integer> readDigitsMap() throws Exception {
        Map<String, Integer> digitsMap = new HashMap<String, Integer>();
        final InputStream stream =
                this.getClass().getResourceAsStream("/digits");
        Scanner inputFileScanner = new Scanner(stream);
        inputFileScanner.useDelimiter(Pattern.compile("\\n\\s*\\n"));
        if (inputFileScanner.hasNext()) {
            String[] lines = readAndValidateNextChunk(inputFileScanner);
            for (int digitNumber = 0; digitNumber < numberOfDigits; digitNumber++) {
                int offset = digitNumber * digitWidth;
                String digit = readDigit(lines, offset, digitWidth, digitHeight);
                digitsMap.put(digit, Integer.valueOf(digitNumber));
            }
        }
        else {
            throw new Exception("Failed reading digits map");
        }
        return digitsMap;
    }

    /**
     * @param inputFileScanner
     * @return array of strings of expected size, or null in case the chunk doesn't conform to expectations.
     */
    private String[] readAndValidateNextChunk(Scanner inputFileScanner) {
        if (inputFileScanner == null) throw new IllegalArgumentException("Scanner argument is null.");
        String chunk = inputFileScanner.next();
        String[] lines = chunk.split("\\n");
        if (validateChunkLines(lines, digitHeight, lineLength)) return lines;
        else return null;
    }

    private boolean validateChunkLines(String[] lines, int numLinesInChunk, int lineLength) {
        if (lines == null) throw new IllegalArgumentException("Lines argument is null");
        if (numLinesInChunk != lines.length) {
            System.out.printf("Incorrect number of lines in chunk. Expected %d, found %d.%n", numLinesInChunk, lines.length);
            return false;
        }
        for (String line : lines) {
            if (lineLength != line.length()) {
                System.out.printf("Line %s has incorrect length. Expected %d, found %d.%n", line, lineLength, line.length());
                return false;
            }
        }
        return true;
    }

    private String readDigit(String[] lines, int offset, int digitWidth, int digitHeight) {
        StringBuffer digit = new StringBuffer();
        for (int row = 0; row < digitHeight; row++) {
            digit.append(lines[row].substring(offset, offset + digitWidth));
        }
        return digit.toString();
    }
}
