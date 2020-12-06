#!/bin/bash
java -cp ./target/*.jar com.olegpro.sc.DigitalNumberScanner ./input/singleChunk
java -cp ./target/*.jar com.olegpro.sc.DigitalNumberScanner ./input/multipleChunks
java -cp ./target/*.jar com.olegpro.sc.DigitalNumberScanner ./input/multipleChunksWithIllegalRow
java -cp ./target/*.jar com.olegpro.sc.DigitalNumberScanner ./input/multipleChunksWithCorruptedChunk
