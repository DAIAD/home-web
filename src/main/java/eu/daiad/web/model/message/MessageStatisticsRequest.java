package eu.daiad.web.model.message;

import eu.daiad.web.model.AuthenticatedRequest;

public class MessageStatisticsRequest extends AuthenticatedRequest 
{    
    private MessageStatisticsQuery query;

    public MessageStatisticsQuery getQuery() {
        return query;
    }

    public void setQuery(MessageStatisticsQuery query) {
        this.query = query;
    }
    
}
