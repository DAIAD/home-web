package eu.daiad.web.model;

import java.util.HashMap;
import java.util.Map;

public enum EnumDayOfWeek
{
    MONDAY(1),
    TUESDAY(2),
    WEDNESDAY(3),
    THURSDAY(4),
    FRIDAY(5),
    SATURDAY(6),
    SYNDAY(7);
    
    int dayOfWeek;
    
    private EnumDayOfWeek(int day)
    {
        this.dayOfWeek = day;
    }
    
    public int toInteger()
    {
        return dayOfWeek;
    }
    
    private static Map<Integer, EnumDayOfWeek> intToEnum = new HashMap<Integer, EnumDayOfWeek>();
    static {
        for (EnumDayOfWeek e: EnumDayOfWeek.values())
            intToEnum.put(e.dayOfWeek, e);
    }
    
    public static EnumDayOfWeek valueOf(int i)
    {
        return intToEnum.get(i);
    }
}
