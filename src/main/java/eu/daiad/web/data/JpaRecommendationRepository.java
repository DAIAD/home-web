package eu.daiad.web.data;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;

import eu.daiad.web.model.recommendation.Recommendation;
import eu.daiad.web.model.recommendation.RecommendationStatic;

@Repository
@Scope("prototype")
public class JpaRecommendationRepository implements IRecommendationRepository {

	private static final Log logger = LogFactory.getLog(JpaRecommendationRepository.class);

	@Autowired
	private ApplicationContext ctx;

	@Autowired
	EntityManager entityManager;

	@Override
	public ArrayList<Recommendation> getStaticRecommendations(String locale) throws Exception {
		ArrayList<Recommendation> recommendations = new ArrayList<Recommendation>();

		try {
			TypedQuery<eu.daiad.web.domain.StaticRecommendation> query = entityManager.createQuery(
							"select r from static_recommendation r where r.locale = :locale",
							eu.daiad.web.domain.StaticRecommendation.class);
			query.setParameter("locale", locale);

			List<eu.daiad.web.domain.StaticRecommendation> result = query.getResultList();

			for (eu.daiad.web.domain.StaticRecommendation r : result) {
				RecommendationStatic recommendationStatic = new RecommendationStatic();

				recommendationStatic.setCategory(r.getCategory().getId());
				recommendationStatic.setDescription(r.getDescription());
				recommendationStatic.setExternaLink(r.getExternaLink());
				recommendationStatic.setId(r.getIndex());
				recommendationStatic.setImage(r.getImage());
				recommendationStatic.setImageLink(r.getImageLink());
				recommendationStatic.setPrompt(r.getPrompt());
				recommendationStatic.setTitle(r.getTitle());
				
				recommendations.add(recommendationStatic);
			}

			return recommendations;
		} catch (Exception ex) {
			logger.error(ex);

			throw new RuntimeException(ex);
		}
	}

}
