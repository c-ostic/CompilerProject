import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

public class Lexer
{
    //The scanner that Lexer will use to get input
    //Provided by the main class
    private Scanner scan;
    private boolean hasError; //true if the last program read had an error
    private boolean isQuoted;
    private boolean isCommented;
    private String buffer;

    public Lexer(Scanner inputScanner)
    {
        scan = inputScanner;
        buffer = "";
    }

    //Returns true if there is another program to be read,
    //  false if the end of file has been reached
    public boolean hasNextProgram()
    {
        return scan.hasNext();
    }

    //Returns true if the last program read had an error, false otherwise
    public boolean hasError()
    {
        return hasError;
    }

    //Returns the list of tokens of the next readable program
    //Returns empty if there are no more programs
    //Returns null if the program had an error
    public List<Token> getNextProgram()
    {
        //TODO: add program counter
        System.out.println("INFO Lexer - Lexing Program");

        //holds the tokens for the current program being tokenized
        List<Token> currProgram = new LinkedList<Token>();

        //if the buffer is empty, get the next string
        if(buffer.length() == 0)
            buffer = scan.next();

        while(!buffer.isEmpty())
        {
            //get the next token and remove the token from the buffer
            Token nextToken = getNextToken();

            if(nextToken.getType() == TokenType.L_COMMENT)
            {
                isCommented = true;
            }
            else if(nextToken.getType() == TokenType.R_COMMENT)
            {
                isCommented = false;
            }
            else if(!isCommented && nextToken.getType() != TokenType.SPACE)
            {
                //enter or exit quotes
                if(nextToken.getType() == TokenType.QUOTE)
                    isQuoted = !isQuoted;

                if (nextToken.getType() == TokenType.ERROR)
                {
                    //if there is an error, report it, and move on
                    hasError = true;
                    System.out.println("ERROR Lexer - Unrecognized Token: " + nextToken);
                }
                else
                {
                    //if there isn't an error, add the token to the list
                    currProgram.add(nextToken);
                    System.out.println("DEBUG Lexer - " + nextToken);
                }

                //if the token is an end of program token, break out of the loop
                if (nextToken.getType() == TokenType.EOP)
                    break;
            }

            //fill the buffer if it is empty and there is still more to scan
            if(buffer.length() == 0 && scan.hasNext())
                buffer = scan.next();
        }

        //print any applicable warnings
        if(currProgram.size() != 0 && currProgram.get(currProgram.size()-1).getType() != TokenType.EOP)
        {
            //TODO: print a warning that an end of program is missing
        }

        if(isCommented)
        {
            //TODO: print a warning that there is an unmatched comment
        }

        if(isQuoted)
        {
            //TODO: print a warning that there is an unmatched quote
        }

        if(hasError)
            System.out.println("ERROR Lexer - Lex failed with errors");
        else
            System.out.println("INFO Lexer - Lex completed with 0 errors");

        //return the tokens
        return currProgram;
    }

    /*
    used to keep track of the current state of DFA (see getNextState())
    if the state is -1, then no other tokens are possible and
      getNextToken() should not continue looking for a larger token
      (ex. in "pri=" the state will be -1 when it reaches the "=")
      (ex. in "apple" the state will be -1 when it reaches "a" since no longer token starts with "a")
    */
    private int currentState;

    //gets the next token starting from the beginning of the buffer string
    //removes the token from the buffer string
    private Token getNextToken()
    {
        currentState = 0;
        TokenType bestType = TokenType.DEFAULT;
        int endOfBestToken = 0;

        for(int i = 0;i < buffer.length();i++)
        {
            TokenType currType = getNextState(buffer.substring(0,i+1));

            //if the current type has a higher precedence (closer to 0), update best
            if(currType.ordinal() < bestType.ordinal())
            {
                bestType = currType;
                endOfBestToken = i + 1;
            }

            if(currentState == -1)
                break;
        }

        //remove the token from the buffer
        String token = buffer.substring(0, endOfBestToken);
        buffer = buffer.substring(endOfBestToken);

        //TODO: get line numbers and column numbers
        return new Token(bestType, token, 0, 0);
    }

    //represents the DFA for valid tokens
    //updates the next state and returns the token type that it would be if ending in this state
    //changes state to -1 if no other tokens are possible
    private TokenType getNextState(String currToken)
    {
        TokenType type = TokenType.DEFAULT;

        /*
        States:
        -1: end state
         0: start state
         1: equality state
         2: inequality state
         3: start_comment state
         4: end_comment state
         5: spaces state (accepts any and all whitespace not in a string)
         6: "print" state
         10: "while" state
         16: "if/int" state
         17: "int" state
         18: "string" state
         23: "boolean" state
         29: "false" state
         33: "true" state
        */

        switch (currentState)
        {
            case 0: // Start state
            {
                switch (currToken.charAt(currToken.length()-1))
                {
                    // The following cases are characters that are only part of single character tokens (hence current state is changed to -1)
                    case '{':
                    {
                        currentState = -1;
                        type = TokenType.L_BRACE;
                        break;
                    }
                    case '}':
                    {
                        currentState = -1;
                        type = TokenType.R_BRACE;
                        break;
                    }
                    case '(':
                    {
                        currentState = -1;
                        type = TokenType.L_PAREN;
                        break;
                    }
                    case ')':
                    {
                        currentState = -1;
                        type = TokenType.R_PAREN;
                        break;
                    }
                    case '\"':
                    {
                        currentState = -1;
                        type = TokenType.QUOTE;
                        break;
                    }
                    case '$':
                    {
                        currentState = -1;
                        type = TokenType.EOP;
                        break;
                    }
                    case '+':
                    {
                        currentState = -1;
                        type = TokenType.ADDITION;
                        break;
                    }
                    case '0': case '1': case '2': case '3': case '4': case '5': case '6': case '7': case '8': case '9':
                    {
                        currentState = -1;
                        type = TokenType.DIGIT;
                        break;
                    }
                    case 'a': case 'c': case 'd': case 'e': case 'g': case 'h': case 'j': case 'k': case 'l': case 'm':
                    case 'n': case 'o': case 'q': case 'r': case 'u': case 'v': case 'x': case 'y': case 'z':
                    {
                        currentState = -1;
                        if (isQuoted)
                            type = TokenType.CHAR;
                        else
                            type = TokenType.ID;
                        break;
                    }

                    // The next cases are characters that may start multi-character tokens
                    case '=':
                    {
                        currentState = 1; //beginning of equality
                        type = TokenType.ASSIGN;
                        break;
                    }
                    case '!':
                    {
                        currentState = 2; //beginning of inequality
                        type = TokenType.ERROR;
                        break;
                    }
                    case '/':
                    {
                        currentState = 3; //beginning of start comment
                        type = TokenType.ERROR;
                        break;
                    }
                    case '*':
                    {
                        currentState = 4; //beginning of end comment
                        type = TokenType.ERROR;
                        break;
                    }
                    case ' ':
                    {
                        if (isQuoted)
                        {
                            currentState = -1;
                            type = TokenType.CHAR;
                        }
                        else
                        {
                            currentState = 5; //beginning of a series of whitespace
                            type = TokenType.ID;
                        }
                        break;
                    }
                    case '\t': case '\n': case '\r':
                    {
                        currentState = 5; //beginning of a series of whitespace
                        type = TokenType.ID;
                        break;
                    }
                    case 'p':
                    {
                        if (isQuoted)
                        {
                            currentState = -1;
                            type = TokenType.CHAR;
                        }
                        else
                        {
                            currentState = 6; //beginning "p" of "print"
                            type = TokenType.ID;
                        }
                        break;
                    }
                    case 'w':
                    {
                        if (isQuoted)
                        {
                            currentState = -1;
                            type = TokenType.CHAR;
                        }
                        else
                        {
                            currentState = 10; //beginning "w" of "while"
                            type = TokenType.ID;
                        }
                        break;
                    }
                    case 'i':
                    {
                        if (isQuoted)
                        {
                            currentState = -1;
                            type = TokenType.CHAR;
                        }
                        else
                        {
                            currentState = 16; //beginning "i" of "if" or "int"
                            type = TokenType.ID;
                        }
                        break;
                    }
                    case 's':
                    {
                        if (isQuoted)
                        {
                            currentState = -1;
                            type = TokenType.CHAR;
                        }
                        else
                        {
                            currentState = 18; //beginning "s" of "string"
                            type = TokenType.ID;
                        }
                        break;
                    }
                    case 'b':
                    {
                        if (isQuoted)
                        {
                            currentState = -1;
                            type = TokenType.CHAR;
                        }
                        else
                        {
                            currentState = 23; //beginning "b" of "boolean"
                            type = TokenType.ID;
                        }
                        break;
                    }
                    case 'f':
                    {
                        if (isQuoted)
                        {
                            currentState = -1;
                            type = TokenType.CHAR;
                        }
                        else
                        {
                            currentState = 29; //beginning "f" of "false"
                            type = TokenType.ID;
                        }
                        break;
                    }
                    case 't':
                    {
                        if (isQuoted)
                        {
                            currentState = -1;
                            type = TokenType.CHAR;
                        }
                        else
                        {
                            currentState = 33; //beginning "t" of "true"
                            type = TokenType.ID;
                        }
                        break;
                    }
                    default:
                    {
                        currentState = -1;
                        type = TokenType.ERROR;
                        break;
                    }
                }
                break;
            }// End case 0

            case 1: // Equality state
            {
                currentState = -1;
                if(currToken.equals("=="))
                    type = TokenType.EQUALITY;
                else
                    type = TokenType.ERROR;
                break;
            }
            case 2: // Inequality state
            {
                currentState = -1;
                if(currToken.equals("!="))
                    type = TokenType.INEQUALITY;
                else
                    type = TokenType.ERROR;
                break;
            }
            case 3: // Start comment state
            {
                currentState = -1;
                if(currToken.equals("/*"))
                    type = TokenType.L_COMMENT;
                else
                    type = TokenType.ERROR;
                break;
            }
            case 4: // End comment state
            {
                currentState = -1;
                if(currToken.equals("*/"))
                    type = TokenType.R_COMMENT;
                else
                    type = TokenType.ERROR;
                break;
            }
            case 5: // Spaces state
            {
                if(currToken.matches("\\s"))
                {
                    //no change to state, so it returns here next time through
                    type = TokenType.SPACE;
                }
                else
                {
                    currentState = -1;
                    type = TokenType.ERROR;
                }
                break;
            }
            case 6: // "print" state
            {
                if(currToken.equals("print"))
                {
                    currentState = -1;
                    type = TokenType.PRINT_KEY;
                }
                else
                {
                    //if the token is not a prefix of print (i.e. "pr","prin", etc.), set the state to -1
                    //otherwise, no change to state, so it will return here
                    type = TokenType.ERROR;
                    if(!("print".startsWith(currToken)))
                        currentState = -1;
                }
                break;
            }
            case 10: // "while" state
            {
                if(currToken.equals("while"))
                {
                    currentState = -1;
                    type = TokenType.WHILE_KEY;
                }
                else
                {
                    //if the token is not a prefix of while, set the state to -1
                    //otherwise, no change to state, so it will return here
                    type = TokenType.ERROR;
                    if(!("while".startsWith(currToken)))
                        currentState = -1;
                }
                break;
            }
            case 16: // "if" or "in" in "int" state
            {
                if(currToken.equals("if"))
                {
                    currentState = -1;
                    type = TokenType.IF_KEY;
                }
                else if(currToken.equals("in")) // prefix of "int"
                {
                    currentState = 17;
                    type = TokenType.ERROR;
                }
                else
                {
                    currentState = -1;
                    type = TokenType.ERROR;
                }
                break;
            }
            case 17: // "int" state
            {
                currentState = -1;
                if(currToken.equals("int")) // at this point, the token is "int" or an invalid token
                    type = TokenType.VAR_TYPE;
                else
                    type = TokenType.ERROR;
                break;
            }
            case 18: // "string" state
            {
                if(currToken.equals("string"))
                {
                    currentState = -1;
                    type = TokenType.VAR_TYPE;
                }
                else
                {
                    //if the token is not a prefix of string, set the state to -1
                    //otherwise, no change to state, so it will return here
                    type = TokenType.ERROR;
                    if(!("string".startsWith(currToken)))
                        currentState = -1;
                }
                break;
            }
            case 23: // "boolean" state
            {
                if(currToken.equals("boolean"))
                {
                    currentState = -1;
                    type = TokenType.VAR_TYPE;
                }
                else
                {
                    //if the token is not a prefix of boolean, set the state to -1
                    //otherwise, no change to state, so it will return here
                    type = TokenType.ERROR;
                    if(!("boolean".startsWith(currToken)))
                        currentState = -1;
                }
                break;
            }
            case 29: // "false" state
            {
                if(currToken.equals("false"))
                {
                    currentState = -1;
                    type = TokenType.BOOL_VAL;
                }
                else
                {
                    //if the token is not a prefix of false, set the state to -1
                    //otherwise, no change to state, so it will return here
                    type = TokenType.ERROR;
                    if(!("false".startsWith(currToken)))
                        currentState = -1;
                }
                break;
            }
            case 33: // "true" state
            {
                if(currToken.equals("true"))
                {
                    currentState = -1;
                    type = TokenType.BOOL_VAL;
                }
                else
                {
                    //if the token is not a prefix of true, set the state to -1
                    //otherwise, no change to state, so it will return here
                    type = TokenType.ERROR;
                    if(!("true".startsWith(currToken)))
                        currentState = -1;
                }
                break;
            }
        }

        return type;
    }
}