package eu.daiad.web.repository.application;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import eu.daiad.web.domain.application.GroupCommunity;
import eu.daiad.web.domain.application.GroupSegment;
import eu.daiad.web.model.group.Account;
import eu.daiad.web.model.group.Cluster;
import eu.daiad.web.model.group.Community;
import eu.daiad.web.model.group.Group;
import eu.daiad.web.model.group.GroupQuery;
import eu.daiad.web.model.group.Segment;
import eu.daiad.web.model.group.Utility;
import eu.daiad.web.model.query.EnumClusterType;
import eu.daiad.web.model.security.AuthenticatedUser;
import eu.daiad.web.repository.BaseRepository;

@Repository
@Transactional("applicationTransactionManager")
public class JpaGroupRepository extends BaseRepository implements IGroupRepository {

    private static final Log logger = LogFactory.getLog(JpaGroupRepository.class);

    @PersistenceContext(unitName = "default")
    EntityManager entityManager;

    @Override
    public List<Group> getAll(GroupQuery query) {
        TypedQuery<eu.daiad.web.domain.application.Group> entityQuery = entityManager.createQuery(
                        "select g from group g where g.utility.key = :utilityKey",
                        eu.daiad.web.domain.application.Group.class);

        entityQuery.setParameter("utilityKey", query.getUtility());

        List<Group> groups = groupEntityToGroupObject(entityQuery.getResultList());

        groups.addAll(getClusters(query.getUtility()));

        groups.addAll(getUtilities(query.getUtility()));

        return groups;
    }


    @Override
    public List<Group> getUtilities(UUID utilityKey) {
        TypedQuery<eu.daiad.web.domain.application.Utility> query = entityManager.createQuery(
                        "select u from utility u where u.key = :utilityKey", eu.daiad.web.domain.application.Utility.class);


        query.setParameter("utilityKey", utilityKey);
        
        return utilityEntityToUtilityObject(query.getResultList());
    }

    @Override
    public List<Group> getClusters(UUID utilityKey) {
        TypedQuery<eu.daiad.web.domain.application.Cluster> query = entityManager.createQuery(
                        "select c from cluster c where c.utility.key = :utilityKey", eu.daiad.web.domain.application.Cluster.class);

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

    private Integer getCurrentUtilityId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        AuthenticatedUser user = null;

        if (auth.getPrincipal() instanceof AuthenticatedUser) {
            user = (AuthenticatedUser) auth.getPrincipal();
        }

        if (user != null) {
            return user.getUtilityId();
        }

        return null;
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

            List<Segment> segments = new ArrayList<Segment>();

            for (GroupSegment groupSegment : ((eu.daiad.web.domain.application.Cluster) entity).getGroups()) {
                Segment segment = new Segment();

                segment.setCreatedOn(groupSegment.getCreatedOn().getMillis());
                segment.setGeometry(groupSegment.getGeometry());
                segment.setKey(groupSegment.getKey());
                segment.setName(groupSegment.getName());
                segment.setSize(groupSegment.getSize());
                segment.setUtilityKey(groupSegment.getUtility().getKey());

                segments.add(segment);
            }

            cluster.setSegments(segments);

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
            case SET:
                Segment segment = new Segment();

                segment.setCreatedOn(entity.getCreatedOn().getMillis());
                segment.setGeometry(entity.getGeometry());
                segment.setKey(entity.getKey());
                segment.setName(entity.getName());
                segment.setSize(entity.getSize());
                segment.setUtilityKey(entity.getUtility().getKey());

                return segment;
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
