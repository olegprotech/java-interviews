package com.olegpro.sc;

import java.util.LinkedList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Consumer;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * I decided not to add the "implements Deque<>" as it would add too much noise.</>
 */
public class ResultsAggregator implements Runnable {
    public static final int TIMEOUT_MILLISECONDS = 1;
    LinkedList<Future<String>> resultsQueue = new LinkedList<>();
    Consumer<String> dataOutputProvider;
    Consumer<String> logOutputProvider;
    boolean finishedPublishing = false;

    public ResultsAggregator(Consumer<String> dataOutputProvider, Consumer<String> logOutputProvider) {
        this.dataOutputProvider = dataOutputProvider;
        this.logOutputProvider = logOutputProvider;
    }

    public void add(Future<String> result) {
        resultsQueue.add(result);
    }

    public int size() {
        return resultsQueue.size();
    }
    @Override
    public void run() {
        while (true) {
            if (resultsQueue.size() == 0) {
                if (finishedPublishing) {
                    logOutputProvider.accept(String.format("Terminating the results aggregator %n "));
                    return;
                }
                try {
                    MILLISECONDS.sleep(TIMEOUT_MILLISECONDS);
                } catch (InterruptedException e) {
                    logOutputProvider.accept(String.format("Failed to wait: %s %n ", e.getMessage()));
                }
            } else {
                try {
                    // this operation will be processing the results in the order they were scanned, so it may be waiting for result N while N+1 is already processed.
                    String result = resultsQueue.pop().get();
                    dataOutputProvider.accept(result);
                } catch (InterruptedException e) {
                    logOutputProvider.accept(String.format("Failed to add the results from a batch with error: %s %n ", e.getMessage()));
                } catch (ExecutionException e) {
                    logOutputProvider.accept(String.format("Failed to add the results from a batch with error: %s %n ", e.getMessage()));
                }
            }
        }
    }
}
