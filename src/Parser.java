import java.util.List;

public class Parser
{
    private int programCount;
    private List<Token> tokenStream;
    private int tokenCount;
    private Object cst; //TODO: placeholder for reminder to add concrete syntax tree when it gets implemented
    private int errors;

    public Parser()
    {
        reset();
    }

    //resets the tokenStream, the cst, and related variables for the next program
    public void reset()
    {
        tokenStream = null;
        tokenCount = 0;
        cst = null;
        errors = 0;
    }
}
