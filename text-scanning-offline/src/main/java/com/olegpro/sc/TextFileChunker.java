package com.olegpro.sc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Scanner;
import java.util.regex.Pattern;

/**
 * Wrapper around java.util.Scanner
 */
public class TextFileChunker {
    static final Pattern CHUNK_DELIMITER_REGEXP_PATTERN = Pattern.compile("\\n\\s*\\n");
    private Scanner inputFileScanner;

    public TextFileChunker(Scanner inputFileScanner ) {
        this.inputFileScanner = inputFileScanner;
        this.inputFileScanner.useDelimiter(CHUNK_DELIMITER_REGEXP_PATTERN);
    }

    public TextFileChunker(String inputFilePath ) throws FileNotFoundException {
        this( new Scanner(new File(inputFilePath)));
    }

    public TextFileChunker(InputStream digitsMapFileInputStream) {
        this( new Scanner(digitsMapFileInputStream));
    }

    public boolean hasNext() {
        return inputFileScanner.hasNext();
    }

    public String next() {
        return inputFileScanner.next();
    }

    public void close() {
        inputFileScanner.close();
    }

}
