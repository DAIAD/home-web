package eu.daiad.web.model.query;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type")
@JsonSubTypes({ @Type(value = UserPopulationFilter.class, name = "USER"),
				@Type(value = GroupPopulationFilter.class, name = "GROUP"),
				@Type(value = UtilityPopulationFilter.class, name = "UTILITY") })
public abstract class PopulationFilter {

	@JsonDeserialize(using = EnumPopulationFilterType.Deserializer.class)
	private EnumPopulationFilterType type = EnumPopulationFilterType.UNDEFINED;

	private String label;

	private Ranking ranking;

	public PopulationFilter() {

	}

	public PopulationFilter(String label) {
		this.label = label;
	}

	public PopulationFilter(String label, Ranking ranking) {
		this.label = label;
		this.ranking = ranking;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public abstract EnumPopulationFilterType getType();

	public Ranking getRanking() {
		return ranking;
	}

}
