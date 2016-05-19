package eu.daiad.web.model.message;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import eu.daiad.web.model.AuthenticatedRequest;

public class MessageRequest extends AuthenticatedRequest {

	private DataPagingOptions[] pagination;

	public DataPagingOptions[] getPagination() {
		return pagination;
	}

	public void setPagination(DataPagingOptions[] pagination) {
		this.pagination = pagination;
	}

	public static class DataPagingOptions {

		@JsonDeserialize(using = EnumMessageType.Deserializer.class)
		private EnumMessageType type;

		private Integer minMessageId;

		private Integer index;

		private Integer size;

		private Boolean ascending;

		public EnumMessageType getType() {
			return type;
		}

		public void setType(EnumMessageType type) {
			this.type = type;
		}

		public Integer getIndex() {
			return index;
		}

		public void setIndex(Integer index) {
			this.index = index;
		}

		public Integer getSize() {
			return size;
		}

		public void setSize(Integer size) {
			this.size = size;
		}

		public Integer getMinMessageId() {
			if (this.minMessageId == null) {
				return -1;
			}
			
			return minMessageId;
		}

		public void setMinMessageId(Integer minMessageId) {
			this.minMessageId = minMessageId;
		}

		public void setAscending(Boolean ascending) {
			this.ascending = ascending;
		}

		public Boolean getAscending() {
			if (this.ascending == null) {
				return true;
			}
			
			return ascending;
		}
	}

}
