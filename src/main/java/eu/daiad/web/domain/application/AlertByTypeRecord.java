package eu.daiad.web.domain.application;

import eu.daiad.web.model.message.EnumAlertType;

public class AlertByTypeRecord
{
    private final EnumAlertType type;

    private final long count;

    public AlertByTypeRecord(EnumAlertType type, long count)
    {
        this.type = type;
        this.count = count;
    }

    public AlertByTypeRecord(String type, long count)
    {
        this(EnumAlertType.valueOf(type), count);
    }

    public AlertByTypeRecord(int code, long count)
    {
        this(EnumAlertType.valueOf(code), count);
    }

    public EnumAlertType getType()
    {
        return type;
    }

    public long getCount()
    {
        return count;
    }
}
