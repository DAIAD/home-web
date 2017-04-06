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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import eu.daiad.web.domain.application.AccountEntity;
import eu.daiad.web.domain.application.ClusterEntity;
import eu.daiad.web.domain.application.FavouriteGroupEntity;
import eu.daiad.web.domain.application.GroupCommonsEntity;
import eu.daiad.web.domain.application.GroupEntity;
import eu.daiad.web.domain.application.GroupMemberEntity;
import eu.daiad.web.domain.application.GroupSegmentEntity;
import eu.daiad.web.domain.application.GroupSetEntity;
import eu.daiad.web.domain.application.UtilityEntity;
import eu.daiad.web.model.error.SharedErrorCode;
import eu.daiad.web.model.group.Cluster;
import eu.daiad.web.model.group.Commons;
import eu.daiad.web.model.group.Group;
import eu.daiad.web.model.group.GroupInfo;
import eu.daiad.web.model.group.GroupMember;
import eu.daiad.web.model.group.Segment;
import eu.daiad.web.model.group.Set;
import eu.daiad.web.model.group.Utility;
import eu.daiad.web.model.query.EnumClusterType;
import eu.daiad.web.model.security.AuthenticatedUser;
import eu.daiad.web.model.security.EnumRole;
import eu.daiad.web.repository.BaseRepository;

@Repository
@Transactional("applicationTransactionManager")
public class JpaGroupRepository extends BaseRepository implements IGroupRepository {

    private static final Log logger = LogFactory.getLog(JpaGroupRepository.class);

    @PersistenceContext(unitName = "default")
    EntityManager entityManager;


    @Override
    public Group getByKey(UUID key) {
        return this.getByKey(key, true);
    }

    @Override
    public Group getByKey(UUID key, boolean includeMembers) {
        String groupQueryString = "SELECT g FROM group g WHERE g.key = :key";

        TypedQuery<GroupEntity> query = entityManager.createQuery(groupQueryString, GroupEntity.class)
                                                     .setParameter("key", key)
                                                     .setMaxResults(1);

        List<GroupEntity> groups = query.getResultList();

        return (groups.isEmpty() ? null : groupEntityToGroupObject(groups.get(0)));
    }

    @Override
    public GroupInfo getGroupInfoByKey(UUID key) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        AuthenticatedUser user = (AuthenticatedUser) auth.getPrincipal();

        if (!user.hasRole(EnumRole.ROLE_SYSTEM_ADMIN, EnumRole.ROLE_UTILITY_ADMIN)) {
            throw createApplicationException(SharedErrorCode.AUTHORIZATION);
        }

        String utilityQueryString = "SELECT u FROM utility u WHERE u.id = :utilityId";

        TypedQuery<UtilityEntity> utilityQuery = entityManager.createQuery(utilityQueryString, UtilityEntity.class)
                                                              .setFirstResult(0)
                                                              .setMaxResults(1);
        utilityQuery.setParameter("utilityId", user.getUtilityId());

        UtilityEntity utility = utilityQuery.getSingleResult();

        String groupQueryString = "SELECT g FROM group g WHERE g.key = :groupKey";

        TypedQuery<GroupEntity> groupQuery = entityManager.createQuery(groupQueryString, GroupEntity.class)
                                                          .setFirstResult(0)
                                                          .setMaxResults(1);
        groupQuery.setParameter("groupKey", key);

        GroupEntity group = groupQuery.getSingleResult();

        if (group.getUtility() != utility) {
            throw createApplicationException(SharedErrorCode.AUTHORIZATION);
        }

        return new GroupInfo(group);
    }

    @Override
    public List<GroupInfo> getUtilityGroupInfo(UUID utilityKey) {
        String groupQueryString = "SELECT g FROM group_set g WHERE g.utility.key = :utilityKey";

        TypedQuery<GroupSetEntity> groupQuery = entityManager.createQuery(groupQueryString, GroupSetEntity.class);
        groupQuery.setParameter("utilityKey",utilityKey);

        List<GroupSetEntity> groups = groupQuery.getResultList();
        List<GroupInfo> groupsInfo = new ArrayList<GroupInfo>();

        for (GroupSetEntity group : groups) {
            groupsInfo.add(new GroupInfo(group));
        }

        return groupsInfo;
    }

    @Override
    public List<Group> getAll(UUID utilityKey) {
        TypedQuery<GroupEntity> entityQuery = entityManager.createQuery("select g from group g where g.utility.key = :utilityKey", GroupEntity.class);

        entityQuery.setParameter("utilityKey", utilityKey);

        List<Group> groups = groupEntityToGroupObject(entityQuery.getResultList());

        groups.addAll(getClusters(utilityKey));

        groups.addAll(getUtilities(utilityKey));

        return groups;
    }

    @Override
    public List<Group> filterByName(UUID utilityKey, String text) {
        List<Group> groups = getAll(utilityKey);

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
    public List<Group> getGroupsByUtilityId(int utilityId) {
        TypedQuery<GroupEntity> entityQuery = entityManager.createQuery("select g from group g where g.utility.id = :utilityId", GroupEntity.class);

        entityQuery.setParameter("utilityId", utilityId);

        List<Group> groups = groupEntityToGroupObject(entityQuery.getResultList());

        return groups;
    }

    @Override
    public List<GroupInfo> getMemberGroups(UUID userKey) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        AuthenticatedUser requestingUser = (AuthenticatedUser) auth.getPrincipal();

        if (!requestingUser.hasRole(EnumRole.ROLE_UTILITY_ADMIN, EnumRole.ROLE_SYSTEM_ADMIN)) {
            throw createApplicationException(SharedErrorCode.AUTHORIZATION);
        }

        String groupQueryString = "SELECT g FROM group_member m JOIN m.group g JOIN m.account a WHERE a.key = :userKey";

        TypedQuery<GroupEntity> userGroupQuery = entityManager.createQuery(groupQueryString, GroupEntity.class)
                                                              .setFirstResult(0);
        userGroupQuery.setParameter("userKey", userKey);

        List<GroupEntity> groups = userGroupQuery.getResultList();
        List<GroupInfo> groupsInfo = new ArrayList<GroupInfo>();

        for (GroupEntity group : groups) {
            groupsInfo.add(new GroupInfo(group));
        }

        return groupsInfo;
    }

    @Override
    public List<Group> getGroupsByUtilityKey(UUID utilityKey) {
        TypedQuery<GroupEntity> entityQuery = entityManager.createQuery(
                        "select g from group g where g.utility.key = :utilityKey",
                        GroupEntity.class);

        entityQuery.setParameter("utilityKey", utilityKey);

        List<Group> groups = groupEntityToGroupObject(entityQuery.getResultList());

        return groups;
    }

    @Override
    public List<Group> getUtilities(UUID utilityKey) {
        TypedQuery<UtilityEntity> query = entityManager.createQuery(
                        "select u from utility u where u.key = :utilityKey",
                        UtilityEntity.class);

        query.setParameter("utilityKey", utilityKey);

        return utilityEntityToUtilityObject(query.getResultList());
    }

    @Override
    public List<Group> getClusters(UUID utilityKey) {
        TypedQuery<ClusterEntity> query = entityManager.createQuery(
                        "select c from cluster c where c.utility.key = :utilityKey",
                        ClusterEntity.class);

        query.setParameter("utilityKey", utilityKey);

        return clusterEntityToClusterObject(query.getResultList());
    }

    @Override
    public ClusterEntity getClusterByKey(UUID key) {
        TypedQuery<ClusterEntity> query = entityManager.createQuery("select c from cluster c where c.key = :key",
                                                                    ClusterEntity.class);

        query.setParameter("key", key);

        return query.getSingleResult();
    }

    @Override
    public List<Group> getClusterSegmentsByKey(UUID clusterKey) {
        TypedQuery<GroupSegmentEntity> query = entityManager.createQuery("select g from group_segment g  "
                        + "where g.utility.id = :utility_id and g.cluster.key = :key", GroupSegmentEntity.class);

        query.setParameter("utility_id", getCurrentUtilityId());
        query.setParameter("key", clusterKey);

        return groupToSegmentList(query.getResultList());
    }

    @Override
    public void createCluster(Cluster cluster) {
        DateTime now = new DateTime();

        TypedQuery<UtilityEntity> utilityQuery = entityManager.createQuery(
                        "select u from utility u where u.key = :key", UtilityEntity.class);

        utilityQuery.setParameter("key", cluster.getUtilityKey());

        UtilityEntity utility = utilityQuery.getSingleResult();

        ClusterEntity clusterEntity = new ClusterEntity();

        clusterEntity.setName(cluster.getName());
        clusterEntity.setCreatedOn(now);
        clusterEntity.setUtility(utility);

        entityManager.persist(clusterEntity);
        entityManager.flush();

        for (Segment segment : cluster.getSegments()) {
            GroupSegmentEntity segmentEntity = new GroupSegmentEntity();

            segmentEntity.setCluster(clusterEntity);
            segmentEntity.setCreatedOn(now);
            segmentEntity.setUpdatedOn(now);
            segmentEntity.setName(segment.getName());
            segmentEntity.setSize(segment.getMembers().size());
            segmentEntity.setUtility(utility);

            entityManager.persist(segmentEntity);
            entityManager.flush();

            TypedQuery<AccountEntity> accountQuery = entityManager
                            .createQuery("select a from account a where a.key = :key",
                                            AccountEntity.class);

            for (UUID userKey : segment.getMembers()) {
                accountQuery.setParameter("key", userKey);

                GroupMemberEntity member = new GroupMemberEntity();

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
        TypedQuery<GroupSetEntity> groupQuery = entityManager.createQuery(
                        "select g from group_set g where g.name = :name",
                        GroupSetEntity.class);

        groupQuery.setParameter("name", name);

        if (!groupQuery.getResultList().isEmpty()) {
            return;
        }

        // Get utility
        TypedQuery<UtilityEntity> utilityQuery = entityManager.createQuery(
                        "select u from utility u where u.id = :id", UtilityEntity.class);

        utilityQuery.setParameter("id", utilityId);

        UtilityEntity utility = utilityQuery.getSingleResult();

        // Get owner
        TypedQuery<AccountEntity> accountQuery = entityManager.createQuery(
                        "select a from account a where a.key = :key and a.utility.id = :utility_id",
                        AccountEntity.class);

        accountQuery.setParameter("key", ownerKey);
        accountQuery.setParameter("utility_id", utilityId);

        AccountEntity owner = accountQuery.getSingleResult();

        // Create group
        GroupSetEntity groupEntity = new GroupSetEntity();

        groupEntity.setCreatedOn(now);
        groupEntity.setUpdatedOn(now);
        groupEntity.setName(name);
        groupEntity.setOwner(owner);
        groupEntity.setSize(members.length);
        groupEntity.setUtility(utility);

        entityManager.persist(groupEntity);

        for (UUID userKey : members) {
            accountQuery.setParameter("key", userKey);
            accountQuery.setParameter("utility_id", utilityId);

            GroupMemberEntity member = new GroupMemberEntity();

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

        TypedQuery<GroupSetEntity> groupQuery = entityManager.createQuery(
                        "select g from group_set g where g.key = :groupKey and g.owner.key = :userKey",
                        GroupSetEntity.class);

        groupQuery.setParameter("groupKey", groupKey);
        groupQuery.setParameter("userKey", getCurrentUser().getKey());

        List<GroupSetEntity> groups = groupQuery.getResultList();

        if (!groupQuery.getResultList().isEmpty()) {
            TypedQuery<FavouriteGroupEntity> favouriteQuery = entityManager.createQuery(
                            "select f from favourite_group f where f.group.key = :groupKey",
                            FavouriteGroupEntity.class);

            favouriteQuery.setParameter("groupKey", groupKey);

            for (FavouriteGroupEntity favourite : favouriteQuery.getResultList()) {
                entityManager.remove(favourite);
            }

            entityManager.remove(groups.get(0));
        }
    }

    @Override
    public void deleteAllClusterByName(String name) {
        TypedQuery<ClusterEntity> query = entityManager.createQuery("select c from cluster c where c.name = :name", ClusterEntity.class);

        query.setParameter("name", name);

        for (ClusterEntity cluster : query.getResultList()) {
            for (GroupSegmentEntity segment : cluster.getGroups()) {
                entityManager.remove(segment);
            }
            entityManager.remove(cluster);
        }
    }

    @Override
    public List<Group> getClusterSegmentsByName(String name) {
        TypedQuery<GroupSegmentEntity> query = entityManager.createQuery("select g from group_segment g "
                        + "where g.utility.id = :utility_id and g.cluster.name = :name", GroupSegmentEntity.class);

        query.setParameter("utility_id", getCurrentUtilityId());
        query.setParameter("name", name);

        return groupToSegmentList(query.getResultList());
    }

    @Override
    public List<Group> getClusterSegmentsByType(EnumClusterType type) {
        TypedQuery<GroupSegmentEntity> query = entityManager.createQuery("select g from group_segment g "
                        + "where g.utility.id = :utility_id and g.cluster.name = :name", GroupSegmentEntity.class);

        query.setParameter("utility_id", getCurrentUtilityId());
        query.setParameter("name", type.getName());

        return groupToSegmentList(query.getResultList());
    }

    @Override
    public List<Group> getSets() {
        TypedQuery<GroupSetEntity> query = entityManager.createQuery(
                        "select g from group_set g ", GroupSetEntity.class);

        List<Group> groups = new ArrayList<Group>();

        for (GroupSetEntity entity : query.getResultList()) {
            groups.add(groupEntityToGroupObject(entity));
        }

        return groups;
    }

    @Override
    public List<GroupMember> getGroupMembers(UUID groupKey) {
        String memberQueryString = "select m.account from group_member m where m.group.key = :groupKey";

        TypedQuery<AccountEntity> query = entityManager.createQuery(memberQueryString, AccountEntity.class);
        query.setParameter("groupKey", groupKey);

        List<GroupMember> members = new ArrayList<GroupMember>();
        for (AccountEntity account : query.getResultList()) {
            members.add(new GroupMember(account));
        }

        return members;
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

    private List<Group> groupToSegmentList(List<GroupSegmentEntity> groups) {
        List<Group> segments = new ArrayList<Group>();

        for (GroupSegmentEntity group : groups) {
            Segment segment = new Segment();

            segment.setCreatedOn(group.getCreatedOn());
            segment.setUpdatedOn(group.getUpdatedOn());
            segment.setGeometry(group.getGeometry());
            segment.setKey(group.getKey());
            segment.setName(group.getName());
            segment.setSize(group.getSize());
            segment.setUtilityKey(group.getUtility().getKey());

            segments.add(segment);
        }

        return segments;
    }

    private List<Group> utilityEntityToUtilityObject(List<UtilityEntity> entities) {
        List<Group> utilities = new ArrayList<Group>();

        for (UtilityEntity entity : entities) {
            Utility utility = new Utility();

            utility.setCreatedOn(entity.getCreatedOn());
            utility.setUpdatedOn(entity.getCreatedOn());
            utility.setKey(entity.getKey());
            utility.setName(entity.getName());
            utility.setUtilityKey(entity.getKey());

            utilities.add(utility);
        }

        return utilities;
    }

    private List<Group> clusterEntityToClusterObject(List<ClusterEntity> entities) {
        List<Group> clusters = new ArrayList<Group>();

        for (ClusterEntity entity : entities) {
            Cluster cluster = new Cluster();

            cluster.setCreatedOn(entity.getCreatedOn());
            cluster.setUpdatedOn(entity.getCreatedOn());
            cluster.setKey(entity.getKey());
            cluster.setName(entity.getName());
            cluster.setUtilityKey(entity.getUtility().getKey());

            for (GroupSegmentEntity groupSegment : ((ClusterEntity) entity).getGroups()) {
                Segment segment = new Segment();

                segment.setCreatedOn(groupSegment.getCreatedOn());
                segment.setUpdatedOn(groupSegment.getUpdatedOn());
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

    private List<Group> groupEntityToGroupObject(List<GroupEntity> entities) {
        List<Group> groups = new ArrayList<Group>();

        for (GroupEntity entity : entities) {
            groups.add(groupEntityToGroupObject(entity));
        }

        return groups;
    }

    private Group groupEntityToGroupObject(GroupEntity entity) {
        switch (entity.getType()) {
            case SEGMENT:
                Segment segment = new Segment();

                segment.setCreatedOn(entity.getCreatedOn());
                segment.setUpdatedOn(entity.getUpdatedOn());
                segment.setGeometry(entity.getGeometry());
                segment.setKey(entity.getKey());
                segment.setName(entity.getName());
                segment.setSize(entity.getSize());
                segment.setUtilityKey(entity.getUtility().getKey());

                segment.setCluster(((GroupSegmentEntity) entity).getCluster().getName());

                return segment;
            case SET:
                Set set = new Set(((GroupSetEntity) entity).getOwner().getKey());

                set.setCreatedOn(entity.getCreatedOn());
                set.setUpdatedOn(entity.getUpdatedOn());
                set.setGeometry(entity.getGeometry());
                set.setKey(entity.getKey());
                set.setName(entity.getName());
                set.setSize(entity.getSize());
                set.setUtilityKey(entity.getUtility().getKey());

                return set;
            case COMMONS:
                Commons community = new Commons();

                community.setCreatedOn(entity.getCreatedOn());
                community.setUpdatedOn(entity.getUpdatedOn());
                community.setGeometry(entity.getGeometry());
                community.setKey(entity.getKey());
                community.setName(entity.getName());
                community.setSize(entity.getSize());
                community.setUtilityKey(entity.getUtility().getKey());

                GroupCommonsEntity communityEntity = (GroupCommonsEntity) entity;

                community.setDescription(communityEntity.getDescription());
                community.setImage(communityEntity.getImage());

                return community;
            default:
                return null;
        }
    }
}
