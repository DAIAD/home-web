package eu.daiad.web.model.group;

import java.util.List;

public class Cluster extends Group {

	private List<Segment> segments;

	public List<Segment> getSegments() {
		return segments;
	}

	public void setSegments(List<Segment> segments) {
		this.segments = segments;
	}

	@Override
	public EnumGroupType getType() {
		return EnumGroupType.CLUSTER;
	}
}
