package eu.daiad.web.model.message;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import eu.daiad.web.model.AuthenticatedRequest;
import eu.daiad.web.model.PagingOptions;

public class MessageRequest extends AuthenticatedRequest
{
	private Options[] messages;

	public Options[] getMessages() {
		return messages;
	}

	public void setMessages(Options[] messages)
	{
		this.messages = (messages == null)? (new Options[0]) : messages;
	}

	public static class Options {

	    public static final int DEFAULT_SIZE = 20;

		@JsonDeserialize(using = EnumMessageType.Deserializer.class)
		private EnumMessageType type;

		private Integer minMessageId;

		private PagingOptions pagination;

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
		        (pagination.getLimit() > 0)? pagination.getLimit() : DEFAULT_SIZE,
		        pagination.getOffset(),
		        pagination.isAscending()
		    );
		}

		public int getMinMessageId() {
			if (minMessageId == null)
				return -1;
			return minMessageId;
		}

		public void setMinMessageId(Integer minMessageId) {
			this.minMessageId = minMessageId;
		}
	}

}
