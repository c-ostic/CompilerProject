import java.util.LinkedList;
import java.util.List;

enum NodeType
{
    // The types for the different non-terminals that could appear in a syntax tree
    PROGRAM ("Program"),
    BLOCK ("Block"),
    STATEMENT_LIST ("StatementList"),
    STATEMENT ("Statement"),
    PRINT_STATEMENT ("PrintStatement"),
    ASSIGNMENT_STATEMENT ("AssignmentStatement"),
    VAR_DECL ("VarDecl"),
    WHILE_STATEMENT ("WhileStatement"),
    IF_STATEMENT ("IfStatement"),
    EXPR ("Expr"),
    INT_EXPR ("IntExpr"),
    STRING_EXPR ("StringExpr"),
    BOOLEAN_EXPR ("BooleanExpr"),
    ID ("Id"),
    CHAR_LIST ("CharList"),

    // these few are specifically used in the AST
    ADDITION ("Addition"),
    EQUALITY ("Equality"),
    INEQUALITY ("Inequality"),

    // Have a type for leaf nodes
    TERMINAL ("Terminal");

    private final String nodeLabel;

    NodeType(String label)
    {
        nodeLabel = label;
    }

    public String toString()
    {
        return nodeLabel;
    }
}

public class SyntaxTreeNode
{
    private final String label;
    private final NodeType nodeType;
    private final Token token;
    private SyntaxTreeNode parent;
    private final List<SyntaxTreeNode> children;
    private final Location location;

    //used specifically for print statement and var decl nodes to save what type they are printing/declaring
    private SymbolType exprType;

    //Constructor for branch nodes that have a non-terminal type and no token
    //Instead of a token, they are given a location of the first token within the nonterminal
    public SyntaxTreeNode(NodeType nonTerminal, Location startLocation)
    {
        nodeType = nonTerminal;
        token = null;
        parent = null;
        label = nonTerminal.toString();
        children = new LinkedList<SyntaxTreeNode>();
        location = startLocation;
    }

    //Constructor for leaf nodes that have an associated token
    public SyntaxTreeNode(Token terminal)
    {
        nodeType = NodeType.TERMINAL;
        token = terminal;
        parent = null;
        label = terminal.getValue();
        children = new LinkedList<SyntaxTreeNode>();
        location = terminal.getLocation();
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

    public NodeType getNodeType()
    {
        return nodeType;
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

    public Location getLocation()
    {
        return location;
    }

    public SymbolType getExprType()
    {
        return exprType;
    }

    public void setExprType(SymbolType type)
    {
        exprType = type;
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
