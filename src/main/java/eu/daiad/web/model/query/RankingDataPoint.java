package eu.daiad.web.model.query;

import java.util.ArrayList;

public class RankingDataPoint extends DataPoint {

	private ArrayList<UserDataPoint> users = new ArrayList<UserDataPoint>();

	public RankingDataPoint() {
		this.type = EnumDataPointType.RANKING;
	}

	public RankingDataPoint(long timestamp) {
		super(timestamp);
		this.type = EnumDataPointType.RANKING;
	}

	public ArrayList<UserDataPoint> getUsers() {
		return users;
	}

}
