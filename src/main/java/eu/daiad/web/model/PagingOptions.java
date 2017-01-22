package eu.daiad.web.model;

public class PagingOptions
{
    private int limit = -1;
    private int offset = 0;
    private boolean ascending = true;

    public PagingOptions(int limit, int offset, boolean ascending)
    {
        this.limit = limit;
        this.offset = offset;
        this.ascending = ascending;
    }

    public PagingOptions(int limit)
    {
        this.limit = limit;
    }

    public PagingOptions() {}

    public int getLimit()
    {
        return limit;
    }

    public void setLimit(int limit)
    {
        this.limit = limit;
    }

    public int getOffset()
    {
        return offset;
    }

    public void setOffset(int offset)
    {
        this.offset = offset;
    }

    public boolean isAscending()
    {
        return ascending;
    }

    public void setAscending(boolean ascending)
    {
        this.ascending = ascending;
    }
}
