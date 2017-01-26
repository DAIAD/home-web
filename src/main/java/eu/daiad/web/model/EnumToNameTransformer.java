package eu.daiad.web.model;

import org.apache.commons.collections4.Transformer;

public class EnumToNameTransformer <E extends Enum<E>>
    implements Transformer<E, String>
{
    @Override
    public String transform(E n)
    {
        return n.name();
    }
}
