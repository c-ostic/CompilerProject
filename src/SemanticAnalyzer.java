public class SemanticAnalyzer
{
    private SyntaxTree ast;
    private int errors;
    private int warnings;

    public SemanticAnalyzer()
    {
        reset();
    }

    //resets the ast, and related variables for the next program
    public void reset()
    {
        ast = new SyntaxTree();
        errors = 0;
        warnings = 0;
    }

    public SyntaxTree tryAnalyzeProgram(SyntaxTree cst, int program, boolean hadPrevError)
    {
        //before doing anything, if lex or parse had an error, skip AST and symbol table
        if(hadPrevError)
        {
            System.out.println("Semantic Analysis for Program " + program + " skipped due to previous errors");
            System.out.println("AST for Program " + program + " skipped due to previous errors");
            System.out.println("Symbol Table for Program " + program + " skipped due to previous errors");
            System.out.println();
            return null;
        }

        //reset all the necessary values
        reset();

        createAST(cst.getRoot());

        return ast;
    }

    // Modified recursive descent through CST to create AST

    private void createAST(SyntaxTreeNode cstRoot)
    {
        assert(cstRoot.getNodeType() == NodeType.PROGRAM);

        //every CST must start with a Program node
        ast.addRootNode(NodeType.PROGRAM);

        //the first child of a Program node is a Block
        createBlock(cstRoot.getChild(0));

        ast.moveUp();
    }

    private void createBlock(SyntaxTreeNode blockNode)
    {
        assert(blockNode.getNodeType() == NodeType.BLOCK);

        ast.addBranchNode(NodeType.BLOCK);

        //under Block is essentially a list of statements
        //so loop to find the all the statements under block
        SyntaxTreeNode statementListNode = blockNode.getChild(1);
        assert(statementListNode.getNodeType() == NodeType.STATEMENT_LIST);

        while(!statementListNode.getChildren().isEmpty()) //empty statement list means the end, epsilon production
        {
            //this is will move past the Statement node directly to the type of statement
            SyntaxTreeNode nextNode = statementListNode.getChild(0).getChild(0);

            switch (nextNode.getNodeType())
            {
            }
        }
    }

    private void createPrintStatement(SyntaxTreeNode current)
    {

    }

    private void createAssignStatement(SyntaxTreeNode current)
    {

    }

    private void createVarDeclStatement(SyntaxTreeNode current)
    {

    }

    private void createWhileStatement(SyntaxTreeNode current)
    {

    }

    private void createIfStatement(SyntaxTreeNode current)
    {

    }
}
