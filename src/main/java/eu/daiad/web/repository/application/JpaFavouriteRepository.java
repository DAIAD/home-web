package eu.daiad.web.repository.application;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.joda.time.DateTime;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import eu.daiad.web.domain.application.Account;
import eu.daiad.web.domain.application.Favourite;
import eu.daiad.web.domain.application.FavouriteAccount;
import eu.daiad.web.domain.application.FavouriteGroup;
import eu.daiad.web.domain.application.Group;
import eu.daiad.web.model.error.FavouriteErrorCode;
import eu.daiad.web.model.error.GroupErrorCode;
import eu.daiad.web.model.error.SharedErrorCode;
import eu.daiad.web.model.error.UserErrorCode;
import eu.daiad.web.model.favourite.CandidateFavouriteAccountInfo;
import eu.daiad.web.model.favourite.CandidateFavouriteGroupInfo;
import eu.daiad.web.model.favourite.EnumFavouriteType;
import eu.daiad.web.model.favourite.FavouriteAccountInfo;
import eu.daiad.web.model.favourite.FavouriteGroupInfo;
import eu.daiad.web.model.favourite.FavouriteInfo;
import eu.daiad.web.model.favourite.UpsertFavouriteRequest;
import eu.daiad.web.model.security.AuthenticatedUser;
import eu.daiad.web.repository.BaseRepository;

@Repository
@Transactional("transactionManager")
public class JpaFavouriteRepository extends BaseRepository implements IFavouriteRepository {

	@PersistenceContext(unitName = "default")
	EntityManager entityManager;

	@Override
	public List<FavouriteInfo> getFavourites() {
		try {
			Authentication auth = SecurityContextHolder.getContext().getAuthentication();
			AuthenticatedUser user = (AuthenticatedUser) auth.getPrincipal();

			// Retrieve admin's account
			TypedQuery<Account> accountQuery = entityManager
							.createQuery("SELECT a FROM account a WHERE a.key = :key", Account.class).setFirstResult(0)
							.setMaxResults(1);
			accountQuery.setParameter("key", user.getKey());

			Account adminAccount = accountQuery.getSingleResult();

			TypedQuery<Favourite> favouriteQuery = entityManager.createQuery(
							"SELECT f FROM favourite f WHERE f.owner = :owner", Favourite.class).setFirstResult(0);
			favouriteQuery.setParameter("owner", adminAccount);

			List<Favourite> favourites = favouriteQuery.getResultList();
			List<FavouriteInfo> favouritesInfo = new ArrayList<FavouriteInfo>();

			for (Favourite favourite : favourites) {
				FavouriteInfo favouriteInfo = new FavouriteInfo(favourite);
				favouritesInfo.add(favouriteInfo);
			}

			return favouritesInfo;
		} catch (Exception ex) {
			throw wrapApplicationException(ex, SharedErrorCode.UNKNOWN);
		}
	}

	@Override
	public FavouriteAccountInfo checkFavouriteAccount(UUID account_id) {
		try {
			Authentication auth = SecurityContextHolder.getContext().getAuthentication();
			AuthenticatedUser user = (AuthenticatedUser) auth.getPrincipal();

			FavouriteAccountInfo favouriteAccountInfo = null;

			// Retrieve admin's account
			TypedQuery<Account> adminAccountQuery = entityManager
							.createQuery("SELECT a FROM account a WHERE a.key = :key", Account.class).setFirstResult(0)
							.setMaxResults(1);
			adminAccountQuery.setParameter("key", user.getKey());

			Account adminAccount = adminAccountQuery.getSingleResult();

			TypedQuery<Favourite> favouriteQuery = entityManager.createQuery(
							"SELECT f FROM favourite f WHERE f.owner = :owner", Favourite.class).setFirstResult(0);
			favouriteQuery.setParameter("owner", adminAccount);

			List<Favourite> favourites = favouriteQuery.getResultList();

			for (Favourite favourite : favourites) {
				if (favourite.getType() == EnumFavouriteType.ACCOUNT) {
					FavouriteAccount favouriteAccount = (FavouriteAccount) favourite;
					if (favouriteAccount.getAccount().getKey().equals(account_id)) {
						favouriteAccountInfo = new FavouriteAccountInfo(favouriteAccount);
					}
				}
			}

			// If the given account does not match with any existing favourite
			// we try to retrieve it
			// in order to send a CandidateFavouriteAccountInfo Object
			if (favouriteAccountInfo == null) {
				try {
					TypedQuery<Account> accountQuery = entityManager
									.createQuery("SELECT a FROM account a WHERE a.key = :key", Account.class)
									.setFirstResult(0).setMaxResults(1);
					accountQuery.setParameter("key", account_id);

					Account account = accountQuery.getSingleResult();

					return new CandidateFavouriteAccountInfo(account);

				} catch (NoResultException ex) {
					throw wrapApplicationException(ex, UserErrorCode.USERID_NOT_FOUND).set("account_id", account_id);
				}
			}

			return favouriteAccountInfo;
		} catch (Exception ex) {
			throw wrapApplicationException(ex, SharedErrorCode.UNKNOWN);
		}
	}

	@Override
	public FavouriteGroupInfo checkFavouriteGroup(UUID group_id) {
		try {
			Authentication auth = SecurityContextHolder.getContext().getAuthentication();
			AuthenticatedUser user = (AuthenticatedUser) auth.getPrincipal();

			FavouriteGroupInfo favouriteGroupInfo = null;

			// Retrieve admin's account
			TypedQuery<Account> accountQuery = entityManager
							.createQuery("SELECT a FROM account a WHERE a.key = :key", Account.class).setFirstResult(0)
							.setMaxResults(1);
			accountQuery.setParameter("key", user.getKey());

			Account adminAccount = accountQuery.getSingleResult();

			TypedQuery<Favourite> favouriteQuery = entityManager.createQuery(
							"SELECT f FROM favourite f WHERE f.owner = :owner", Favourite.class).setFirstResult(0);
			favouriteQuery.setParameter("owner", adminAccount);

			List<Favourite> favourites = favouriteQuery.getResultList();

			for (Favourite favourite : favourites) {
				if (favourite.getType() == EnumFavouriteType.GROUP) {
					FavouriteGroup favouriteGroup = (FavouriteGroup) favourite;
					if (favouriteGroup.getGroup().getKey().equals(group_id)) {
						favouriteGroupInfo = new FavouriteGroupInfo(favouriteGroup);
					}
				}
			}

			// If the given group does not match with any existing favourite we
			// try to retrieve it
			// in order to send a CandidateFavouriteGroupInfo Object
			if (favouriteGroupInfo == null) {
				try {
					TypedQuery<Group> groupQuery = entityManager
									.createQuery("SELECT g FROM group g WHERE g.key = :key", Group.class)
									.setFirstResult(0).setMaxResults(1);
					groupQuery.setParameter("key", group_id);

					Group group = groupQuery.getSingleResult();

					return new CandidateFavouriteGroupInfo(group);

				} catch (NoResultException ex) {
					throw wrapApplicationException(ex, GroupErrorCode.GROUP_DOES_NOT_EXIST).set("groupId", group_id);
				}
			}

			return favouriteGroupInfo;
		} catch (Exception ex) {
			throw wrapApplicationException(ex, SharedErrorCode.UNKNOWN);
		}

	}

	@Override
	public void upsertFavourite(UpsertFavouriteRequest favouriteInfo) {
		try {
			Authentication auth = SecurityContextHolder.getContext().getAuthentication();
			AuthenticatedUser user = (AuthenticatedUser) auth.getPrincipal();

			// Retrieve admin's account
			TypedQuery<Account> adminAccountQuery = entityManager
							.createQuery("SELECT a FROM account a WHERE a.key = :key", Account.class).setFirstResult(0)
							.setMaxResults(1);
			adminAccountQuery.setParameter("key", user.getKey());

			Account adminAccount = adminAccountQuery.getSingleResult();

			// Checking if the Favourite's type is invalid
			if (favouriteInfo.getType() != EnumFavouriteType.GROUP
							&& favouriteInfo.getType() != EnumFavouriteType.ACCOUNT) {
				throw createApplicationException(FavouriteErrorCode.INVALID_FAVOURITE_TYPE);
			}

			if (favouriteInfo.getType() == EnumFavouriteType.ACCOUNT) {
				// checking if the Account exists at all
				Account account = null;
				try {
					TypedQuery<Account> accountQuery = entityManager
									.createQuery("SELECT a FROM account a WHERE a.key = :key", Account.class)
									.setFirstResult(0).setMaxResults(1);
					accountQuery.setParameter("key", favouriteInfo.getKey());

					account = accountQuery.getSingleResult();
				} catch (NoResultException ex) {
					throw wrapApplicationException(ex, UserErrorCode.USERID_NOT_FOUND).set("accountId",
									favouriteInfo.getKey());
				}

				// Checking if the selected account belongs to the same utility
				// as admin
				if (account.getUtility().getId() != adminAccount.getUtility().getId()) {
					throw createApplicationException(UserErrorCode.ACCOUNT_ACCESS_RESTRICTED);
				}

				// Checking if the account is already an admin's favourite
				TypedQuery<Favourite> favouriteQuery = entityManager.createQuery(
								"SELECT f FROM favourite f WHERE f.owner = :owner", Favourite.class).setFirstResult(0);
				favouriteQuery.setParameter("owner", adminAccount);

				List<Favourite> favourites = favouriteQuery.getResultList();

				FavouriteAccount selectedFavourite = null;
				for (Favourite favourite : favourites) {
					if (favourite.getType() == EnumFavouriteType.ACCOUNT) {
						FavouriteAccount favouriteAccount = (FavouriteAccount) favourite;
						if (favouriteAccount.getAccount().getId() == account.getId()) {
							selectedFavourite = favouriteAccount;
						}
					}
				}

				// Favourite already exists just set the label
				if (selectedFavourite != null) {
					selectedFavourite.setLabel(favouriteInfo.getLabel());

				} else {
					selectedFavourite = new FavouriteAccount();
					selectedFavourite.setLabel(favouriteInfo.getLabel());
					selectedFavourite.setOwner(adminAccount);
					selectedFavourite.setCreatedOn(new DateTime());
					selectedFavourite.setAccount(account);
				}

				this.entityManager.persist(selectedFavourite);
			} else {
				// checking if the Group exists at all
				Group group = null;
				try {
					TypedQuery<Group> groupQuery = entityManager
									.createQuery("SELECT g FROM group g WHERE g.key = :key", Group.class)
									.setFirstResult(0).setMaxResults(1);
					groupQuery.setParameter("key", favouriteInfo.getKey());

					group = groupQuery.getSingleResult();
				} catch (NoResultException ex) {
					throw wrapApplicationException(ex, GroupErrorCode.GROUP_DOES_NOT_EXIST).set("groupId",
									favouriteInfo.getKey());
				}

				// Checking if the selected group belongs to the same utility as
				// admin
				if (group.getUtility().getId() != adminAccount.getUtility().getId()) {
					throw createApplicationException(GroupErrorCode.GROUP_ACCESS_RESTRICTED);
				}

				// Checking if the group is already an admin's favourite
				TypedQuery<Favourite> favouriteQuery = entityManager.createQuery(
								"SELECT f FROM favourite f WHERE f.owner = :owner", Favourite.class).setFirstResult(0);
				favouriteQuery.setParameter("owner", adminAccount);

				List<Favourite> favourites = favouriteQuery.getResultList();

				FavouriteGroup selectedFavourite = null;
				for (Favourite favourite : favourites) {
					if (favourite.getType() == EnumFavouriteType.GROUP) {
						FavouriteGroup favouriteGroup = (FavouriteGroup) favourite;
						if (favouriteGroup.getGroup().getId() == group.getId()) {
							selectedFavourite = favouriteGroup;
						}
					}
				}

				// Favourite already exists just set the label
				if (selectedFavourite != null) {
					selectedFavourite.setLabel(favouriteInfo.getLabel());

				} else {
					selectedFavourite = new FavouriteGroup();
					selectedFavourite.setLabel(favouriteInfo.getLabel());
					selectedFavourite.setOwner(adminAccount);
					selectedFavourite.setCreatedOn(new DateTime());
					selectedFavourite.setGroup(group);
				}

				this.entityManager.persist(selectedFavourite);
			}

		} catch (Exception ex) {
			throw wrapApplicationException(ex, SharedErrorCode.UNKNOWN);
		}

	}

	@Override
	public void deleteFavourite(UUID favourite_id) {
		try {
			Authentication auth = SecurityContextHolder.getContext().getAuthentication();
			AuthenticatedUser user = (AuthenticatedUser) auth.getPrincipal();

			if (!user.hasRole("ROLE_ADMIN") && !user.hasRole("ROLE_SUPERUSER")) {
				throw createApplicationException(SharedErrorCode.AUTHORIZATION);
			}

			Favourite favourite = null;
			// Check if favourite exists
			try {
				TypedQuery<Favourite> favouriteQuery = entityManager
								.createQuery("select f from favourite f where f.key = :favourite_id", Favourite.class)
								.setFirstResult(0).setMaxResults(1);
				favouriteQuery.setParameter("favourite_id", favourite_id);
				favourite = favouriteQuery.getSingleResult();

				// Check that admin is the owner of the group
				// Get admin's account
				TypedQuery<eu.daiad.web.domain.application.Account> adminAccountQuery = entityManager
								.createQuery("select a from account a where a.id = :adminId",
												eu.daiad.web.domain.application.Account.class).setFirstResult(0)
								.setMaxResults(1);
				adminAccountQuery.setParameter("adminId", user.getId());
				Account adminAccount = adminAccountQuery.getSingleResult();

				if (favourite.getOwner() == adminAccount) {
					this.entityManager.remove(favourite);

				} else {
					throw createApplicationException(FavouriteErrorCode.FAVOURITE_ACCESS_RESTRICTED).set("favouriteId",
									favourite_id);
				}

			} catch (NoResultException ex) {
				throw wrapApplicationException(ex, FavouriteErrorCode.FAVOURITE_DOES_NOT_EXIST).set("favouriteId",
								favourite_id);
			}

		} catch (Exception ex) {
			throw wrapApplicationException(ex, SharedErrorCode.UNKNOWN);
		}
	}
}
