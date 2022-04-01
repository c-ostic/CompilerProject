public class SemanticAnalyzer
{
    private SyntaxTree ast;
    private int errors;
    private int warnings;

    public SemanticAnalyzer()
    {
        reset();
    }

    //resets the ast, and related variables for the next program
    public void reset()
    {
        ast = new SyntaxTree();
        errors = 0;
        warnings = 0;
    }

    public SyntaxTree tryAnalyzeProgram(SyntaxTree cst, int program, boolean hadPrevError)
    {
        //before doing anything, if lex or parse had an error, skip AST and symbol table
        if(hadPrevError)
        {
            System.out.println("Semantic Analysis for Program " + program + " skipped due to previous errors");
            System.out.println("AST for Program " + program + " skipped due to previous errors");
            System.out.println("Symbol Table for Program " + program + " skipped due to previous errors");
            System.out.println();
            return null;
        }

        //reset all the necessary values
        reset();

        return ast;
    }
}
