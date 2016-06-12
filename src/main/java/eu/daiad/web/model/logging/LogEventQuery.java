package eu.daiad.web.model.logging;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

public class LogEventQuery {

	private Integer index = 0;

	private Integer size = 10;

	@JsonDeserialize(using = EnumLevel.Deserializer.class)
	private EnumLevel level = EnumLevel.UNDEFINED;

	private String account;

	private Long startDate;

	private Long endDate;

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

	public EnumLevel getLevel() {
		if (level == null) {
			return EnumLevel.UNDEFINED;
		}
		return level;
	}

	public void setLevel(EnumLevel level) {
		this.level = level;
	}

	public String getAccount() {
		return account;
	}

	public void setAccount(String account) {
		this.account = account;
	}

	public Long getStartDate() {
		return startDate;
	}

	public void setStartDate(Long startDate) {
		this.startDate = startDate;
	}

	public Long getEndDate() {
		return endDate;
	}

	public void setEndDate(Long endDate) {
		this.endDate = endDate;
	}
}
