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
        System.out.println("INFO Parser - parseStatementList()");
        TokenType nextToken = match(false, true,
                TokenType.PRINT_KEY,
                TokenType.ID,
                TokenType.VAR_TYPE,
                TokenType.WHILE_KEY,
                TokenType.IF_KEY,
                TokenType.L_BRACE);

        if(nextToken != TokenType.DEFAULT)
        {
            parseStatement();
            parseStatementList();
        }
        else
        {
            //epsilon production
            //TokenType was default, which means the next token did not match any valid Statement
        }
    }

    // ::== PrintStatement
    // ::== AssignmentStatement
    // ::== VarDecl
    // ::== WhileStatement
    // ::== IfStatement
    // ::== Block
    private void parseStatement() throws InvalidTokenException
    {
        switch (match(false, false,
            TokenType.PRINT_KEY,
            TokenType.ID,
            TokenType.VAR_TYPE,
            TokenType.WHILE_KEY,
            TokenType.IF_KEY,
            TokenType.L_BRACE))
        {
            case PRINT_KEY:
            {
                parsePrintStatement();
                break;
            }
            case ID:
            {
                parseAssignStatement();
                break;
            }
            case VAR_TYPE:
            {
                parseVarDeclStatement();
                break;
            }
            case WHILE_KEY:
            {
                parseWhileStatement();
                break;
            }
            case IF_KEY:
            {
                parseIfStatement();
                break;
            }
            case L_BRACE:
            {
                parseBlock();
                break;
            }
        }
    }

    // ::== print ( Expr )
    private void parsePrintStatement() throws InvalidTokenException
    {
        match(true, false, TokenType.PRINT_KEY);
        match(true, false, TokenType.L_PAREN);
        parseExpr();
        match(true, false, TokenType.R_PAREN);
    }

    // ::== Id = Expr
    private void parseAssignStatement() throws InvalidTokenException
    {
        parseId();
        match(true, false, TokenType.ASSIGN);
        parseExpr();
    }

    // ::== type Id
    private void parseVarDeclStatement() throws InvalidTokenException
    {
        match(true, false, TokenType.VAR_TYPE);
        parseId();
    }

    // ::== while BooleanExpr Block
    private void parseWhileStatement() throws InvalidTokenException
    {
        match(true, false, TokenType.WHILE_KEY);
        parseBooleanExpr();
        parseBlock();
    }

    // ::== if BooleanExpr Block
    private void parseIfStatement() throws InvalidTokenException
    {
        match(true, false, TokenType.IF_KEY);
        parseBooleanExpr();
        parseBlock();
    }

    // ::== IntExpr
    // ::== StringExpr
    // ::== BooleanExpr
    // ::== Id
    private void parseExpr() throws InvalidTokenException
    {
        switch(match(false, false,
                TokenType.DIGIT,
                TokenType.QUOTE,
                TokenType.L_PAREN,
                TokenType.BOOL_VAL,
                TokenType.ID))
        {
            case DIGIT:
            {
                parseIntExpr();
                break;
            }
            case QUOTE:
            {
                parseStringExpr();
                break;
            }
            case L_PAREN: case BOOL_VAL:
            {
                parseBooleanExpr();
                break;
            }
            case ID:
            {
                parseId();
                break;
            }
        }
    }

    // ::== digit intop Expr
    // ::== digit
    private void parseIntExpr() throws InvalidTokenException
    {
        match(true, false, TokenType.DIGIT);

        //if the next token is an intop, it is consumed, otherwise it (and the following if statement) is skipped
        TokenType nextToken = match(true, true, TokenType.ADDITION);
        if(nextToken == TokenType.ADDITION)
            parseExpr();
    }

    // ::== " CharList "
    private void parseStringExpr() throws InvalidTokenException
    {
        match(true, false, TokenType.QUOTE);
        parseCharList();
        match(true, false, TokenType.QUOTE);
    }

    // ::== ( Expr boolop Expr )
    // ::== boolVal
    private void parseBooleanExpr() throws InvalidTokenException
    {
        TokenType nextToken = match(true, false, TokenType.L_PAREN, TokenType.BOOL_VAL);

        if(nextToken == TokenType.L_PAREN)
        {
            parseExpr();
            match(true, false, TokenType.EQUALITY, TokenType.INEQUALITY);
            parseExpr();
        }
        else
        {
            //do nothing
            //this means the token is of type BOOL_VAL (or an error was thrown in match)
        }
    }

    // ::== char
    private void parseId() throws InvalidTokenException
    {
        match(true, false, TokenType.ID);
    }

    // ::== char CharList
    // ::== space CharList
    // ::== epsilon
    private void parseCharList() throws InvalidTokenException
    {
        //Token Type CHAR includes both a-z and spaces inside of strings
        TokenType nextToken = match(true, true, TokenType.CHAR);
        if(nextToken == TokenType.CHAR)
        {
            parseCharList();
        }
        else
        {
            //do nothing
            //epsilon production
        }
    }

    //Checks and consumes the next token in the stream or throws an error if there is no match
    //Returns the type that the token matched (mostly for cases when there are multiple types)
    //Handles all references to the tokenStream as well as error throwing
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
