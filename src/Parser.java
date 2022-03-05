import java.util.List;

public class Parser
{
    private List<Token> tokenStream;
    private int tokenCount;
    private TreeNode cst_root;
    private TreeNode current;
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
        cst_root = null;
        current = null;
        errors = 0;
    }

    //Tries to parse the program given by the list of tokens
    //If the parse fails, an exception will be thrown in recursive descent and caught here
    public TreeNode tryParseProgram(List<Token> tokens, int program)
    {
        //reset all the necessary values
        reset();
        tokenStream = tokens;

        try
        {
            System.out.println("INFO Parser - Parsing program " + program);
            System.out.println("INFO Parser - parse()");
            parseProgram();
            System.out.println("INFO Parser - Parse completed with 0 errors");
        }
        catch (Exception e)
        {
            //if the parse fails, it will end up here
            errors++;
            System.out.println("ERROR Parser - Parse failed with " + errors + " error(s)");
        }

        return cst_root;
    }

    /*
        Tree Utility Methods
     */

    //method for adding the root node to the tree
    //the root node has a label and no parents (null in the TreeNode constructor)
    private void addRootNode(String label)
    {
        cst_root = new TreeNode(label, null);
        current = cst_root;
    }

    //method for adding a branch node to the tree
    //non-leaf nodes always have labels instead of tokens
    private void addBranchNode(String label)
    {
        TreeNode newNode = new TreeNode(label, current);
        current = newNode;
    }

    //method for adding a leaf node to the tree
    //leaf nodes always have tokens
    private void addLeafNode(Token token)
    {
        TreeNode newNode = new TreeNode(token, current);
    }

    //utility method for moving back up the tree
    private void moveUp()
    {
        current = current.getParent();
    }

    /*
        Recursive Descent Methods
     */

    private void parseProgram()
    {

    }

    //Checks and consumes the next token in the stream or throws an error if there is no match
    //Returns the type that the token matched (mostly for cases when there are multiple types)
    //If overrideOnMismatch is true, no error is thrown and TokenType.Default is returned (used for epsilon production)
    private TokenType match(boolean overrideOnMismatch, TokenType... types) throws Exception
    {
        //determine if the current token in the stream matches any of the types given
        for(TokenType type : types)
        {
            //if the type matches, increment the counter and return the type the token matched
            if(tokenStream.get(tokenCount).getType() == type)
            {
                tokenCount++;
                return type;
            }
        }

        //if overrideOnMismatch is true, then an error should not be thrown even if none of the types match
        //this is used in the case of StatementList and CharList, where no matches mean the epsilon production
        if(overrideOnMismatch)
            return TokenType.DEFAULT;
        else
            throw new Exception();
    }
}
