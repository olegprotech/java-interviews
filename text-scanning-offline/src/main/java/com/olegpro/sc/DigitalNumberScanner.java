package com.olegpro.sc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import java.util.function.Consumer;
import java.util.regex.Pattern;

/**
 * TODO:
 * Handle "0" digit as in the single chunk. This would require to have different sizes for digits map and input files.
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
    int numberOfDigitsInAChunk;
    int numberOfDigits;
    int lineLength;
    DigitReader digitReader;
    Map<String, Integer> digitsMap;
    Consumer<String> outputStreamProvider = (output) -> { System.out.print(output); };

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
            digitalNumberScanner.scan(inputFilePath);
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
            numberOfDigitsInAChunk = Integer.valueOf(properties.getProperty("numberOfDigitsInAChunk"));
            lineLength = numberOfDigitsInAChunk * digitWidth;
            digitReader = new DigitReader(digitWidth, digitHeight);
            initDigitsMap();
        } catch (IOException e) {
            throw new Exception("Initialisation failed", e);
        }
    }

    public void scan(String inputFilePath) throws Exception {
        try {
            System.out.printf("Reading %s %n", inputFilePath);
            File inputFile = new File(inputFilePath);
            Scanner inputFileScanner = new Scanner(inputFile);
            inputFileScanner.useDelimiter(CHUNK_DELIMITER_REGEXP_PATTERN);
            while (inputFileScanner.hasNext()) {
                String chunk = inputFileScanner.next();
                String[] lines = chunk.split(LINE_DELIMITER_REGEXP);
                if (!validateChunkLines(lines)) {
                    System.out.printf("Cannot read the chunk %n%s%n", chunk);
                    continue;
                }
                Boolean hadIllegalSymbols = Boolean.FALSE;
                for (int digitNumber = 0; digitNumber < numberOfDigitsInAChunk; digitNumber++) {
                    String digit = digitReader.read(lines, digitNumber);
                    String output = recognizeDigit(digit);
                    // This operation is excessive. Can be optimized if done only in case the unrecognized characters.
                    hadIllegalSymbols |= (UNRECOGNIZED_SYMBOL_SIGN.equals(output));
                    // Output as soon as possible.
                    outputStreamProvider.accept(output);
                }
                if (hadIllegalSymbols) {
                    outputStreamProvider.accept("ILL");
                }
                outputStreamProvider.accept(String.format("%n"));
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
                String[] lines = inputFileScanner.next().split(LINE_DELIMITER_REGEXP);
                for (int digitNumber = 0; digitNumber < numberOfDigits; digitNumber++) {
                    String digit = digitReader.read(lines, digitNumber);
                    digitsMap.put(digit, Integer.valueOf(digitNumber));
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

    boolean validateChunkLines(String[] lines) {
        if (lines == null) throw new IllegalArgumentException("Lines argument is null");
        if (digitHeight != lines.length) {
            System.out.printf("Incorrect number of lines in chunk. Expected %d, found %d.%n", digitHeight, lines.length);
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
