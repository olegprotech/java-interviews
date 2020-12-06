package com.olegpro.sc;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.function.Consumer;

/**
 * It's the main class. It loads the property file and the map of known digits, initializes the application.
 * Servers as the entry point.
 * Takes the file to be processed as a command line argument.
 * Currently only supports single file. Can be easily extended to multiple files.
 */
public class DigitalNumberScanner {
    static final String DIGITS_MAP_FILE_CLASSPATH_RESOURCE_PATH = "/digits";
    static final String APP_PROPERTIES_CLASSPATH_RESOURCE_PATH = "/DigitalNumberScanner.properties";
    static final String LINE_DELIMITER_REGEXP = "\\n";
    static final String UNRECOGNIZED_SYMBOL_SIGN = "?";
    public static final String ILLEGAL_INPUT_INDICATOR = "ILL";
    private int digitWidth;
    private int digitHeight;
    private int numberOfDigitsInAChunk;
    private int numberOfDigitsInDigitsMap;
    private int chunkLineLength;
    private DigitReader digitReader;
    private Map<String, Integer> digitsMap;
    Consumer<String> dataOutputProvider = System.out::print;
    Consumer<String> logOutputProvider = System.out::print;

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
            digitWidth = Integer.valueOf(properties.getProperty("digital.number.scanner.digit.width"));
            digitHeight = Integer.valueOf(properties.getProperty("digital.number.scanner.digit.height"));
            numberOfDigitsInDigitsMap = Integer.valueOf(properties.getProperty("digital.number.scanner.map.numberOfDigits"));
            numberOfDigitsInAChunk = Integer.valueOf(properties.getProperty("digital.number.scanner.chunk.numberOfDigits"));
            chunkLineLength = numberOfDigitsInAChunk * digitWidth;
            digitReader = new DigitReader(digitWidth, digitHeight);
            initDigitsMap();
        } catch (IOException e) {
            throw new Exception("Initialisation failed", e);
        }
    }

    public void scan(String inputFilePath) throws Exception {
        try {
            logOutputProvider.accept(String.format("Reading %s %n", inputFilePath));
            TextFileChunker inputFileChunker = new TextFileChunker(inputFilePath);
            while (inputFileChunker.hasNext()) {
                String chunk = inputFileChunker.next();
                String[] lines = chunk.split(LINE_DELIMITER_REGEXP);
                if (!validateChunkLines(lines)) {
                    logOutputProvider.accept(String.format("Cannot read the chunk %n%s%n", chunk));
                    continue;
                }
                boolean hadIllegalSymbols = Boolean.FALSE;
                for (int digitNumber = 0; digitNumber < numberOfDigitsInAChunk; digitNumber++) {
                    String digit = digitReader.read(lines, digitNumber);
                    String output = recognizeDigit(digit);
                    // This operation is excessive. Can be optimized if done only in case the unrecognized characters.
                    hadIllegalSymbols |= (UNRECOGNIZED_SYMBOL_SIGN.equals(output));
                    // Output as soon as possible.
                    dataOutputProvider.accept(output);
                }
                if (hadIllegalSymbols) {
                    dataOutputProvider.accept(ILLEGAL_INPUT_INDICATOR);
                }
                dataOutputProvider.accept(String.format("%n"));
            }
        } catch (FileNotFoundException e) {
            throw new Exception("Processing failed", e);
        }
    }

    String recognizeDigit(String digit) {
        return (digitsMap.containsKey(digit)) ? digitsMap.get(digit).toString() : UNRECOGNIZED_SYMBOL_SIGN;
    }

    /** Creates a new map and fills it from the statically defined file in resources.
     * @throws Exception when initialisation fails.
     */
    private void initDigitsMap() throws Exception {
        digitsMap = new HashMap<String, Integer>();
        try {
            final InputStream digitsMapFileInputStream =
                    this.getClass().getResourceAsStream(DIGITS_MAP_FILE_CLASSPATH_RESOURCE_PATH);
            TextFileChunker inputFileChunker = new TextFileChunker(digitsMapFileInputStream);
            if (inputFileChunker.hasNext()) {
                String[] lines = inputFileChunker.next().split(LINE_DELIMITER_REGEXP);
                for (int digitNumber = 0; digitNumber < numberOfDigitsInDigitsMap; digitNumber++) {
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
            logOutputProvider.accept(String.format("Incorrect number of lines in chunk. Expected %d, found %d.%n", digitHeight, lines.length));
            return false;
        }
        for (String line : lines) {
            if (chunkLineLength != line.length()) {
                logOutputProvider.accept(String.format("Line %s has incorrect length. Expected %d, found %d.%n", line, chunkLineLength, line.length()));
                return false;
            }
        }
        return true;
    }

}
