package eu.daiad.web.repository.application;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import eu.daiad.web.domain.application.GroupCommunity;
import eu.daiad.web.domain.application.GroupSegment;
import eu.daiad.web.model.error.SharedErrorCode;
import eu.daiad.web.model.group.Account;
import eu.daiad.web.model.group.Cluster;
import eu.daiad.web.model.group.Community;
import eu.daiad.web.model.group.Group;
import eu.daiad.web.model.group.Segment;
import eu.daiad.web.model.group.Set;
import eu.daiad.web.model.group.Utility;
import eu.daiad.web.model.query.EnumClusterType;
import eu.daiad.web.repository.BaseRepository;

@Repository
@Transactional("applicationTransactionManager")
public class JpaGroupRepository extends BaseRepository implements IGroupRepository {

    private static final Log logger = LogFactory.getLog(JpaGroupRepository.class);

    @PersistenceContext(unitName = "default")
    EntityManager entityManager;

    @Override
    public List<Group> getAll(UUID utilityKey) {
        TypedQuery<eu.daiad.web.domain.application.Group> entityQuery = entityManager.createQuery(
                        "select g from group g where g.utility.key = :utilityKey",
                        eu.daiad.web.domain.application.Group.class);

        entityQuery.setParameter("utilityKey", utilityKey);

        List<Group> groups = groupEntityToGroupObject(entityQuery.getResultList());

        groups.addAll(getClusters(utilityKey));

        groups.addAll(getUtilities(utilityKey));

        return groups;
    }

    @Override
    public List<Group> filterByName(UUID utilityKey, String text) {
        List<Group> groups = this.getAll(utilityKey);

        for (int i = groups.size() - 1; i >= 0; i--) {
            boolean remove = false;

            switch (groups.get(i).getType()) {
                case UTILITY:
                case SET:
                    remove = (!StringUtils.contains(groups.get(i).getName(), text));
                    break;
                case SEGMENT:
                    remove = ((!StringUtils.contains(groups.get(i).getName(), text)) && (!StringUtils.contains(
                                    ((Segment) groups.get(i)).getCluster(), text)));
                    break;
                default:
                    remove = true;
                    break;
            }
            if (remove) {
                groups.remove(i);
            }
        }

        return groups;
    }

    @Override
    public List<Group> getGroups(UUID utilityKey) {
        TypedQuery<eu.daiad.web.domain.application.Group> entityQuery = entityManager.createQuery(
                        "select g from group g where g.utility.key = :utilityKey",
                        eu.daiad.web.domain.application.Group.class);

        entityQuery.setParameter("utilityKey", utilityKey);

        List<Group> groups = groupEntityToGroupObject(entityQuery.getResultList());

        return groups;
    }

    @Override
    public List<Group> getUtilities(UUID utilityKey) {
        TypedQuery<eu.daiad.web.domain.application.Utility> query = entityManager.createQuery(
                        "select u from utility u where u.key = :utilityKey",
                        eu.daiad.web.domain.application.Utility.class);

        query.setParameter("utilityKey", utilityKey);

        return utilityEntityToUtilityObject(query.getResultList());
    }

    @Override
    public List<Group> getClusters(UUID utilityKey) {
        TypedQuery<eu.daiad.web.domain.application.Cluster> query = entityManager.createQuery(
                        "select c from cluster c where c.utility.key = :utilityKey",
                        eu.daiad.web.domain.application.Cluster.class);

        query.setParameter("utilityKey", utilityKey);

        return clusterEntityToClusterObject(query.getResultList());
    }

    @Override
    public List<Group> getClusterByKeySegments(UUID clusterKey) {
        TypedQuery<GroupSegment> query = entityManager.createQuery("select g from group_segment g  "
                        + "where g.utility.id = :utility_id and g.cluster.key = :key", GroupSegment.class);

        query.setParameter("utility_id", this.getCurrentUtilityId());
        query.setParameter("key", clusterKey);

        return groupToSegmentList(query.getResultList());
    }

    @Override
    public void createCluster(Cluster cluster) {
        DateTime now = new DateTime();

        TypedQuery<eu.daiad.web.domain.application.Utility> utilityQuery = entityManager.createQuery(
                        "select u from utility u where u.key = :key", eu.daiad.web.domain.application.Utility.class);

        utilityQuery.setParameter("key", cluster.getUtilityKey());

        eu.daiad.web.domain.application.Utility utility = utilityQuery.getSingleResult();

        eu.daiad.web.domain.application.Cluster clusterEntity = new eu.daiad.web.domain.application.Cluster();

        clusterEntity.setName(cluster.getName());
        clusterEntity.setCreatedOn(now);
        clusterEntity.setUtility(utility);

        entityManager.persist(clusterEntity);
        entityManager.flush();

        for (Segment segment : cluster.getSegments()) {
            eu.daiad.web.domain.application.GroupSegment segmentEntity = new eu.daiad.web.domain.application.GroupSegment();

            segmentEntity.setCluster(clusterEntity);
            segmentEntity.setCreatedOn(now);
            segmentEntity.setName(segment.getName());
            segmentEntity.setSize(segment.getMembers().size());
            segmentEntity.setUtility(utility);

            entityManager.persist(segmentEntity);
            entityManager.flush();

            TypedQuery<eu.daiad.web.domain.application.Account> accountQuery = entityManager
                            .createQuery("select a from account a where a.key = :key",
                                            eu.daiad.web.domain.application.Account.class);

            for (UUID userKey : segment.getMembers()) {
                accountQuery.setParameter("key", userKey);

                eu.daiad.web.domain.application.GroupMember member = new eu.daiad.web.domain.application.GroupMember();

                member.setAccount(accountQuery.getSingleResult());
                member.setGroup(segmentEntity);
                member.setCreatetOn(now);

                entityManager.persist(member);
            }

        }

        entityManager.flush();
    }

    @Override
    public void createGroupSet(UUID ownerKey, String name, UUID[] members) {
        DateTime now = new DateTime();

        int utilityId = getCurrentUtilityId();

        // Check if exists
        TypedQuery<eu.daiad.web.domain.application.GroupSet> groupQuery = entityManager.createQuery(
                        "select g from group_set g where g.name = :name",
                        eu.daiad.web.domain.application.GroupSet.class);

        groupQuery.setParameter("name", name);

        if (!groupQuery.getResultList().isEmpty()) {
            return;
        }

        // Get utility
        TypedQuery<eu.daiad.web.domain.application.Utility> utilityQuery = entityManager.createQuery(
                        "select u from utility u where u.id = :id", eu.daiad.web.domain.application.Utility.class);

        utilityQuery.setParameter("id", utilityId);

        eu.daiad.web.domain.application.Utility utility = utilityQuery.getSingleResult();

        // Get owner
        TypedQuery<eu.daiad.web.domain.application.Account> accountQuery = entityManager.createQuery(
                        "select a from account a where a.key = :key and a.utility.id = :utility_id",
                        eu.daiad.web.domain.application.Account.class);

        accountQuery.setParameter("key", ownerKey);
        accountQuery.setParameter("utility_id", utilityId);

        eu.daiad.web.domain.application.Account owner = accountQuery.getSingleResult();

        // Create group
        eu.daiad.web.domain.application.GroupSet groupEntity = new eu.daiad.web.domain.application.GroupSet();

        groupEntity.setCreatedOn(now);
        groupEntity.setName(name);
        groupEntity.setOwner(owner);
        groupEntity.setSize(members.length);
        groupEntity.setUtility(utility);

        entityManager.persist(groupEntity);

        for (UUID userKey : members) {
            accountQuery.setParameter("key", userKey);
            accountQuery.setParameter("utility_id", utilityId);

            eu.daiad.web.domain.application.GroupMember member = new eu.daiad.web.domain.application.GroupMember();

            member.setAccount(accountQuery.getSingleResult());
            member.setGroup(groupEntity);
            member.setCreatetOn(now);

            entityManager.persist(member);

        }

        entityManager.flush();
    }

    @Override
    public void deleteGroupSet(UUID groupKey) {
        if (getCurrentUser() == null) {
            throw createApplicationException(SharedErrorCode.AUTHORIZATION);
        }

        TypedQuery<eu.daiad.web.domain.application.GroupSet> groupQuery = entityManager.createQuery(
                        "select g from group_set g where g.key = :groupKey and g.owner.key = :userKey",
                        eu.daiad.web.domain.application.GroupSet.class);

        groupQuery.setParameter("groupKey", groupKey);
        groupQuery.setParameter("userKey", getCurrentUser().getKey());

        List<eu.daiad.web.domain.application.GroupSet> groups = groupQuery.getResultList();

        if (!groupQuery.getResultList().isEmpty()) {
            TypedQuery<eu.daiad.web.domain.application.FavouriteGroup> favouriteQuery = entityManager.createQuery(
                            "select f from favourite_group f where f.group.key = :groupKey",
                            eu.daiad.web.domain.application.FavouriteGroup.class);

            favouriteQuery.setParameter("groupKey", groupKey);

            for (eu.daiad.web.domain.application.FavouriteGroup favourite : favouriteQuery.getResultList()) {
                entityManager.remove(favourite);
            }

            entityManager.remove(groups.get(0));
        }
    }

    @Override
    public void deleteAllClusterByName(String name) {
        TypedQuery<eu.daiad.web.domain.application.Cluster> query = entityManager.createQuery(
                        "select c from cluster c where c.name = :name", eu.daiad.web.domain.application.Cluster.class);

        query.setParameter("name", name);

        for (eu.daiad.web.domain.application.Cluster cluster : query.getResultList()) {
            for (eu.daiad.web.domain.application.GroupSegment segment : cluster.getGroups()) {
                entityManager.remove(segment);
            }
            entityManager.remove(cluster);
        }
    }

    @Override
    public List<Group> getClusterByNameSegments(String name) {
        TypedQuery<GroupSegment> query = entityManager.createQuery("select g from group_segment g "
                        + "where g.utility.id = :utility_id and g.cluster.name = :name", GroupSegment.class);

        query.setParameter("utility_id", this.getCurrentUtilityId());
        query.setParameter("name", name);

        return groupToSegmentList(query.getResultList());
    }

    @Override
    public List<Group> getClusterByTypeSegments(EnumClusterType type) {
        TypedQuery<GroupSegment> query = entityManager.createQuery("select g from group_segment g "
                        + "where g.utility.id = :utility_id and g.cluster.name = :name", GroupSegment.class);

        query.setParameter("utility_id", this.getCurrentUtilityId());
        query.setParameter("name", type.getName());

        return groupToSegmentList(query.getResultList());
    }

    @Override
    public List<Group> getSets() {
        TypedQuery<eu.daiad.web.domain.application.GroupSet> query = entityManager.createQuery(
                        "select g from group_set g ", eu.daiad.web.domain.application.GroupSet.class);

        List<Group> groups = new ArrayList<Group>();

        for (eu.daiad.web.domain.application.GroupSet entity : query.getResultList()) {
            groups.add(groupEntityToGroupObject(entity));
        }

        return groups;
    }

    public List<Group> getCommunities() {
        TypedQuery<eu.daiad.web.domain.application.GroupCommunity> query = entityManager.createQuery(
                        "select g from group_community g ", eu.daiad.web.domain.application.GroupCommunity.class);

        List<Group> groups = new ArrayList<Group>();

        for (eu.daiad.web.domain.application.GroupCommunity entity : query.getResultList()) {
            groups.add(groupEntityToGroupObject(entity));
        }

        return groups;
    }

    public List<Account> getGroupMembers(UUID groupKey) {
        List<Account> accounts = new ArrayList<Account>();

        TypedQuery<eu.daiad.web.domain.application.Account> query = entityManager.createQuery(
                        "select m.account from group_member m where m.group.key = :groupKey",
                        eu.daiad.web.domain.application.Account.class);

        query.setParameter("groupKey", groupKey);

        for (eu.daiad.web.domain.application.Account entity : query.getResultList()) {
            Account account = new Account();

            account.setKey(entity.getKey());
            account.setLocation(entity.getLocation());
            account.setUsername(entity.getUsername());
            account.setFullName(entity.getFullname());

            accounts.add(account);
        }

        return accounts;

    }

    @Override
    public List<UUID> getGroupMemberKeys(UUID groupKey) {
        ArrayList<UUID> result = new ArrayList<UUID>();
        try {
            Query query = entityManager.createNativeQuery("select CAST(a.key as char varying) from \"group\" g "
                            + "inner join group_member gm on g.id = gm.group_id "
                            + "inner join account a on gm.account_id = a.id where g.key = CAST(? as uuid)");
            query.setParameter(1, groupKey.toString());

            List<?> keys = query.getResultList();
            for (Object key : keys) {
                result.add(UUID.fromString((String) key));
            }
        } catch (Exception ex) {
            logger.error(String.format("Failed to load user keys for group [%s].", groupKey), ex);
        }

        return result;
    }

    @Override
    public List<UUID> getUtilityByKeyMemberKeys(UUID utilityKey) {
        ArrayList<UUID> result = new ArrayList<UUID>();
        try {
            Query query = entityManager.createNativeQuery("select CAST(a.key as char varying) from utility u "
                            + "inner join account a on u.id = a.utility_id where u.key = CAST(? as uuid)");
            query.setParameter(1, utilityKey.toString());

            List<?> keys = query.getResultList();
            for (Object key : keys) {
                result.add(UUID.fromString((String) key));
            }
        } catch (Exception ex) {
            logger.error(String.format("Failed to load user keys for utility [%s]", utilityKey), ex);
        }

        return result;
    }

    @Override
    public List<UUID> getUtilityByIdMemberKeys(int utilityId) {
        ArrayList<UUID> result = new ArrayList<UUID>();
        try {
            Query query = entityManager.createNativeQuery("select CAST(a.key as char varying) from utility u "
                            + "inner join account a on u.id = a.utility_id where u.id = :utilityId");
            query.setParameter("utilityId", utilityId);

            List<?> keys = query.getResultList();
            for (Object key : keys) {
                result.add(UUID.fromString((String) key));
            }
        } catch (Exception ex) {
            logger.error(String.format("Failed to load user keys for utility [%d]", utilityId), ex);
        }

        return result;
    }

    private List<Group> groupToSegmentList(List<eu.daiad.web.domain.application.GroupSegment> groups) {
        List<Group> segments = new ArrayList<Group>();

        for (eu.daiad.web.domain.application.GroupSegment group : groups) {
            Segment segment = new Segment();

            segment.setCreatedOn(group.getCreatedOn().getMillis());
            segment.setGeometry(group.getGeometry());
            segment.setKey(group.getKey());
            segment.setName(group.getName());
            segment.setSize(group.getSize());
            segment.setUtilityKey(group.getUtility().getKey());

            segments.add(segment);
        }

        return segments;
    }

    private List<Group> utilityEntityToUtilityObject(List<eu.daiad.web.domain.application.Utility> entities) {
        List<Group> utilities = new ArrayList<Group>();

        for (eu.daiad.web.domain.application.Utility entity : entities) {
            Utility utility = new Utility();

            utility.setCreatedOn(entity.getCreatedOn().getMillis());
            utility.setKey(entity.getKey());
            utility.setName(entity.getName());
            utility.setUtilityKey(entity.getKey());

            utilities.add(utility);
        }

        return utilities;
    }

    private List<Group> clusterEntityToClusterObject(List<eu.daiad.web.domain.application.Cluster> entities) {
        List<Group> clusters = new ArrayList<Group>();

        for (eu.daiad.web.domain.application.Cluster entity : entities) {
            Cluster cluster = new Cluster();

            cluster.setCreatedOn(entity.getCreatedOn().getMillis());
            cluster.setKey(entity.getKey());
            cluster.setName(entity.getName());
            cluster.setUtilityKey(entity.getUtility().getKey());

            for (GroupSegment groupSegment : ((eu.daiad.web.domain.application.Cluster) entity).getGroups()) {
                Segment segment = new Segment();

                segment.setCreatedOn(groupSegment.getCreatedOn().getMillis());
                segment.setGeometry(groupSegment.getGeometry());
                segment.setKey(groupSegment.getKey());
                segment.setName(groupSegment.getName());
                segment.setSize(groupSegment.getSize());
                segment.setUtilityKey(groupSegment.getUtility().getKey());

                cluster.getSegments().add(segment);
            }

            clusters.add(cluster);
        }

        return clusters;
    }

    private List<Group> groupEntityToGroupObject(List<eu.daiad.web.domain.application.Group> entities) {
        List<Group> groups = new ArrayList<Group>();

        for (eu.daiad.web.domain.application.Group entity : entities) {
            groups.add(groupEntityToGroupObject(entity));
        }

        return groups;
    }

    private Group groupEntityToGroupObject(eu.daiad.web.domain.application.Group entity) {
        switch (entity.getType()) {
            case SEGMENT:
                Segment segment = new Segment();

                segment.setCreatedOn(entity.getCreatedOn().getMillis());
                segment.setGeometry(entity.getGeometry());
                segment.setKey(entity.getKey());
                segment.setName(entity.getName());
                segment.setSize(entity.getSize());
                segment.setUtilityKey(entity.getUtility().getKey());

                segment.setCluster(((GroupSegment) entity).getCluster().getName());

                return segment;
            case SET:
                Set set = new Set();

                set.setCreatedOn(entity.getCreatedOn().getMillis());
                set.setGeometry(entity.getGeometry());
                set.setKey(entity.getKey());
                set.setName(entity.getName());
                set.setSize(entity.getSize());
                set.setUtilityKey(entity.getUtility().getKey());

                return set;
            case COMMONS:
                Community community = new Community();

                community.setCreatedOn(entity.getCreatedOn().getMillis());
                community.setGeometry(entity.getGeometry());
                community.setKey(entity.getKey());
                community.setName(entity.getName());
                community.setSize(entity.getSize());
                community.setUtilityKey(entity.getUtility().getKey());

                GroupCommunity communityEntity = (GroupCommunity) entity;

                community.setDescription(communityEntity.getDescription());
                community.setImage(communityEntity.getImage());

                return community;
            default:
                return null;
        }
    }
}
