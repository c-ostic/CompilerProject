public class SemanticAnalyzer
{
    private SyntaxTree ast;
    private ScopeTree scopeTree;
    private int errors;
    private int warnings;
    private int programNum;
    private boolean previousError;

    public SemanticAnalyzer()
    {
        reset();
    }

    //resets the ast, and related variables for the next program
    public void reset()
    {
        ast = new SyntaxTree();
        scopeTree = new ScopeTree();
        errors = 0;
        warnings = 0;
        programNum = 0;
        previousError = false;
    }

    public boolean hasError()
    {
        return errors > 0;
    }

    public SyntaxTree tryAnalyzeProgram(SyntaxTree cst, int program, boolean hadPrevError)
    {
        //reset all the necessary values
        reset();

        //save the program number
        programNum = program;

        //before doing anything, if lex or parse had an error, skip AST and symbol table
        if(hadPrevError)
        {
            System.out.println("Semantic Analysis for Program " + program + " skipped due to previous errors");
            errors++;
            previousError = true;
            return null;
        }

        System.out.println("INFO Semantic Analysis - Analyzing program " + program);

        //create both the ast and scope tree/symbol table
        System.out.println("INFO Semantic Analysis - Creating AST");
        createAST(cst.getRoot());
        System.out.println("INFO Semantic Analysis - Analyzing Scope and Type");
        createScopeTree();
        scopeTree.printWarnings();

        //add together the errors and warnings from here and from the scope tree
        errors += scopeTree.getErrorCount();
        warnings += scopeTree.getWarningCount();

        //print ending result of semantic analysis
        if(errors > 0)
        {
            System.out.println("ERROR Semantic Analysis - Analysis failed with " + errors + " errors and " + warnings + " warnings");
        }
        else
        {
            System.out.println("INFO Semantic Analysis - Analysis succeeded with " + errors + " errors and " + warnings + " warnings");
        }

        return ast;
    }

    public void printAST()
    {
        if(!previousError)
        {
            System.out.println("AST for program " + programNum);
            System.out.println(ast.treeToString());
        }
        else
            System.out.println("AST for Program " + programNum + " skipped due to previous errors");
    }

    public void printSymbolTable()
    {
        if(errors == 0)
        {
            System.out.println("Symbol Table for program " + programNum);
            System.out.println(scopeTree.treeToString());
        }
        else
            System.out.println("Symbol Table for Program " + programNum + " skipped due to previous errors");
    }

    /*---------------------------------------- Recursive Descent Methods ---------------------------------------------*/

    private void createAST(SyntaxTreeNode cstRoot)
    {
        assert(cstRoot.getNodeType() == NodeType.PROGRAM);

        //every CST must start with a Program node
        ast.addRootNode(NodeType.PROGRAM, cstRoot.getLocation());

        //the first child of a Program node is a Block
        createBlock(cstRoot.getChild(0));

        ast.moveUp();
    }

    private void createBlock(SyntaxTreeNode blockNode)
    {
        assert(blockNode.getNodeType() == NodeType.BLOCK);

        ast.addBranchNode(NodeType.BLOCK, blockNode.getLocation());

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

        ast.addBranchNode(NodeType.PRINT_STATEMENT, printNode.getLocation());

        // the expression in the print statement is the third child
        createExpr(printNode.getChild(2));

        ast.moveUp();
    }

    private void createAssignStatement(SyntaxTreeNode assignNode)
    {
        assert(assignNode.getNodeType() == NodeType.ASSIGNMENT_STATEMENT);

        ast.addBranchNode(NodeType.ASSIGNMENT_STATEMENT, assignNode.getLocation());

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

        ast.addBranchNode(NodeType.VAR_DECL, varDeclNode.getLocation());

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

        ast.addBranchNode(NodeType.WHILE_STATEMENT, whileNode.getLocation());

        // get the boolean expression part of the while statement
        createBooleanExpr(whileNode.getChild(1));

        // get the block of the while statement
        createBlock(whileNode.getChild(2));

        ast.moveUp();
    }

    private void createIfStatement(SyntaxTreeNode ifNode)
    {
        assert(ifNode.getNodeType() == NodeType.IF_STATEMENT);

        ast.addBranchNode(NodeType.IF_STATEMENT, ifNode.getLocation());

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
            // add the addition node and give it the location of the "+" token
            ast.addBranchNode(NodeType.ADDITION, intExprNode.getChild(1).getLocation());

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
                ast.addBranchNode(NodeType.EQUALITY, boolOpNode.getLocation());
            else if(boolOpNode.getToken().getType() == TokenType.INEQUALITY)
                ast.addBranchNode(NodeType.INEQUALITY, boolOpNode.getLocation());

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

    /*--------------------------------------- Scope Tree Creation Methods --------------------------------------------*/

    private void createScopeTree()
    {
        //the first child of the root is the first block in the program
        checkBlock(ast.getRoot().getChild(0));
    }

    private void checkBlock(SyntaxTreeNode block)
    {
        //create a new scope for this block (the first block will create the root node)
        if(scopeTree.getRoot() == null)
            scopeTree.addRootNode();
        else
            scopeTree.addBranchNode();

        for(SyntaxTreeNode child : block.getChildren())
        {
            switch(child.getNodeType())
            {
                case PRINT_STATEMENT:
                {
                    SymbolType exprType = getExprType(child.getChild(0));

                    System.out.println("DEBUG Semantic Analysis - Printing type " + exprType);

                    //a print statement can print any type except UNKNOWN
                    if(exprType == SymbolType.UNKNOWN)
                    {
                        System.out.println("ERROR Semantic Analysis - Cannot print UNKNOWN type " + child.getChild(0).getToken());
                        errors++;
                    }

                    //set the printType for this print statement
                    child.setExprType(exprType);

                    break;
                }
                case ASSIGNMENT_STATEMENT:
                {
                    //get the type of the expression from the right side of the assignment
                    SymbolType assignType = getExprType(child.getChild(1));

                    //initialize the variable from the left side with the type from the right side
                    scopeTree.initializeId(child.getChild(0).getToken(), assignType);

                    break;
                }
                case VAR_DECL:
                {
                    //the first child of a var_decl is the type of the declaration
                    Token varTypeToken = child.getChild(0).getToken();
                    //figure out which type it is based on the value of the token
                    SymbolType varType;
                    switch(varTypeToken.getValue())
                    {
                        case "int":
                        {
                            varType = SymbolType.INT;
                            child.setExprType(SymbolType.INT);
                            break;
                        }
                        case "string":
                        {
                            varType = SymbolType.STRING;
                            child.setExprType(SymbolType.STRING);
                            break;
                        }
                        case "boolean":
                        {
                            varType = SymbolType.BOOLEAN;
                            child.setExprType(SymbolType.BOOLEAN);
                            break;
                        }
                        default:
                        {
                            varType = SymbolType.UNKNOWN;
                            break;
                        }
                    }

                    //declare the id in the current scope
                    scopeTree.declareId(child.getChild(1).getToken(), varType);

                    break;
                }
                case WHILE_STATEMENT:
                case IF_STATEMENT:
                {
                    //check the condition
                    //this SHOULD always be of type boolean, but just in case...
                    if(getExprType(child.getChild(0)) != SymbolType.BOOLEAN)
                    {
                        System.out.println("ERROR Semantic Analysis - Unexpected condition type in " + child.getToken());
                        errors++;
                    }

                    //recursively call the next block
                    checkBlock(child.getChild(1));

                    break;
                }
                case BLOCK:
                {
                    //recursively call the next block
                    checkBlock(child);
                }
            }
        }

        scopeTree.moveUp();
    }

    private SymbolType getExprType(SyntaxTreeNode expr)
    {
        switch(expr.getNodeType())
        {
            case ADDITION:
            {
                //get the type of the left side of the operator
                SymbolType firstType = getExprType(expr.getChild(0));

                //get the type of the right side of the operator
                SymbolType secondType = getExprType(expr.getChild(1));

                //if either of them are not of type int, then print an error
                if(firstType != SymbolType.INT || secondType != SymbolType.INT)
                {
                    System.out.println("ERROR Semantic Analysis - Cannot add " + firstType + " to " + secondType + " at " + expr.getLocation());
                    errors++;
                }

                return SymbolType.INT;
            }
            case EQUALITY:
            case INEQUALITY:
            {
                //get the type of the left side of the operator
                SymbolType firstType = getExprType(expr.getChild(0));

                //get the type of the right side of the operator
                SymbolType secondType = getExprType(expr.getChild(1));

                System.out.println("DEBUG Semantic Analysis - Comparing " + firstType + " to " + secondType);

                //if the two types are not the same, then print an error
                if(firstType != secondType)
                {
                    System.out.println("ERROR Semantic Analysis - Cannot compare " + firstType + " to " + secondType + " at " + expr.getLocation());
                    errors++;
                }

                return SymbolType.BOOLEAN;
            }
            case TERMINAL:
            {
                //TERMINAL means this is a leaf node and has a token
                Token token = expr.getToken();

                //return the appropriate type based upon what kind of terminal it is
                switch(token.getType())
                {
                    case DIGIT:
                        return SymbolType.INT;
                    case BOOL_VAL:
                        return SymbolType.BOOLEAN;
                    case STRING:
                        return SymbolType.STRING;
                    case ID:
                        return scopeTree.useId(token);
                    default:
                        return SymbolType.UNKNOWN;
                }
            }
            //defensive default in case I missed something
            default:
            {
                return SymbolType.UNKNOWN;
            }
        }
    }
}
