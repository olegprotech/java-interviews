package com.olegpro.sc;

import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
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
    public static final int SYMBOL_RECOGNITION_ARTIFICIAL_SLOWDOWN_MICROSECONDS = 1;
    private int digitWidth;
    private int digitHeight;
    private int numberOfDigitsInAChunk;
    private int numberOfDigitsInDigitsMap;
    private int chunkLineLength;
    private int numberOfChunksInBlock = 100;// TODO to property file
    private int numberOfPreFetchBlocks = 15;// TODO to property file
    private int numberOfParallelBlocksProcessed = 5;// TODO to property file


    private DigitReader digitReader;
    private Map<String, String> digitsMap;
    Consumer<String> dataOutputProvider = System.out::print;
    Consumer<String> logOutputProvider = System.out::print;
    boolean fuzzyMatchingMode = false;
    boolean delayArtificially = false;

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
            boolean parallel = false;
            boolean delay = false;
            if (args.length >= 2) {
                parallel = ("parallel".equals(args[1]));
            }
            if (args.length >= 3) {
                delay = ("delay".equals(args[2]));
            }
            DigitalNumberScanner digitalNumberScanner = new DigitalNumberScanner();
            digitalNumberScanner.init();
            digitalNumberScanner.delayArtificially = delay;
            //digitalNumberScanner.fuzzyMatchingMode = true;
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
    public void scanFile(String inputFilePath) throws ScanException {
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

    /** It will open the file, read it sequentially, break down into blocks of chunks and process them in parallel.
     *  Each chunk in a block is then processed sequentially inside of a separate process.
     *  Even if the chunk fails to process, others would still be attempted.
     *  It ensures that no more than certain amount of blocks are loaded into memory at every point in time.
     *  Maximum queue size should be larger than the thread pool size so that there are blocks pre-fetched and ready to be processed.
     *  Results are aggregated and written to output in the sequential order in a separate thread.
     * @param inputFilePath
     * @throws ScanException
     */
    public void scanFileParallel(String inputFilePath) throws ScanException {
        TextFileChunker inputFileChunker = null;

        try {
            logOutputProvider.accept(String.format("Reading %s %n", inputFilePath));
            inputFileChunker = new TextFileChunker(inputFilePath);
            List<String> block = new LinkedList<>();
            int blockNumber = 0;
            ExecutorService scanningExecutor = Executors.newFixedThreadPool(numberOfParallelBlocksProcessed);
            ResultsAggregator resultsAggregator = new ResultsAggregator(this.dataOutputProvider, this.logOutputProvider);
            Thread resultsAggregatorThread = new Thread(resultsAggregator);
            resultsAggregatorThread.start();

            while (inputFileChunker.hasNext()) {
                if (resultsAggregator.size() >= numberOfPreFetchBlocks) {
                    logOutputProvider.accept(String.format("Waiting for the queue to clear before scheduling more tasks. %n") );
                    TimeUnit.MILLISECONDS.sleep(1);
                    continue;
                }
                try {
                    String chunk = inputFileChunker.next();
                    block.add(chunk);
                    if (block.size() == numberOfChunksInBlock) {
                        ScanBlockOfChunksTask scanBlockOfChunksTask = new ScanBlockOfChunksTask(block, blockNumber, this);
                        // submit the task and add to results
                        resultsAggregator.add(scanningExecutor.submit(scanBlockOfChunksTask));
                        // starting the new block
                        block = new LinkedList<>();
                        blockNumber++;
                    }
                } catch (Exception e) {
                    // this is what would go into log for investigation and manual correction later.
                    logOutputProvider.accept("Failed processing one chunk but proceeding with others. Error" + e.getStackTrace());
                }
            }
            // submitting the last task
            if (block.size() != 0) {
                ScanBlockOfChunksTask scanBlockOfChunksTask = new ScanBlockOfChunksTask(block, blockNumber, this);
                resultsAggregator.add(scanningExecutor.submit(scanBlockOfChunksTask));
            }
            resultsAggregator.finishedPublishing = true;
            scanningExecutor.shutdown();
            resultsAggregatorThread.join();
            scanningExecutor.awaitTermination(10, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new ScanException(e);
        }
        finally {
            if (null != inputFileChunker) { inputFileChunker.close(); }
        }
    }

    void scanChunk(String chunk) {
        scanChunk(chunk, this.dataOutputProvider, this.logOutputProvider);
    }

    /** Added this method signature to support parallel processing where the data output has to be buffered independently for parallel processes. */
    void scanChunk(String chunk, Consumer<String> dataOutputProvider, Consumer<String> logOutputProvider) {
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

    /** Always check for exact match first. If this didn't work - try a fuzzy match if it's enabled.
     * @param digit
     * @return
     */
    String recognizeDigit(String digit) {
        //TODO Here we are artificially slowing down the process to simulate a complex processing that benefits from parallel execution.
        delayArtificially();

        if (digitsMap.containsKey(digit)) {
            return digitsMap.get(digit);
        } else if (fuzzyMatchingMode) {
            Map<Integer, String> symbolByDistanceMap = new HashMap<>();
            for (String symbol : digitsMap.keySet()) {
                int distance = StringUtils.getLevenshteinDistance(symbol, digit);
                symbolByDistanceMap.put(Integer.valueOf(distance), symbol);
            }
            Integer minDistance =  symbolByDistanceMap.keySet().stream().min(Integer::compareTo).get();
            String fuzzyMatchSymbol = symbolByDistanceMap.get(minDistance);
            if (digitsMap.containsKey(fuzzyMatchSymbol)) {
                return digitsMap.get(fuzzyMatchSymbol);
            }
        }
        return UNRECOGNIZED_SYMBOL_SIGN;
    }

    private void delayArtificially() {
        if (delayArtificially) {
            try {
                TimeUnit.MICROSECONDS.sleep(SYMBOL_RECOGNITION_ARTIFICIAL_SLOWDOWN_MICROSECONDS);
            } catch (InterruptedException e) {
                logOutputProvider.accept(String.format("Failed to delay recognizeDigit with error %s %n", e.getMessage()));
            }
        }
    }

    /** Creates a new map and fills it from the statically defined file in resources.
     * @throws InitException when initialisation fails.
     */
    private void initDigitsMap() throws InitException {
        digitsMap = new HashMap<String, String>();
        TextFileChunker inputFileChunker = null;
        InputStream digitsValMapFileInputStream = null;
        try {
            final InputStream digitsMapFileInputStream =
                    this.getClass().getResourceAsStream(DIGITS_MAP_FILE_CLASSPATH_RESOURCE_PATH);
            inputFileChunker = new TextFileChunker(digitsMapFileInputStream);
            digitsValMapFileInputStream =
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
            if (null != digitsValMapFileInputStream) {
                try {
                    digitsValMapFileInputStream.close();
                } catch (IOException e) {
                    throw new InitException("Failed to close the digit value map file.", e);
                }
            }
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
