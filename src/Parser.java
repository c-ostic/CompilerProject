import java.util.List;

class InvalidTokenException extends Exception
{
    public InvalidTokenException(String message)
    {
        super(message);
    }
}

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
            System.out.println("ERROR Parser - " + e.getMessage());
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

    // ::== Block $
    private void parseProgram() throws InvalidTokenException
    {
        System.out.println("INFO Parser - parseProgram()");
        parseBlock();
        match(true, false, TokenType.EOP);
    }

    // ::== { StatementList }
    private void parseBlock() throws InvalidTokenException
    {
        System.out.println("INFO Parser - parseBlock()");
        match(true, false, TokenType.L_BRACE);
        parseStatementList();
        match(true, false, TokenType.R_BRACE);
    }

    // ::== Statement StatementList
    // ::== epsilon
    private void parseStatementList() throws InvalidTokenException
    {

    }

    // ::== PrintStatement
    // ::== AssignmentStatement
    // ::== VarDecl
    // ::== WhileStatement
    // ::== IfStatement
    // ::== Block
    private void parseStatement() throws InvalidTokenException
    {

    }

    // ::== print ( Expr )
    private void parsePrintStatement() throws InvalidTokenException
    {

    }

    // ::== Id = Expr
    private void parseAssignStatement() throws InvalidTokenException
    {

    }

    // ::== type Id
    private void parseVarDeclStatement() throws InvalidTokenException
    {

    }

    // ::== while BooleanExpr Block
    private void parseWhileStatement() throws InvalidTokenException
    {

    }

    // ::== if BooleanExpr Block
    private void parseIfStatement() throws InvalidTokenException
    {

    }

    // ::== IntExpr
    // ::== StringExpr
    // ::== BooleanExpr
    // ::== Id
    private void parseExpr() throws InvalidTokenException
    {

    }

    // ::== digit intop Expr
    // ::== digit
    private void parseIntExpr() throws InvalidTokenException
    {

    }

    // ::== " CharList "
    private void parseStringExpr() throws InvalidTokenException
    {

    }

    // ::== ( Expr boolop Expr )
    // ::== boolVal
    private void parseBooleanExpr() throws InvalidTokenException
    {

    }

    // ::== char
    private void parseId() throws InvalidTokenException
    {

    }

    // ::== char CharList
    // ::== space CharList
    // ::== epsilon
    private void parseCharList() throws InvalidTokenException
    {

    }

    //Checks and consumes the next token in the stream or throws an error if there is no match
    //Returns the type that the token matched (mostly for cases when there are multiple types)
    //Params
    //consumeToken: whether this match should consume the next token or just compare it
    //canBeEpsilon: whether an epsilon production is allowed (if so, don't throw an error if nothing matches)
    //types: the expected types for the current token
    private TokenType match(boolean consumeToken, boolean canBeEpsilon, TokenType... types) throws InvalidTokenException
    {
        //determine if the current token in the stream matches any of the types given
        for(TokenType type : types)
        {
            //throw an error if the end of the token stream was reached
            if(tokenCount >= tokenStream.size())
                throw  new InvalidTokenException("Expected " + types.toString() + " but found end of file");

            //if the type matches, increment the counter and return the type the token matched
            if(tokenStream.get(tokenCount).getType() == type)
            {
                if(consumeToken)
                    tokenCount++;
                return type;
            }
        }

        //At this point, the current token did not match any of the expected types

        //if canBeEpsilon is true, then an error should not be thrown even if none of the types match
        //this is used in the case of StatementList and CharList, where no matches mean the epsilon production
        if(canBeEpsilon)
            return TokenType.DEFAULT;
        else
            throw new InvalidTokenException("Expected " + types.toString() + " but found " + tokenStream.get(tokenCount).toString());
    }
}
