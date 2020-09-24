package eu.daiad.common.domain.application;

import eu.daiad.common.model.message.EnumAlertType;

public class AlertByTypeRecord
{
    private final EnumAlertType type;

    private final int count;

    public AlertByTypeRecord(EnumAlertType type, Long count)
    {
        this.type = type;
        this.count = count.intValue();
    }

    public AlertByTypeRecord(String type, Long count)
    {
        this(EnumAlertType.valueOf(type), count);
    }

    public AlertByTypeRecord(int code, Long count)
    {
        this(EnumAlertType.valueOf(code), count);
    }

    public EnumAlertType getType()
    {
        return type;
    }

    public int getCount()
    {
        return count;
    }
}
