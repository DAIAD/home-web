package eu.daiad.web.model.query;

import java.util.ArrayList;
import java.util.UUID;

public class UserPopulationFilter extends PopulationFilter {

	private ArrayList<UUID> users = new ArrayList<UUID>();

	public UserPopulationFilter() {
		super();
	}

	public UserPopulationFilter(String label) {
		super(label);
	}

	public UserPopulationFilter(String label, UUID user) {
		super(label);

		users.add(user);
	}

	public UserPopulationFilter(String label, UUID[] users) {
		super(label);
		for (UUID userKey : users) {
			this.users.add(userKey);
		}
	}

	public UserPopulationFilter(String label, UUID[] users, Ranking ranking) {
		super(label, ranking);
		for (UUID userKey : users) {
			this.users.add(userKey);
		}
	}

	public UserPopulationFilter(String label, UUID[] users, EnumRankingType ranking, EnumMetric metric, int limit) {
		super(label, new Ranking(ranking, metric, limit));
		for (UUID userKey : users) {
			this.users.add(userKey);
		}
	}

	public ArrayList<UUID> getUsers() {
		return users;
	}

	@Override
	public EnumPopulationFilterType getType() {
		return EnumPopulationFilterType.USER;
	}

}
