#!/bin/bash
java -cp ./target/*.jar com.olegpro.sc.App ./input/multipleChunksWithIllegalRow
java -cp ./target/*.jar com.olegpro.sc.App ./input/multipleChunksWithCorruptedChunk
