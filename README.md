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
<pre>
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
Example Output 1/2 *.lst
 
001	100		INIT_ARRAY	START	100	.LOAD THE PROGRAM AT LOC 100
002	100	03204E	NULL	LDA	ZERO	.A<--(ZERO)
003	103	0F2027	NULL	STA	VALUE2ST	.VALUE2ST<--(A)
004	106	032048	NULL	LDA	ZERO	.A<--(ZERO)
005	109	0F2024	NULL	STA	INDEX	.INDEX<--(A)
006	10C	072021	NULL	LDX	INDEX	.X<--(INDEX)
007	10F	03201B	LOOP_START	LDA	VALUE2ST	.A<--(VALUE2ST)
008	112	1B203F	NULL	ADD	ONE	.A<--(A)+1
009	115	0F2015	NULL	STA	VALUE2ST	.VALUE2ST<--(A)
010	118	072015	NULL	LDX	INDEX	.X<--(INDEX)
011	11B	0FA015	NULL	STA	SRC_ARRAY,X	.SRC_ARRAY + X<--(A); SAME AS WRITING SRC_ARRAY[X]<--(A)
012	11E	03200F	NULL	LDA	INDEX	.A<--(INDEX)
013	121	1B2033	NULL	ADD	THREE	.A<--(A) + 3
014	124	0F2009	NULL	STA	INDEX	.INDEX <--(A)
015	127	2B2030	NULL	COMP	THIRTY	.(A) : (THIRTY, THIRTY+1,THIRTY+2) AND SET CC ACCORDINGLY
016	12A	3B2FE2	NULL	JLT	LOOP_START	.IF CC IS <, THEN JUMP TO LOOP_START
017	12D		VALUE2ST	RESW	1	.RESERVE A WORD AND CALL IT VALUE2ST
018	130		INDEX	RESW	1	NULL
019	133		SRC_ARRAY	RESW	10	NULL
020	151	000000	ZERO	WORD	0	.DEFINE A WORD CONSTANT INITIALIZED TO 0
021	154	000001	ONE	WORD	1	NULL
022	157	000003	THREE	WORD	3	NULL
023	15A	00001E	THIRTY	WORD	30	NULL
024	15D		NULL	END	INIT_ARRAY	.END OF PROGRAM
----------------------
Example Output 2/2 *.obj

HINIT_ARRAY00010000005D
T0001001E03204E0F20270320480F202407202103201B1B203F0F20150720150FA015
T00011E0F03200F1B20330F20092B20303B2FE2
T0001510C00000000000100000300001E
E000100
</pre>
