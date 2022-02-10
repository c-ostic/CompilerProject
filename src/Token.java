enum TokenType
{
    //all types of tokens ordered by precedence

    //Keywords - highest precedence
    PRINT_KEY,  // print
    WHILE_KEY,  // while
    IF_KEY,     // if
    BOOL_VAL,   // true | false
    VAR_TYPE,   // int | boolean | string

    //Identifiers
    ID,         // a-z

    //Symbols
    L_BRACE,    // {
    R_BRACE,    // }
    L_PAREN,    // (
    R_PAREN,    // )
    QUOTE,      // "
    EQUALITY,   // ==   higher precedence than ASSIGN
    ASSIGN,     // =
    INEQUALITY, // !=
    ADDITION,   // +
    EOP,        // $
    L_COMMENT,  // /*   these won't be added to the token list, but are there so the
    R_COMMENT,  // */       lexer can recognize when there are comments

    //Digits and Characters
    DIGIT,      // 0-9
    CHAR,       // a-z | *spaces in strings*
    SPACE,      // *spaces/whitespace not in strings*

    //Error/Invalid Token
    ERROR,

    //Default/Empty Token - lowest precedence
    DEFAULT     // used by lexer as a default token value

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

    public String toString()
    {
        return type.name() + " [ " + value + " ] found at (" + line + ":" + column + ")";
    }
}
