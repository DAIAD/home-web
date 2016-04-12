package eu.daiad.web.model.query;

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

	public UUID getGroup() {
		return group;
	}

	@Override
	public EnumPopulationFilterType getType() {
		return EnumPopulationFilterType.GROUP;
	}
}
