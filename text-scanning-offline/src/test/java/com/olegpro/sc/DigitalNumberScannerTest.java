package com.olegpro.sc;

import static org.junit.Assert.assertEquals;
import static com.olegpro.sc.DigitalNumberScanner.UNRECOGNIZED_SYMBOL_SIGN;

import org.junit.Before;
import org.junit.Test;

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
     * Validate whether the symbols are recognized correctly
     */
    @Test
    public void shouldRecognizeDigits()
    {
        assertEquals( "Recognizing 7", "7", digitalNumberScanner.recognizeDigit(" _   |  |"));
        assertEquals( "Recognizing a corrupt symbol", UNRECOGNIZED_SYMBOL_SIGN, digitalNumberScanner.recognizeDigit("x_   |  |"));
    }
}
