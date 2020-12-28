package com.olegpro.sc;

import java.io.Closeable;
import java.io.InputStream;
import java.util.Scanner;
import java.util.regex.Pattern;

/**
 * Wrapper around java.util.Scanner
 */
public abstract class TextChunker implements Closeable {
    static final Pattern CHUNK_DELIMITER_REGEXP_PATTERN = Pattern.compile("\\n\\s*\\n");
    private Scanner inputTextScanner;

    public TextChunker(Scanner inputFileScanner ) {
        this.inputTextScanner = inputFileScanner;
        this.inputTextScanner.useDelimiter(CHUNK_DELIMITER_REGEXP_PATTERN);
    }

    public TextChunker(InputStream inputStream) {
        this( new Scanner(inputStream));
    }

    public boolean hasNext() {
        return inputTextScanner.hasNext();
    }

    public String next() {
        return inputTextScanner.next();
    }

    public void close() {
        inputTextScanner.close();
    }

}
