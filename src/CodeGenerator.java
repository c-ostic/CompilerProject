import java.util.HashMap;

class CodeGenException extends Exception
{
    public CodeGenException(String message)
    {
        super(message);
    }
}

public class CodeGenerator
{
    //set the executable size
    private final int EXE_SIZE = 0x100;

    private String[] executable;
    private BackpatchTable backpatchTable;
    private int heapStart;
    private int errors;

    // variable to handle boolean hell and the saving of many temp variables
    private int boolExprCount;

    //maps a string to its starting location in the heap
    private HashMap<String,String> heapStrings;

    private int programNum;

    public CodeGenerator()
    {
        reset();
    }

    public void reset()
    {
        //create the executable and default all to 00
        executable = new String[EXE_SIZE];
        for(int i = 0;i < executable.length;i++)
            executable[i] = "00";

        //create the backpatch table
        backpatchTable = new BackpatchTable();

        //set the start of the heap to the end of the executable
        heapStart = EXE_SIZE;

        //create an empty hashmap for the heap
        heapStrings = new HashMap<String, String>();

        //reset the number of errors
        errors = 0;
    }

    public void tryCodeGeneration(SyntaxTree ast, int program, boolean hadPrevError)
    {
        //reset all the necessary values
        reset();

        //save the program number
        programNum = program;

        //before doing anything, if lex, parse, or semantic analysis had an error, skip code generation
        if(hadPrevError)
        {
            System.out.println("Code Generation for Program " + program + " skipped due to previous errors");
            errors++;
            return;
        }

        System.out.println("INFO Code Generation - Generating code for program " + program);

        try
        {
            generateProgram(ast);
        }
        catch (CodeGenException e)
        {
            System.out.println(e.getMessage());
            errors++;
        }

        if(errors > 0)
        {
            System.out.println("ERROR Code Generation - Generation failed with " + errors + " errors");
        }
        else
        {
            System.out.println("INFO Code Generation - Generation succeeded with " + errors + " errors");
        }
    }

    public void printExecutable()
    {
        if(errors == 0)
        {
            System.out.println("Executable:");

            for (int i = 0; i < executable.length; i++)
            {
                System.out.print(executable[i] + " ");

                if (i % 8 == 7)
                    System.out.println();
            }
        }
        else
            System.out.println("Executable for Program " + programNum + " skipped due to previous errors");
    }

    /*-------------------------------------------- Code Gen Methods --------------------------------------------------*/

    private final String TEMP_ID = "temp";

    private void generateProgram(SyntaxTree ast) throws CodeGenException
    {
        System.out.println("DEBUG Code Gen - Generating Program Code");

        //start the backpatch off with a temp storage value
        backpatchTable.findOrCreate(TEMP_ID, 0);

        //start the heap off with true and false
        addStringToHeap("true");
        addStringToHeap("false");

        //start the boolExprCount at 0
        boolExprCount = 0;

        //the first child of the root is the first block in the program
        //get the code in the form of a space delineated string and add a halt op code
        String codeString = generateBlock(ast.getRoot().getChild(0)) + "00 ";

        //turn the codeString into a usable array
        String[] codeArray = codeString.split(" ");

        //if the combined length of the code and the backpatch is less than the heapStart, then no collisions occur
        if(codeArray.length + backpatchTable.size() < heapStart)
        {
            System.out.println("DEBUG Code Gen - Backpatching variables");

            //backpatch the table
            backpatchTable.backpatch(codeArray.length);

            //iterate through the code array to put it into the executable, backpatching along the way
            for (int i = 0; i < codeArray.length; i++)
            {
                if (codeArray[i].startsWith("T"))
                    executable[i] = backpatchTable.getBackpatchValue(codeArray[i]);
                else
                    executable[i] = codeArray[i];
            }
        }
        else
        {
            System.out.println("ERROR Code Generation - Stack collided with Heap, ran out of memory");
            errors++;
        }
    }

    private String generateBlock(SyntaxTreeNode blockNode) throws CodeGenException
    {
        System.out.println("DEBUG Code Gen - Generating Block Code");

        //generates the code in string form for easier concatenation of program lines
        String codeString = "";

        for(SyntaxTreeNode child : blockNode.getChildren())
        {
            switch (child.getNodeType())
            {
                case PRINT_STATEMENT:
                {
                    codeString += generatePrint(child);
                    break;
                }
                case ASSIGNMENT_STATEMENT:
                {
                    codeString += generateAssignment(child);
                    break;
                }
                case VAR_DECL:
                {
                    codeString += generateVarDecl(child);
                    break;
                }
                case WHILE_STATEMENT:
                {
                    codeString += generateWhile(child);
                    break;
                }
                case IF_STATEMENT:
                {
                    codeString += generateIf(child);
                    break;
                }
                case BLOCK:
                {
                    //recursively call the next block
                    codeString += generateBlock(child);
                    break;
                }
            }
        }

        return codeString;
    }

    private String generatePrint(SyntaxTreeNode printNode) throws CodeGenException
    {
        System.out.println("DEBUG Code Gen - Generating Print Code");

        String codeString = "";

        String printExpr = generateExpr(printNode.getChild(0));

        //set the Y register
        if(printExpr.length() == 3)
            codeString += "A0 " + printExpr;
        else if(printExpr.length() == 6)
            codeString += "AC " + printExpr;
        else
            codeString += printExpr + "AC " + backpatchTable.findOrCreate(TEMP_ID, 0);

        //set the X register
        if(printNode.getExprType() == SymbolType.INT)
            codeString += "A2 01 ";
        else if(printNode.getExprType() == SymbolType.STRING || printNode.getExprType() == SymbolType.BOOLEAN)
            codeString += "A2 02 ";

        //add the system call
        codeString += "FF ";

        return codeString;
    }

    private String generateAssignment(SyntaxTreeNode assignmentNode) throws CodeGenException
    {
        System.out.println("DEBUG Code Gen - Generating Assignment Code");

        String codeString = "";

        String idCode = generateExpr(assignmentNode.getChild(0));
        String assignmentExpr = generateExpr(assignmentNode.getChild(1));

        if(assignmentExpr.length() == 3)
            codeString += "A9 " + assignmentExpr;
        else if(assignmentExpr.length() == 6)
            codeString += "AD " + assignmentExpr;
        else
            codeString += assignmentExpr + "AD " + backpatchTable.findOrCreate(TEMP_ID, 0);

        codeString += "8D " + idCode;

        return codeString;
    }

    private String generateVarDecl(SyntaxTreeNode varDeclNode) throws CodeGenException
    {
        System.out.println("DEBUG Code Gen - Generating Var Decl Code");

        String codeString = "";

        String idCode = generateExpr(varDeclNode.getChild(1));

        if(varDeclNode.getExprType() == SymbolType.STRING)
        {
            //var declaration of string (set to end of execution since it is guaranteed to be "00")
            codeString += "A9 FF ";
        }
        else if(varDeclNode.getExprType() == SymbolType.BOOLEAN)
        {
            //var declaration of bool (set to "false" string in the heap)
            codeString += "A9 " + addStringToHeap("false");
        }
        else
        {
            //var declaration of int (set to 0)
            codeString += "A9 00 ";
        }

        codeString += "8D " + idCode;

        return codeString;
    }

    private String generateWhile(SyntaxTreeNode whileNode) throws CodeGenException
    {
        System.out.println("DEBUG Code Gen - Generating While Statement Code");

        String codeString = "";

        String condition = generateExpr(whileNode.getChild(0));
        String block = generateBlock(whileNode.getChild(1));

        //load the temp with the result of the condition
        if(condition.length() == 3)
            codeString += "A9 " + condition + "8D " + backpatchTable.findOrCreate(TEMP_ID, 0);
        else if(condition.length() == 6)
            codeString += "AD " + condition + "8D " + backpatchTable.findOrCreate(TEMP_ID, 0);
        else
            codeString += condition; //if the string is >6, then it's already stored in temp

        //load x with true and compare with the value in temp
        codeString += "A2 " + addStringToHeap("true");
        codeString += "EC " + backpatchTable.findOrCreate(TEMP_ID, 0);

        //add unconditional jump at the end of the block
        block += "A2 " + addStringToHeap("false"); //load x with false
        block += "A9 " + addStringToHeap("true"); //load acc and temp with true
        block += "8D " + backpatchTable.findOrCreate(TEMP_ID, 0);
        block += "EC " + backpatchTable.findOrCreate(TEMP_ID, 0) + "D0 "; //compare and jump (leaving off end number)

        //jump over the code size of the block (the plus 1 is account for not-yet-calculated backwards jump distance)
        String blockSize = String.format("%2s", Integer.toString(block.length()/3 + 1, 16)).replace(' ', '0').toUpperCase();
        codeString += "D0 " + blockSize + " ";
        codeString += block;

        //calculate size of entire while to add to the final jump backwards
        int whileSize = codeString.length()/3 + 1;
        String jump = String.format("%2s", Integer.toString(256-whileSize, 16)).replace(' ', '0').toUpperCase();
        codeString += jump + " ";

        return codeString;
    }

    private String generateIf(SyntaxTreeNode ifNode) throws CodeGenException
    {
        System.out.println("DEBUG Code Gen - Generating If Statement Code");

        String codeString = "";

        String condition = generateExpr(ifNode.getChild(0));
        String block = generateBlock(ifNode.getChild(1));

        //load the temp with the result of the condition
        if(condition.length() == 3)
            codeString += "A9 " + condition + "8D " + backpatchTable.findOrCreate(TEMP_ID, 0);
        else if(condition.length() == 6)
            codeString += "AD " + condition + "8D " + backpatchTable.findOrCreate(TEMP_ID, 0);
        else
            codeString += condition; //if the string is >6, then it's already stored in temp

        //load x with true and compare with the value in temp
        codeString += "A2 " + addStringToHeap("true");
        codeString += "EC " + backpatchTable.findOrCreate(TEMP_ID, 0);

        //jump over the code size of the block
        String blockSize = String.format("%2s", Integer.toString(block.length()/3, 16)).replace(' ', '0').toUpperCase();
        codeString += "D0 " + blockSize + " ";
        codeString += block;

        return codeString;
    }

    /*
    Generates code for all expressions, including those that are single symbols/ids/strings
    This method handles all temporary backpatch values

    This method returns String lengths of 3 different sizes
    Length 3 - this means some int or bool literal (ex. "9", "4", "true")
    Length 6 - this means a memory address for an id (ex. "a")
    Length >6 - this means some kind of expression that saves its result in the TEMP_ID location
     */
    private String generateExpr(SyntaxTreeNode exprNode) throws CodeGenException
    {
        System.out.println("DEBUG Code Gen - Generating Expression Code");

        String codeString = "";

        //set a bool to true here so that the equality and inequality cases can share code except for one statement
        boolean equality = true;

        switch(exprNode.getNodeType())
        {
            case ADDITION:
            {
                //get both halves of the expression
                String firstHalf = generateExpr(exprNode.getChild(0));
                String secondHalf = generateExpr(exprNode.getChild(1));

                if(secondHalf.length() == 3)
                {
                    //if the second half is a literal, first save it into the temp location, then load the first half and add the temp
                    codeString += "A9 " + secondHalf + "8D " + backpatchTable.findOrCreate(TEMP_ID, 0);
                    codeString += "A9 " + firstHalf; //the first half is guaranteed to be a single digit
                    codeString += "6D " + backpatchTable.findOrCreate(TEMP_ID, 0);
                }
                else if(secondHalf.length() == 6)
                {
                    //if the length is 6 (an id) just load the first half and add the id
                    codeString += "A9 " + firstHalf; //the first half is guaranteed to be a single digit
                    codeString += "6D " + secondHalf;
                }
                else
                {
                    //if the length is not an id (saved into temp), add the second half op codes, load the first half, and add the temp
                    codeString += secondHalf;
                    codeString += "A9 " + firstHalf; //the first half is guaranteed to be a single digit
                    codeString += "6D " + backpatchTable.findOrCreate(TEMP_ID, 0);
                }

                //save the result back into temp
                codeString += "8D " + backpatchTable.findOrCreate(TEMP_ID, 0);

                break;
            }
            case INEQUALITY:
            {
                //sets the equality bool to false, so this can share code with the equality case statement
                equality = false;
            }
            case EQUALITY:
            {
                boolExprCount++; //increase the count for this iteration of bool expression
                //if(exprNode.getParent().getNodeType() == NodeType.EQUALITY ||
                        //exprNode.getParent().getNodeType() == NodeType.INEQUALITY)
                    //throw new CodeGenException("ERROR Code Generation - Nested Booleans not supported");

                String firstHalf = generateExpr(exprNode.getChild(0));
                String secondHalf = generateExpr(exprNode.getChild(1));

                //load accumulator with the result of the first expression
                if(firstHalf.length() == 3)
                    codeString += "A9 " + firstHalf;
                else if(firstHalf.length() == 6)
                    codeString += "AD " + firstHalf;
                else
                    codeString += firstHalf + "AD " + backpatchTable.findOrCreate(TEMP_ID, 0);

                //save the value into a temp value based upon how many bool expressions have been nested
                codeString += "8D " + backpatchTable.findOrCreate("bool" + boolExprCount, 0); //its a temp, so no need to worry about scope

                //load the temp with the result of the second expression
                if(secondHalf.length() == 3)
                    codeString += "A9 " + secondHalf + "8D " + backpatchTable.findOrCreate(TEMP_ID, 0);
                else if(secondHalf.length() == 6)
                    codeString += "AD " + secondHalf + "8D " + backpatchTable.findOrCreate(TEMP_ID, 0);
                else
                    codeString += secondHalf; //if the string is >6, then it's already stored in temp

                //load x with the temp from the first value
                codeString += "AE " + backpatchTable.findOrCreate("bool" + boolExprCount, 0);

                //perform the comparison
                codeString += "EC " + backpatchTable.findOrCreate(TEMP_ID, 0);

                //add the appropriate true/false value into temp
                if(equality)
                {
                    //starts with "false" in the acc. if not equal, jumps over the change to "true"
                    codeString += "A9 " + addStringToHeap("false");
                    codeString += "D0 02 ";
                    codeString += "A9 " + addStringToHeap("true");
                    codeString += "8D " + backpatchTable.findOrCreate(TEMP_ID, 0);
                }
                else
                {
                    //opposite of above
                    codeString += "A9 " + addStringToHeap("true");
                    codeString += "D0 02 ";
                    codeString += "A9 " + addStringToHeap("false");
                    codeString += "8D " + backpatchTable.findOrCreate(TEMP_ID, 0);
                }

                boolExprCount--; //decrease the count for this iteration of bool expression

                break;
            }
            case TERMINAL:
            {
                //TERMINAL means this is a leaf node and has a token
                Token token = exprNode.getToken();

                //add the appropriate op codes to the code string
                switch(token.getType())
                {
                    case DIGIT:
                    {
                        //add 0 padded digit (since literal ints can be only 1 digit)
                        codeString += "0" + token.getValue() + " ";
                        break;
                    }
                    case BOOL_VAL:
                    {
                        //add the locations of strings "true" and "false" from the heap
                        if(token.getValue().equals("true"))
                            codeString += addStringToHeap("true");
                        else
                            codeString += addStringToHeap("false");
                        break;
                    }
                    case STRING:
                    {
                        //the strings in the tokens are internally surrounded by quotes, so they need to be removed
                        String quotedString = token.getValue();
                        String fixedString = quotedString.substring(1, quotedString.length()-1);

                        codeString += addStringToHeap(fixedString);
                        break;
                    }
                    case ID:
                    {
                        codeString += backpatchTable.findOrCreate(token.getValue(), token.getScope());
                        break;
                    }
                }

                break;
            }
        }

        return codeString;
    }

    private String addStringToHeap(String s)
    {
        String stringLoc = heapStrings.get(s);

        //if the string is not already in the heap, add it
        if(stringLoc == null)
        {
            System.out.println("DEBUG Code Gen - Adding \"" + s + "\" to the heap");

            //modify the heap start (-1 for the 00 at the end of the string)
            heapStart = heapStart - s.length() - 1;

            //if heap start ends up < 0, check and break out of the for loop
            //let the error be handled at the end when the heap size is checked against the code and stack size

            //add each character to the executable using ASCII char conversion and in base 16
            for(int i = 0;i < s.length() && heapStart >= 0;i++)
                executable[heapStart + i] = Integer.toString(s.charAt(i), 16).toUpperCase();

            //add the string to the hash map
            heapStrings.put(s, Integer.toString(heapStart, 16).toUpperCase());

            stringLoc = heapStrings.get(s);
        }

        return stringLoc + " ";
    }
}