package com.olegpro.sc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Scanner;

/**
 * Wrapper around java.util.Scanner
 */
public class TextFileChunker extends TextChunker {
    public TextFileChunker(String inputFilePath ) throws FileNotFoundException {
        super( new Scanner(new File(inputFilePath)));
    }

    public TextFileChunker(InputStream inputStream) {
        super(inputStream);
    }
}
