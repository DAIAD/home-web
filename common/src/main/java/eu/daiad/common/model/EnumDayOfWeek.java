package eu.daiad.common.model;

import java.util.HashMap;
import java.util.Map;

public enum EnumDayOfWeek
{
    MONDAY(1, Type.WEEKDAY),
    TUESDAY(2, Type.WEEKDAY),
    WEDNESDAY(3, Type.WEEKDAY),
    THURSDAY(4, Type.WEEKDAY),
    FRIDAY(5, Type.WEEKDAY),
    SATURDAY(6, Type.WEEKEND),
    SYNDAY(7, Type.WEEKEND);
    
    public enum Type {
        WEEKDAY,
        WEEKEND;
    }
    
    private int dayOfWeek;
    
    private Type type;
    
    private EnumDayOfWeek(int day, Type type)
    {
        this.dayOfWeek = day;
        this.type = type;
    }
    
    public int toInteger()
    {
        return dayOfWeek;
    }
    
    public Type getType()
    {
        return type;
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
