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
 */
public class DigitalNumberScanner {
    static final Pattern CHUNK_DELIMITER_REGEXP_PATTERN = Pattern.compile("\\n\\s*\\n");
    static final String DIGITS_MAP_FILE_CLASSPATH_RESOURCE_PATH = "/digits";
    static final String APP_PROPERTIES_CLASSPATH_RESOURCE_PATH = "/DigitalNumberScanner.properties";
    static final String LINE_DELIMITER_REGEXP = "\\n";
    static final String UNRECOGNIZED_SYMBOL_SIGN = "?";
    int digitWidth;
    int digitHeight;
    int numberOfDigits;
    int lineLength;
    DigitReader digitReader = new DigitReader();
    Map<String, Integer> digitsMap;

    /**
     * This entry point expects the name of file to process as the first argument.
     * It can be refactored to take in many files or a folder.
     * For simplicity, leaving it to a single file for now.
     * */
    public static void main(String[] args) {
        try {
            String inputFilePath = args[0];
            DigitalNumberScanner digitalNumberScanner = new DigitalNumberScanner();
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
    void init() throws Exception {
        final Properties properties = new Properties();
        try (
                final InputStream propertiesFileInputStream =
                        this.getClass().getResourceAsStream(APP_PROPERTIES_CLASSPATH_RESOURCE_PATH)) {
            properties.load(propertiesFileInputStream);
            digitWidth = Integer.valueOf(properties.getProperty("digitWidth"));
            digitHeight = Integer.valueOf(properties.getProperty("digitHeight"));
            numberOfDigits = Integer.valueOf(properties.getProperty("numberOfDigits"));
            lineLength = numberOfDigits * digitWidth;
            initDigitsMap();
        } catch (IOException e) {
            throw new Exception("Initialisation failed", e);
        }
    }

    private void scanInputFile(String inputFilePath) throws Exception {
        try {
            System.out.printf("Reading %s %n", inputFilePath);
            File inputFile = new File(inputFilePath);
            Scanner inputFileScanner = new Scanner(inputFile);
            inputFileScanner.useDelimiter(CHUNK_DELIMITER_REGEXP_PATTERN);
            while (inputFileScanner.hasNext()) {
                // Read and validate that the lines conform to expectations. If not, the null is returned and the chunk is skipped.
                String[] lines = readAndValidateNextChunk(inputFileScanner);
                if (lines == null) continue;
                Boolean hadIllegalSymbols = Boolean.FALSE;
                for (int digitNumber = 0; digitNumber < numberOfDigits; digitNumber++) {
                    int offset = digitNumber * digitWidth;
                    String digit = digitReader.read(lines, offset, digitWidth, digitHeight);
                    String output = recognizeDigit(digit);
                    hadIllegalSymbols |= (UNRECOGNIZED_SYMBOL_SIGN.equals(output));
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

    String recognizeDigit(String digit) {
        return (digitsMap.containsKey(digit)) ? digitsMap.get(digit).toString() : UNRECOGNIZED_SYMBOL_SIGN;
    }

    /** Creates a new map and fills it from the statically defined file in resources.
     * @throws Exception
     */
    private void initDigitsMap() throws Exception {
        digitsMap = new HashMap<String, Integer>();
        Scanner inputFileScanner;
        try (InputStream stream = this.getClass().getResourceAsStream(DIGITS_MAP_FILE_CLASSPATH_RESOURCE_PATH)) {
            inputFileScanner = new Scanner(stream);
            inputFileScanner.useDelimiter(CHUNK_DELIMITER_REGEXP_PATTERN);
            if (inputFileScanner.hasNext()) {
                String[] lines = readAndValidateNextChunk(inputFileScanner);
                for (int digitNumber = 0; digitNumber < numberOfDigits; digitNumber++) {
                    int offset = digitNumber * digitWidth;
                    String digit = digitReader.read(lines, offset, digitWidth, digitHeight);
                    digitsMap.put(digit, Integer.valueOf(digitNumber+1));
                }
            }
            else {
                throw new Exception("Failed reading digits map");
            }
        }
        catch (RuntimeException e) {
            throw new Exception("Failed reading digits map", e);
        }

    }

    /**
     * @param inputFileScanner
     * @return array of strings of expected size, or null in case the chunk doesn't conform to expectations.
     */
    String[] readAndValidateNextChunk(Scanner inputFileScanner) {
        if (inputFileScanner == null) throw new IllegalArgumentException("Scanner argument is null.");
        String chunk = inputFileScanner.next();
        String[] lines = chunk.split(LINE_DELIMITER_REGEXP);
        if (validateChunkLines(lines, digitHeight, lineLength)) return lines;
        else {
            System.out.printf("Cannot read the chunk %n%s%n", chunk);
            return null;
        }
    }

    boolean validateChunkLines(String[] lines, int numLinesInChunk, int lineLength) {
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

}
