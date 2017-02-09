package eu.daiad.web.model;

public class PagingOptions
{
    private int size = -1;
    private int offset = 0;
    private boolean ascending = true;

    public PagingOptions(int limit, int offset, boolean ascending)
    {
        size = limit;
        this.offset = offset;
        this.ascending = ascending;
    }

    public PagingOptions(int limit)
    {
        size = limit;
    }

    public PagingOptions() {}

    public int getSize()
    {
        return size;
    }

    public void setSize(int limit)
    {
        size = limit;
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

    @Override
    public String toString()
    {
        return String.format("PagingOptions(size=%d, offset=%d, ascending=%s)", offset, size, ascending? "T" : "F");
    }
}
