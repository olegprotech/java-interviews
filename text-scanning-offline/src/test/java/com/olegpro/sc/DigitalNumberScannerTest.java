package com.olegpro.sc;

import org.junit.Before;
import org.junit.Test;

import static com.olegpro.sc.DigitalNumberScanner.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Unit test for simple DigitalNumberScanner.
 * In fact some of them are technically integration / acceptance tests.
 */
public class DigitalNumberScannerTest
{
    DigitalNumberScanner digitalNumberScanner;
    @Before
    public void setup() throws Exception {
        digitalNumberScanner = new DigitalNumberScanner();
        digitalNumberScanner.init();
    }

    /**
     * Validate whether the symbols are recognized correctly.
     * Need to move the file to test resources and load from there.
     */
    @Test
    public void shouldScanSingleChunk() throws Exception {
        StringBuilder consoleOutput = new StringBuilder();
        digitalNumberScanner.dataOutputProvider = consoleOutput::append;
        digitalNumberScanner.scan(this.getClass().getResource("/singleChunk").getPath());
        assertEquals(String.format("000000000%n"), consoleOutput.toString());
    }

    /**
     * Validate whether the symbols are recognized correctly.
     * Need to move the file to test resources and load from there.
     */
    @Test
    public void shouldScanMultipleChunk() throws Exception {
        StringBuilder consoleOutput = new StringBuilder();
        digitalNumberScanner.dataOutputProvider = consoleOutput::append;
        digitalNumberScanner.scan(this.getClass().getResource("/multipleChunks").getPath());
        assertEquals("Output should contain 3 lines", 3, consoleOutput.toString().split(LINE_DELIMITER_REGEXP).length);
    }

    @Test
    public void shouldHaveOneILLIndicator() throws Exception {
        StringBuilder consoleOutput = new StringBuilder();
        digitalNumberScanner.dataOutputProvider = consoleOutput::append;
        digitalNumberScanner.scan(this.getClass().getResource("/multipleChunksWithIllegalRow").getPath());
        assertTrue("Output should contain 1 ILL message", consoleOutput.toString().contains(ILLEGAL_INPUT_INDICATOR));
    }

    @Test
    public void shouldHaveOneUnrecognisedSymbolIndicator() throws Exception {
        StringBuilder consoleOutput = new StringBuilder();
        digitalNumberScanner.dataOutputProvider = consoleOutput::append;
        digitalNumberScanner.scan(this.getClass().getResource("/multipleChunksWithIllegalRow").getPath());
        assertTrue("Output should contain 1 ? sign", consoleOutput.toString().contains(UNRECOGNIZED_SYMBOL_SIGN));
    }

    @Test
    public void shouldHaveUnrecognisedChunk() throws Exception {
        StringBuilder output = new StringBuilder();
        digitalNumberScanner.logOutputProvider = output::append;
        digitalNumberScanner.scan(this.getClass().getResource("/multipleChunksWithCorruptedChunk").getPath());
        assertTrue("Output should contain error message", output.toString().contains("Cannot read the chunk"));
    }

    @Test(expected = Exception.class)
    public void shouldThrowException() throws Exception {
        digitalNumberScanner.scan("/missingFile");
    }

    /**
     * Validate whether the symbols are recognized correctly.
     * Need to move the file to test resources and load from there.
     */
    @Test
    public void shouldHandleExceptionsWhenProcessingMultipleChunk() throws Exception {
        digitalNumberScanner = new DigitalNumberScanner() {
            private boolean toggle = true;
          void scanChunk(String chunk) {
              toggle = !toggle;
              if (toggle) {
                  throw new RuntimeException("Error processing chunk");
              } else {
                  dataOutputProvider.accept(String.format("000000000%n"));
              }
            }
        };
        digitalNumberScanner.init();
        StringBuilder consoleOutput = new StringBuilder();
        digitalNumberScanner.dataOutputProvider = consoleOutput::append;
        digitalNumberScanner.scan(this.getClass().getResource("/multipleChunks").getPath());
        assertEquals("Output should contain 3 lines", 2, consoleOutput.toString().split(LINE_DELIMITER_REGEXP).length);
    }


    @Test
    public void shouldRecognizeDigits() {
        assertEquals( "Recognizing 7", "7", digitalNumberScanner.recognizeDigit(" _   |  |"));
    }

    @Test
    public void shouldNotRecognizeDigits() {
        assertEquals( "Recognizing a corrupt symbol", UNRECOGNIZED_SYMBOL_SIGN, digitalNumberScanner.recognizeDigit("x_   |  |"));
    }
}
