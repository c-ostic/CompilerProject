import java.util.LinkedList;
import java.util.List;

public class TreeNode
{
    private final String label;
    private final Token token;
    private final TreeNode parent;
    private final List<TreeNode> children;

    //Constructor for branch nodes that have a label but no token
    //If parentNode is left null, this acts as a root node
    public TreeNode(String nonTerminal, TreeNode parentNode)
    {
        label = nonTerminal;
        token = null;
        parent = parentNode;
        children = new LinkedList<TreeNode>();

        if(parent != null)
            parent.addChild(this);
    }

    //Constructor for leaf nodes that have an associated token
    public TreeNode(Token terminal, TreeNode parentNode)
    {
        label = terminal.getValue();
        token = terminal;
        parent = parentNode;
        children = new LinkedList<TreeNode>();

        if(parent != null)
            parent.addChild(this);
    }

    public void addChild(TreeNode child)
    {
        children.add(child);
    }

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
}
