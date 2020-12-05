#!/bin/bash
java -cp ./target/*.jar com.olegpro.sc.DigitalNumberScanner ./input/multipleChunksWithIllegalRow
java -cp ./target/*.jar com.olegpro.sc.DigitalNumberScanner ./input/multipleChunksWithCorruptedChunk
