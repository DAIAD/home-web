package eu.daiad.web.model.recommendation;

import java.util.ArrayList;

import eu.daiad.web.model.RestResponse;

public class StaticRecommendationResponse extends RestResponse {

	ArrayList<Recommendation> recommendations = new ArrayList<Recommendation>();

	public ArrayList<Recommendation> getRecommendations() {
		return recommendations;
	}

	public void setRecommendations(ArrayList<Recommendation> recommendations) {
		this.recommendations = recommendations;
	}
	
	
}
