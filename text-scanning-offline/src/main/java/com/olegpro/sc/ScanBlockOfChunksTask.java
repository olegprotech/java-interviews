package com.olegpro.sc;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

public class ScanBlockOfChunksTask implements Callable<String> {
    public ScanBlockOfChunksTask(List<String> block, int blockNumber, DigitalNumberScanner scanner) {
        this.block = block;
        this.blockNumber = blockNumber;
        this.scanner = scanner;
    }

    List<String> block;
    int blockNumber;
    DigitalNumberScanner scanner;

    @Override
    public String call() throws Exception {
        StringBuilder data = new StringBuilder();
        scanBlockOfChunks(block, data::append, System.out::print);
        return data.toString();
    }

    public void scanBlockOfChunks(List<String> block, Consumer<String> dataOutputProvider, Consumer<String> logOutputProvider) throws ScanException {
        try {
            long startTime = System.currentTimeMillis();
            logOutputProvider.accept(String.format("Scanning the block %d %n", blockNumber));
            for (String chunk : block) {
                try {
                    scanner.scanChunk(chunk, dataOutputProvider, logOutputProvider);
                } catch (Exception e) {
                    // this is what would go into log for investigation and manual correction later.
                    logOutputProvider.accept("Failed processing one chunk but proceeding with others. Error" + e.getStackTrace());
                }
            }
            logOutputProvider.accept(String.format("Completed the block %d in %d ms. %n", blockNumber, System.currentTimeMillis() - startTime));
        } catch (Exception e) {
            throw new ScanException(e);
        }
    }

}
