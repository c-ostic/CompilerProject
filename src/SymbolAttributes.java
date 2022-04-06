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
    private boolean initialized;
    private boolean used;

    public SymbolAttributes(SymbolType type)
    {
        symbolType = type;
        initialized = false;
        used = false;
    }

    public SymbolType getSymbolType()
    {
        return symbolType;
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
