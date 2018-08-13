/* This program is a SIC/XE assembler.
 *  Written by: Isabelle Diokno
 *              Kevan Johnston
 *              Randall Jackson
 *  Date: 11/28/17
 */
import java.io.*;
import java.util.*;
import java.lang.*;
     
public class SicXeAssm
{
  public static void main(String args[]) throws IOException
  {
     if(args.length == 0)
  { //Check for arguments.
      System.out.println("ERROR: no arguments.");
      return;
  }
    
   File f = new File(args[0]); //Use first argument.
    
   Object[] carries = passOne(f);
       
   carries[4] = args[0]; //pass filename to passTwo
         
   passTwo(carries);
  }//End main
//_____________________________________________________________________________________________________methods that we are keeping are below
   
//_____________________________________________________________________________________________________hex methods
 public static String addHex(String num1, String num2) //in (string, string) out (string)
    {
    int i = Integer.parseInt(num1, 16); //Convert hex to decimal.
    int j = Integer.parseInt(num2, 16);
    int sum = i + j; //operation
    
    String output = Integer.toHexString(sum); //Convert decimal to hex.
    return output;
    }//End addHex
    
    public static int subHex(String num1, String num2) //in (string, string) out (int)
  {
    int i = Integer.parseInt(num1, 16); //Convert hex to decimal.
    int j = Integer.parseInt(num2, 16);
    int diff = i - j;         //operation
    
    return diff;
    }//End subHex
     
//____________________________________________________________________________________________________________pass one
    
 public static Object[] passOne(File f)
 {
    int lines = 0;
    Scanner counter, scan;
        String s;
    String[] current;
    int LOCCTR; //location counter
    int start = 0; //starting address
        int intLine = 0;  //current line index in input file and intFile
        int use = 1; //add LOCCTR value to intFile
        int inc; //increment to LOCCTR
       
    OPTAB opTable = new OPTAB();
    opInfo results = new opInfo();
      
        try{ //Check for file.
        counter = new Scanner(f);
        scan = new Scanner(f);
    }catch(FileNotFoundException e){
        System.out.println("ERROR: file not found.");
      return null;
    }
    
    while(counter.hasNext()){ //Count lines.
    counter.nextLine();
    lines++;
    }
    counter.close();
    String[][] intFile = new String[lines][5]; //Make intermediate file.
      
    ErrorList errors = new ErrorList();
    
    SYMTAB sTab = new SYMTAB(); //Create SYMTAB.
    
    s = scan.nextLine(); //Read first input line.
       
    current = squeeze(s.toUpperCase(), opTable);
       
    while((current[0] == null) && (current[1] == null) && (current[2] == null))                           //handle comments at start
    { 
            writeTo(intFile, intLine, current, 0, 0);
            intLine++;
            s = scan.nextLine().toUpperCase();
            current = squeeze(s, opTable);
    }
     
    if(current[1].equals("START"))                                                                                          //if OPCODE = START
    { 
            start = Integer.parseInt(current[2], 16); //Save OPERAND as starting address.
           
            LOCCTR = start; //Initialize LOCCTR to starting address.
           
            sTab.createEntry(current[0], Integer.toHexString(LOCCTR)); //add name to SYMTAB
           
            writeTo(intFile, intLine, current, 1, LOCCTR); //Write line to intFile.
               
        intLine++;
               
        s = scan.nextLine().toUpperCase(); //Read next input line.
               
        current = squeeze(s, opTable);
    }
    else
            LOCCTR = 0; //Initialize LOCCTR to 0.
    
    while(!((current[1] != null) && current[1].equals("END")))                                          //while OPCODE != END
    { 
            inc = 0; //Reset incrementation.
           
            use = 0; //Assume LOCCTR is not being recorded.
           
            if(!((current[0] == null) && (current[1] == null) && (current[2] == null)))                     //if this is not a comment line, an empty line, or an error line
        { 
                use = 1; //Assume LOCCTR is being recorded.
       
                    if(current[0] != null)                      //if there is a symbol in the LABEL field
            { 
                            boolean findIt = sTab.symtab.findS(current[0]); //Search SYMTAB for LABEL
        
                if(findIt)
                     
                                errors.add(new Error(intLine+1, "duplicate symbol")); //Set error flag (duplicate symbol).
                 
                            else
                     
                                sTab.createEntry(current[0], Integer.toHexString(LOCCTR)); //Insert (LABEL, LOCCTR) into SYMTAB.
                        }
                           
                results = opTable.opTabSearch(current[1]);
             
                if(!results.errorPresent()) //Search OPTAB for OPCODE.
              { 
                               
                inc = results.getFormat(); //Add instruction length to LOCCTR.
   
              }
                           
                else if(current[1].equals("WORD")) //if OPCODE = WORD
                           
                inc = 3; //Add 3 to LOCCTR.
                           
              else if(current[1].equals("RESW")) //if OPCODE = RESW
                           
                inc = (3 * Integer.parseInt(current[2])); //Add 3 * #[OPERAND] to LOCCTR.
       
              else if(current[1].equals("RESB")) //if OPCODE = RESB
       
                inc = Integer.parseInt(current[2]); //Add #[OPERAND] to LOCCTR.
       
              else if(current[1].equals("BYTE"))                                   //if OPCODE = BYTE
              { 
                               
                int l = findLength(current[2]); //Find length of constant in bytes.
                               
                if( l != -1)
                                   
                  inc = l; //Add length to LOCCTR.
                               
                else
                  errors.add(new Error(intLine+1, "invalid BYTE argument")); //Record error.
                    }
                               
            else if(current[1].equals("BASE")) //if OPCODE = BASE (SIC/XE)
                 
                        use = 0; //Do not record LOCCTR's value. Incrementation (inc) stays at 0.
       
            else if(check(current[2]) == 2)
        
              errors.add(new Error(intLine+1, "literal")); //Record error (literal).
       
            else                            //Error. Incrementation stays at 0.
            { 
                        errors.add(new Error(intLine+1, "invalid opcode")); //Set error flag (invalid opcode).
                        use = 0; //Do not record LOCCTR's value. inc stays at 0.
                        current[0] = null; //Overwrite line.
                        current[1] = null;
                        current[2] = null;
                        current[3] = ".ERROR";
                    }
            }
                   
                writeTo(intFile, intLine, current, use, LOCCTR); //Write line to intFile.
                       
                LOCCTR = LOCCTR + inc; //Apply addition to LOCCTR.
                       
                if(!((current[0] == null) && (current[1] == null) && (current[2] == null) && (current[3] == null)))
                               
              intLine++; //if blank line, ignore and overwrite next time
                       
            s = scan.nextLine().toUpperCase(); //Read next input line.
                       
                current = squeeze(s, opTable);
        }
    
      writeTo(intFile, intLine, current, 1, LOCCTR); //Write last line to intFile.
     
      Integer progLen = new Integer(LOCCTR - start); //Save (LOCCTR - starting address) as program length.
    
        Object[] returns = new Object[5]; //array of return values
           
      returns[0] = intFile;
           
        returns[1] = progLen;
           
        returns[2] = sTab;
     
        returns[3] = errors;
    
        return returns;
 }//End passOne
 
 //________________________________________________________________________________________passTwo start                                           
 public static void passTwo(Object[] carries)
 {
   
  String[][] intFile = (String[][])(carries[0]); //Unpack information.
    
  int progLen = (Integer)(carries[1]);
    
  SYMTAB sTab = (SYMTAB)(carries[2]);
    
  ErrorList errors = (ErrorList)(carries[3]);
    
  String fileName = (String)(carries[4]);
    
  OPTAB opTab = new OPTAB();
    
  opInfo results = new opInfo();
     
  int intLine = 0;
     
  String base = null;
     
  String PC = nextPCvalue(intFile, intLine);
     
  String text = null;
    
  String programName = "000000";
  String startAddr = "000000";
     
  ModList mrecords = new ModList(); //M records
   
  FileWriter lstWriter; //FileWriters for listing file and object program
  FileWriter objWriter;
   
  try
  {
   
  lstWriter = new FileWriter(fileName + ".lst"); //initialize FileWriters
  objWriter = new FileWriter(fileName + ".obj");
   
  } 
  catch (IOException e) 
  {
  
  errors.add(new Error(intLine+1, "FileWriter error"));
  return;
   
  }
   
  String[] current = intFile[intLine]; //Read first input line (from intermediate file).
    
  while((current[0] == null) && (current[1] == null) && (current[2] == null) && (current[3] == null))
  {
  writeToFile(current[4], lstWriter, errors, intLine);
  intLine++;
  current = intFile[intLine];
  }
    
  if((current[2] != null) && (current[2].equals("START"))){ //if OPCODE = START
   writeToFile(concat(current, "\t", intLine+1), lstWriter, errors, intLine);//Write line to lst file.
   programName = current[1]; //Record program name.
   startAddr = current[0]; //Record starting address.
   intLine++;
   current = intFile[intLine]; //Read next input line.
   PC = nextPCvalue(intFile, intLine);
  }
   
  String header = "H" + pad(programName, 6, 1) + pad(startAddr, 6, 0) + pad(Integer.toHexString(progLen), 6, 0); //Write Header record to object program.
  writeToFile(header, objWriter, errors, intLine);
  String tr = "T" + pad(current[0], 6, 0) + "  "; //Initialize first Text record.  Length is blank.
     
  while((current[2] != null) && !current[2].equals("END")){ //While OPCODE != END.
   if(!((current[1] == null) && (current[2] == null) && (current[3] == null))){ //if this is not a comment line
    text = "";
       
    results = opTab.opTabSearch(current[2]); //Search OPTAB for OPCODE.
      
    if(!(results.errorPresent())){ //if found then
     int format = results.getFormat();
     String address = null;
     boolean format2 = false;
     boolean isImm = false; //immediate addressing
     int immType = 0; //0: label, 1: decimal int, 2: error
     boolean isError = false;
       
     if(current[3] != null)
     { //if there is a symbol in the OPERAND field
      String operand = null;
      isImm = false; //Assume not immediate.
      immType = 0; //Assume if immediate, label.
        
      if(sTab.symtab.findS(current[3])) //symbol
       operand = current[3];
        
      if((current[3].charAt(0) == '@') && (sTab.symtab.findS(current[3].substring(1)))) //@symbol
       operand = current[3].substring(1);
        
      if(current[3].charAt(0) == '#') //#symbol
      {
       operand = current[3].substring(1);
       isImm = true;
        
       if(!sTab.symtab.findS(operand))
       {
        immType = 1; //Assume immediate addressing with decimal.
         
        try
        {
         Integer.parseInt(operand); //check for decimal int
        }
        catch(NumberFormatException e)
        {
        immType = 2; //Not a decimal integer.
        }      
         
        if(immType == 1)
         address = Integer.toHexString(Integer.parseInt(operand)); 
       } 
      }
         
      if((current[3].length() > 2) && (current[3].charAt(current[3].length() - 2) == ',') && (sTab.symtab.findS(current[3].substring(0, current[3].length() - 2)))) //symbol,X
       operand = current[3].substring(0, current[3].length() - 2);
         
      if(format == 2)
       format2 = true;
         
      
      if((!format2) && (sTab.symtab.findS(operand))) //Search SYMTAB for operand.
      { 
       address = sTab.symtab.FindAddress(operand); //Store symbol value as operand address.
      }
      else if(!(isImm && (immType == 1))){
        
       if(format == 3)
        address = "000";
       else if(format == 4)
        address = "00000";
       if(!format2)
        errors.add(new Error(intLine+1, "undefined symbol")); //Set error flag (undefined symbol).
        isError = true;
          }
      }
        
     else
     {
       
      if(format == 3) //Store 0 as operand address.
       address = "000";
      else if(format == 4)
       address = "00000";
         
     }
     if(!isError)
     {
      text = assemble(results, current[3], address, format, base, PC, errors, intLine, immType); //Assemble the object code instruction.
 
      if((format == 4) && !results.getName().equals("+RSUB") && !(isImm && (immType == 1))) //if format 4
       mrecords.add(new Mod(addHex(current[0], "1"), "05")); //M record
     }
    }else if(current[2].equals("BYTE") || current[2].equals("WORD")) //if OPCODE = BYTE or WORD
    {
        
     if(current[2].equals("BYTE")) //BYTE directive
     {
      String value = current[3].substring(2, current[3].length() - 1);
      int fieldLength = 2 * findLength(current[3]);
        
      if(current[3].charAt(0) == 'X') //into hex
      {
       text = pad(value, fieldLength, 0);
      }
      else if(current[3].charAt(0) == 'C') //into ASCII
      {
       text = toASCII(value);
      }
     }
     else if(current[2].equals("WORD")) //WORD directive
      text = pad(Integer.toHexString(Integer.parseInt(current[3])), 6, 0);
        
    }else if(current[2].equals("BASE")) //BASE directive
    {
     if(sTab.symtab.findS(current[3]))
      base = sTab.symtab.FindAddress(current[3]);
     else //error
      errors.add(new Error(intLine+1, "invalid base operand"));
    }
    if(((tr.length() + text.length()) > 69) || ((current[2].equals("RESW") || current[2].equals("RESB")) && (tr.length() > 10))) //if object code will not fit into text record or OPCODE is RESW or RESB
    {
     String first = tr.substring(0, 7); //before length     
     String last = tr.substring(9); //object code
       
     String length = pad(Integer.toHexString(last.length()/2), 2, 0); //length of object code in bytes
        
     tr = first + length + last; //reassemble into text record
        
     writeToFile(tr, objWriter, errors, intLine); //write Text record to object program
        
     tr = "T" + pad(nextLoc(intFile, intLine), 6, 0) + "  "; //Initialize next Text record.  Length is blank.
    }
   tr = tr + text; //add object code to text record
  }
  writeToFile(concat(current, text, intLine+1), lstWriter, errors, intLine); //write listing line
  
  
  intLine++;
  current = intFile[intLine]; //Read next input line.
  PC = nextPCvalue(intFile, intLine); //change PC value
  }
              
  if(tr.length() > 10)
  {
   String first = tr.substring(0, 7); //before length
   String last = tr.substring(9); //object code
   String length = pad(Integer.toHexString(last.length()/2), 2, 0); //length of object code in bytes
   tr = first + length + last; //reassemble into text record
   writeToFile(tr, objWriter, errors, intLine); //write last Text record to object program
  } 
   
  Mod currentM = mrecords.getFirst();
  if(currentM != null){ //if there are M records
   String M;
   while(currentM != null){ //LL traversal
    M = "M" + pad(currentM.getLoc(), 6, 0) + currentM.getStart(); //construct record
    writeToFile(M, objWriter, errors, intLine); //write to object file
    currentM = currentM.getNext();
   }
  }
    
  String end = "E";
  if((current[3] != null) && (sTab.symtab.findS(current[3])))
   end = end + pad(sTab.symtab.FindAddress(current[3]), 6, 0);
  else if(current[3] != null) //error
   errors.add(new Error(intLine+1, "invalid end operand"));
  writeToFile(end, objWriter, errors, intLine); //write End record to object program
  
  writeToFile(concat(current, "\t", intLine+1), lstWriter, errors, intLine);//write last listing line
 
  Error currentE = errors.getFirst(); //Write errors to listing file.
  if(currentE != null){ //if there are errors
   String E;
   while(currentE != null){ //LL traversal
    E = "Error at line " + currentE.getLine() + ": " + currentE.getError(); //Record error in string.
    writeToFile(E, lstWriter, errors, intLine); //write to listing file
    currentE = currentE.getNext();
   }
  }
  try
  {
   lstWriter.close(); //Close FileWriters (buffered writers).
   objWriter.close();
  }catch(IOException e)
  {
   System.out.println("ERROR: FileWriter closing threw exception"); //Print to screen.
  }
 }//_____________________________________________________________________________________________________________End passTwo
   
 public static String toASCII(String s){ //convert string to ASCII hex string
  String r = "";
    
  for(int i = 0; i < s.length(); i++)
   r = r + pad(Integer.toHexString((int)(s.charAt(i))), 2, 0);
     
  return r;
 }//End toASCII
               
 public static String assemble(opInfo results, String operand, String address, int format, String base, String PC, ErrorList errors, int l, int immType){ //Assemble instruction.
  String instr = null;
  
  if(format == 2) //if format 2
  {
   String first = Character.toString(operand.charAt(0)); //first operand
   String second = Character.toString(operand.charAt(3)); //second operand
     
   if(first == "A") //use number of register
    first = "0";
   else if(first == "X")
    first = "1";
   else if(first == "L")
    first = "2";
   else if(first == "B")
    first = "3";
   else if(first == "S")
    first = "4";
   else if(first == "T")
    first = "5";
     
   if(!(results.getName().equals("CLEAR") || results.getName().equals("TIXR"))) // not CLEAR or TIXR  
   {
     
    if(second == "A") //use number of register
     second = "0";
    else if(second == "X")
     second = "1";
    else if(second == "L")
     second = "2";
    else if(second == "B")
     second = "3";
    else if(second == "S")
     second = "4";
    else if(second == "T")
     second = "5";
     
   }else
    second = "0"; //field = 0
      
   instr = results.getOpCode() + first + second; //Build instruction.
      
  }else if((format == 3) || (format == 4)) //format 3 or 4
  {
   String opcode = results.getOpCode(); //individual fields
   int ni = 3;
   int x = 0;
   int bp = 0;
   int e = 0;
   String addressField;
   boolean isImm = false; //immediate addressing, assume false
       
   if(operand != null)
   {
    if(operand.charAt(0) == '@') //indirect
     ni = 2;
    else if(operand.charAt(0) == '#') //immediate
    {
     ni = 1;
     isImm = true;
    }
        
    if(operand.substring(operand.length() - 2).equals(",X")) //indexed
     x = 8;
   }
        
   if(format == 4) //extended
   {
        
    e = 1;
 
    if(results.getName().equals("+RSUB")) //RSUB: no operands
     addressField = "00000";
    else
    {
     if(address.length() > 5) //cut off extra digits
      address = address.substring(address.length() - 5);
     addressField = pad(address, 5, 0);
    } 
      
   instr = pad(addHex(opcode, Integer.toHexString(ni)), 2, 0) + pad(addHex(addHex(Integer.toHexString(x), Integer.toHexString(bp)), Integer.toHexString(e)), 1, 0) + addressField; //Build instruction.
        
   }else if(format == 3) //format 3
   {
    e = 0; //not extended
    int dispPC = subHex(address, PC); //PC displacement (int)
    String PCdisp = Integer.toHexString(dispPC); //PC displacement (string)
    int dispB = 0;
    String baseDisp = null;
    if(base != null)
    {
     dispB = subHex(address, base); //base displacement (int)
     baseDisp = Integer.toHexString(dispB); //base displacement (string)
    }
        
    if(results.getName().equals("RSUB")) //RSUB: no operands
    {
     addressField = "000";
    }
     
    if(isImm && (immType == 1)) //immediate addressing, decimal int
    {
     if(address.length() > 3) //cut off extra digits
      address = address.substring(address.length() - 3);
     addressField = address;
    }else if((-2048 <= dispPC) && (dispPC <= 2047)) //in PC range
    {
     bp = 2;
     if(PCdisp.length() > 3) //cut off extra digits
      PCdisp = PCdisp.substring(PCdisp.length() - 3);
     addressField = PCdisp;
    }else if((base != null) && (0 <= dispB) && (dispB <= 4095)) //in base range
    {
     bp = 4;
     if(baseDisp.length() > 3) //cut off extra digits
      baseDisp = baseDisp.substring(baseDisp.length() - 3);
     addressField = baseDisp;
    }else //error: out of range
    {
     errors.add(new Error(l+1, "address out of range"));
                   
     addressField = "000";
    }
                   
   instr = pad(addHex(opcode, Integer.toHexString(ni)), 2, 0) + pad(addHex(addHex(Integer.toHexString(x), Integer.toHexString(bp)), Integer.toHexString(e)), 1, 0) + pad(addressField, 3, 0); //Build instruction.
   }
  }
  return instr;
 }//End assemble
      
 public static String nextPCvalue(String[][] file, int line)
 {
  for(int i = line + 1; i < file.length; i++)
   if(file[i][0] != null)
   {
    return file[i][0];
   } 
         
  return null;
 }//End nextPCvalue
                  
 public static String nextLoc(String[][] file, int line)
 {
  for(int i = line + 1; i < file.length; i++)
   if((file[i][0] != null) && (!file[i][2].equals("RESW") && !file[i][2].equals("RESB") && (check(file[i][2]) != 1)))
    return file[i][0];
      
  return "0";
 }//End nextLoc
   
 public static void writeToFile(String s, FileWriter w, ErrorList r, int l){ //Write string to file.
  try
  {
  
   w.write((s + System.getProperty("line.separator")).toUpperCase());
   
  } 
  catch (IOException e)
  {
  
  r.add(new Error(l, "writing error"));
   
  }
 }//End writeToFile
   
 public static String concat(String[] s1, String s2, int i){ //formatting for lst file
  String s;
  
  if(s2.equals(""))
   s = "\t";
  else
   s = s2;
 
  String r = pad(Integer.toString(i), 3, 0) + "\t" + s1[0] + "\t" + s + "\t" + s1[1] + "\t" + s1[2] + "\t" + s1[3] + "\t" + s1[4];
  return r;
 }//End concat
   
 public static String pad(String s, int spaces, int type){ //Fill empty spaces. Type = 0: before, type = 1: after.
  String padding = "";
  String r = null;
  String pad = null;
    
  if(type == 0)
   pad = "0";
  else if(type == 1)
   pad = " ";
  
  if(s.length() < spaces)
   for(int i = 0; i < (spaces - s.length()); i++)
    padding = padding + pad;
      
  if(type == 0)
   r = padding + s;
  else if(type == 1)
   r = s + padding;
  
  return r;
 }//End pad
                 
 public static String[] squeeze(String s, OPTAB opTable){
  final String[] line = new String[4]; //array
  String spaces = "\\s+"; //delimiter
  String[] a;
  String comment;
  int op = 0; //opcode location
  opInfo results;
      
  for(int i = 0; i < 4; i++) //Initialize array.
   line[i] = null;
    
  String[] s2 = s.split("\\."); //Try to extract comments.
  if(s2.length > 1){ //if there are comments
   a = s2[0].split(spaces); //Split non-comment section.
   comment = "." + s2[1]; //Collect comments.
  }else{ //if there are no comments
   a = s.split(spaces); //Split line.
   comment = null; //no comments
  }
    
  for(int i = 0; i < a.length; i++){ //Remove empty entries.
   if(a[i].equals("")){
    String[] b = new String[a.length - 1];
    for(int j = 0; j < i; j++)
     b[j] = a[j];
    for(int j = i; j < b.length; j++)
     b[j] = a[j+1];
    a = b;
   }
  }
    
  if(s.replaceAll(spaces, "").equals(".")) //dot only
  {
   line[3] = ".";
   return line;
  }
    
  if((s.length() == 0) || (s.replaceAll(spaces, "").length() == 0)) //Handle empty line.
   return line;
    
  if((s.replaceAll(spaces, "").charAt(0) == '.') || ((a.length == 1) && (a[0].charAt(0) == '.'))){ //all comments
   line[3] = comment;
   return line;
  }
    
  for(int i = 0; i < a.length; i++){ //Search array for OPCODE, +OPCODE, RESW, RESB, WORD, BYTE, START, END, BASE or unimplemented operations (found at index i).
   results = opTable.opTabSearch(a[i]);
       
   if(!results.errorPresent()){ //find opcode
    op = i;
    break;
   }else if(a[i].equals("RESW")){
    op = i;
    break;
   }else if(a[i].equals("RESB")){
    op = i;
    break;
   }else if(a[i].equals("WORD")){
    op = i;
    break;
   }else if(a[i].equals("BYTE")){
    op = i;
    break;
   }else if(a[i].equals("START")){
    op = i;
    break;
   }else if(a[i].equals("END")){
    op = i;
    break;
   }else if(a[i].equals("BASE")){
    op = i;
    break;
   }else if(check(a[i]) == 1){
    op = i;
    break;
   } //op should contain the opcode's location.
  }
      
  if(a[op].equals("RSUB") || ((op == (a.length-1)) && (check(a[op]) == 1))){ //RSUB or Format 1
   if(op == 1){ //label
    line[0] = a[0]; //label
    line[1] = a[1]; //opcode
    line[3] = comment; //comments
    return line;
   }else if(op == 0){ //no label
    line[1] = a[0]; //opcode
    line[3] = comment; //comments
    return line;
   }
  }
    
  if(a[op].equals("END") && (a.length == 1)){ //END directive, no operand
   line[1] = a[0]; //opcode
   line[3] = comment; //comments
   return line;
  }
    
  if((a.length == 3) && (op == 1)){ //case: label, opcode, operand (op = 1)
   line[0] = a[0]; //label
   line[1] = a[1]; //opcode
   line[2] = a[2]; //operand
  }else if((a.length == 2) && (op == 0)){ //case: opcode, operand (op = 0)
   line[1] = a[0]; //opcode
   line[2] = a[1]; //operand
  }
    
  line[3] = comment; //Append comments.
    
  return line;
 }//End squeeze  
     
 public static int check(String s){ //check for unrequired instructions
  int r = 0; //return value (0: not an unimplemented instruction, 1: unimplemented instruction or directive, 2: literal)
    
  if(s.equals("EQU") || s.equals("USE") || s.equals("CSECT")){ //search for first invalid instruction
   r = 1;
  }else if((s.length() > 3) && (s.charAt(0) == '=') && ((s.charAt(1) == 'X') || (s.charAt(1) == 'C')) && (s.charAt(2) == '\'') && (s.charAt(s.length() - 1) == '\'')){
   r = 2;
  }else if(s.equals("ADDF") || s.equals("+ADDF") || s.equals("COMPF") || s.equals("+COMPF") || s.equals("DIVF") || s.equals("+DIVF") || s.equals("FIX") || s.equals("FLOAT") || s.equals("LDF")){
   r = 1;
  }else if(s.equals("+LDF") || s.equals("MULF") || s.equals("+MULF") || s.equals("NORM") || s.equals("STF") || s.equals("+STF") || s.equals("SUBF") || s.equals("+SUBF")){
   r = 1;
  }else if(s.equals("DIV") || s.equals("+DIV") || s.equals("DIVR")){
   r = 1;
  }else if(s.equals("HIO") || s.equals("LPS") || s.equals("+LPS") || s.equals("SSK") || s.equals("+SSK") || s.equals("STI") || s.equals("+STI")){
   r = 1;
  }else if(s.equals("STSW") || s.equals("+STSW") || s.equals("SVC") || s.equals("SIO") || s.equals("TIO")){
   r = 1;
  }//r contains the code for the string.
    
  return r;
 }//End check
     
 public static int findLength(String s){
  if((s.charAt(0) == 'C') && (s.charAt(1) == '\'') && (s.charAt(s.length() - 1) == '\'')) //characters: one byte for each ASCII character
   return (s.length() - 3);
  else if((s.charAt(0) == 'X') && (s.charAt(1) == '\'') && (s.charAt(s.length() - 1) == '\'')){ //hex integer: one byte for every two hex digits
   int field = s.length() - 3;
   if(field % 2 == 0)
    return (field / 2);
   else
    return ((field / 2) + 1);
   }
  return -1;
 }//End findLength
     
 public static void writeTo(String[][] file, int line, String[] input, int use, int counter){
  if(use == 1) //if LOCCTR is being recorded
    file[line][0] = Integer.toHexString(counter); //LOCCTR to hex string //Location
    
   //0[ADDR] 1[LABEL] 2[OPCODE] 3[OPERAND] 4[Comments]  
  file[line][1] = input[0]; //Shift and record. //Label
  file[line][2] = input[1]; //OPCODE
  file[line][3] = input[2]; //Operand
  file[line][4] = input[3]; //Comment
  return;
 }//End writeTo
}//End class SicXeAssm
//~~~~~~~~~~~~~
  class SYMTAB
{   
    //Variable Declarations
    HashTable symtab = new HashTable();
    SItem item;
    public void createEntry (String label,String address)
    {
         item = new SItem(label,address);
         symtab.insertSQ(item);
    } 
          
       
}
class SItem
{    
    private String label;   // data item (key)
    private String address;
    private int iProbe =1; // probelength for insertion
                  
//--------------------------------------------------------------
   public SItem(String l,String ad)          // constructor
   {
      label=l;
      address=ad;
         
   }
//--------------------------------------------------------------
   public String getKey()
   { //return sData;
     return label;
   }
 //--------------------------------------------------------------
   public String getAddress()
   { //return sData;
     return address;
   }  
}  // end class SItem
// // //===================================================================================
  // // //===================================================================================
class HashTable
{
   //private DataItem[] hashArray;    // array holds hash table
//    private int arraySize;
//    private DataItem nonItem;        // for deleted items
       
   private SItem[] symtab;
   private int arraySizeS;
   private SItem nonItemS;
   //     
//    private int dpSuc=0;
//    private int dpF=0;
//    private double dtotS=0.0;
//    private double dtotF=0.0;
//        
//    private int pSuc=0;
//    private int pF=0;
//    private double totS=0.0;
//    private double totF=0.0;
       
// -------------------------------------------------------------
   public HashTable()       // constructor
   {
     arraySizeS = 500;
    
     symtab = new SItem [arraySizeS];
      
          
   }
   public int hashFunc(String key)
   {
     
      int hashVal=0;
      for (int j=0; j<key.length(); j++) //left to right
      {
         int letter = key.charAt(j) - 96;
         hashVal= (hashVal*26+letter)%arraySizeS;
         if (hashVal<0)
         {
            hashVal = hashVal * -1;
         }
                
      }
      
      return hashVal;
   }//end of hashFunct()
//----------------------------------------------------------------
   public boolean findS(String key)    // find item with key
   {
      int hashVal = hashFunc(key);  // hash the key
      int currentQ=1;
      int powQ;
      int probelen =1;
      String keyVal;
      boolean f = false;
          
    
      while(symtab[hashVal] != null )  // until empty cell,
      {                               // found the key?
            keyVal = symtab[hashVal].getKey();
            if(keyVal.equals(key))
            {
               f=true;
                return f;
                
                    
            }//end of if
            probelen++;
            powQ =currentQ*currentQ;
            hashVal=hashVal+powQ;
            currentQ++;
            hashVal %= arraySizeS;      // wraparound if necessary
        }//end of while
              return f;      
    }
//-------------------------------------------------------------------    
    public String FindAddress(String key)    // find item with key
   {
      int hashVal = hashFunc(key);  // hash the key
      int currentQ=1;
      int powQ;
      int probelen =1;
      String keyVal;
      boolean f = false;
          
    
      while(symtab[hashVal] != null )  // until empty cell,
      {                               // found the key?
            keyVal = symtab[hashVal].getKey();
            if(keyVal.equals(key))
            {
               f=true;
            
            return symtab[hashVal].getAddress();
                
                    
            }//end of if
            probelen++;
            powQ =currentQ*currentQ;
            hashVal=hashVal+powQ;
            currentQ++;
            hashVal %= arraySizeS;      // wraparound if necessary
        }//end of while
          
      return null;      
    }
    
// // //--------------------------------------------------------------- 
// // //---------------------------------------------------------------
   public void insertSQ(SItem item) // insert a DataItem
   // (assumes table not full)
   {
      //System.out.println("A");
      //System.out.println("Inserting "+item.getKey()+" into symtab"); 
      String key = item.getKey();      // extract key///
      int hashVal = hashFunc(key);  // hash the key
      int current=1;
      int pow;
               
      //System.out.println("key "+key+"hval is "+hashVal);                          // until empty cell or -1,
     
      while(symtab[hashVal] != null)
    
      {
        
         pow =current*current;
         hashVal=hashVal+pow;
         
         current++;
             
         hashVal %= arraySizeS;      // wraparound if necessary
      }
     
      symtab[hashVal] = item;    // insert item
      //System.out.println("insert success");
      //System.out.println(key+"  address: "+hashVal);
     // System.out.println();
   }  // end insertQ()
    
} //End of HashTable class 
//____________________________________________________________________________________________________________OPTAB
//_________opInfo object returned by opTab object
class opInfo
{
  String name; //mnemonic
  String opCode; //opcode
  int format; // # of format
  String error; // string that reads "Unrecognized Mnemonic: [bad mnemonic]"
  boolean isError; //true if not in optab 
      
  public opInfo(String x, String y, int z) 
  {
        name = x;
        opCode = y;
        format = z;
  }
      
  public opInfo(){}
      
  public String getName()
  {
  return name;
  }
      
  public String getOpCode()
  {
  return opCode;
  }
      
  public int getFormat()
  {
  return format;
  }
      
  public void setError(String x)
  {
  error = x; // stores default error message
  isError = true;  //sets isError to true
  }
      
  public String getError()
  {
  return error;
  }
      
  public boolean errorPresent()
  {
  return isError;
  }
}
    
//__________OPTAB hashtable
class OPTAB
{
  Hashtable<String, String> opTable = new Hashtable<String, String>();
      
  //declarations for variables in opTabSearch method
  String input;
  int format;
  opInfo opReturn;
      
  public OPTAB()
  {
         //mnemonics without '+'
      opTable.put("ADD", "18");
      opTable.put("AND", "40");
      opTable.put("ADDR", "90");
      opTable.put("CLEAR", "B4");
      opTable.put("COMP", "28");
      opTable.put("COMPR", "A0");
                //opTable.put("DIV", "24");     
                    //opTable.put("DIVR", "9C");        
                //opTable.put("HIO", "F4");
      opTable.put("J", "3C");
      opTable.put("JEQ", "30");
      opTable.put("JGT", "34");
      opTable.put("JLT", "38");
      opTable.put("JSUB", "48");
      opTable.put("LDA", "00");
      opTable.put("LDB", "68");
      opTable.put("LDCH", "50");
      opTable.put("LDL", "08");
      opTable.put("LDS", "6C");
      opTable.put("LDT", "74");
      opTable.put("LDX", "04");
                //opTable.put("LPS", "D0");
      opTable.put("MUL", "20");
      opTable.put("MULR", "98");
      opTable.put("OR", "44");
      opTable.put("RD", "D8");
      opTable.put("RMO", "AC");
      opTable.put("RSUB", "4C");
      opTable.put("SHIFTL", "A4");
      opTable.put("SHIFTR", "A8");
                //opTable.put("SIO", "F0");
      opTable.put("SSK", "EC");
      opTable.put("STA", "0C");
        opTable.put("STB", "78");
      opTable.put("STCH", "54");
                //opTable.put("STI", "D4");
      opTable.put("STL", "14");
      opTable.put("STS", "7C");
      opTable.put("STT", "84");
      opTable.put("STX", "10");
      opTable.put("SUB", "1C");
      opTable.put("SUBR", "94");
                //opTable.put("SVC", "B0");
      opTable.put("TD", "E0");
                //opTable.put("TIO", "F8");
      opTable.put("TIX", "2C");
      opTable.put("TIXR", "B8");
      opTable.put("WD", "DC");
          
      //mnemonics with '+'
      opTable.put("+ADD", "18");
      opTable.put("+AND", "40");
      opTable.put("+ADDR", "90");
      opTable.put("+CLEAR", "B4");
      opTable.put("+COMP", "28");
      opTable.put("+COMPR", "A0");
                //opTable.put("+DIV", "24");
                //opTable.put("+DIVR", "9C");
                //opTable.put("+HIO", "F4");
      opTable.put("+J", "3C");
      opTable.put("+JEQ", "30");
      opTable.put("+JGT", "34");
      opTable.put("+JLT", "38");
      opTable.put("+JSUB", "48");
      opTable.put("+LDA", "00");
      opTable.put("+LDB", "68");
      opTable.put("+LDCH", "50");
      opTable.put("+LDL", "08");
      opTable.put("+LDS", "6C");
      opTable.put("+LDT", "74");
      opTable.put("+LDX", "04");
                //opTable.put("+LPS", "D0");
      opTable.put("+MUL", "20");
      opTable.put("+MULR", "98");
      opTable.put("+OR", "44");
      opTable.put("+RD", "D8");
      opTable.put("+RMO", "AC");
      opTable.put("+RSUB", "4C");
      opTable.put("+SHIFTL", "A4");
      opTable.put("+SHIFTR", "A8");
                //opTable.put("+SIO", "F0");
      opTable.put("+SSK", "EC");
      opTable.put("+STA", "0C");
        opTable.put("+STB", "78");
      opTable.put("+STCH", "54");
                //opTable.put("+STI", "D4");
      opTable.put("+STL", "14");
      opTable.put("+STS", "7C");
      opTable.put("+STT", "84");
      opTable.put("+STX", "10");
      opTable.put("+SUB", "1C");
      opTable.put("+SUBR", "94");
                //opTable.put("+SVC", "B0");
      opTable.put("+TD", "E0");
                //opTable.put("+TIO", "F8");
      opTable.put("+TIX", "2C");
      opTable.put("+TIXR", "B8");
      opTable.put("+WD", "DC");
}
        
        public opInfo opTabSearch(String x) //returns opInfo object 
      {
      input = x;
          
      if(opTable.containsKey(input))
         {
         //conditions for setting format
         if(input.contains("+"))
            format = 4;
         else if(input.equals("ADDR") || input.equals("CLEAR") || input.equals("COMPR") || input.equals("DIVR") || input.equals("MULR") || input.equals("RMO") || input.equals("SHIFT") || input.equals("SHIFTL") || input.equals("SHIFTR") || input.equals("SUBR") || input.equals("SVC") || input.equals("TIXR") )
            format = 2;
         else if(input.equals("FLOAT") || input.equals("FIX") || input.equals("HIO") || input.equals("NORM") || input.equals("SIO") || input.equals("TIO"))
            format = 1;
         else
            format = 3;
                
         opReturn = new opInfo(input, opTable.get(input), format);
             
         return opReturn; 
         }
      else
         {
         //if not in optab sets error string and flags as not found
         opReturn = new opInfo();
         opReturn.setError("Unrecognized Mnemonic: " + input);
         return opReturn;
         }
      }
}
   
class ErrorList {
 private Error first;
    
 public ErrorList(){
  first = null;
 }//End constructor
    
 public void add(Error e){ //Add error to list.
  Error locate;
    
  if(first == null) //if first, node becomes root
   first = e;
  else{ //otherwise, attach to end
   locate = first;
   while(locate.getNext() != null)
    locate = locate.getNext();
   locate.setNext(e);
  }
 }//End add
    
 public void display(){ //display all errors
  Error current = first;
    
  if(first == null) //If there are no errors, do nothing.
   return;
  System.out.println("Errors:"); //If there are errors, display them.
  System.out.println("Line: " + current.getLine() + " " + "Type: " + current.getError()); //display first
  while(current.getNext() != null){ //traversal loop
   current = current.getNext();
   System.out.println("Line: " + current.getLine() + " " + "Type: " + current.getError()); //display next
  }
 }//End display
  
 public Error getFirst(){ //getter (first)
  return first;
 }//End getFirst
}//End class ErrorList
    
class Error {
 private int line;
 private String error;
 private Error next;
    
 public Error(int loc, String code){ //constructor
  line = loc;
  error = code;
  next = null;
 }//End constructor
    
 public int getLine(){ //getter (line)
  return line;
 }//End getLine
    
 public String getError(){ //getter (error)
  return error;
 }//End getError
    
 public Error getNext(){ //getter (next)
  return next;
 }//End getNext
    
 public void setNext(Error e){ //setter (next)
  next = e;
 }//End setNext
}//End class Error
                  
class ModList {
 private Mod first;
    
 public ModList(){
  first = null;
 }//End constructor
    
 public void add(Mod m){ //Add error to list.
  Mod locate;
    
  if(first == null) //if first, node becomes root
   first = m;
  else{ //otherwise, attach to end
   locate = first;
   while(locate.getNext() != null)
    locate = locate.getNext();
   locate.setNext(m);
  }
 }//End add
  
 public Mod getFirst(){ //getter (first)
  return first;
 }//End getFirst
}//End class ModList
    
class Mod {
 private String loc;
 private String start;
 private Mod next;
    
 public Mod(String l, String s){ //constructor
  loc = l;
  start = s;
  next = null;
 }//End constructor
    
 public String getLoc(){ //getter (line)
  return loc;
 }//End getLoc
    
 public String getStart(){ //getter (error)
  return start;
 }//End getStart
    
 public Mod getNext(){ //getter (next)
  return next;
 }//End getNext
    
 public void setNext(Mod m){ //setter (next)
  next = m;
 }//End setNext
}//End class Mod