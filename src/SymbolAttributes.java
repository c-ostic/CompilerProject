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
    private boolean initialized;
    private boolean used;

    public SymbolAttributes(SymbolType type, Location declareLoc)
    {
        symbolType = type;
        initialized = false;
        used = false;
        declareLocation = declareLoc;
    }

    public SymbolType getSymbolType()
    {
        return symbolType;
    }

    public Location getDeclareLocation()
    {
        return declareLocation;
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
