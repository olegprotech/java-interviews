package com.olegpro.sc;

/**
 * TODO this can be refactored more to separate parameters to properties.
 */
public class DigitReader {

    int digitWidth;
    int digitHeight;

    public DigitReader(int digitWidth, int digitHeight) {
        this.digitWidth = digitWidth;
        this.digitHeight = digitHeight;
    }

    /**
     * @param lines array of strings representing the digital number
     * @param digitNumber number of the digit to read
     * @param digitWidth width of the digit to read
     * @param digitHeight number of lines of the digit to read
     * @return returns the string containing the array of characters representing the complete digit.
     * E.g. for the digit "0" represented as 3x3, we will get " _ | ||_|".
     */
    public String read(String[] lines, int digitNumber) {
        int offset = digitNumber * digitWidth;
        StringBuffer digit = new StringBuffer();
        for (int row = 0; row < digitHeight; row++) {
            digit.append(lines[row].substring(offset, offset + digitWidth));
        }
        return digit.toString();
    }
}
