package com.olegpro.sc;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Unit test for simple DigitalNumberScanner.
 */
public class DigitReaderTest
{
    /**
     * Checks that the reader correctly extracts the 3x3 character matrix into the string.
     */
    @Test
    public void shouldRead7()
    {
        DigitReader digitReader = new DigitReader();
        String[] lines = {" _  _  _ ", "| || |  |", "|_||_|  |"};
        String digit = digitReader.read(lines, 6, 3, 3);
        assertEquals(" _   |  |", digit);
    }
}
