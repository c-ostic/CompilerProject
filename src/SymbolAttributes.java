enum SymbolType
{
    INT,
    STRING,
    BOOLEAN,
    UNKNOWN
}

public class SymbolAttributes
{
    private final SymbolType symbolType;
    private final Location declareLocation;
    private final int declareScope;
    private boolean initialized;
    private boolean used;

    public SymbolAttributes(SymbolType type, Location declareLoc, int scope)
    {
        symbolType = type;
        initialized = false;
        used = false;
        declareLocation = declareLoc;
        declareScope = scope;
    }

    public SymbolType getSymbolType()
    {
        return symbolType;
    }

    public Location getDeclareLocation()
    {
        return declareLocation;
    }

    public int getScope()
    {
        return declareScope;
    }

    public boolean isInitialized()
    {
        return initialized;
    }

    public boolean isUsed()
    {
        return used;
    }

    public void setInitialized(boolean initialized)
    {
        this.initialized = initialized;
    }

    public void setUsed(boolean used)
    {
        this.used = used;
    }
}
