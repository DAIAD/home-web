package eu.daiad.web.model;

import org.springframework.util.Assert;

public class Priority
{
    private int value;
    
    private Priority(int value)
    {
        this.value = value;
    }
    
    public int intValue()
    {
        return value;
    }
    
    private static final int N = 10;
    
    private static final Priority[] instances = populate(N); 
    
    public static final Priority LOWEST_PRIORITY = instances[0];
    
    public static final Priority NORMAL_PRIORITY = instances[N/2];
    
    public static final Priority HIGHEST_PRIORITY = instances[N - 1];
    
    private static Priority[] populate(int n)
    {
        Priority[] r = new Priority[n];
        for (int i = 0; i < n; i++)
            r[i] = new Priority(i);
        return r;
    }
    
    public static Priority valueOf(int i)
    {
        Assert.isTrue(i >= 0 && i < N, "Invalid priority number");
        return instances[i];    
    }
    
    @Override
    public String toString()
    {
        return String.format("Priority(%d)", value);
    }
}
