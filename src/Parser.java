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
    private SyntaxTree cst;
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
        cst = new SyntaxTree();
        errors = 0;
    }

    //Tries to parse the program given by the list of tokens
    //If the parse fails, an exception will be thrown in recursive descent and caught here
    public SyntaxTree tryParseProgram(List<Token> tokens, int program, boolean hadPrevError)
    {
        //before doing anything, if lex had an error, skip parsing and CST
        if(hadPrevError)
        {
            System.out.println("Parse for Program " + program + " skipped due to previous errors");
            System.out.println("CST for Program " + program + " skipped due to previous errors");
            System.out.println();
            return null;
        }

        //reset all the necessary values
        reset();
        tokenStream = tokens;

        try
        {
            System.out.println("INFO Parser - Parsing program " + program);
            System.out.println("INFO Parser - parse()");
            parseProgram();
            System.out.println("INFO Parser - Parse completed with 0 errors");
            System.out.println();
            System.out.println(cst.treeToString());
        }
        catch (InvalidTokenException e)
        {
            //if the parse fails, it will end up here
            errors++;
            System.out.println("ERROR Parser - " + e.getMessage());
            System.out.println("ERROR Parser - Parse failed with " + errors + " error(s)");
            System.out.println();
            System.out.println("CST for Program " + program + " skipped due to parse errors");
            System.out.println();

            //dump whatever partial tree was created
            cst = null;
        }

        return cst;
    }

    /*---------------------------------------- Recursive Descent Methods ---------------------------------------------*/

    // ::== Block $
    private void parseProgram() throws InvalidTokenException
    {
        System.out.println("INFO Parser - parseProgram()");
        cst.addRootNode("Program");
        parseBlock();
        match(true, false, TokenType.EOP);
    }

    // ::== { StatementList }
    private void parseBlock() throws InvalidTokenException
    {
        System.out.println("INFO Parser - parseBlock()");
        cst.addBranchNode("Block");
        match(true, false, TokenType.L_BRACE);
        parseStatementList();
        match(true, false, TokenType.R_BRACE);
        cst.moveUp();
    }

    // ::== Statement StatementList
    // ::== epsilon
    private void parseStatementList() throws InvalidTokenException
    {
        System.out.println("INFO Parser - parseStatementList()");
        cst.addBranchNode("StatementList");
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
        cst.moveUp();
    }

    // ::== PrintStatement
    // ::== AssignmentStatement
    // ::== VarDecl
    // ::== WhileStatement
    // ::== IfStatement
    // ::== Block
    private void parseStatement() throws InvalidTokenException
    {
        System.out.println("INFO Parser - parseStatement()");
        cst.addBranchNode("Statement");
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
        cst.moveUp();
    }

    // ::== print ( Expr )
    private void parsePrintStatement() throws InvalidTokenException
    {
        System.out.println("INFO Parser - parsePrintStatement()");
        cst.addBranchNode("PrintStatement");
        match(true, false, TokenType.PRINT_KEY);
        match(true, false, TokenType.L_PAREN);
        parseExpr();
        match(true, false, TokenType.R_PAREN);
        cst.moveUp();
    }

    // ::== Id = Expr
    private void parseAssignStatement() throws InvalidTokenException
    {
        System.out.println("INFO Parser - parseAssignmentStatement()");
        cst.addBranchNode("AssignmentStatement");
        parseId();
        match(true, false, TokenType.ASSIGN);
        parseExpr();
        cst.moveUp();
    }

    // ::== type Id
    private void parseVarDeclStatement() throws InvalidTokenException
    {
        System.out.println("INFO Parser - parseVarDecl()");
        cst.addBranchNode("VarDecl");
        match(true, false, TokenType.VAR_TYPE);
        parseId();
        cst.moveUp();
    }

    // ::== while BooleanExpr Block
    private void parseWhileStatement() throws InvalidTokenException
    {
        System.out.println("INFO Parser - parseWhileStatement()");
        cst.addBranchNode("WhileStatement");
        match(true, false, TokenType.WHILE_KEY);
        parseBooleanExpr();
        parseBlock();
        cst.moveUp();
    }

    // ::== if BooleanExpr Block
    private void parseIfStatement() throws InvalidTokenException
    {
        System.out.println("INFO Parser - parseIfStatement()");
        cst.addBranchNode("IfStatement");
        match(true, false, TokenType.IF_KEY);
        parseBooleanExpr();
        parseBlock();
        cst.moveUp();
    }

    // ::== IntExpr
    // ::== StringExpr
    // ::== BooleanExpr
    // ::== Id
    private void parseExpr() throws InvalidTokenException
    {
        System.out.println("INFO Parser - parseExpr()");
        cst.addBranchNode("Expr");
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
        cst.moveUp();
    }

    // ::== digit intop Expr
    // ::== digit
    private void parseIntExpr() throws InvalidTokenException
    {
        System.out.println("INFO Parser - parseIntExpr()");
        cst.addBranchNode("IntExpr");
        match(true, false, TokenType.DIGIT);

        //if the next token is an intop, it is consumed, otherwise it (and the following if statement) is skipped
        TokenType nextToken = match(true, true, TokenType.ADDITION);
        if(nextToken == TokenType.ADDITION)
            parseExpr();
        cst.moveUp();
    }

    // ::== " CharList "
    private void parseStringExpr() throws InvalidTokenException
    {
        System.out.println("INFO Parser - parseStringExpr()");
        cst.addBranchNode("StringExpr");
        match(true, false, TokenType.QUOTE);
        parseCharList();
        match(true, false, TokenType.QUOTE);
        cst.moveUp();
    }

    // ::== ( Expr boolop Expr )
    // ::== boolVal
    private void parseBooleanExpr() throws InvalidTokenException
    {
        System.out.println("INFO Parser - parseBooleanExpr()");
        cst.addBranchNode("BooleanExpr");
        TokenType nextToken = match(true, false, TokenType.L_PAREN, TokenType.BOOL_VAL);

        if(nextToken == TokenType.L_PAREN)
        {
            parseExpr();
            match(true, false, TokenType.EQUALITY, TokenType.INEQUALITY);
            parseExpr();
            match(true, false, TokenType.R_PAREN);
        }
        else
        {
            //do nothing
            //this means the token was of type BOOL_VAL and was already consumed in the first match function
        }
        cst.moveUp();
    }

    // ::== char
    private void parseId() throws InvalidTokenException
    {
        System.out.println("INFO Parser - parseId()");
        cst.addBranchNode("Id");
        match(true, false, TokenType.ID);
        cst.moveUp();
    }

    // ::== char CharList
    // ::== space CharList
    // ::== epsilon
    private void parseCharList() throws InvalidTokenException
    {
        System.out.println("INFO Parser - parseCharList()");
        cst.addBranchNode("CharList");
        
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
        cst.moveUp();
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
            //theoretically this should never happen since lex adds an EOP token if there is not one,
            //which would either correctly end the program or throw an error
            if(tokenCount >= tokenStream.size())
                throw  new InvalidTokenException("Expected " + typesArrayToString(types) + " but found end of file");

            //if the type matches, increment the counter and return the type the token matched
            if(tokenStream.get(tokenCount).getType() == type)
            {
                if(consumeToken)
                {
                    cst.addLeafNode(tokenStream.get(tokenCount));
                    tokenCount++;
                }
                return type;
            }
        }

        //At this point, the current token did not match any of the expected types

        //if canBeEpsilon is true, then an error should not be thrown even if none of the types match
        //this is used in the case of StatementList and CharList, where no matches mean the epsilon production
        if(canBeEpsilon)
            return TokenType.DEFAULT;
        else
            throw new InvalidTokenException("Expected " + typesArrayToString(types) + " but found " + tokenStream.get(tokenCount).toString());
    }

    //utility method for match to help print a list of token types
    //assumes the types array is not empty
    private String typesArrayToString(TokenType[] types)
    {
        String result = "[ ";

        result += types[0];

        for(int i = 1;i < types.length;i++)
            result += ", " + types[i];

        return result + " ]";
    }
}
