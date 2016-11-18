package eu.daiad.web.repository.application;

import java.util.ArrayList;
import java.util.Arrays;
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

import eu.daiad.web.domain.application.AccountEntity;
import eu.daiad.web.domain.application.FavouriteEntity;
import eu.daiad.web.domain.application.FavouriteGroupEntity;
import eu.daiad.web.domain.application.GroupEntity;
import eu.daiad.web.domain.application.GroupMemberEntity;
import eu.daiad.web.domain.application.GroupSetEntity;
import eu.daiad.web.domain.application.UtilityEntity;
import eu.daiad.web.model.admin.AccountActivity;
import eu.daiad.web.model.error.GroupErrorCode;
import eu.daiad.web.model.error.SharedErrorCode;
import eu.daiad.web.model.favourite.EnumFavouriteType;
import eu.daiad.web.model.group.CreateGroupSetRequest;
import eu.daiad.web.model.group.EnumGroupType;
import eu.daiad.web.model.group.GroupInfo;
import eu.daiad.web.model.group.GroupMemberInfo;
import eu.daiad.web.model.security.AuthenticatedUser;
import eu.daiad.web.model.security.EnumRole;
import eu.daiad.web.repository.BaseRepository;

@Repository
@Transactional("applicationTransactionManager")
public class JpaUserGroupRepository extends BaseRepository implements IUserGroupRepository {

	@PersistenceContext(unitName = "default")
	EntityManager entityManager;

	@Override
	public List<GroupInfo> getGroups() {
		try {
			Authentication auth = SecurityContextHolder.getContext().getAuthentication();
			AuthenticatedUser user = (AuthenticatedUser) auth.getPrincipal();

            if (!user.hasRole(EnumRole.ROLE_SYSTEM_ADMIN, EnumRole.ROLE_UTILITY_ADMIN)) {
				throw createApplicationException(SharedErrorCode.AUTHORIZATION);
			}

			TypedQuery<GroupSetEntity> groupQuery = entityManager.createQuery(
							"SELECT g FROM group_set g WHERE g.utility.id = :utility_id", GroupSetEntity.class);
			groupQuery.setParameter("utility_id", user.getUtilityId());

			List<GroupSetEntity> groups = groupQuery.getResultList();
			List<GroupInfo> groupsInfo = new ArrayList<GroupInfo>();

			for (GroupSetEntity group : groups) {
				GroupInfo groupInfo = new GroupInfo(group);
				groupsInfo.add(groupInfo);
			}

			return groupsInfo;

		} catch (Exception ex) {
			throw wrapApplicationException(ex, SharedErrorCode.UNKNOWN);
		}
	}

	@Override
	public List<GroupMemberInfo> getGroupCurrentMembers(UUID group_id) {
		try {
			Authentication auth = SecurityContextHolder.getContext().getAuthentication();
			AuthenticatedUser user = (AuthenticatedUser) auth.getPrincipal();

			if (!user.hasRole(EnumRole.ROLE_SYSTEM_ADMIN, EnumRole.ROLE_UTILITY_ADMIN)) {
				throw createApplicationException(SharedErrorCode.AUTHORIZATION);
			}

			TypedQuery<AccountEntity> groupMemberQuery = entityManager.createQuery(
							"SELECT m.account FROM group_member m WHERE m.group.key = :group_key", AccountEntity.class)
							.setFirstResult(0);
			groupMemberQuery.setParameter("group_key", group_id);

			List<AccountEntity> members = groupMemberQuery.getResultList();
			List<GroupMemberInfo> groupMembersInfo = new ArrayList<GroupMemberInfo>();
			for (AccountEntity member : members) {
				groupMembersInfo.add(new GroupMemberInfo(member));
			}

			return groupMembersInfo;
		} catch (Exception ex) {
			throw wrapApplicationException(ex, SharedErrorCode.UNKNOWN);
		}
	}

	@Override
	public List<AccountActivity> getGroupAccounts(UUID group_id) {
		try {
			Authentication auth = SecurityContextHolder.getContext().getAuthentication();
			AuthenticatedUser user = (AuthenticatedUser) auth.getPrincipal();


			if (!user.hasRole(EnumRole.ROLE_SYSTEM_ADMIN, EnumRole.ROLE_UTILITY_ADMIN)) {
				throw createApplicationException(SharedErrorCode.AUTHORIZATION);
			}

			TypedQuery<AccountEntity> groupMemberQuery = entityManager.createQuery(
							"SELECT a.account FROM group_member a WHERE a.group.key = :group_key", AccountEntity.class)
							.setFirstResult(0);
			groupMemberQuery.setParameter("group_key", group_id);

			List<AccountEntity> accounts = groupMemberQuery.getResultList();

            List<AccountActivity> accountActivityList = new ArrayList<>();

            for (AccountEntity a : accounts) {
                eu.daiad.web.model.admin.AccountActivity accountActivity = new eu.daiad.web.model.admin.AccountActivity();
                accountActivity.setAccountId(a.getId());
                accountActivity.setUsername(a.getUsername());
                accountActivity.setLastName(a.getLastname());
                accountActivityList.add(accountActivity);
            }

            return accountActivityList;
		} catch (Exception ex) {

			throw wrapApplicationException(ex, SharedErrorCode.UNKNOWN);
		}
	}

	@Override
	public List<GroupMemberInfo> getGroupPossibleMembers(UUID group_id) {
		try {
			Authentication auth = SecurityContextHolder.getContext().getAuthentication();
			AuthenticatedUser user = (AuthenticatedUser) auth.getPrincipal();

			if (!user.hasRole(EnumRole.ROLE_SYSTEM_ADMIN, EnumRole.ROLE_UTILITY_ADMIN)) {
				throw createApplicationException(SharedErrorCode.AUTHORIZATION);
			}

			TypedQuery<AccountEntity> groupPossibleMemberQuery;

			if (group_id != null) {
				groupPossibleMemberQuery = entityManager
								.createQuery("SELECT a FROM account a, group g JOIN a.utility u JOIN a.roles ar JOIN ar.role r "
												+ "WHERE a.utility = g.utility AND g.key = :group_key "
												+ "AND a.id NOT IN (SELECT m.account.id FROM group_member m JOIN m.group g WHERE g.key = :group_key) "
												+ "AND r.name = :user_role", AccountEntity.class).setFirstResult(0);
				groupPossibleMemberQuery.setParameter("group_key", group_id);
				groupPossibleMemberQuery.setParameter("user_role", EnumRole.ROLE_USER.toString());
			} else {
				groupPossibleMemberQuery = entityManager.createQuery(
								"SELECT a FROM account a JOIN a.utility u JOIN a.roles ar JOIN ar.role r "
												+ "WHERE r.name = :user_role " + "AND a.utility.id = :utility_id",
								AccountEntity.class).setFirstResult(0);
				groupPossibleMemberQuery.setParameter("utility_id", user.getUtilityId());
				groupPossibleMemberQuery.setParameter("user_role", EnumRole.ROLE_USER.toString());
			}

			List<AccountEntity> possibleMembers = groupPossibleMemberQuery.getResultList();
			List<GroupMemberInfo> groupMembersInfo = new ArrayList<GroupMemberInfo>();
			for (AccountEntity possibleMember : possibleMembers) {
				groupMembersInfo.add(new GroupMemberInfo(possibleMember));
			}

			return groupMembersInfo;
		} catch (Exception ex) {
			throw wrapApplicationException(ex, SharedErrorCode.UNKNOWN);
		}
	}

	@Override
	public void createGroupSet(CreateGroupSetRequest groupSetInfo) {
		try {
			Authentication auth = SecurityContextHolder.getContext().getAuthentication();
			AuthenticatedUser user = (AuthenticatedUser) auth.getPrincipal();

			if (!user.hasRole(EnumRole.ROLE_SYSTEM_ADMIN, EnumRole.ROLE_UTILITY_ADMIN)) {
				throw createApplicationException(SharedErrorCode.AUTHORIZATION);
			}

			TypedQuery<eu.daiad.web.domain.application.GroupEntity> groupQuery = entityManager
							.createQuery("select g from group g where g.name = :groupName",
											eu.daiad.web.domain.application.GroupEntity.class).setFirstResult(0)
							.setMaxResults(1);
			groupQuery.setParameter("groupName", groupSetInfo.getName());
			List<GroupEntity> groupEntries = groupQuery.getResultList();

			if (!groupEntries.isEmpty()) {
				throw createApplicationException(GroupErrorCode.GROUP_EXISTS).set("groupName", groupSetInfo.getName());
			}

			// Get admin's account
			TypedQuery<eu.daiad.web.domain.application.AccountEntity> adminAccountQuery = entityManager
							.createQuery("select a from account a where a.id = :adminId",
											eu.daiad.web.domain.application.AccountEntity.class).setFirstResult(0)
							.setMaxResults(1);
			adminAccountQuery.setParameter("adminId", user.getId());
			AccountEntity adminAccount = adminAccountQuery.getSingleResult();

			// Get admin's utility
			TypedQuery<eu.daiad.web.domain.application.UtilityEntity> utilityQuery = entityManager
							.createQuery("select a.utility from account a where a.id = :adminId",
											eu.daiad.web.domain.application.UtilityEntity.class).setFirstResult(0)
							.setMaxResults(1);
			utilityQuery.setParameter("adminId", user.getId());
			UtilityEntity utilityEntry = utilityQuery.getSingleResult();

			// Get Members
			TypedQuery<eu.daiad.web.domain.application.AccountEntity> accountQuery = entityManager.createQuery(
							"select a from account a where a.key IN :memberKeys",
							eu.daiad.web.domain.application.AccountEntity.class).setFirstResult(0);
			accountQuery.setParameter("memberKeys", Arrays.asList(groupSetInfo.getMembers()));
			List<AccountEntity> memberAccounts = accountQuery.getResultList();

			GroupSetEntity newGroupSet = new GroupSetEntity();
			newGroupSet.setUtility(utilityEntry);
			newGroupSet.setName(groupSetInfo.getName());
			newGroupSet.setOwner(adminAccount);
			newGroupSet.setCreatedOn(new DateTime());
			newGroupSet.setSize(memberAccounts.size());

			this.entityManager.persist(newGroupSet);
			this.entityManager.flush();

			for (AccountEntity memberAcccount : memberAccounts) {
				GroupMemberEntity member = new GroupMemberEntity();
				member.setGroup(newGroupSet);
				member.setAccount(memberAcccount);
				member.setCreatetOn(new DateTime());
				this.entityManager.persist(member);
			}

		} catch (Exception ex) {
			throw wrapApplicationException(ex, SharedErrorCode.UNKNOWN);
		}
	}

	@Override
	public GroupInfo getSingleGroupByKey(UUID group_id) {
		try {
			Authentication auth = SecurityContextHolder.getContext().getAuthentication();
			AuthenticatedUser user = (AuthenticatedUser) auth.getPrincipal();

			if (!user.hasRole(EnumRole.ROLE_SYSTEM_ADMIN, EnumRole.ROLE_UTILITY_ADMIN)) {
				throw createApplicationException(SharedErrorCode.AUTHORIZATION);
			}

			TypedQuery<UtilityEntity> utilityQuery = entityManager
							.createQuery("SELECT u FROM utility u WHERE u.id = :admin_utility_id", UtilityEntity.class)
							.setFirstResult(0).setMaxResults(1);
			utilityQuery.setParameter("admin_utility_id", user.getUtilityId());

			UtilityEntity adminUtility = utilityQuery.getSingleResult();

			TypedQuery<GroupEntity> groupQuery = entityManager
							.createQuery("SELECT g FROM group g WHERE g.key = :group_id", GroupEntity.class)
							.setFirstResult(0).setMaxResults(1);
			groupQuery.setParameter("group_id", group_id);

			GroupEntity group = groupQuery.getSingleResult();

			if (group.getUtility() != adminUtility) {
				throw createApplicationException(SharedErrorCode.AUTHORIZATION);
			}

			return new GroupInfo(group);
		} catch (Exception ex) {
			throw wrapApplicationException(ex, SharedErrorCode.UNKNOWN);
		}
	}

	@Override
	public void deleteGroup(UUID group_id) {
		try {
			Authentication auth = SecurityContextHolder.getContext().getAuthentication();
			AuthenticatedUser user = (AuthenticatedUser) auth.getPrincipal();

            if (!user.hasRole(EnumRole.ROLE_SYSTEM_ADMIN, EnumRole.ROLE_UTILITY_ADMIN)) {
				throw createApplicationException(SharedErrorCode.AUTHORIZATION);
			}

			GroupSetEntity group = null;
			// Check if group exists
			try {
				TypedQuery<GroupSetEntity> groupQuery = entityManager
								.createQuery("select g from group_set g where g.key = :group_id", GroupSetEntity.class)
								.setFirstResult(0).setMaxResults(1);
				groupQuery.setParameter("group_id", group_id);
				group = groupQuery.getSingleResult();
			} catch (NoResultException ex) {
				throw wrapApplicationException(ex, GroupErrorCode.GROUP_DOES_NOT_EXIST).set("groupId", group_id);
			}

			// Check that administrator is the owner of the group
			if (group.getType() == EnumGroupType.SET) {
				if (group.getOwner().getId() == user.getId()) {
					this.entityManager.remove(group);

					// check if this group is someone's favorite, in order to
					// delete these favorites as well
					TypedQuery<FavouriteEntity> favouriteQuery = entityManager.createQuery("select f from favourite f",
									FavouriteEntity.class).setFirstResult(0);

					List<FavouriteEntity> favourites = favouriteQuery.getResultList();
					for (FavouriteEntity f : favourites) {
						if (f.getType().equals(EnumFavouriteType.GROUP)) {
							FavouriteGroupEntity fg = (FavouriteGroupEntity) f;
							if (fg.getGroup().getId() == group.getId()) {
								this.entityManager.remove(f);
							}
						}

					}
				} else {
					throw createApplicationException(GroupErrorCode.GROUP_ACCESS_RESTRICTED).set("groupId", group_id);
				}
			}

		} catch (Exception ex) {
			throw wrapApplicationException(ex, SharedErrorCode.UNKNOWN);
		}

	}

	@Override
	public List<GroupInfo> getGroupsByMember(UUID user_id) {
		try {
			Authentication auth = SecurityContextHolder.getContext().getAuthentication();
			AuthenticatedUser requestingUser = (AuthenticatedUser) auth.getPrincipal();

			if (!requestingUser.hasRole(EnumRole.ROLE_UTILITY_ADMIN, EnumRole.ROLE_SYSTEM_ADMIN)) {
				throw createApplicationException(SharedErrorCode.AUTHORIZATION);
			}

			TypedQuery<eu.daiad.web.domain.application.GroupEntity> userGroupQuery = entityManager
					.createQuery("SELECT g FROM group_member m JOIN m.group g JOIN m.account a WHERE a.key = :user_key",
							eu.daiad.web.domain.application.GroupEntity.class)
					.setFirstResult(0);
			userGroupQuery.setParameter("user_key", user_id);

			List<eu.daiad.web.domain.application.GroupEntity> groups = userGroupQuery.getResultList();
			List<GroupInfo> groupsInfo = new ArrayList<GroupInfo>();

			for (eu.daiad.web.domain.application.GroupEntity group : groups) {
				GroupInfo groupInfo = new GroupInfo(group);
				groupsInfo.add(groupInfo);
			}

			return groupsInfo;

		} catch (Exception ex) {
			throw wrapApplicationException(ex, SharedErrorCode.UNKNOWN);
		}
	}
}
