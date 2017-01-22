package eu.daiad.web.model.message;

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
                (pagination.getLimit() > 0)? pagination.getLimit() : DEFAULT_PAGE_SIZE,
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

    private Options[] messages;

	public Options[] getMessages() {
		return messages;
	}

	public void setMessages(Options[] messages)
	{
		this.messages = (messages == null)? (new Options[0]) : messages;
	}
}
