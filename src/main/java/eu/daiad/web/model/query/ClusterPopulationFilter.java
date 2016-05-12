package eu.daiad.web.model.query;

import java.util.UUID;

public class ClusterPopulationFilter extends PopulationFilter {

	private UUID cluster;

	private String name;

	private EnumClusterType clusterType;

	public ClusterPopulationFilter() {
		super();
	}

	public ClusterPopulationFilter(String label, UUID cluster) {
		super(label);

		this.cluster = cluster;
	}

	public ClusterPopulationFilter(String label, UUID cluster, Ranking ranking) {
		super(label, ranking);

		this.cluster = cluster;
	}

	public ClusterPopulationFilter(String label, UUID cluster, EnumRankingType ranking, EnumMetric metric, int limit) {
		super(label, new Ranking(ranking, metric, limit));

		this.cluster = cluster;
	}

	public ClusterPopulationFilter(String label, String name) {
		super(label);

		this.name = name;
	}

	public ClusterPopulationFilter(String label, String name, Ranking ranking) {
		super(label, ranking);

		this.name = name;
	}

	public ClusterPopulationFilter(String label, String name, EnumRankingType ranking, EnumMetric metric, int limit) {
		super(label, new Ranking(ranking, metric, limit));

		this.name = name;
	}

	public ClusterPopulationFilter(String label, EnumClusterType clusterType) {
		super(label);

		this.clusterType = clusterType;
	}

	public ClusterPopulationFilter(String label, EnumClusterType clusterType, Ranking ranking) {
		super(label, ranking);

		this.clusterType = clusterType;
	}

	public ClusterPopulationFilter(String label, EnumClusterType clusterType, EnumRankingType ranking,
					EnumMetric metric, int limit) {
		super(label, new Ranking(ranking, metric, limit));

		this.clusterType = clusterType;
	}

	@Override
	public EnumPopulationFilterType getType() {
		return EnumPopulationFilterType.CLUSTER;
	}

	public UUID getCluster() {
		return cluster;
	}

	public String getName() {
		return name;
	}

	public EnumClusterType getClusterType() {
		return clusterType;
	}
}
