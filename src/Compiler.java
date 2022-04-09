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

            SemanticAnalyzer analyzer = new SemanticAnalyzer();
            SyntaxTree ast;

            while (lexer.hasNextProgram())
            {
                tokens = lexer.getNextProgram();
                System.out.println();

                cst = parser.tryParseProgram(tokens, lexer.getProgramCount(), lexer.hasError());
                System.out.println();

                ast = analyzer.tryAnalyzeProgram(cst, lexer.getProgramCount(), lexer.hasError() | parser.hasError());
                System.out.println();

                //print CST, AST, and SymbolTable
                parser.printCST();
                analyzer.printAST();
                analyzer.printSymbolTable();
            }
        }
        catch(FileNotFoundException e)
        {
            System.out.println("File not found. Make sure the test file is in the same directory as the class file" +
                    " or is has a relative path from that directory");
        }
    }
}