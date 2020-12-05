package com.olegpro.sc;

/**
 * TODO this can be refactored more to separate parameters to properties.
 */
public class DigitReader {
    /**
     * @param lines array of strings representing the digital number
     * @param offset position to start reading the digit from lines
     * @param digitWidth width of the digit to read
     * @param digitHeight number of lines of the digit to read
     * @return returns the string containing the array of characters representing the complete digit.
     * E.g. for the digit "0" represented as 3x3, we will get " _ | ||_|".
     */
    public String read(String[] lines, int offset, int digitWidth, int digitHeight) {
        StringBuffer digit = new StringBuffer();
        for (int row = 0; row < digitHeight; row++) {
            digit.append(lines[row].substring(offset, offset + digitWidth));
        }
        return digit.toString();
    }
}
