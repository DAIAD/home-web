package eu.daiad.web.data;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;

import eu.daiad.web.domain.Utility;
import eu.daiad.web.model.commons.Community;
import eu.daiad.web.model.error.ApplicationException;
import eu.daiad.web.model.error.SharedErrorCode;

@Primary
@Repository()
@Transactional()
@Scope("prototype")
public class JpaCommunityRepository implements ICommunityRepository {

	@Autowired
	EntityManager entityManager;

	@Override
	public void create(Community community) throws ApplicationException {
		try {
			TypedQuery<Utility> query = entityManager.createQuery("select u from utility u", Utility.class)
							.setFirstResult(0).setMaxResults(1);

			Utility u = query.getSingleResult();

			eu.daiad.web.domain.Community c = new eu.daiad.web.domain.Community();

			c.setCreatedOn(DateTime.now());
			c.setDescription(community.getDescription());

			if (community.getGeometry() != null) {
				community.getGeometry().setSRID(4326);
				c.setGeometry(community.getGeometry());
			}

			c.setImage(community.getImage());
			c.setLocale(community.getLocale());
			c.setName(community.getName());
			c.setSize(0);

			c.setUtility(u);

			this.entityManager.persist(c);
		} catch (Exception ex) {
			throw ApplicationException.wrap(ex, SharedErrorCode.UNKNOWN);
		}
	}

}