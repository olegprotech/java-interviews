package com.olegpro.sc;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
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
 * * Do we have to statically define the amount of digits in the number? Definitey not... this can be inferred by dividing the line length by the digit width.
 */
public class App {
    static int digitWidth = 3;
    static int digitHeight = 3;
    static int numberOfDigits = 9;
    static int lineLength = numberOfDigits * digitWidth;
    static String multipleChunksFilePath = "/Users/oleg/IdeaProjects/java-interviews/text-scanning-offline/input/multipleChunks";
    static String multipleChunksWithIllegalRowFilePath = "/Users/oleg/IdeaProjects/java-interviews/text-scanning-offline/input/multipleChunksWithIllegalRow";

    public static void main(String[] args) {

        try {
            Map<String, Integer> digitsMap = readDigitsMap();
            readInputFile(digitsMap, multipleChunksFilePath);
            readInputFile(digitsMap, multipleChunksWithIllegalRowFilePath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    private static void readInputFile(Map<String, Integer> digitsMap, String path) throws FileNotFoundException {
        System.out.printf("Reading %s %n", path);
        File inputFile = new File(path);
        Scanner inputFileScanner = new Scanner(inputFile);
        inputFileScanner.useDelimiter(Pattern.compile("\\n\\s*\\n"));
        while(inputFileScanner.hasNext()) {

            String[] lines = readAndValidateNextChunk(inputFileScanner);
            Boolean hadIllegalSymbols = Boolean.FALSE;
            for (int digitNumber = 0; digitNumber < numberOfDigits; digitNumber++) {
                int offset = digitNumber*digitWidth;
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
    }

    private static Map<String, Integer> readDigitsMap() throws FileNotFoundException {
        Map<String, Integer> digitsMap = new HashMap<String, Integer>();
        File inputFile = new File("/Users/oleg/IdeaProjects/java-interviews/text-scanning-offline/input/digits");
        Scanner inputFileScanner = new Scanner(inputFile);
        inputFileScanner.useDelimiter(Pattern.compile("\\n\\s*\\n"));
        if(inputFileScanner.hasNext()) {

            String[] lines = readAndValidateNextChunk(inputFileScanner);

            for (int digitNumber = 0; digitNumber < numberOfDigits; digitNumber++) {
                int offset = digitNumber*digitWidth;
                String digit = readDigit(lines, offset, digitWidth, digitHeight);
                digitsMap.put(digit, Integer.valueOf(digitNumber));
            }


        }
        return digitsMap;
    }

    private static String[] readAndValidateNextChunk(Scanner inputFileScanner) {
        String chunk = inputFileScanner.next();
        String[] lines = chunk.split("\\n");
        validateLines(lines, digitHeight, lineLength);
        return lines;
    }

    private static void validateLines(String[] lines, int numLinesInChunk, int lineLength) {
        if (lines == null) throw new IllegalArgumentException("Lines argument is null");
        if (numLinesInChunk != lines.length) {
            System.out.printf("Incorrect number of lines in chunk. Expected %d, found %d",numLinesInChunk, lines.length);
        }
        for(String line : lines) {
            if (lineLength != line.length()) {
                System.out.printf("Line %s has incorrect length. Expected %d, found %d", line, lineLength, line.length());
            }
        }
    }

    private static String readDigit(String[] lines, int offset, int digitWidth, int digitHeight) {
        StringBuffer digit = new StringBuffer();
        for (int row = 0; row < digitHeight; row++) {
            digit.append(lines[row].substring(offset, offset+digitWidth));
        }
        return digit.toString();
    }
}
