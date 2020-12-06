package com.olegpro.sc;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class TextFileChunkerTest {

    private TextFileChunker chunker;

    @Before
    public void setUp() throws Exception {
        this.chunker = new TextFileChunker(this.getClass().getResource("/multipleChunks").getPath());
    }

    @Test
    public void shouldHaveNext() {
        assertTrue("Should have one chunk", chunker.hasNext());
    }

    @Test
    public void shouldReturn3Chunks() {
        assertTrue("Should have chunk 1", !chunker.next().isEmpty());
        assertTrue("Should have chunk 2", !chunker.next().isEmpty());
        assertTrue("Should have chunk 3", !chunker.next().isEmpty());
        assertTrue("Should have no more chunks", !chunker.hasNext());
    }
}