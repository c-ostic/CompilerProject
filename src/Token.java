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
    STRING,     // for use in the AST when character lists are combined into one token

    //Error/Invalid Token
    ERROR,

    //Default/Empty Token - lowest precedence
    DEFAULT     // used by lexer as a default token value

}

/*
Represents a single Token
Has a type (the enum above), its value, and its location in terms of line and column numbers
 */
public class Token
{
    private final TokenType type;
    private final String value;
    private final Location location;

    //some tokens (specifically ids) may be assigned a scope for the purposes of distinguishing them in code gen
    private int scope;

    public Token(TokenType tokenType, String tokenValue, int lineNumber, int colNumber)
    {
        type = tokenType;
        value = tokenValue;
        location = new Location(lineNumber, colNumber);

        //default scope to 0
        scope = 0;
    }

    //getters and setters
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
        return location.getLine();
    }

    public int getColumnNumber()
    {
        return location.getColumn();
    }

    public Location getLocation()
    {
        return location;
    }

    public void setScope(int scope)
    {
        this.scope = scope;
    }

    public int getScope()
    {
        return scope;
    }

    //toString shows all bits of information in the Token
    public String toString()
    {
        return type.name() + " [ " + value + " ] at " + location;
    }
}
