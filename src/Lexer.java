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

    //Returns the list of tokens of the next readable program
    //Returns empty if there are no more programs
    //Returns null if the program had an error
    public List<Token> getNextProgram()
    {
        //holds the tokens for the current program being tokenized
        List<Token> currProgram = new LinkedList<Token>();

        //if the buffer is empty, get the next string
        if(buffer.length() == 0)
            buffer = scan.next();

        while(!buffer.isEmpty())
        {
            //get the next token and remove the token from the buffer
            Token nextToken = getNextToken();

            if(nextToken.getType() == TokenType.ERROR)
            {
                //if there is an error, report it, and move on
                hasError = true;
                //TODO: print some error message
            }
            else
            {
                //if there isn't an error, add the token to the list
                currProgram.add(nextToken);
                //TODO: print out token
            }

            //if the token is an end of program token, break out of the loop
            if(nextToken.getType() == TokenType.EOP)
                break;

            //fill the buffer if it is empty and there is still more to scan
            if(buffer.length() == 0 && scan.hasNext())
                buffer = scan.next();
        }

        //print any applicable warnings
        if(currProgram.size() != 0 && currProgram.get(currProgram.size()-1).getType() != TokenType.EOP)
        {
            //TODO: print a warning that an end of file is missing
        }

        if(isCommented)
        {
            //TODO: print a warning that there is an unmatched comment
        }

        if(isQuoted)
        {
            //TODO: print a warning that there is an unmatched quote
        }

        //return the tokens
        return currProgram;
    }

    //used to keep track of the current state of DFA (see getNextState())
    private int currentState;

    //gets the next token starting from the beginning of the buffer string
    //removes the token from the buffer string
    private Token getNextToken()
    {
        return new Token(TokenType.ID, "test", 0, 0);
    }

    //represents the DFA for valid tokens
    //updates the next state and returns the token type that it would be if ending in this state
    private TokenType getNextState(char nextChar)
    {
        return TokenType.ERROR;
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
}