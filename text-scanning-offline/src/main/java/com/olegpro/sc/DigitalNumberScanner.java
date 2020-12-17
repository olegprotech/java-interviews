package com.olegpro.sc;

import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
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
    static final String DIGITS_VAL_MAP_FILE_CLASSPATH_RESOURCE_PATH = "/digitsVal";
    static final String APP_PROPERTIES_CLASSPATH_RESOURCE_PATH = "/DigitalNumberScanner.properties";
    static final String LINE_DELIMITER_REGEXP = "\\r?\\n";
    static final String UNRECOGNIZED_SYMBOL_SIGN = "?";
    public static final String ILLEGAL_INPUT_INDICATOR = "ILL";
    private int digitWidth;
    private int digitHeight;
    private int numberOfDigitsInAChunk;
    private int numberOfDigitsInDigitsMap;
    private int chunkLineLength;
    private DigitReader digitReader;
    private Map<String, String> digitsMap;
    Consumer<String> dataOutputProvider = System.out::print;
    Consumer<String> logOutputProvider = System.out::print;
    boolean fuzzyMatchingMode = false;

    /**
     * This entry point expects the name of file to process as the first argument.
     * It can be refactored to take in many files or a folder.
     * For simplicity, leaving it to a single file for now.
     * */
    public static void main(String[] args) {
        try {
            if (null == args || args.length < 1) {
                System.out.println("Please provide the name of the file as the first argument.");
                System.exit(1);
            }
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
    void init() throws InitException {
        initProperties();
        initDigitsMap();
    }

    private void initProperties() throws InitException {
        try (
                final InputStream propertiesFileInputStream =
                        this.getClass().getResourceAsStream(APP_PROPERTIES_CLASSPATH_RESOURCE_PATH)) {
            final Properties properties = new Properties();
            properties.load(propertiesFileInputStream);
            digitWidth = Integer.valueOf(properties.getProperty("digital.number.scanner.digit.width"));
            digitHeight = Integer.valueOf(properties.getProperty("digital.number.scanner.digit.height"));
            numberOfDigitsInDigitsMap = Integer.valueOf(properties.getProperty("digital.number.scanner.map.numberOfDigits"));
            numberOfDigitsInAChunk = Integer.valueOf(properties.getProperty("digital.number.scanner.chunk.numberOfDigits"));
            chunkLineLength = numberOfDigitsInAChunk * digitWidth;
            digitReader = new DigitReader(digitWidth, digitHeight);
        } catch (Exception e) {
            throw new InitException(e);
        }
    }

    /** It will open the file, chunk it and process each chunk independently. Even if the chunk fails to process, others would still be attempted.
     * @param inputFilePath
     * @throws ScanException
     */
    public void scan(String inputFilePath) throws ScanException {
        TextFileChunker inputFileChunker = null;
        try {
            logOutputProvider.accept(String.format("Reading %s %n", inputFilePath));
            inputFileChunker = new TextFileChunker(inputFilePath);
            while (inputFileChunker.hasNext()) {
                try {
                    String chunk = inputFileChunker.next();
                    scanChunk(chunk);
                } catch (Exception e) {
                    // this is what would go into log for investigation and manual correction later.
                    logOutputProvider.accept("Failed processing one chunk but proceeding with others. Error" + e.getStackTrace());
                }
             }
        } catch (Exception e) {
            throw new ScanException(e);
        }
        finally {
            if (null != inputFileChunker) { inputFileChunker.close(); }
        }
    }

    void scanChunk(String chunk) {
        String[] lines = Arrays.stream(chunk.split(LINE_DELIMITER_REGEXP)).map(s -> s.replace("\r", "")).toArray(String[]::new);

        if (!validateChunkLines(lines)) {
            logOutputProvider.accept(String.format("Cannot read the chunk %n%s%n", chunk));
            return;
        }
        boolean hadIllegalSymbols = false;
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

    String recognizeDigit(String digit) {
        if (fuzzyMatchingMode) {
            Map<String, Integer> distanceMap = new HashMap<>();
            Map<Integer, String> inversedDistanceMap = new HashMap<>();
            for (String symbol : digitsMap.keySet()) {
                int distance = StringUtils.getLevenshteinDistance(symbol, digit);
                distanceMap.put(symbol, Integer.valueOf(distance));
                inversedDistanceMap.put(Integer.valueOf(distance), symbol);
            }
            Integer min =  distanceMap.values().stream().min(Integer::compareTo).get();
            String fuzzyMatchDigit = digitsMap.get(inversedDistanceMap.get(min).toString());
            return (null != fuzzyMatchDigit) ? fuzzyMatchDigit.toString() : UNRECOGNIZED_SYMBOL_SIGN;
        } else {
            return (digitsMap.containsKey(digit)) ? digitsMap.get(digit).toString() : UNRECOGNIZED_SYMBOL_SIGN;
        }
    }

    /** Creates a new map and fills it from the statically defined file in resources.
     * @throws InitException when initialisation fails.
     */
    private void initDigitsMap() throws InitException {
        digitsMap = new HashMap<String, String>();
        TextFileChunker inputFileChunker = null;
        try {
            final InputStream digitsMapFileInputStream =
                    this.getClass().getResourceAsStream(DIGITS_MAP_FILE_CLASSPATH_RESOURCE_PATH);
            inputFileChunker = new TextFileChunker(digitsMapFileInputStream);
            final InputStream digitsValMapFileInputStream =
                    this.getClass().getResourceAsStream(DIGITS_VAL_MAP_FILE_CLASSPATH_RESOURCE_PATH);
            BufferedReader reader = new BufferedReader(new InputStreamReader(digitsValMapFileInputStream));
            String symbols = reader.readLine();
            if (inputFileChunker.hasNext()) {
                String[] lines = inputFileChunker.next().split(LINE_DELIMITER_REGEXP);
                for (int digitNumber = 0; digitNumber < numberOfDigitsInDigitsMap; digitNumber++) {
                    String digit = digitReader.read(lines, digitNumber);
                    digitsMap.put(digit, String.valueOf(symbols.charAt(digitNumber)));
                }
            }
            else {
                throw new InitException("Failed reading digits map");
            }
        }
        catch (Exception e) {
            throw new InitException(e);
        }
        finally {
            if (null != inputFileChunker) {inputFileChunker.close(); }
        }
    }

    boolean validateChunkLines(String[] lines) {
        if (lines == null) { throw new IllegalArgumentException("Lines argument is null"); }
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
