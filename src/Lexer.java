import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

public class Lexer
{
    //The scanner that Lexer will use to get input
    //Provided by the main class
    private Scanner scan;

    private List<Token> currProgram; //holds the tokens for the current program being tokenized
    private boolean hasError; //true if the last program read had an error

    public Lexer(Scanner inputScanner)
    {
        scan = inputScanner;
    }

    //Returns the list of tokens of the next readable program
    //Returns empty if there are no more programs
    //Returns null if the program had an error
    public List<Token> getNextProgram()
    {
        currProgram = new LinkedList<Token>();



        return currProgram;
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
