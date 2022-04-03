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

        // get the type, which is directly the first child of VarDecl
        SyntaxTreeNode varTypeNode = varDeclNode.getChild(0);
        assert(varTypeNode.getNodeType() == NodeType.TERMINAL);
        ast.addLeafNode(varTypeNode.getToken());

        // get the id, which is under the second child of VarDecl
        SyntaxTreeNode idNode = varDeclNode.getChild(1).getChild(0);
        assert(idNode.getNodeType() == NodeType.TERMINAL);
        ast.addLeafNode(idNode.getToken());

        ast.moveUp();
    }

    private void createWhileStatement(SyntaxTreeNode whileNode)
    {
        assert(whileNode.getNodeType() == NodeType.WHILE_STATEMENT);

        ast.addBranchNode(NodeType.WHILE_STATEMENT);

        // get the boolean expression part of the while statement
        createBooleanExpr(whileNode.getChild(1));

        // get the block of the while statement
        createBlock(whileNode.getChild(2));

        ast.moveUp();
    }

    private void createIfStatement(SyntaxTreeNode ifNode)
    {
        assert(ifNode.getNodeType() == NodeType.IF_STATEMENT);

        ast.addBranchNode(NodeType.IF_STATEMENT);

        // get the boolean expression part of the if statement
        createBooleanExpr(ifNode.getChild(1));

        // get the block of the if statement
        createBlock(ifNode.getChild(2));

        ast.moveUp();
    }

    private void createExpr(SyntaxTreeNode exprNode)
    {
        assert(exprNode.getNodeType() == NodeType.EXPR);

        // no branch node is created, that is left up to the individual expression types

        // the only child of Expr is the type of expression (int, bool, string, id)
        SyntaxTreeNode nextNode = exprNode.getChild(0);

        switch(nextNode.getNodeType())
        {
            case INT_EXPR:
            {
                createIntExpr(nextNode);
                break;
            }
            case BOOLEAN_EXPR:
            {
                createBooleanExpr(nextNode);
                break;
            }
            case STRING_EXPR:
            {
                createStringExpr(nextNode);
                break;
            }
            case ID:
            {
                // this is the simplest case for the expression, since ID always has one and only one child
                SyntaxTreeNode idNode = nextNode.getChild(0);
                assert(idNode.getNodeType() == NodeType.TERMINAL);
                ast.addLeafNode(idNode.getToken());
            }
        }
    }

    private void createIntExpr(SyntaxTreeNode intExprNode)
    {
        assert(intExprNode.getNodeType() == NodeType.INT_EXPR);

        // the first node added should always be a digit
        SyntaxTreeNode digitNode = intExprNode.getChild(0);
        assert(digitNode.getNodeType() == NodeType.TERMINAL);

        // if there is only one child, then the int expression is only the single digit
        if(intExprNode.getChildren().size() == 1)
        {
            ast.addLeafNode(digitNode.getToken());
        }
        // if there is more than one child, then there is addition between the digit and another expression
        // side note: if there were zero children somehow, it would have been in error in parse
        else
        {
            ast.addBranchNode(NodeType.ADDITION);

            // add in the left-hand side of the operation
            ast.addLeafNode(digitNode.getToken());

            // add in the right-hand side of the operation, which is the third child of IntExpr
            createExpr(intExprNode.getChild(2));

            ast.moveUp();
        }
    }

    private void createBooleanExpr(SyntaxTreeNode boolExprNode)
    {
        assert(boolExprNode.getNodeType() == NodeType.BOOLEAN_EXPR);

        // if there is only one child, then the expression is either the value "true" or "false"
        if(boolExprNode.getChildren().size() == 1)
        {
            SyntaxTreeNode boolValNode = boolExprNode.getChild(0);
            assert(boolValNode.getNodeType() == NodeType.TERMINAL);
            ast.addLeafNode(boolValNode.getToken());
        }
        // if there is more than one child, then the expression is equality or inequality between expressions
        else
        {
            // child 0 is "(", 1 is Expr, 2 is "==" or "!=", 3 is the second Expr, and 4 is ")"
            SyntaxTreeNode boolOpNode = boolExprNode.getChild(2);
            if(boolOpNode.getToken().getType() == TokenType.EQUALITY)
                ast.addBranchNode(NodeType.EQUALITY);
            else if(boolOpNode.getToken().getType() == TokenType.INEQUALITY)
                ast.addBranchNode(NodeType.INEQUALITY);

            // get the two expressions on either side of the operator
            createExpr(boolExprNode.getChild(1));
            createExpr(boolExprNode.getChild(3));

            ast.moveUp();
        }
    }

    private void createStringExpr(SyntaxTreeNode stringExprNode)
    {
        assert(stringExprNode.getNodeType() == NodeType.STRING_EXPR);

        // add the beginning quote
        String fullString = "\"";

        // save the token of the open quote to use its positional data
        Token openQuote = stringExprNode.getChild(0).getToken();

        // a stringExpr always has a charList as the second child (even if the charList is empty)
        SyntaxTreeNode charListNode = stringExprNode.getChild(1);
        assert(charListNode.getNodeType() == NodeType.CHAR_LIST);

        // loop through all non-empty charLists to find all characters
        while(!charListNode.getChildren().isEmpty())
        {
            SyntaxTreeNode charNode = charListNode.getChild(0);
            assert(charNode.getNodeType() == NodeType.TERMINAL);

            fullString += charNode.getLabel();

            // for a non-empty charList, the second child is the next charList
            charListNode = charListNode.getChild(1);
            assert(charListNode.getNodeType() == NodeType.CHAR_LIST);
        }

        // add the ending quote
        fullString += "\"";

        // create and add a new token to represent the full string
        Token stringToken = new Token(TokenType.STRING, fullString, openQuote.getLineNumber(), openQuote.getColumnNumber());
        ast.addLeafNode(stringToken);
    }
}
