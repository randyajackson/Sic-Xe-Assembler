# Sic/Xe-Assembler

Spring 2017

This project is a SIC/XE assembler in JAVA
with the requirements as mentioned below:

-The program accepts the name of the input source file as a commandline
argument. It must report an error message if the file is not found.

-The program works accurately with PC relative and base relative modes.

-There are two methods named passOne and passTwo which does their
respective tasks.

-The program handles any number of whitespaces or tabs in
the input source code.

-The data structures OPTAB and SYMTAB are implemented as hash
tables.

-The program generates two files: the listing file having .lst extension
and the object program having .obj extension.

-The program does common types of error-checks such as incorrect
spelling of a mnemonic, label not defined, inability to assemble because
displacement is out of range etc.

Example Input
---------------
HW3		START 100	.Load at loc 100

		LDS #3		.Initialize step
		LDT #15		.Initialize limit
		LDX #0		.Initialize index
		LDA #2		.Initialize value

INIT_LP		STA ARRAY_FIVE,X
		ADDR S,X
		COMPR X,T
		JLT INIT_LP	.initialization loop

		JSUB SUM	.call subroutine

ARRAY_SUM	RESW 1		.one-word variable

ARRAY_FIVE	RESW 5		.array variable

SUM		LDA #0
		LDX #0
ADD_LP		ADD ARRAY_FIVE,X
		ADDR S,X
		COMPR X,T
		JLT ADD_LP
		STA ARRAY_SUM
		RSUB		.subroutine

		END HW3		.End the program
 ----------------
 Example Output
