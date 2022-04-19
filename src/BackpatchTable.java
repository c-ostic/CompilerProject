import java.util.ArrayList;
import java.util.List;

public class BackpatchTable
{
    //four parallel lists for each part of the table
    private List<String> ids;
    private List<Integer> scopes;
    private List<String> placeholders;
    private List<String> backpatchValues; //this one won't be populated until the end when the actual values are known

    //the total number of entries in the table
    private int varCount;

    public BackpatchTable()
    {
        placeholders = new ArrayList<String>();
        backpatchValues = new ArrayList<String>();
        ids = new ArrayList<String>();
        scopes = new ArrayList<Integer>();

        varCount = 0;
    }

    //tries to find the variable and scope pair in the table
    //if found, return the temp value
    //else, add a new entry into the table and return the new temp value
    public String findOrCreate(String id, int scope)
    {
        //search through the current table by iterating through the ids and scopes
        for(int i = 0;i < varCount;i++)
        {
            //if the id (in the correct scope) is found, return its temp value
            if(ids.get(i).equals(id) && scopes.get(i).equals(scope))
                return placeholders.get(i);
        }

        //if not found, create a new entry in the table (new value in each list)
        ids.add(id);
        scopes.add(scope);
        //the placeholder name is T and the current count
        String tempName = "T" + varCount + " ";
        varCount++;
        placeholders.add(tempName);

        return tempName;
    }

    //fill in the backpatch row of the table
    public void backpatch(int startingValue)
    {
        int currentValue = startingValue;
        for(int i = 0;i < varCount;i++)
        {
            //add the base 16 representation of the value
            backpatchValues.add(Integer.toString(currentValue, 16));
            //each variable only needs one byte, so just increase the value by 1
            currentValue++;
        }
    }

    //get the backpatch value of the associated temp value
    public String getBackpatchValue(String placeholderValue)
    {
        int index = placeholders.indexOf(placeholderValue);
        return backpatchValues.get(index);
    }
}
