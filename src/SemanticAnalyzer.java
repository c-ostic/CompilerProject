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
        System.out.println(ast.treeToString());

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
        SyntaxTreeNode statementListNode = blockNode.getChild(1); // 1st and 3rd child is "{" and "}"
        assert(statementListNode.getNodeType() == NodeType.STATEMENT_LIST);

        while(!statementListNode.getChildren().isEmpty()) //empty statement list means the end, epsilon production
        {
            //this is will move past the Statement node directly to the type of statement
            SyntaxTreeNode nextNode = statementListNode.getChild(0).getChild(0);

            switch (nextNode.getNodeType())
            {
                case PRINT_STATEMENT:
                {
                    createPrintStatement(nextNode);
                    break;
                }
                case ASSIGNMENT_STATEMENT:
                {
                    createAssignStatement(nextNode);
                    break;
                }
                case VAR_DECL:
                {
                    createVarDeclStatement(nextNode);
                    break;
                }
                case WHILE_STATEMENT:
                {
                    createWhileStatement(nextNode);
                    break;
                }
                case IF_STATEMENT:
                {
                    createIfStatement(nextNode);
                    break;
                }
                case BLOCK:
                {
                    createBlock(nextNode);
                    break;
                }
            }

            // move to the next StatementList node,
            // which is the second child of the previous (non-empty) StatementList node
            statementListNode = statementListNode.getChild(1);
            assert(statementListNode.getNodeType() == NodeType.STATEMENT_LIST);
        }

        ast.moveUp();
    }

    private void createPrintStatement(SyntaxTreeNode printNode)
    {
        assert(printNode.getNodeType() == NodeType.PRINT_STATEMENT);

        ast.addBranchNode(NodeType.PRINT_STATEMENT);

        // the expression in the print statement is the third child
        createExpr(printNode.getChild(2));

        ast.moveUp();
    }

    private void createAssignStatement(SyntaxTreeNode assignNode)
    {
        assert(assignNode.getNodeType() == NodeType.ASSIGNMENT_STATEMENT);

        ast.addBranchNode(NodeType.ASSIGNMENT_STATEMENT);

        // get the left-hand side of the assign statement
        // specifically get the terminal value under the id node
        SyntaxTreeNode idNode = assignNode.getChild(0).getChild(0);
        assert(idNode.getNodeType() == NodeType.TERMINAL);
        ast.addLeafNode(idNode.getToken());

        // get the right-hand side of the assign, which is the third child
        createExpr(assignNode.getChild(2));

        ast.moveUp();
    }

    private void createVarDeclStatement(SyntaxTreeNode varDeclNode)
    {
        assert(varDeclNode.getNodeType() == NodeType.VAR_DECL);

        ast.addBranchNode(NodeType.VAR_DECL);

        ast.moveUp();
    }

    private void createWhileStatement(SyntaxTreeNode whileNode)
    {
        assert(whileNode.getNodeType() == NodeType.WHILE_STATEMENT);

        ast.addBranchNode(NodeType.WHILE_STATEMENT);

        ast.moveUp();
    }

    private void createIfStatement(SyntaxTreeNode ifNode)
    {
        assert(ifNode.getNodeType() == NodeType.IF_STATEMENT);

        ast.addBranchNode(NodeType.IF_STATEMENT);

        ast.moveUp();
    }

    private void createExpr(SyntaxTreeNode exprNode)
    {
        //temp code for testing
        ast.addBranchNode(NodeType.EXPR);
        ast.moveUp();
    }
}
