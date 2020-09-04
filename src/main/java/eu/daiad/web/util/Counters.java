package eu.daiad.web.util;

import java.util.EnumMap;

import org.apache.commons.lang.mutable.MutableInt;

public class  Counters <E extends Enum<E>>
{
    private EnumMap<E, MutableInt> map;

    public Counters(Class<E> cls)
    {
        map = new EnumMap<E, MutableInt>(cls);

        // Initialize all counters to zero
        for (E key: cls.getEnumConstants())
            map.put(key, new MutableInt(0));
    }

    public int get(E key)
    {
        return map.get(key).intValue();
    }

    public void incr(E key)
    {
        map.get(key).increment();
    }

    public int incrAndGet(E key)
    {
        MutableInt n = map.get(key);
        n.increment();
        return n.intValue();
    }

    public void decr(E key)
    {
        map.get(key).decrement();
    }

    public int decrAndGet(E key)
    {
        MutableInt n = map.get(key);
        n.decrement();
        return n.intValue();
    }
}
