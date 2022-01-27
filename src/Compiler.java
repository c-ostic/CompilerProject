public class Compiler
{
    public static void main(String[] args)
    {
        System.out.println("Hello World!");

        Test.test();

        if(args.length > 0)
            System.out.println(args[0]);
    }
}