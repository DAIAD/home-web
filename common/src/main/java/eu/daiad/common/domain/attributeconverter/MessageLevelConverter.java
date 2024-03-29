package eu.daiad.common.domain.attributeconverter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import eu.daiad.common.model.message.EnumMessageLevel;

@Converter
public class MessageLevelConverter
    implements AttributeConverter<EnumMessageLevel, Integer>
{
    @Override
    public Integer convertToDatabaseColumn(EnumMessageLevel a)
    {
        return a.getValue();
    }

    @Override
    public EnumMessageLevel convertToEntityAttribute(Integer n)
    {
        return EnumMessageLevel.valueOf(n);
    }
}
