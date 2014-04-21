package com.atlassian.jira.association;

import com.atlassian.crowd.embedded.api.User;
import org.ofbiz.core.entity.GenericValue;

import java.util.List;

/**
 * This Store is used to work with relationships between Users and other objects.
 *
 * @since v4.3
 */
public interface UserAssociationStore
{
    /**
     * Tests if the given association exists.
     *
     * @param associationType the Association type
     * @param user the User
     * @param sink the sink node
     * @return true if the given association exists.
     */
    public boolean associationExists(final String associationType, final User user, final GenericValue sink);

    /**
     * Tests if the given association exists.
     *
     * @param associationType the Association type
     * @param user the User
     * @param sinkNodeEntity The entity name of the sink node (eg "Issue").
     * @param sinkNodeId The id of the sink node.
     * @return true if the given association exists.
     */
    public boolean associationExists(final String associationType, final User user, final String sinkNodeEntity, final Long sinkNodeId);

    /**
     * Finds and returns a list of usernames associated with a given sink.
     *
     * @param associationType the Association type
     * @param sink the sink node
     * @return a list of associated usernames (never null)
     */
    public List<String> getUsernamesFromSink(String associationType, GenericValue sink);

    /**
     * Returns all the sinks that are associated with the given User.
     *
     * @param associationType the Association type
     * @param user the User
     * @param sinkNodeEntity The entity name of the sink node (eg "Issue").
     * @return all the sinks that are associated with the given User.
     */
    public List<GenericValue> getSinksFromUser(String associationType, User user, String sinkNodeEntity);

    /**
     * Finds and returns a list of Users associated with a given sink.
     *
     * @param associationType the Association type
     * @param sink the sink node
     * @return a list of associated Users (never null)
     */
    public List<User> getUsersFromSink(String associationType, GenericValue sink);

    /**
     * Creates an association between a user and a sink node.
     *
     * @param associationType the Association type
     * @param user the user to associate with the sink node.
     * @param sink the sink node
     */
    public void createAssociation(String associationType, User user, GenericValue sink);

    /**
     * Creates an association between a user and a sink node.
     *
     * @param associationType the Association type
     * @param userName the user name to associate with the sink node.
     * @param sinkNodeEntity the entity name of the sink node
     * @param sinkNodeId the id of the sink node entity
     */
    public void createAssociation(final String associationType, final String userName, final String sinkNodeEntity, final Long sinkNodeId);

    /**
     * Removes an association between a user and a sink node.
     *
     * @param associationType the Association type
     * @param username the user to associate with the sink node.
     * @param sink the sink node
     */
    public void removeAssociation(String associationType, String username, GenericValue sink);

    /**
     * Removes an association between a user and a sink node.
     *
     * @param associationType the Association type
     * @param user the user to associate with the sink node.
     * @param sink the sink node
     */
    public void removeAssociation(String associationType, User user, GenericValue sink);

    /**
     * Removes all User Associations for this User of the given associationType
     *
     * @param associationType the Association type
     * @param user the User
     * @param sinkNodeEntity The entity name of the sink node (eg "Issue").
     */
    public void removeUserAssociationsFromUser(final String associationType, final User user, final String sinkNodeEntity);
}
