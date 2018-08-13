# Sic/Xe-Assembler
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

Example Input Code:

INIT_ARRAY		START 100

			LDA ZERO
			STA VALUE2ST
			
			LDA ZERO
			STA INDEX
			LDX INDEX

LOOP_START		LDA VALUE2ST
			ADD ZERO
			STA VALUE2ST

			LDX INDEX
			STA ALPHA,X

			LDA INDEX
			ADD THREE
			STA INDEX
			COMP THREEHUNDO

			JLT LOOP_START

VALUE2ST			RESW 1
INDEX			RESW 1
ALPHA			RESW 100

ZERO			WORD 0
THREE			WORD 3
THREEHUNDO		WORD 300

			END INIT_ARRAY
