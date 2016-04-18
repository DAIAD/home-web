package eu.daiad.web.repository.application;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.joda.time.DateTime;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import eu.daiad.web.domain.application.Account;
import eu.daiad.web.model.commons.Community;
import eu.daiad.web.model.error.ApplicationException;
import eu.daiad.web.model.error.SharedErrorCode;
import eu.daiad.web.model.security.AuthenticatedUser;

@Repository()
@Transactional("transactionManager")
public class JpaCommunityRepository implements ICommunityRepository {

	@PersistenceContext(unitName="default")
	EntityManager entityManager;

	@Override
	public void create(Community community) throws ApplicationException {
		try {
			Authentication auth = SecurityContextHolder.getContext().getAuthentication();

			TypedQuery<Account> query = entityManager
							.createQuery("select a from account a where a.key = :key", Account.class).setFirstResult(0)
							.setMaxResults(1);
			query.setParameter("key", ((AuthenticatedUser) auth.getPrincipal()).getKey());

			Account account = query.getSingleResult();

			eu.daiad.web.domain.application.GroupCommunity c = new eu.daiad.web.domain.application.GroupCommunity();

			c.setCreatedOn(DateTime.now());
			c.setDescription(community.getDescription());

			if (community.getGeometry() != null) {
				community.getGeometry().setSRID(4326);
				c.setGeometry(community.getGeometry());
			}

			c.setImage(community.getImage());
			c.setSize(0);

			c.setUtility(account.getUtility());

			this.entityManager.persist(c);
		} catch (Exception ex) {
			throw ApplicationException.wrap(ex, SharedErrorCode.UNKNOWN);
		}
	}

}