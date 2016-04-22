package eu.daiad.web.repository.application;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import eu.daiad.web.domain.application.Group;
import eu.daiad.web.domain.application.Utility;
import eu.daiad.web.model.error.ApplicationException;
import eu.daiad.web.model.error.SharedErrorCode;
import eu.daiad.web.model.group.GroupInfo;
import eu.daiad.web.model.security.AuthenticatedUser;

@Repository
@Transactional("transactionManager")
public class JpaGroupRepository implements IGroupRepository{
	
	@PersistenceContext(unitName="default")
	EntityManager entityManager;

	@Override
	public List<GroupInfo> getGroups() {
		try{
			Authentication auth = SecurityContextHolder.getContext().getAuthentication();
			AuthenticatedUser user = (AuthenticatedUser) auth.getPrincipal();
			
			if (!user.hasRole("ROLE_ADMIN")) {
				throw new ApplicationException(SharedErrorCode.AUTHORIZATION);
			}
			
			TypedQuery<Utility> utilityQuery = entityManager.createQuery(
					"SELECT u FROM utility u WHERE u.id = :admin_utility_id",
					Utility.class).setFirstResult(0).setMaxResults(1);
			utilityQuery.setParameter("admin_utility_id", user.getUtilityId());
			
			Utility adminUtility = utilityQuery.getSingleResult();
			
			
			TypedQuery<Group> groupQuery = entityManager.createQuery(
					"SELECT g FROM group g WHERE g.utility = :utility",
					Group.class).setFirstResult(0);
			groupQuery.setParameter("utility", adminUtility);
			
			List <Group> groups = groupQuery.getResultList();
			List <GroupInfo> groupsInfo = new ArrayList <GroupInfo>();
			
			for (Group group : groups){
				GroupInfo groupInfo = new GroupInfo(group);
				groupsInfo.add(groupInfo);
			}

			return groupsInfo;
			
		} catch (Exception ex) {
			throw ApplicationException.wrap(ex, SharedErrorCode.UNKNOWN);
		}
	}
	
}