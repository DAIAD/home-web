package eu.daiad.web.util.csv;

public interface RecordMapper <T>
{
    public String toLine(T obj) throws Exception;
    
    public String toHeaderLine();
    
    public T fromLine(String line) throws Exception;
}
