public class SyntaxTree
{
    private SyntaxTreeNode root;
    private SyntaxTreeNode current;

    public SyntaxTree()
    {
        root = null;
        current = null;
    }

    //return the root node of the tree
    public SyntaxTreeNode getRoot()
    {
        return root;
    }

    //method for adding the root node to the tree
    //the root node has a label and no parents (null in the TreeNode constructor)
    public void addRootNode(NodeType nodeType, Location location)
    {
        root = new SyntaxTreeNode(nodeType, location);
        current = root;
    }

    //method for adding a branch node to the tree
    //non-leaf nodes always have labels instead of tokens
    public void addBranchNode(NodeType nodeType, Location location)
    {
        SyntaxTreeNode newNode = new SyntaxTreeNode(nodeType, location);
        current.addChild(newNode);
        current = newNode;
    }

    //method for adding a leaf node to the tree
    //leaf nodes always have tokens
    public void addLeafNode(Token token)
    {
        SyntaxTreeNode newNode = new SyntaxTreeNode(token);
        current.addChild(newNode);
    }

    //utility method for moving back up the tree
    public void moveUp()
    {
        current = current.getParent();
    }

    //This method and its helper expand() based on code by
    //      Alan G. Labouseur, and based on the 2009
    //      work by Michael Ardizzone and Tim Smith.
    public String treeToString()
    {
        return expand(root, 0);
    }

    //Code based on other work, see treeToString() above
    //Recursive function to handle the expansion of the nodes.
    private String expand(SyntaxTreeNode node, int depth)
    {
        //immediately stop if root is null
        if(root == null)
            return "";

        String traversalResult = "";

        // Space out based on the current depth so
        // this looks at least a little tree-like.
        for (int i = 0; i < depth; i++)
        {
            traversalResult += "-";
        }

        // If the node is a leaf node...
        if (node.isLeaf())
        {
            // ... note the leaf node.
            traversalResult += "[" + node + "]";
            traversalResult += "\n";
        }
        else
        {
            // There are children, so note these interior/branch nodes and ...
            traversalResult += "<" + node + "> \n";
            // .. recursively expand them.
            for (int i = 0; i < node.getChildren().size(); i++)
            {
                traversalResult += expand(node.getChildren().get(i), depth + 1);
            }
        }

        return traversalResult;
    }
}
