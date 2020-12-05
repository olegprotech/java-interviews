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

Please make it a maven project. Testing your solution is important part of the task - we will pay special attention to the coverage and corner cases considered.
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
* NFR2 Testing your solution is important part of the task - we will pay special attention to the coverage and corner cases considered.
* NFR3 Also bear in mind that the implementation will set the stage for our live session to follow - as part of this session we will ask you to evolve your solution to support new requirement.
  * Extendable so that we can adopt new requirements relatively easily.

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