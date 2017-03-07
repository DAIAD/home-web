package eu.daiad.web.model.message;

import java.util.Locale;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import eu.daiad.web.model.AuthenticatedRequest;
import eu.daiad.web.model.PagingOptions;

public class MessageRequest extends AuthenticatedRequest
{
    public static class Options
    {
        public static final int DEFAULT_PAGE_SIZE = 20;

        @JsonDeserialize(using = EnumMessageType.Deserializer.class)
        private EnumMessageType type;

        private int minMessageId = -1;

        private PagingOptions pagination = new PagingOptions(DEFAULT_PAGE_SIZE);

        public Options() {}
        
        public Options(EnumMessageType type, int pageSize) 
        {
            this.type = type;
            this.pagination = new PagingOptions(pageSize);
        }
        
        public EnumMessageType getType() {
            return type;
        }

        public void setType(EnumMessageType type) {
            this.type = type;
        }

        public PagingOptions getPagination() {
            return pagination;
        }

        public void setPagination(PagingOptions pagination)
        {
            this.pagination = new PagingOptions(
                (pagination.getSize() > 0)? pagination.getSize() : DEFAULT_PAGE_SIZE,
                pagination.getOffset(),
                pagination.isAscending());
        }

        public int getMinMessageId()
        {
            return minMessageId;
        }

        public void setMinMessageId(int minMessageId)
        {
            this.minMessageId = minMessageId;
        }
    }

    @JsonIgnore
    private Locale locale;
    
    @JsonIgnore
    private Options[] options = new Options[0];

    public MessageRequest()
    {}
    
    public MessageRequest(String language)
    {
        setLocale(language);
    }
    
    @JsonProperty("messages")
    public Options[] getOptions() {
        return options;
    }
    
    public Options getOptionsForType(EnumMessageType type)
    {
        for (Options o: options)
            if (o.type == type)
                return o;
        return null;
    }

    @JsonProperty("messages")
    public void setOptions(Options[] options)
    {
        if (options != null && options.length > 0)
            this.options = options;
    }

    @JsonIgnore
    public void setOptions(Options o1)
    {
        if (o1 != null)
            options = new Options[] { o1 };
    }
    
    public MessageRequest withOptions(Options[] options)
    {
        setOptions(options);
        return this;
    }
    
    public MessageRequest withOptions(Options options)
    {
        setOptions(options);
        return this;
    }
    
    @JsonProperty("locale")
    public String getLanguage()
    {
        return locale == null? null : locale.getLanguage();
    }
    
    @JsonIgnore
    public Locale getLocale()
    {
        return locale;
    }
    
    @JsonProperty("locale")
    public void setLocale(String language)
    {
        locale = Locale.forLanguageTag(language);
    }
    
    // Note: Support for API backwards compatibility
    @JsonProperty("pagination")
    public void setPagination(Options[] options)
    {
        this.options = (options == null)? (new Options[0]) : options;
    }
}
