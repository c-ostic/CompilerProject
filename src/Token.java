enum TokenType
{
    //Tokens that don't have multiple possible values
    L_BRACE,
    R_BRACE,
    L_PAREN,
    R_PAREN,
    QUOTE,
    PRINT_KEY,
    WHILE_KEY,
    IF_KEY,
    ASSIGN,
    EQUALITY,
    INEQUALITY,
    ADDITION,
    EOP,

    //Tokens that have multiple possible values
    VAR_TYPE,
    ID,
    CHAR,
    DIGIT
}

public class Token
{
    private final TokenType type;
    private final String value;
    private final int line;
    private final int column;

    public Token(TokenType tokenType, String tokenValue, int lineNumber, int colNumber)
    {
        type = tokenType;
        value = tokenValue;
        line = lineNumber;
        column = colNumber;
    }

    public TokenType getType()
    {
        return type;
    }

    public String getValue()
    {
        return value;
    }

    public int getLineNumber()
    {
        return line;
    }

    public int getColumnNumber()
    {
        return column;
    }
}
