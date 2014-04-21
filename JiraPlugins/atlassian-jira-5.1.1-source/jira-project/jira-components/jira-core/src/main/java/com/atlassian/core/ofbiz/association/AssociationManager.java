package com.atlassian.core.ofbiz.association;

import com.atlassian.crowd.embedded.api.User;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.List;

/**
 * This was taken from atlassian-ofbiz and placed into its now rightful home of JIRA.
 *
 * @since 4.3
 */
public interface AssociationManager
{
    /**
     * Create an association between two entities, given a particular association type.
     * <p/>
     * If the association already exists - it will not be created.
     *
     * @param source the source
     * @param sink the sink
     * @param associationType the Association Type
     *
     * @return The new association, or the existing association if it already existed.
     *
     * @throws GenericEntityException If there is a DB Exception.
     */
    public GenericValue createAssociation(GenericValue source, GenericValue sink, String associationType)
            throws GenericEntityException;

    /**
     * Create an association between two entities, given a particular association type.
     * <p/>
     * If the association already exists - it will not be created.
     * <p/>
     * NOTE: this is a convenience method that should only be used when you are certain of the related entity id's. This
     * method does not verify the integrity of the links it creates.
     *
     * @return The new association, or the existing association if it already existed.
     *
     * @throws GenericEntityException If there is a DB Exception.
     */
    public GenericValue createAssociation(Long sourceNodeId, String sourceNodeEntity, Long sinkNodeId, String sinkNodeEntity, String associationType)
            throws GenericEntityException;

    /**
     * Removes association between two entities, given a particular association type.
     *
     * @param source the source entity
     * @param sink  generic value, e.g. issue
     * @param associationType association type
     *
     * @throws GenericEntityException
     */
    public void removeAssociation(GenericValue source, GenericValue sink, String associationType)
            throws GenericEntityException;

    /**
     * Removes association between the user with given username and the generic value.
     *
     * @param user the user
     * @param sink  generic value, e.g. issue
     * @param associationType  association type
     * @throws GenericEntityException
     */
    public void removeAssociation(User user, GenericValue sink, String associationType) throws GenericEntityException;

    /**
     * Removes association between the user with given username and the generic value
     *
     * @param username username
     * @param sink generic value, e.g. issue
     * @param associationType association type
     *
     * @throws GenericEntityException If there is a DB Exception.
     */
    public void removeAssociation(String username, GenericValue sink, String associationType)
            throws GenericEntityException;

    /**
     * Remove all entity<->entity associations, given the source.
     *
     * @param source the Source
     *
     * @throws GenericEntityException If there is a DB Exception.
     */
    public void removeAssociationsFromSource(GenericValue source) throws GenericEntityException;

    /**
     * Remove all entity<->entity associations, given the sink.
     *
     * @param sink the sink
     *
     * @throws GenericEntityException If there is a DB Exception.
     */
    public void removeAssociationsFromSink(GenericValue sink) throws GenericEntityException;

    /**
     * Remove all user<->entity associations, given the entity
     *
     * @param sink the sink
     *
     * @throws GenericEntityException If there is a DB Exception.
     */
    public void removeUserAssociationsFromSink(GenericValue sink) throws GenericEntityException;

    /**
     * Remove all uer associations given an entity and association type
     *
     * @param sink The entity disassociate with all users
     * @param associationType the association type to remove
     *
     * @throws GenericEntityException throws if problem with ofbiz
     */
    public void removeUserAssociationsFromSink(GenericValue sink, String associationType) throws GenericEntityException;

    /**
     * Remove all user<->entity associations, given the user
     *
     * @param user the User
     *
     * @throws GenericEntityException If there is a DB Exception.
     */
    public void removeUserAssociationsFromUser(User user) throws GenericEntityException;

    /**
     * Remove all user<->entity associations, given the user and association type
     *
     * @param user The user to remove all associations with
     * @param associationType the type of associations to remove
     *
     * @throws GenericEntityException if database exception occurs
     */
    public void removeUserAssociationsFromUser(User user, String associationType) throws GenericEntityException;

    /**
     * Remove all user<->entity associations, given the user and association type
     *
     * @param user The user to remove all associations with
     * @param entityName The type of entity to remove
     * @param associationType the type of associations to remove
     *
     * @throws GenericEntityException if database exception occurs
     */
    public void removeUserAssociationsFromUser(User user, String associationType, String entityName)
            throws GenericEntityException;


    /**
     * Swap all associations of a particular type from one sink to another.
     * <p/>
     * Used in ComponentDelete and VersionDelete.
     *
     * @param sourceEntityType the Source Entity Type
     * @param associationType the Association Type
     * @param fromSink the From sink
     * @param toSink the To sink
     *
     * @throws GenericEntityException If there is a DB Exception.
     */
    public void swapAssociation(String sourceEntityType, String associationType, GenericValue fromSink, GenericValue toSink)
            throws GenericEntityException;

    /**
     * Swaps all associations for a given list of entities (say move a list of unresolved issue entities to a new fix for version)
     *
     * @param entities the entities
     * @param associationType the Association Type
     * @param fromSink the From sink
     * @param toSink the To sink
     *
     * @throws GenericEntityException If there is a DB Exception.
     */
    public void swapAssociation(List<GenericValue> entities, String associationType, GenericValue fromSink, GenericValue toSink)
            throws GenericEntityException;


    /**
     * Operates on NodeAssociations - gets MANY sinks from ONE source
     *
     * @throws GenericEntityException If there is a DB Exception.
     */
    public List<GenericValue> getSinkFromSource(GenericValue source, String sinkName, String associationType, boolean useCache)
            throws GenericEntityException;

    public List<GenericValue> getSinkFromSource(GenericValue source, String sinkName, String associationType, boolean useCache, boolean useSequence)
            throws GenericEntityException;

    /**
     * Operates on NodeAssociations - gets MANY sources from ONE sink
     *
     * @throws GenericEntityException If there is a DB Exception.
     */
    public List<GenericValue> getSourceFromSink(GenericValue sink, String sourceName, String associationType, boolean useCache)
            throws GenericEntityException;

    /**
     * Operates on NodeAssociations - gets MANY sources from ONE sink
     *
     * @throws GenericEntityException If there is a DB Exception.
     */
    public List<GenericValue> getSourceFromSink(GenericValue sink, String sourceName, String associationType, boolean useCache, boolean useSequence)
            throws GenericEntityException;

    /**
     * Operates on UserAssociations - gets MANY sinks from ONE user
     *
     * @param source The associated user
     * @param sinkName The type of entity
     * @param associationType The association type
     * @param useCache Do we use the cache
     * @return a List of sinks (GenericValue)
     *
     * @throws GenericEntityException If there is a DB Exception.
     */
    public List<GenericValue> getSinkFromUser(User source, String sinkName, String associationType, boolean useCache)
            throws GenericEntityException;

    /**
     * Operates on UserAssociations - gets MANY sinks Ids from ONE user
     *
     * @param source The associated user
     * @param sinkName The type of entity
     * @param associationType The association type
     * @param useCache Do we use the cache
     * @return a List of ids (Long)
     *
     * @throws GenericEntityException If there is a DB Exception.
     */
    public List<Long> getSinkIdsFromUser(User source, String sinkName, String associationType, boolean useCache)
            throws GenericEntityException;

    /**
     * Operates on UserAssociations - gets MANY sinks from ONE user.
     *
     * @param source the source User
     * @param sinkName The type of entity
     * @param associationType the association type
     * @param useCache use cache flag
     * @param useSequence use sequence number flag
     * @return a list of sinks
     *
     * @throws GenericEntityException If there is a DB Exception.
     */
    public List<GenericValue> getSinkFromUser(User source, String sinkName, String associationType, boolean useCache, boolean useSequence)
            throws GenericEntityException;

    /**
     * Operates on UserAssociations - gets MANY users from ONE sink
     *
     * @param sink the Sink generic value
     * @param associationType association type
     * @param useCache use cache flag
     * @return a list of associated users, never null
     *
     * @throws GenericEntityException If there is a DB Exception.
     */
    public List<User> getUserFromSink(GenericValue sink, String associationType, boolean useCache)
            throws GenericEntityException;

    /**
     * Operates on UserAssociations - gets MANY users from ONE sink
     *
     * @param sink the Sink generic value
     * @param associationType association type
     * @param useCache use cache flag
     * @param useSequence use sequence number flag
     * @return a list of associated users, never null
     *
     * @throws GenericEntityException If there is a DB Exception.
     */
    public List<User> getUserFromSink(GenericValue sink, String associationType, boolean useCache, boolean useSequence)
            throws GenericEntityException;

    /**
     * Finds and returns a list of associated usernames, never null.
     *
     * @param sink the Sink generic value
     * @param associationType association type
     * @param useCache use cache flag
     * @param useSequence use sequence number flag
     * @return a list of associated usernames, never null
     *
     * @throws GenericEntityException If there is a DB Exception.
     */
    public List<String> getUsernamesFromSink(GenericValue sink, String associationType, boolean useCache, boolean useSequence)
            throws GenericEntityException;

    public GenericValue getAssociation(GenericValue source, GenericValue sink, String associationType)
            throws GenericEntityException;

    public GenericValue getAssociation(User user, GenericValue sink, String associationType)
            throws GenericEntityException;

    public List<Long> getSinkIdsFromSource(GenericValue source, String sinkEntity, String associationType)
            throws GenericEntityException;

    public List<Long> getSourceIdsFromSink(GenericValue sink, String sourceEntity, String associationType)
            throws GenericEntityException;


}
