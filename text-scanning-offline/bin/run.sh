#!/bin/bash
source ./bin/com.sh
java -cp "$CP" com.olegpro.sc.App ./input/singleChunk
java -cp "$CP" com.olegpro.sc.App ./input/multipleChunks
java -cp "$CP" com.olegpro.sc.App ./input/multipleChunksWithIllegalRow
java -cp "$CP" com.olegpro.sc.App ./input/multipleChunksWithCorruptedChunk
