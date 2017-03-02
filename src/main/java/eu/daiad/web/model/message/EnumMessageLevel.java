package eu.daiad.web.model.message;

import java.util.Comparator;

public enum EnumMessageLevel
{
    LOG(0),
    NOTIFY(10);
    
    private final int value;
    
    private EnumMessageLevel(int value)
    {
        this.value = value;
    }

    public int getValue()
    {
        return value;
    }
    
    public static EnumMessageLevel valueOf(int value)
    {
        for (EnumMessageLevel level: EnumMessageLevel.values())
            if (level.value == value)
                return level;
        return null;
    }
    
    public static class ValueComparator 
        implements Comparator<EnumMessageLevel>
    {
        @Override
        public int compare(EnumMessageLevel l1, EnumMessageLevel l2)
        {
            return Integer.compare(l1.value, l2.value);
        }        
    }
    
    public static Comparator<EnumMessageLevel> byValue = new ValueComparator();
    
    public static int compare(EnumMessageLevel l1, EnumMessageLevel l2)
    {
        return byValue.compare(l1, l2);
    }
}
