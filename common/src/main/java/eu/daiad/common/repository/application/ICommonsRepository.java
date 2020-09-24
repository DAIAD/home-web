package eu.daiad.common.repository.application;

import java.util.List;
import java.util.UUID;

import eu.daiad.common.domain.application.GroupCommonsEntity;
import eu.daiad.common.model.group.CommonsCreateRequest;
import eu.daiad.common.model.group.CommonsInfo;
import eu.daiad.common.model.group.CommonsMemberInfo;
import eu.daiad.common.model.group.CommonsMemberQuery;
import eu.daiad.common.model.group.CommonsMemberQueryResult;
import eu.daiad.common.model.group.CommonsQuery;
import eu.daiad.common.model.group.CommonsQueryResult;

public interface ICommonsRepository {

    /**
     * Get a commons by key.
     *
     * @param userKey the key of the user who requests the commons information.
     * @param commonsKey the commons key.
     * @return a {@link CommonsInfo} object.
     */
    CommonsInfo getCommonsByKey(UUID userKey, UUID commonsKey);

    /**
     * Gets a commons group by name.
     *
     * @param userKey the user key.
     * @param name the name of the commons.
     * @return an instance of {@link GroupCommonsEntity} or null if no commons with this name exists.
     */
    GroupCommonsEntity getCommonsByName(UUID userKey, String name);

    /**
     * Gets all COMMONS group keys for which a user is a member.
     *
     * @param userKey the user key.
     * @return a list of all commons keys.
     */
    List<UUID> getAccountCommons(UUID userKey);

    /**
     * Creates a new commons.
     *
     * @param userKey the key of the user who creates the new commons.
     * @param request the new commons properties.
     * @return the key of the new commons.
     */
    UUID create(UUID userKey, CommonsCreateRequest request);

    /**
     * Updates an existing commons.
     *
     * @param userKey the key of the user who requests the update.
     * @param commonsKey the key of commons to update.
     * @param commons the commons property values.
     */
    void update(UUID userKey, UUID commonsKey, CommonsCreateRequest commons);

    /**
     * Deletes the commons with the given key.
     *
     * @param userKey the key of the user who requests the delete operation.
     * @param commonsKey the key of commons to delete.
     */
    void remove(UUID userKey, UUID commonsKey);

    /**
     * Adds a user to a commons group.
     *
     * @param userKey the key of the user to add.
     * @param commonsKey the key of the commons to add the user to.
     */
    void join(UUID userKey, UUID commonsKey);

    /**
     * Removes a user from a commons group.
     *
     * @param userKey the key of the user to add.
     * @param commonsKey the key of the commons that the user left.
     */
    void leave(UUID userKey, UUID commonsKey);

    /**
     * Filters commons using the given query.
     *
     * @param userKey the key of the user who executes the search operation.
     * @param query the query.
     * @return a list of {@link CommonsInfo} objects.
     */
    CommonsQueryResult search(UUID userKey, CommonsQuery query);

    /**
     * Gets all commons for the user with the given key.
     *
     * @param userKey the user key.
     * @return a list of {@link CommonsInfo} objects.
     */
    List<CommonsInfo> getCommonsByUserKey(UUID userKey);

    /**
     * Selects, filters and sorts members of a commons group.
     *
     * @param userKey the key of the user who executes the search operation.
     * @param query the query.
     * @return a list of {@link CommonsMemberInfo} objects.
     */
    CommonsMemberQueryResult getMembers(UUID userKey, CommonsMemberQuery query);

    /**
     * Checks if two users are members of at least on shared commons group.
     *
     * @param user1Key the key of the first user.
     * @param user2Key the key of the second user.
     * @return true if there is at least one commons group for which both users are members.
     */
    boolean shareCommonsMembership(UUID user1Key, UUID user2Key);

}
