package eu.daiad.common.model.message;

import eu.daiad.common.model.AuthenticatedRequest;

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
