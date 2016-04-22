package eu.daiad.web.model.query;

import java.util.UUID;

public class UtilityPopulationFilter extends PopulationFilter {

	private UUID utility;

	public UtilityPopulationFilter() {
		super();
	}

	public UtilityPopulationFilter(String label) {
		super(label);
	}

	public UtilityPopulationFilter(String label, UUID utility) {
		super(label);

		this.utility = utility;
	}

	public UtilityPopulationFilter(String label, UUID utility, Ranking ranking) {
		super(label, ranking);

		this.utility = utility;
	}

	public UtilityPopulationFilter(String label, UUID utility, EnumRankingType ranking, EnumMetric metric, int limit) {
		super(label, new Ranking(ranking, metric, limit));

		this.utility = utility;
	}

	public UUID getUtility() {
		return utility;
	}

	@Override
	public EnumPopulationFilterType getType() {
		return EnumPopulationFilterType.UTILITY;
	}

}
