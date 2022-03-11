import java.util.LinkedList;
import java.util.List;

public class TreeNode
{
    private final String label;
    private final Token token;
    private TreeNode parent;
    private final List<TreeNode> children;

    //Constructor for branch nodes that have a label but no token
    //If parentNode is left null, this acts as a root node
    public TreeNode(String nonTerminal)
    {
        label = nonTerminal;
        token = null;
        parent = null;
        children = new LinkedList<TreeNode>();
    }

    //Constructor for leaf nodes that have an associated token
    public TreeNode(Token terminal)
    {
        label = terminal.getValue();
        token = terminal;
        parent = null;
        children = new LinkedList<TreeNode>();
    }

    public void addChild(TreeNode child)
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

    public TreeNode getParent()
    {
        return parent;
    }

    public List<TreeNode> getChildren()
    {
        return children;
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
