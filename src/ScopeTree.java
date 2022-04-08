public class ScopeTree
{
    private ScopeTreeNode root;
    private ScopeTreeNode current;
    private int scopeCount;
    private int errors;
    private int warnings;

    public ScopeTree()
    {
        root = null;
        current = null;
        scopeCount = 0;
    }

    //return the root node of the tree
    public ScopeTreeNode getRoot()
    {
        return root;
    }

    //method for adding the root node to the tree
    //the root node has a label and no parents
    public void addRootNode()
    {
        root = new ScopeTreeNode(scopeCount);
        current = root;
        scopeCount++;
    }

    //method for adding a branch node to the tree
    //non-leaf nodes always have labels instead of tokens
    public void addBranchNode()
    {
        ScopeTreeNode newNode = new ScopeTreeNode(scopeCount);
        current.addChild(newNode);
        current = newNode;
        scopeCount++;
    }

    //utility method for moving back up the tree
    public void moveUp()
    {
        current = current.getParent();
    }

    public int getErrorCount()
    {
        return errors;
    }

    public int getWarningCount()
    {
        return warnings;
    }

    public String treeToString()
    {
        //TODO: Make table format
        return "";
    }

    //used to declare a new id in the scope
    //uses the symbolType passed in as its type after checking it doesn't already exist
    public void declareId(Token id, SymbolType symbolType)
    {
        //try to find the symbol in the current scope (not parents)
        //if not found, then declare the identifier in the scope
        if(current.getSymbol(id.getValue(), false) == null)
        {
            current.addIdentifier(id.getValue(), symbolType);
        }
        else
        {
            //if the id is found in the current scope,
            //then log an error that the variable is already declared
            System.out.println("ERROR - Semantic Analysis - " + id + " already declared in scope");
        }
    }

    //used to initialize an existing id to a certain type (symbolType)
    public void initializeId(Token id, SymbolType symbolType)
    {
        SymbolAttributes idInfo = current.getSymbol(id.getValue(), true);

        //if the symbol was found, continue to check type
        if(idInfo != null)
        {
            //if the symbol type matches, then there is no error, and id is successfully initialized
            if(idInfo.getSymbolType() == symbolType)
            {
                idInfo.setInitialized(true);
            }
            else
            {
                //otherwise there is a type mismatch error
                System.out.println("ERROR - Semantic Analysis - Type mismatch: " +
                        "Cannot assign " + symbolType + " to " + idInfo.getSymbolType() + " " + id);
            }
        }
        else
        {
            //if the symbol wasn't found, log an undeclared error
            System.out.println("ERROR - Semantic Analysis - " + id + " not declared");
        }
    }

    //used to use an already existing id and get its type
    public SymbolType useId(Token id)
    {
        SymbolAttributes idInfo = current.getSymbol(id.getValue(), true);

        //if the symbol was found, check if it was initialized
        if(idInfo != null)
        {
            //if the symbol was not initialized, print a warning, but still mark the id as used
            if(!idInfo.isInitialized())
            {
                System.out.println("WARN - Semantic Analysis - " + id + " used but not initialized");
            }

            idInfo.setUsed(true);
            return idInfo.getSymbolType();
        }
        else
        {
            //if the symbol wasn't found, log an undeclared error and return an unknown type
            System.out.println("ERROR - Semantic Analysis - " + id + " not declared");
            return SymbolType.UNKNOWN;
        }
    }
}
