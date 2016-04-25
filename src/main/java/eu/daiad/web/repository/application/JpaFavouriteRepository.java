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

import eu.daiad.web.domain.application.Account;
import eu.daiad.web.domain.application.Favourite;
import eu.daiad.web.model.error.ApplicationException;
import eu.daiad.web.model.error.SharedErrorCode;
import eu.daiad.web.model.favourite.FavouriteInfo;
import eu.daiad.web.model.security.AuthenticatedUser;

@Repository
@Transactional("transactionManager")
public class JpaFavouriteRepository implements IFavouriteRepository {
	
	@PersistenceContext(unitName="default")
	EntityManager entityManager;

	@Override
	public List<FavouriteInfo> getFavourites() {
		try{
			Authentication auth = SecurityContextHolder.getContext().getAuthentication();
			AuthenticatedUser user = (AuthenticatedUser) auth.getPrincipal();
			
			//Retrieve admin's account
			TypedQuery<Account> accountQuery = entityManager.createQuery(
					"SELECT a FROM account a WHERE a.key = :key",
					Account.class).setFirstResult(0).setMaxResults(1);
			accountQuery.setParameter("key", user.getKey());
			
			Account adminAccount = accountQuery.getSingleResult();
			
			TypedQuery<Favourite> favouriteQuery = entityManager.createQuery(
					"SELECT f FROM favourite f WHERE f.owner = :owner",
					Favourite.class).setFirstResult(0);
			favouriteQuery.setParameter("owner", adminAccount);
			
			List <Favourite> favourites = favouriteQuery.getResultList();
			List <FavouriteInfo> favouritesInfo = new ArrayList <FavouriteInfo>();
			
			for (Favourite favourite : favourites){
				FavouriteInfo favouriteInfo = new FavouriteInfo(favourite);
				favouritesInfo.add(favouriteInfo);
			}
	
			return favouritesInfo;
		} catch (Exception ex) {
			throw ApplicationException.wrap(ex, SharedErrorCode.UNKNOWN);
		}
	}
}