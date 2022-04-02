import java.util.LinkedList;
import java.util.List;

public class SyntaxTreeNode
{
    private final String label;
    private final Token token;
    private SyntaxTreeNode parent;
    private final List<SyntaxTreeNode> children;

    //Constructor for branch nodes that have a label but no token
    //If parentNode is left null, this acts as a root node
    public SyntaxTreeNode(String nonTerminal)
    {
        label = nonTerminal;
        token = null;
        parent = null;
        children = new LinkedList<SyntaxTreeNode>();
    }

    //Constructor for leaf nodes that have an associated token
    public SyntaxTreeNode(Token terminal)
    {
        label = terminal.getValue();
        token = terminal;
        parent = null;
        children = new LinkedList<SyntaxTreeNode>();
    }

    public void addChild(SyntaxTreeNode child)
    {
        children.add(child);
        child.parent = this;
    }

    // same as toString, but here for good measure
    public String getLabel()
    {
        return label;
    }

    public Token getToken()
    {
        return token;
    }

    public SyntaxTreeNode getParent()
    {
        return parent;
    }

    public List<SyntaxTreeNode> getChildren()
    {
        return children;
    }

    public SyntaxTreeNode getChild(int index)
    {
        return children.get(index);
    }

    //the node is a leaf node iff it has a token instead of just a label
    public boolean isLeaf()
    {
        return token != null;
    }

    public String toString()
    {
        return label;
    }
}
