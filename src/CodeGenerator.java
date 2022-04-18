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

        generateProgram(ast);
    }

    /*-------------------------------------------- Code Gen Methods --------------------------------------------------*/

    private void generateProgram(SyntaxTree ast)
    {
        //the first child of the root is the first block in the program
        //get the code in the form of a space delineated string
        String codeString = generateBlock(ast.getRoot().getChild(0));

        //iterate through the full string to put it into the executable

        //backpatch the table, then iterate through the array changing necessary parts
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
        return codeString;
    }

    private String generateAssignment(SyntaxTreeNode assignmentNode)
    {
        String codeString = "";
        return codeString;
    }

    private String generateVarDecl(SyntaxTreeNode varDeclNode)
    {
        String codeString = "";
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
}
