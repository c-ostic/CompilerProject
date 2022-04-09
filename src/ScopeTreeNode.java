import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class ScopeTreeNode
{
    private final int scope;
    private final HashMap<String,SymbolAttributes> identifiers;
    private ScopeTreeNode parent;
    private final List<ScopeTreeNode> children;

    public ScopeTreeNode(int scopeNum)
    {
        identifiers = new HashMap<String,SymbolAttributes>();
        parent = null;
        children = new LinkedList<ScopeTreeNode>();
        scope = scopeNum;
    }

    public void addChild(ScopeTreeNode child)
    {
        children.add(child);
        child.parent = this;
    }

    public ScopeTreeNode getParent()
    {
        return parent;
    }

    public List<ScopeTreeNode> getChildren()
    {
        return children;
    }

    public int getScope()
    {
        return scope;
    }

    public HashMap<String, SymbolAttributes> getIdentifiers()
    {
        return identifiers;
    }

    public void addIdentifier(String id, SymbolType symbolType, Location location)
    {
        identifiers.put(id, new SymbolAttributes(symbolType, location));
    }

    // Returns the symbol attributes of the given id if it exists
    // Otherwise it returns null
    // If searchParents is true, the method will call up through the whole scope tree trying to find the id
    public SymbolAttributes getSymbol(String id, boolean searchParents)
    {
        if(identifiers.containsKey(id))
            return identifiers.get(id);
        else if(parent != null && searchParents)
            return parent.getSymbol(id, true);
        else
            return null;
    }
}
