package eu.daiad.common.model.query;

import java.util.UUID;

public class GroupPopulationFilter extends PopulationFilter {

	private UUID group;

	public GroupPopulationFilter() {
		super();
	}

	public GroupPopulationFilter(String label) {
		super(label);
	}

	public GroupPopulationFilter(String label, UUID group) {
		super(label);

		this.group = group;
	}

	public GroupPopulationFilter(String label, UUID group, Ranking ranking) {
		super(label, ranking);

		this.group = group;
	}

	public GroupPopulationFilter(String label, UUID group, EnumRankingType ranking, EnumMetric metric, int limit) {
		super(label, new Ranking(ranking, metric, limit));

		this.group = group;
	}

	public UUID getGroup() {
		return group;
	}

	@Override
	public EnumPopulationFilterType getType() {
		return EnumPopulationFilterType.GROUP;
	}
}
