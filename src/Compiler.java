import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Scanner;

public class Compiler
{
    public static void main(String[] args) throws FileNotFoundException
    {
        Scanner scan = new Scanner(new File(args[0]));

        Lexer lexer = new Lexer(scan);
        List<Token> tokens;

        while(lexer.hasNextProgram())
        {
            tokens = lexer.getNextProgram();
            System.out.println();

            //for(Token token : tokens)
                //System.out.println(token.getValue());
        }
    }
}