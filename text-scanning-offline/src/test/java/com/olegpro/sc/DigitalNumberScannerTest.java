package com.olegpro.sc;

import static org.junit.Assert.assertEquals;
import static com.olegpro.sc.DigitalNumberScanner.UNRECOGNIZED_SYMBOL_SIGN;

import org.junit.Before;
import org.junit.Test;

import java.net.URL;
import java.nio.file.Paths;

/**
 * Unit test for simple DigitalNumberScanner.
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
        StringBuffer consoleOutput = new StringBuffer();
        digitalNumberScanner.outputStreamProvider = (output) -> {consoleOutput.append(output);};
        digitalNumberScanner.scan(this.getClass().getResource("/singleChunk").getPath());
        assertEquals(String.format("000000000%n"), consoleOutput.toString());
    }

    /**
     * Validate whether the symbols are recognized correctly
     */
    @Test
    public void shouldRecognizeDigits() {
        assertEquals( "Recognizing 7", "7", digitalNumberScanner.recognizeDigit(" _   |  |"));
        assertEquals( "Recognizing a corrupt symbol", UNRECOGNIZED_SYMBOL_SIGN, digitalNumberScanner.recognizeDigit("x_   |  |"));
    }
}
