import java.util.HashMap;
import java.util.LinkedList;

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

    public void printWarnings()
    {
        //immediately stop if root is null
        if(root == null)
            return;

        //make the queue to traverse the tree in level order (breadth first search)
        LinkedList<ScopeTreeNode> queue = new LinkedList<ScopeTreeNode>();
        queue.add(root);

        while(!queue.isEmpty())
        {
            //get the node at the front of the queue
            ScopeTreeNode current = queue.pop();

            //check each of the identifiers in the current scope tree node
            HashMap<String, SymbolAttributes> ids = current.getIdentifiers();
            for(String id : ids.keySet())
            {
                SymbolAttributes attributes = ids.get(id);
                //check if the id is used or not
                //if it is used, there are no new warnings to print (used but not initialized is handled in useId())
                if(!attributes.isUsed())
                {
                    if(attributes.isInitialized())
                        System.out.println("WARN Semantic Analysis - id [ " + id + " ] declared at " + attributes.getDeclareLocation() +
                                " and initialized but not used");
                    else
                        System.out.println("WARN Semantic Analysis - id [ " + id + " ] declared at " + attributes.getDeclareLocation() +
                                " but not initialized or used");
                    warnings++;
                }
            }

            //add all the current node's children to the queue
            for(ScopeTreeNode child : current.getChildren())
                queue.add(child);
        }
    }

    //returns a string representation of the symbol table
    public String treeToString()
    {
        //immediately stop if root is null
        if(root == null)
            return "";

        String result = "";

        //make table headers
        result += "Symbol Table\n";
        result += "_______________________________\n";
        result += "Name  Type      Scope  Location\n";
        result += "_______________________________\n";

        //make the queue to traverse the tree in level order (breadth first search)
        LinkedList<ScopeTreeNode> queue = new LinkedList<ScopeTreeNode>();
        queue.add(root);

        while(!queue.isEmpty())
        {
            //get the node at the front of the queue
            ScopeTreeNode current = queue.pop();

            //print out each of the identifiers in the current scope tree node
            HashMap<String, SymbolAttributes> ids = current.getIdentifiers();
            for(String id : ids.keySet())
            {
                result += String.format("%-6s", id);
                SymbolAttributes attributes = ids.get(id);
                result += String.format("%-10s", attributes.getSymbolType());
                result += String.format("%-7s", current.getScope());
                result += attributes.getDeclareLocation() + "\n";
            }

            //add all the current node's children to the queue
            for(ScopeTreeNode child : current.getChildren())
                queue.add(child);
        }

        return result;
    }

    //used to declare a new id in the scope
    //uses the symbolType passed in as its type after checking it doesn't already exist
    public void declareId(Token id, SymbolType symbolType)
    {
        System.out.println("DEBUG Semantic Analysis - Trying to declare " + id);

        //try to find the symbol in the current scope (not parents)
        //if not found, then declare the identifier in the scope
        if(current.getSymbol(id.getValue(), false) == null)
        {
            System.out.println("DEBUG Semantic Analysis - Declaring " + id + " in scope " + current.getScope() + " as type " + symbolType);
            current.addIdentifier(id.getValue(), symbolType, id.getLocation(), current.getScope());
            id.setScope(current.getScope());
        }
        else
        {
            //if the id is found in the current scope,
            //then log an error that the variable is already declared
            System.out.println("ERROR Semantic Analysis - " + id + " already declared in scope");
            errors++;
        }
    }

    //used to initialize an existing id to a certain type (symbolType)
    public void initializeId(Token id, SymbolType symbolType)
    {
        System.out.println("DEBUG Semantic Analysis - Trying to initialize " + id);

        SymbolAttributes idInfo = current.getSymbol(id.getValue(), true);

        //if the symbol was found, continue to check type
        if(idInfo != null)
        {
            //if the symbol type matches, then there is no error, and id is successfully initialized
            if(idInfo.getSymbolType() == symbolType)
            {
                System.out.println("DEBUG Semantic Analysis - Initializing " + id + " in scope " + idInfo.getScope() + " to type " + symbolType);
                idInfo.setInitialized(true);
                id.setScope(idInfo.getScope());
            }
            else
            {
                //otherwise there is a type mismatch error
                System.out.println("ERROR Semantic Analysis - Type mismatch: " +
                        "Cannot assign " + symbolType + " to " + idInfo.getSymbolType() + " " + id);
                errors++;
            }
        }
        else
        {
            //if the symbol wasn't found, log an undeclared error
            System.out.println("ERROR Semantic Analysis - " + id + " not declared");
            errors++;
        }
    }

    //used to use an already existing id and get its type
    public SymbolType useId(Token id)
    {
        System.out.println("DEBUG Semantic Analysis - Trying to use " + id);

        SymbolAttributes idInfo = current.getSymbol(id.getValue(), true);

        //if the symbol was found, check if it was initialized
        if(idInfo != null)
        {
            //if the symbol was not initialized, print a warning, but still mark the id as used
            if(!idInfo.isInitialized())
            {
                System.out.println("WARN Semantic Analysis - " + id + " used but not initialized");
                warnings++;
            }

            System.out.println("DEBUG Semantic Analysis - Using " + id + " in scope " + idInfo.getScope());

            idInfo.setUsed(true);
            id.setScope(idInfo.getScope());
            return idInfo.getSymbolType();
        }
        else
        {
            //if the symbol wasn't found, log an undeclared error and return an unknown type
            System.out.println("ERROR Semantic Analysis - " + id + " not declared");
            errors++;
            return SymbolType.UNKNOWN;
        }
    }
}
