package eu.daiad.web.model.query;

public class Ranking {

	private EnumRankingType type = EnumRankingType.UNDEFINED;

	private EnumDataField field = EnumDataField.VOLUME;

	private EnumMetric metric = EnumMetric.SUM;

	private Integer limit;

	public Ranking() {

	}

	public Ranking(EnumRankingType type, int limit) {
		this.type = type;
		this.metric = EnumMetric.SUM;
		this.limit = limit;
		this.field = EnumDataField.VOLUME;
	}

	public Ranking(EnumRankingType type, EnumMetric metric, int limit) {
		this.type = type;
		this.metric = metric;
		this.limit = limit;
		this.field = EnumDataField.VOLUME;
	}

	public Ranking(EnumRankingType type, EnumMetric metric, EnumDataField field, int limit) {
		this.type = type;
		this.metric = metric;
		this.limit = limit;
		this.field = field;
	}

	public EnumRankingType getType() {
		return type;
	}

	public void setType(EnumRankingType type) {
		this.type = type;
	}

	public EnumMetric getMetric() {
		return metric;
	}

	public void setMetric(EnumMetric metric) {
		this.metric = metric;
	}

	public Integer getLimit() {
		return limit;
	}

	public void setLimit(Integer limit) {
		this.limit = limit;
	}

	public EnumDataField getField() {
		return field;
	}

	public void setField(EnumDataField field) {
		this.field = field;
	}

}
