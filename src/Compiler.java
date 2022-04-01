import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Scanner;

public class Compiler
{
    public static void main(String[] args)
    {
        try
        {
            Scanner scan = new Scanner(new File(args[0]));

            Lexer lexer = new Lexer(scan);
            List<Token> tokens;

            Parser parser = new Parser();
            SyntaxTree cst;

            while (lexer.hasNextProgram())
            {
                tokens = lexer.getNextProgram();
                System.out.println();

                cst = parser.tryParseProgram(tokens, lexer.getProgramCount(), lexer.hasError());
                System.out.println();
            }
        }
        catch(FileNotFoundException e)
        {
            System.out.println("File not found. Make sure the test file is in the same directory as the class file" +
                    " or is has a relative path from that directory");
        }
    }
}