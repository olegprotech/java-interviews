# Digital Number Scanning Application

Suppose there are nine digit numbers represented in digital format.
It looks like the following example ("." denotes a whitespace for your readability):

```text
...._.._....._.._.._.._.._.
..|._|._||_||_.|_...||_||_|
..||_.._|..|._||_|..||_|._|
```

Each digital number is three lines long, and each line is exactly 27 characters wide.
A number has nine digits, from 0-9.
The numbers are composed of underscores "_" and pipes "|".
An empty line separates multiple numbers.
An digital.number.scanner.input file could contain up to 400 entries.

## Requirements
* Each number should be parsed and written to the console as actual number (i.e.: 123456789 for the above digital number example), line by line.
* Unfortunately, the digital.number.scanner.classifier is not perfect and sometimes provides illegal characters that do not transform into a number.
If the number cannot be read, replace the illegal characters with an "?" and append "ILL" to the output.

Please make it a maven project. Testing your solution is important part of the scanBlockOfChunksTask - we will pay special attention to the coverage and corner cases considered.
Also bear in mind that the implementation will set the stage for our live session to follow - as part of this session we will ask you to evolve your solution to support new requirement.

## Functional Decomposition
* BR1 Each number should be parsed and written to the console as actual number (i.e.: 123456789 for the above digital number example), line by line.
  * FR1.1 File parser that is able to stream results into the processor rather than reading the whole file in.
  * FR1.2 Chunker to break the input by the defined rules: double new line is a separator.
  * FR1.3 Processor that can process parts of the input. Probably chunk by chunk since a number is always represented by 3 lines.
  * FR1.4 Symbol reader that can iterate over the 3-char wider digits in a number.
  * FR1.5 Symbol matcher that can interpret the digit represented by 3x3 char matrix.
  * FR1.6 Output writer that converts recognized digits to string and writes to console.
* BR2 Unfortunately, the digital.number.scanner.classifier is not perfect and sometimes provides illegal characters that do not transform into a number.
If the number cannot be read, replace the illegal characters with an "?" and append "ILL" to the output.
  * FR2.1 Symbol validator that identifies that the symbol is not one of the valid digits. This is mostly same solution as FR1.5

## Non-Functional Requirements
* NFR1 Please make it a maven project. 
* NFR2 Testing your solution is important part of the scanBlockOfChunksTask - we will pay special attention to the coverage and corner cases considered.
  * AC 
    * run.sh with all the test files supplied
    * JUnit
      * Cover all classes and non-private methods
      * Cover all types of known input data problems with files provided + wrong size lines
      * Cover missing input file test-case
* NFR3 Also bear in mind that the implementation will set the stage for our live session to follow - as part of this session we will ask you to evolve your solution to support new requirement.
  * Extendable so that we can adopt new requirements relatively easily.
  * Extract components where practical.

## Use-cases
* UC1 Handle the file with multiple chunks
  * As a user of the scanner, I want all the correct chunks to be streamed to the console as soon as possible.
  * Possible extension: perhaps we want to allow writing to the end of the file in parallel with the processing starting from the start of the file.
* UC2 Handle unrecognised symbols gracefully
  * As a user of the scanner, I would like the number to be interpreted to the greatest possible degree, and to be informed about the all unrecognised symbols so that I can manually review and correct only the necessary ones. 
  * Invalid means anything for which we haven't got a digit defined.
  * Invalid symbol is still 3x3 characters.
* UC3 Handle incorrect lines lengths gracefully
  * As a use of the scanner, I would like to be informed of all the corrupt chunks so that I can manually correct them and re-process later.
  * Possible extension: write all the corrupt chunks to a separate file (a-la DLQ).
* UC4 Handle any amount of whitespace characters in the delimiter line
  * As a user of the scanner, I want the scanner to ignore any whitespace characters in the delimiter line.
  
## Backlog - Further Extensions, Other Ideas etc.
* Much more input validation
* Infer the sizing from the digit map file... maybe?
* Do we have to statically define the amount of digits in the number? Definitely not... this can be inferred by dividing the line length by the digit width.

# Extensions
* DONE Recognize non-digit symbols, e.g. letter "A"
* DONE Fuzzy-match instead of showing "?" in case of unrecognized symbol.
* Parallelize processing a really large file

## Parallelize processing a really large file
When we have a really large file, at the moment it will be parsed sequentially and will take a long time. 
We need to speed up processing of the large file, but keep the order of output the same.
Assume what takes time is the actual processing and not the file I/O.

Functional Requirements
* Read large blocks of text from file and process them in parallel
* Limit the amount of blocks processed down to the amount of cores. E.g. if computer has 8 CPU cores overall, limit processing down to 5 in parallel.
* Limit the amount of data read into memory. 
  * If it's a 100 GB file but computer has 5GB RAM available for the app, only load 5*10^9 / 200 byte = 2.5*10^7 = 25 M chunks. This means 1 block should not take more than 5 M chunks.
  * Let's say it will be a 1000 chunks each time. This way we preserve memory well.
  * Pre-fetch M blocks.
* Write the output as soon as the next block in order is ready.

One way we can do that is to have one process split the file into multiple files, another process taking them as unputs.
More optimal way would be to use pointers and start reading the file in parallel starting at various points.
Using RxJava/Reactor to develop this using observable pattern.
File Chunker >> Observable1
Observable1 >> Aggregator

We can do this in threads by creating batches of e.g. 10 blocks, and waiting for all of them to finish before reading the next batch. This is fairly inflexible.

Or we can separate into concurrent threads.

So what we can have is:
* Producer
  * Separate thread that pre-fetches the file sequentially and populates the queue of tasks.
  * Runs in the loop. Checks the queue size. If it's less than the target length, creates the next scanBlockOfChunksTask.
  * Increments the total number of blocks
* Task Queue
  * FIFO
  * LinkedList a 
* Task Scheduler
  * Takes the scanBlockOfChunksTask from the queue and sends to ExecutionService
  * Removes the scanBlockOfChunksTask from the queue
* Task
  * Has the block of input
  * Has the block number
  * Chunks and scans the block.
  * Buffers the results into the StringBuilder
  * Once ready, puts the buffer to the results map
* Results Queue
  * Map<Block Number, Block Results Buffer>
* Results Aggregator
  * Has the number of the current block starting from zero
  * Has the number of the largest block number
  * Loops while current block is less or equal to the number of blocks
  * Checks the map for result at the next position
  * If there is something, processes it, removes from the map and the queue
  * If there is nothing, can have a little wait before trying again.
  


