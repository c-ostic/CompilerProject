public class CodeGenerator
{
    //set the executable size
    private final int EXE_SIZE = 0x100;

    private String[] executable;
    private BackpatchTable backpatchTable;
    private int heapStart;
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
        heapStart = EXE_SIZE - 1;
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
            return;
        }

        System.out.println("INFO Code Generation - Generating code for program " + program);
        generateProgram(ast);
    }

    public void printExecutable()
    {
        System.out.println("Executable:");

        for(int i = 0;i < executable.length;i++)
        {
            System.out.print(executable[i] + " ");

            if(i % 8 == 7)
                System.out.println();
        }
    }

    /*-------------------------------------------- Code Gen Methods --------------------------------------------------*/

    private final String TEMP_ID = "temp";

    private void generateProgram(SyntaxTree ast)
    {
        //start the backpatch off with a temp storage value
        backpatchTable.findOrCreate(TEMP_ID, 0);

        //the first child of the root is the first block in the program
        //get the code in the form of a space delineated string and add a halt op code
        String codeString = generateBlock(ast.getRoot().getChild(0)) + "00 ";

        //turn the codeString into a usable array
        String[] codeArray = codeString.split(" ");

        //backpatch the table
        backpatchTable.backpatch(codeArray.length);

        //iterate through the code array to put it into the executable, backpatching along the way
        for(int i = 0;i < codeArray.length;i++)
        {
            if(codeArray[i].startsWith("T"))
                executable[i] = backpatchTable.getBackpatchValue(codeArray[i]);
            else
                executable[i] = codeArray[i];
        }
        //TODO: add check for crashing into heap
    }

    private String generateBlock(SyntaxTreeNode blockNode)
    {
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

    private String generatePrint(SyntaxTreeNode printNode)
    {
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
        if(printNode.getExprType() == SymbolType.INT || printNode.getExprType() == SymbolType.BOOLEAN)
            codeString += "A2 01 ";
        else if(printNode.getExprType() == SymbolType.STRING)
            codeString += "A2 02 ";

        //add the system call
        codeString += "FF ";

        return codeString;
    }

    private String generateAssignment(SyntaxTreeNode assignmentNode)
    {
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

    private String generateVarDecl(SyntaxTreeNode varDeclNode)
    {
        String codeString = "";

        String idCode = generateExpr(varDeclNode.getChild(1));

        if(varDeclNode.getExprType() == SymbolType.STRING)
        {
            //var declaration of string (set to end of execution since it is guaranteed to be "00")
            codeString += "A9 FF ";
        }
        else
        {
            //var declaration of int or bool
            codeString += "A9 00 ";
        }

        codeString += "8D " + idCode;

        return codeString;
    }

    private String generateWhile(SyntaxTreeNode whileNode)
    {
        String codeString = "";
        return codeString;
    }

    private String generateIf(SyntaxTreeNode ifNode)
    {
        String codeString = "";
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
    private String generateExpr(SyntaxTreeNode exprNode)
    {
        String codeString = "";

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
            case EQUALITY:
            {
                break;
            }
            case INEQUALITY:
            {
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
                        //add 01 for true, 00 for false
                        if(token.getValue().equals("true"))
                            codeString += "01 ";
                        else
                            codeString += "00 ";
                        break;
                    }
                    case STRING:
                    {
                        //TODO
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
}
