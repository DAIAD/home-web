package eu.daiad.web.repository.application;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.vividsolutions.jts.geom.Geometry;

import eu.daiad.web.domain.application.AccountEntity;
import eu.daiad.web.domain.application.GroupCommonsEntity;
import eu.daiad.web.domain.application.GroupEntity;
import eu.daiad.web.domain.application.GroupMemberEntity;
import eu.daiad.web.model.error.CommonsErrorCode;
import eu.daiad.web.model.error.SharedErrorCode;
import eu.daiad.web.model.group.CommonsCreateRequest;
import eu.daiad.web.model.group.CommonsInfo;
import eu.daiad.web.model.group.CommonsQuery;
import eu.daiad.web.model.group.CommonsQueryResult;
import eu.daiad.web.model.group.EnumGroupType;
import eu.daiad.web.repository.BaseRepository;

@Repository
@Transactional("applicationTransactionManager")
public class JpaCommonsRepository extends BaseRepository implements ICommonsRepository {

    /**
     * Entity manager for persisting data.
     */
    @PersistenceContext(unitName = "default")
    EntityManager entityManager;

    /**
     * Gets a user by its key.
     *
     * @param userKey the user key.
     * @return an instance of {@link AccountEntity}.
     */
    private AccountEntity getAccountByKey(UUID userKey) {
        String accountQueryString = "select a from account a where a.key = :key";
        TypedQuery<AccountEntity> query = entityManager.createQuery(accountQueryString, AccountEntity.class)
                        .setFirstResult(0)
                        .setMaxResults(1);
        query.setParameter("key", userKey);

        return query.getSingleResult();
    }

    /**
     * Gets a commons group by name.
     *
     * @param userKey the user key.
     * @param name the name of the commons.
     * @return an instance of {@link GroupCommonsEntity} or null if no commons with this name exists.
     */
    @Override
    public GroupCommonsEntity getCommonsByName(UUID userKey, String name) {
        String commonsQueryString = "select c from group_commons c where c.name = :name and c.owner.key = :userKey";

        TypedQuery<GroupCommonsEntity> query = entityManager.createQuery(commonsQueryString, GroupCommonsEntity.class)
                                                            .setParameter("userKey", userKey)
                                                            .setParameter("name", name)
                                                            .setFirstResult(0)
                                                            .setMaxResults(1);

        List<GroupCommonsEntity> result= query.getResultList();

        return (result.isEmpty() ? null : result.get(0));
    }

    /**
     * Gets all COMMONS group keys for which a user is a member.
     *
     * @param userKey the user key.
     * @return a list of all commons keys.
     */
    @Override
    public List<UUID> getAccountCommons(UUID userKey) {
        String memberQueryString = "select  CAST(g.key as char varying) " +
                                   "from    \"group\" g " +
                                   "        inner join group_member gm on g.id = gm.group_id " +
                                   "        inner join account a on gm.account_id = a.id " +
                                   "where   a.key = CAST(? as uuid)";

        Query query = entityManager.createNativeQuery(memberQueryString)
                                   .setParameter(1, userKey);

        List<?> keys = query.getResultList();
        List<UUID> result = new ArrayList<UUID>();
        for (Object key : keys) {
            result.add(UUID.fromString((String) key));
        }

        return result;
    }

    /**
     * Get a commons by key.
     *
     * @param userKey the key of the user who requests the commons information.
     * @param commonsKey the commons key.
     * @return a {@link CommonsInfo} object.
     */
    @Override
    public CommonsInfo getCommonsByKey(UUID userKey, UUID commonsKey) {
        // Get data
        AccountEntity account = getAccountByKey(userKey);

        String commonsQueryString = "select c from group_commons c where c.key = :key";

        TypedQuery<GroupCommonsEntity> query = entityManager.createQuery(commonsQueryString, GroupCommonsEntity.class)
                                                            .setParameter("key", commonsKey)
                                                            .setFirstResult(0)
                                                            .setMaxResults(1);

        List<GroupCommonsEntity> commonsResult = query.getResultList();
        if(commonsResult.isEmpty()) {
            throw createApplicationException(CommonsErrorCode.NOT_FOUND);
        }
        GroupCommonsEntity commons = commonsResult.get(0);

        // Check permissions
        if (!commons.getUtility().getKey().equals(account.getUtility().getKey())) {
            throw createApplicationException(SharedErrorCode.AUTHORIZATION);
        }

        // Check owner and membership
        boolean isOwner = false;
        boolean isMember = false;
        if(commons.getOwner().getKey().equals(userKey)) {
            isOwner = true;
        }
        if(getAccountCommons(userKey).contains(commons.getKey())) {
            isMember = true;
        }

        CommonsInfo result = new CommonsInfo(query.getSingleResult());
        result.setOwner(isOwner);
        result.setMember(isMember);

        return result;
    }

    /**
     * Creates a new commons.
     *
     * @param userKey the key of the user who creates the new commons.
     * @param commons the new commons properties.
     * @return the key of the new commons.
     */
    @Override
    public UUID create(UUID userKey, CommonsCreateRequest request) {
        AccountEntity account = getAccountByKey(userKey);

        Geometry geometry = request.getGeometry();
        if ((geometry != null) && (geometry.getSRID() == 0)) {
            geometry.setSRID(4326);
        }

        if (getCommonsByName(userKey, request.getName()) != null) {
            throw createApplicationException(CommonsErrorCode.NAME_EXISTS).set("name", request.getName());
        }

        GroupCommonsEntity commons = new GroupCommonsEntity();

        commons.setName(request.getName());
        commons.setDescription(request.getDescription());
        commons.setCreatedOn(new DateTime());
        commons.setUpdatedOn(commons.getCreatedOn());
        commons.setGeometry(request.getGeometry());
        commons.setImage(request.getImage());
        commons.setSize(1);

        commons.setOwner(account);
        commons.setUtility(account.getUtility());

        entityManager.persist(commons);

        GroupMemberEntity member = new GroupMemberEntity();
        member.setAccount(account);
        member.setGroup(commons);
        member.setCreatetOn(commons.getCreatedOn());

        entityManager.persist(member);

        entityManager.flush();

        return commons.getKey();
    }

    /**
     * Creates a new commons.
     *
     * @param userKey the key of the user who creates the new commons.
     * @param request the new commons properties.
     */
    @Override
    public void update(UUID userKey, UUID commonsKey, CommonsCreateRequest request) {
        // Get commons and check permission
        String commonsQueryString = "select c from group_commons c where c.key = :commonsKey and c.owner.key = :userKey";

        TypedQuery<GroupCommonsEntity> query = entityManager.createQuery(commonsQueryString, GroupCommonsEntity.class)
                                                            .setParameter("commonsKey", commonsKey)
                                                            .setParameter("userKey", userKey)
                                                            .setFirstResult(0)
                                                            .setMaxResults(1);

        List<GroupCommonsEntity> commonsResult = query.getResultList();
        if(commonsResult.isEmpty()) {
            throw createApplicationException(CommonsErrorCode.NOT_FOUND);
        }
        GroupCommonsEntity commons = commonsResult.get(0);

        // Update commons
        Geometry geometry = request.getGeometry();
        if ((geometry != null) && (geometry.getSRID() == 0)) {
            geometry.setSRID(4326);
        }

        commons.setName(request.getName());
        commons.setDescription(request.getDescription());
        commons.setUpdatedOn(new DateTime());
        commons.setGeometry(request.getGeometry());
        commons.setImage(request.getImage());

        entityManager.flush();
    }

    /**
     * Deletes the commons with the given key.
     *
     * @param userKey the key of the user who requests the delete operation.
     * @param commonsKey the key of commons to delete.
     */
    @Override
    public void remove(UUID userKey, UUID commonsKey) {
        // Get commons and check permission
        String commonsQueryString = "select c from group_commons c where c.key = :commonsKey and c.owner.key = :userKey";

        TypedQuery<GroupCommonsEntity> query = entityManager.createQuery(commonsQueryString, GroupCommonsEntity.class)
                                                            .setParameter("commonsKey", commonsKey)
                                                            .setParameter("userKey", userKey)
                                                            .setFirstResult(0)
                                                            .setMaxResults(1);

        List<GroupCommonsEntity> commonsResult = query.getResultList();
        if(commonsResult.isEmpty()) {
            throw createApplicationException(CommonsErrorCode.NOT_FOUND);
        }

        entityManager.remove(commonsResult.get(0));
        entityManager.flush();
    }

    /**
     * Adds a user to a commons group.
     *
     * @param userKey the key of the user to add.
     * @param commonsKey the key of the commons to add the user to.
     */
    @Override
    public void join(UUID userKey, UUID commonsKey) {
        // Get data
        AccountEntity account = getAccountByKey(userKey);

        String commonsQueryString = "select c from group_commons c where c.key = :key";

        TypedQuery<GroupCommonsEntity> query = entityManager.createQuery(commonsQueryString, GroupCommonsEntity.class)
                                                            .setParameter("key", commonsKey)
                                                            .setFirstResult(0)
                                                            .setMaxResults(1);

        List<GroupCommonsEntity> commonsResult = query.getResultList();
        if(commonsResult.isEmpty()) {
            throw createApplicationException(CommonsErrorCode.NOT_FOUND);
        }
        GroupCommonsEntity commons = commonsResult.get(0);

        // Check permissions
        if (!commons.getUtility().getKey().equals(account.getUtility().getKey())) {
            throw createApplicationException(SharedErrorCode.AUTHORIZATION);
        }

        if(!getAccountCommons(userKey).contains(commons.getKey())) {
            GroupMemberEntity member = new GroupMemberEntity();
            member.setAccount(account);
            member.setGroup(commons);
            member.setCreatetOn(new DateTime());

            entityManager.persist(member);

            commons.setSize(commons.getSize() + 1);

            entityManager.flush();
        }
    }

    /**
     * Removes a user from a commons group.
     *
     * @param userKey the key of the user to add.
     * @param commonsKey the key of the commons that the user left.
     */
    @Override
    public void leave(UUID userKey, UUID commonsKey) {
        // Get data
        AccountEntity account = getAccountByKey(userKey);

        String commonsQueryString = "select c from group_commons c where c.key = :key";

        TypedQuery<GroupCommonsEntity> commonsQuery = entityManager.createQuery(commonsQueryString, GroupCommonsEntity.class)
                                                                   .setParameter("key", commonsKey)
                                                                   .setFirstResult(0)
                                                                   .setMaxResults(1);

        List<GroupCommonsEntity> commonsResult = commonsQuery.getResultList();
        if(commonsResult.isEmpty()) {
            throw createApplicationException(CommonsErrorCode.NOT_FOUND);
        }
        GroupCommonsEntity commons = commonsResult.get(0);

        // Check permissions
        if (!commons.getUtility().getKey().equals(account.getUtility().getKey())) {
            throw createApplicationException(SharedErrorCode.AUTHORIZATION);
        }

        if(commons.getOwner().getKey().equals(account.getKey())) {
            throw createApplicationException(CommonsErrorCode.OWNER_CANNOT_LEAVE_COMMONS);
        }
        if(getAccountCommons(userKey).contains(commons.getKey())) {
            String memberQueryString = "select m from group_member m where m.account.key = :userKey and m.group.key = :commonsKey";

            TypedQuery<GroupMemberEntity> memberQuery = entityManager.createQuery(memberQueryString, GroupMemberEntity.class)
                                                                     .setParameter("userKey", userKey)
                                                                     .setParameter("commonsKey", commonsKey)
                                                                     .setFirstResult(0)
                                                                     .setMaxResults(1);


            entityManager.remove(memberQuery.getSingleResult());
            commons.setSize(commons.getSize() - 1);

            entityManager.flush();
        }
    }

    /**
     * Filters commons using the given query.
     *
     * @param userKey the key of the user who executes the search operation.
     * @param query the query.
     * @return a list of {@link CommonsInfo} objects.
     */
    @Override
    public CommonsQueryResult search(UUID userKey, CommonsQuery query) {
        if(query == null) {
            return new CommonsQueryResult();
        }

        AccountEntity account = getAccountByKey(userKey);
        List<UUID> accountCommons = getAccountCommons(userKey);

        Geometry geometry = query.getGeometry();
        if ((geometry != null) && (geometry.getSRID() == 0)) {
            geometry.setSRID(4326);
        }

        // Load data
        String command = "";

        // Resolve filters
        List<String> filters = new ArrayList<>();

        filters.add("(g.utility.id = :utilityId)");

        if (!StringUtils.isBlank(query.getName())) {
            filters.add("(g.name like :name)");
        }
        if (query.getSize() != null) {
            filters.add("(g.size >= :size)");
        }
        if (geometry != null) {
            filters.add("(intersects(:geometry, g.geometry) = true)");
        }

        // Count total number of records
        command = "select count(g.id) from group_commons g ";

        Integer totalCommons;

        if (!filters.isEmpty()) {
            command += " where " + StringUtils.join(filters, " and ");
        }

        TypedQuery<Number> countQuery = entityManager.createQuery(command, Number.class);

        if (!StringUtils.isBlank(query.getName())) {
            countQuery.setParameter("name", query.getName() + "%");
        }
        if (query.getSize() != null) {
            countQuery.setParameter("size", query.getSize());
        }
        if (geometry != null) {
            countQuery.setParameter("geometry", geometry);
        }
        countQuery.setParameter("utilityId", account.getUtility().getId());

        totalCommons = countQuery.getSingleResult().intValue();

        CommonsQueryResult result = new CommonsQueryResult(query.getPageIndex(), query.getPageSize(), totalCommons);

        // Load data
        command = "select g from group_commons g ";

        if (!filters.isEmpty()) {
            command += " where " + StringUtils.join(filters, " and ");
        }

        switch(query.getSortBy()) {
            case NAME:
                command += " order by g.name ";
                break;
            case SIZE:
                command += " order by g.size ";
                break;
            case AREA:
                command += " order by st_area(g.geometry) ";
                break;
        }
        if(query.isSortAscending()) {
            command += " asc";
        } else {
            command += " desc";
        }

        TypedQuery<GroupCommonsEntity> selectQuery = entityManager.createQuery(command, GroupCommonsEntity.class);

        if (!StringUtils.isBlank(query.getName())) {
            selectQuery.setParameter("name", query.getName() + "%");
        }
        if (query.getSize() != null) {
            selectQuery.setParameter("size", query.getSize());
        }
        if (geometry != null) {
            selectQuery.setParameter("geometry", geometry);
        }
        selectQuery.setParameter("utilityId", account.getUtility().getId());

        selectQuery.setFirstResult(query.getPageIndex() * query.getPageSize());
        selectQuery.setMaxResults(query.getPageSize());

        for(GroupCommonsEntity commons : selectQuery.getResultList()) {
            // Check owner and membership
            boolean isOwner = false;
            boolean isMember = false;
            if(commons.getOwner().getKey().equals(userKey)) {
                isOwner = true;
            }
            if(accountCommons.contains(commons.getKey())) {
                isMember = true;
            }

            result.getGroups().add(new CommonsInfo(commons, isOwner, isMember));
        }

        return result;
    }

    @Override
    public List<CommonsInfo> getCommonsByUserKey(UUID userKey) {
        List<CommonsInfo> result = new ArrayList<CommonsInfo>();
        List<UUID> accountCommons = getAccountCommons(userKey);

        String groupQueryString = "select m.group from group_member m where m.account.key = :userKey";

        TypedQuery<GroupEntity> query = entityManager.createQuery(groupQueryString, GroupEntity.class)
                                                                   .setParameter("userKey", userKey);

        for(GroupEntity group : query.getResultList()) {
            if(group.getType() != EnumGroupType.COMMONS) {
                continue;
            }

            GroupCommonsEntity commons = (GroupCommonsEntity) group;

            // Check owner and membership
            boolean isOwner = false;
            boolean isMember = false;
            if(commons.getOwner().getKey().equals(userKey)) {
                isOwner = true;
            }
            if(accountCommons.contains(commons.getKey())) {
                isMember = true;
            }

            result.add(new CommonsInfo(commons, isOwner, isMember));
        }

        return result;
    }

}
