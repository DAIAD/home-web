package eu.daiad.web.repository.application;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import eu.daiad.web.domain.application.Utility;
import eu.daiad.web.model.error.ApplicationException;
import eu.daiad.web.model.error.SharedErrorCode;
import eu.daiad.web.model.utility.UtilityInfo;

@Repository
@Transactional("transactionManager")
public class JpaUtilityRepository implements IUtilityRepository {

	@PersistenceContext(unitName = "default")
	EntityManager entityManager;

	@Override
	public List<UtilityInfo> getUtilities() {
		try {
			TypedQuery<Utility> utilityQuery = entityManager.createQuery("SELECT u  FROM utility u", Utility.class)
							.setFirstResult(0);

			List<Utility> utilities = utilityQuery.getResultList();
			List<UtilityInfo> utilitiesInfo = new ArrayList<UtilityInfo>();

			for (Utility utility : utilities) {
				UtilityInfo utilityInfo = new UtilityInfo(utility);
				utilitiesInfo.add(utilityInfo);
			}

			return utilitiesInfo;

		} catch (Exception ex) {
			throw ApplicationException.wrap(ex, SharedErrorCode.UNKNOWN);
		}
	}

	@Override
	public UtilityInfo getUtilityById(int id) {
		try {
			TypedQuery<Utility> utilityQuery = entityManager.createQuery("SELECT u FROM utility u where u.id = :id",
							Utility.class).setFirstResult(0);

			utilityQuery.setParameter("id", id);

			return new UtilityInfo(utilityQuery.getSingleResult());
		} catch (Exception ex) {
			throw ApplicationException.wrap(ex, SharedErrorCode.UNKNOWN);
		}
	}

	@Override
	public UtilityInfo getUtilityByKey(UUID key) {
		try {
			TypedQuery<Utility> utilityQuery = entityManager.createQuery("SELECT u FROM utility u where u.key = :key",
							Utility.class).setFirstResult(0);

			utilityQuery.setParameter("key", key);

			return new UtilityInfo(utilityQuery.getSingleResult());
		} catch (Exception ex) {
			throw ApplicationException.wrap(ex, SharedErrorCode.UNKNOWN);
		}
	}
}