package eu.daiad.web.repository.application;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import eu.daiad.web.model.error.ApplicationException;
import eu.daiad.web.model.error.SharedErrorCode;
import eu.daiad.web.model.recommendation.Recommendation;
import eu.daiad.web.model.recommendation.RecommendationStatic;

@Repository
@Transactional
public class JpaRecommendationRepository implements IRecommendationRepository {

	@Autowired
	private ApplicationContext ctx;

	@PersistenceContext(unitName="default")
	EntityManager entityManager;

	@Override
	public ArrayList<Recommendation> getStaticRecommendations(String locale) throws ApplicationException {
		ArrayList<Recommendation> recommendations = new ArrayList<Recommendation>();

		try {
			TypedQuery<eu.daiad.web.domain.application.StaticRecommendation> query = entityManager.createQuery(
							"select r from static_recommendation r where r.locale = :locale",
							eu.daiad.web.domain.application.StaticRecommendation.class);
			query.setParameter("locale", locale);

			List<eu.daiad.web.domain.application.StaticRecommendation> result = query.getResultList();

			for (eu.daiad.web.domain.application.StaticRecommendation r : result) {
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
			throw ApplicationException.wrap(ex, SharedErrorCode.UNKNOWN);
		}
	}

}
