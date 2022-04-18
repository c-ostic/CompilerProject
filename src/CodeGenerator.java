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
    }
}
