package eu.daiad.web.data;

import java.util.ArrayList;

import eu.daiad.web.model.error.ApplicationException;
import eu.daiad.web.model.recommendation.Recommendation;

public interface IRecommendationRepository {

	public abstract ArrayList<Recommendation> getStaticRecommendations(String locale) throws ApplicationException;

}
