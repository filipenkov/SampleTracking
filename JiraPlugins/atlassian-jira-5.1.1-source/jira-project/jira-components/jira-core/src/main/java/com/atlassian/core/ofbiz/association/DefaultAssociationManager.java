package com.atlassian.core.ofbiz.association;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.user.UserUtils;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.DelegatorInterface;
import org.ofbiz.core.entity.EntityUtil;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericPK;
import org.ofbiz.core.entity.GenericValue;
import org.ofbiz.core.util.UtilMisc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This was taken from atlassian-ofbiz and placed into its now rightful home of JIRA.
 *
 * @since 4.3
 */
public class DefaultAssociationManager implements AssociationManager
{
    private static final Logger log = Logger.getLogger(DefaultAssociationManager.class);
    private final DelegatorInterface delegator;

    public DefaultAssociationManager(DelegatorInterface delegator)
    {
        this.delegator = delegator;
    }

    /**
     * Create an association between two entities, given a particular association type.
     * <p/>
     * If the association already exists - it will not be created.
     *
     * @return The new association, or the existing association if it already existed.
     */
    public GenericValue createAssociation(GenericValue source, GenericValue sink, String associationType)
            throws GenericEntityException
    {
        final GenericValue existingAssociation = getAssociation(source, sink, associationType);
        if (existingAssociation == null)
        {
            GenericValue v = delegator.makeValue("NodeAssociation", UtilMisc.toMap(
                    "associationType", associationType,
                    "sourceNodeId", source.getLong("id"),
                    "sourceNodeEntity", source.getEntityName(),
                    "sinkNodeId", sink.getLong("id"),
                    "sinkNodeEntity", sink.getEntityName()));
            v.create();
            return v;
        }
        return existingAssociation;
    }

    public GenericValue createAssociation(Long sourceNodeId, String sourceNodeEntity, Long sinkNodeId, String sinkNodeEntity, String associationType)
            throws GenericEntityException
    {
        final GenericValue existingAssociation = getAssociation(sourceNodeId, sourceNodeEntity, sinkNodeId, sinkNodeEntity, associationType);
        if (existingAssociation == null)
        {
            GenericValue v = delegator.makeValue("NodeAssociation", UtilMisc.toMap(
                    "associationType", associationType,
                    "sourceNodeId", sourceNodeId,
                    "sourceNodeEntity", sourceNodeEntity,
                    "sinkNodeId", sinkNodeId,
                    "sinkNodeEntity", sinkNodeEntity));
            v.create();
            return v;
        }
        return existingAssociation;
    }

    public GenericValue createAssociation(String userName, Long sinkNodeId, String sinkNodeEntity, String associationType)
            throws GenericEntityException
    {
        final GenericValue existingAssociation = getAssociation(userName, sinkNodeId, sinkNodeEntity, associationType);
        if (existingAssociation == null)
        {
            GenericValue v = delegator.makeValue("UserAssociation", UtilMisc.toMap(
                    "associationType", associationType,
                    "sourceName", userName,
                    "sinkNodeId", sinkNodeId,
                    "sinkNodeEntity", sinkNodeEntity));
            v.create();
            v.refresh();
            return v;
        }
        return existingAssociation;
    }

    public GenericValue createAssociation(User user, GenericValue sink, String associationType)
            throws GenericEntityException
    {
        final GenericValue v = delegator.makeValue("UserAssociation", UtilMisc.toMap(
                "associationType", associationType,
                "sourceName", user.getName(),
                "sinkNodeId", sink.getLong("id"),
                "sinkNodeEntity", sink.getEntityName()));
        v.create();
        v.refresh();
        return v;
    }


    public void removeAssociation(User user, GenericValue sink, String associationType) throws GenericEntityException
    {
        removeAssociation(user.getName(), sink, associationType);
    }

    public void removeAssociation(String username, GenericValue sink, String associationType)
            throws GenericEntityException
    {
        final Map<String, Object> fields = new HashMap<String, Object>();
        fields.put("sinkNodeId", sink.getLong("id"));
        fields.put("sinkNodeEntity", sink.getEntityName());
        fields.put("sourceName", username);
        fields.put("associationType", associationType);
        delegator.removeByPrimaryKey(delegator.makePK("UserAssociation", fields));
    }

    public void removeAssociation(GenericValue source, GenericValue sink, String associationType)
            throws GenericEntityException
    {
        final Map<String, Object> fields = new HashMap<String, Object>();
        fields.put("sinkNodeId", sink.getLong("id"));
        fields.put("sinkNodeEntity", sink.getEntityName());
        fields.put("sourceNodeId", source.getLong("id"));
        fields.put("sourceNodeEntity", source.getEntityName());
        fields.put("associationType", associationType);
        delegator.removeByPrimaryKey(delegator.makePK("NodeAssociation", fields));
    }

    /**
     * Remove all entity<->entity associations, given the source
     */
    public void removeAssociationsFromSource(GenericValue source) throws GenericEntityException
    {
        final Map<String, Object> fields = new HashMap<String, Object>();
        fields.put("sourceNodeId", source.getLong("id"));
        fields.put("sourceNodeEntity", source.getEntityName());
        delegator.removeByAnd("NodeAssociation", fields);
    }

    public void removeAssociationsFromSink(GenericValue sink) throws GenericEntityException
    {
        final Map<String, Object> fields = new HashMap<String, Object>();
        fields.put("sinkNodeId", sink.getLong("id"));
        fields.put("sinkNodeEntity", sink.getEntityName());
        delegator.removeByAnd("NodeAssociation", fields);
    }

    /**
     * Remove all user<->entity associations, given the entity
     */
    public void removeUserAssociationsFromSink(GenericValue sink) throws GenericEntityException
    {
        final Map<String, Object> fields = new HashMap<String, Object>();
        fields.put("sinkNodeId", sink.getLong("id"));
        fields.put("sinkNodeEntity", sink.getEntityName());
        delegator.removeByAnd("UserAssociation", fields);
    }

    /**
     * Remove all user<->entity associations, given the entity and association type
     */
    public void removeUserAssociationsFromSink(GenericValue sink, String associationType) throws GenericEntityException
    {
        final Map<String, Object> fields = new HashMap<String, Object>();
        fields.put("sinkNodeId", sink.getLong("id"));
        fields.put("sinkNodeEntity", sink.getEntityName());
        fields.put("associationType", associationType);
        delegator.removeByAnd("UserAssociation", fields);
    }


    /**
     * Remove all user<->entity associations, given the user
     */
    public void removeUserAssociationsFromUser(User user) throws GenericEntityException
    {
        final Map<String, Object> fields = new HashMap<String, Object>();
        fields.put("sourceName", user.getName());
        delegator.removeByAnd("UserAssociation", fields);
    }

    public void removeUserAssociationsFromUser(final User user, final String associationType)
            throws GenericEntityException
    {
        final Map<String, Object> fields = new HashMap<String, Object>();
        fields.put("sourceName", user.getName());
        fields.put("associationType", associationType);
        delegator.removeByAnd("UserAssociation", fields);
    }

    public void removeUserAssociationsFromUser(final User user, final String associationType, final String entityName)
            throws GenericEntityException
    {
        final Map<String, Object> fields = new HashMap<String, Object>();
        fields.put("sourceName", user.getName());
        fields.put("associationType", associationType);
        fields.put("sinkNodeEntity", entityName);
        delegator.removeByAnd("UserAssociation", fields);
    }


    /**
     * Swap all assocaitions of a particular type from one sink to another.
     * <p/>
     * Used in ComponentDelete and VersionDelete.
     */
    public void swapAssociation(String sourceEntityName, String associationType, GenericValue fromSink, GenericValue toSink)
            throws GenericEntityException
    {
        final List<GenericValue> sources = getSourceFromSink(fromSink, sourceEntityName, associationType, false);
        swapAssociation(sources, associationType, fromSink, toSink);
    }

    public void swapAssociation(List<GenericValue> entities, String associationType, GenericValue fromSink, GenericValue toSink)
            throws GenericEntityException
    {
        for (final GenericValue entity : entities)
        {
            createAssociation(entity, toSink, associationType);
            removeAssociation(entity, fromSink, associationType);
        }
    }

    List<GenericValue> getAssociations(String associationName, Map<String, ?> fields, boolean useCache, boolean useSequence)
            throws GenericEntityException
    {
        List<GenericValue> result;
        if (useCache)
        {
            result = delegator.findByAndCache(associationName, fields);
        }
        else
        {
            result = delegator.findByAnd(associationName, fields);
        }

        if (useSequence)
        {
            result = EntityUtil.orderBy(result, UtilMisc.toList("sequence"));
        }
        return result;
    }

    public GenericValue getAssociation(GenericValue source, GenericValue sink, String associationType)
            throws GenericEntityException
    {
        return getAssociation(source.getLong("id"), source.getEntityName(), sink.getLong("id"), sink.getEntityName(), associationType);
    }

    public GenericValue getAssociation(User user, GenericValue sink, String associationType)
            throws GenericEntityException
    {
        if (user == null)
        {
            return null;
        }

        return EntityUtil.getOnly(delegator.findByAnd("UserAssociation", UtilMisc.toMap(
                "associationType", associationType,
                "sourceName", user.getName(),
                "sinkNodeId", sink.getLong("id"),
                "sinkNodeEntity", sink.getEntityName())));
    }

    /**
     * Operates on NodeAssociations - gets MANY sinks from ONE source
     */
    public List<GenericValue> getSinkFromSource(GenericValue source, String sinkName, String associationType, boolean useCache)
            throws GenericEntityException
    {
        return getSinkFromSource(source, sinkName, associationType, useCache, false);
    }

    public List<GenericValue> getSinkFromSource(GenericValue source, String sinkName, String associationType, boolean useCache, boolean useSequence)
            throws GenericEntityException
    {
        if (source == null)
        {
            throw new IllegalArgumentException("Source GenericValue can not be null.");
        }

        final List<GenericValue> result = getSinkIdsFromSource(source, sinkName, associationType, useCache, useSequence);

        final List<GenericValue> outList = new ArrayList<GenericValue>(result.size());
        for (final GenericValue value : result)
        {
            GenericValue byPrimaryKey;
            if (useCache)
            {
                byPrimaryKey = delegator.findByPrimaryKeyCache(sinkName, UtilMisc.toMap("id", value.getLong("sinkNodeId")));
            }
            else
            {
                byPrimaryKey = delegator.findByPrimaryKey(sinkName, UtilMisc.toMap("id", value.getLong("sinkNodeId")));
            }

            if (byPrimaryKey != null)
            {
                outList.add(byPrimaryKey);
            }
        }
        return result.isEmpty() ? Collections.<GenericValue>emptyList() : outList;
    }

    private List<GenericValue> getSinkIdsFromSource(GenericValue source, String sinkName, String associationType, boolean useCache, boolean useSequence)
            throws GenericEntityException
    {
        final Map<String, Object> fields = new HashMap<String, Object>();
        fields.put("sourceNodeId", source.getLong("id"));
        fields.put("sourceNodeEntity", source.getEntityName());
        fields.put("associationType", associationType);
        fields.put("sinkNodeEntity", sinkName);
        return getAssociations("NodeAssociation", fields, useCache, useSequence);
    }

    /**
     * Operates on NodeAssociations - gets MANY sources from ONE sink
     */
    public List<GenericValue> getSourceFromSink(GenericValue sink, String sourceName, String associationType, boolean useCache)
            throws GenericEntityException
    {
        return getSourceFromSink(sink, sourceName, associationType, useCache, false);
    }

    /**
     * Operates on NodeAssociations - gets MANY sources from ONE sink
     */
    public List<GenericValue> getSourceFromSink(GenericValue sink, String sourceName, String associationType, boolean useCache, boolean useSequence)
            throws GenericEntityException
    {
        if (sink == null)
        {
            throw new IllegalArgumentException("Sink GenericValue can not be null.");
        }
        final List<GenericValue> result = getSourceIdsFromSink(sink, associationType, sourceName, useCache, useSequence);
        final List<GenericValue> outList = new ArrayList<GenericValue>(result.size());
        for (final GenericValue value : result)
        {
            GenericPK pk = delegator.makePK(sourceName, UtilMisc.toMap("id", value.getLong("sourceNodeId")));
            GenericValue byPrimaryKey;
            if (useCache)
            {
                byPrimaryKey = delegator.findByPrimaryKeyCache(pk);
            }
            else
            {
                byPrimaryKey = delegator.findByPrimaryKey(pk);
            }

            if (byPrimaryKey != null)
            {
                outList.add(byPrimaryKey);
            }
        }
        return result.isEmpty() ? Collections.<GenericValue>emptyList() : outList;
    }

    private List<GenericValue> getSourceIdsFromSink(GenericValue sink, String associationType, String sourceName, boolean useCache, boolean useSequence)
            throws GenericEntityException
    {
        final Map<String, Object> fields = new HashMap<String, Object>();
        fields.put("sinkNodeId", sink.getLong("id"));
        fields.put("sinkNodeEntity", sink.getEntityName());
        fields.put("associationType", associationType);
        fields.put("sourceNodeEntity", sourceName);
        return getAssociations("NodeAssociation", fields, useCache, useSequence);
    }

    /**
     * Operates on UserAssociations - gets MANY sinks from ONE user
     */
    public List<GenericValue> getSinkFromUser(User source, String sinkName, String associationType, boolean useCache)
            throws GenericEntityException
    {
        return getSinkFromUser(source, sinkName, associationType, useCache, false);
    }

    public List<Long> getSinkIdsFromUser(final User source, final String sinkName, final String associationType, final boolean useCache)
            throws GenericEntityException
    {
        final List<GenericValue> result = getAssociationsForUser(source, sinkName, associationType, useCache, false);
        final List<Long> out = new ArrayList<Long>(result.size());
        for (GenericValue value : result)
        {
            out.add(value.getLong("sinkNodeId"));
        }
        return out;
    }

    /**
     * Operates on UserAssociations - gets MANY sinks from ONE user
     */
    public List<GenericValue> getSinkFromUser(User source, String sinkName, String associationType, boolean useCache, boolean useSequence)
            throws GenericEntityException
    {
        final List<GenericValue> result = getAssociationsForUser(source, sinkName, associationType, useCache, useSequence);

        final List<GenericValue> outList = new ArrayList<GenericValue>(result.size());
        for (final GenericValue value : result)
        {
            GenericPK pk = delegator.makePK(sinkName, UtilMisc.toMap("id", value.getLong("sinkNodeId")));
            GenericValue byPrimaryKey;
            if (useCache)
            {
                byPrimaryKey = delegator.findByPrimaryKeyCache(pk);
            }
            else
            {
                byPrimaryKey = delegator.findByPrimaryKey(pk);
            }

            if (byPrimaryKey != null)
            {
                outList.add(byPrimaryKey);
            }
        }
        return result.isEmpty() ? Collections.<GenericValue>emptyList() : outList;
    }

    private List<GenericValue> getAssociationsForUser(final User source, final String sinkName, final String associationType, final boolean useCache, final boolean useSequence)
            throws GenericEntityException
    {
        if (source == null)
        {
            throw new IllegalArgumentException("User can not be null.");
        }
        final Map<String, Object> fields = new HashMap<String, Object>();
        fields.put("sourceName", source.getName());
        fields.put("associationType", associationType);
        fields.put("sinkNodeEntity", sinkName);
        return getAssociations("UserAssociation", fields, useCache, useSequence);
    }

    /**
     * Operates on UserAssociations - gets MANY users from ONE sink
     */
    public List<User> getUserFromSink(GenericValue sink, String associationType, boolean useCache)
            throws GenericEntityException
    {
        return getUserFromSink(sink, associationType, useCache, false);
    }

    /**
     * Operates on UserAssociations - gets MANY users from ONE sink
     */
    public List<User> getUserFromSink(GenericValue sink, String associationType, boolean useCache, boolean useSequence)
            throws GenericEntityException
    {
        final List<String> usernames = getUsernamesFromSink(sink, associationType, useCache, useSequence);
        final List<User> users = new ArrayList<User>(usernames.size());
        for (final String username : usernames)
        {
            final User user = UserUtils.getUser(username);
            if (user != null)
            {
                users.add(user);
            }
            else
            {
                log.error("Cannot find user with username '" + username + "'.");
            }
        }
        return users;
    }

    public List<String> getUsernamesFromSink(GenericValue sink, String associationType, boolean useCache, boolean useSequence)
            throws GenericEntityException
    {
        if (sink == null)
        {
            throw new IllegalArgumentException("Sink GenericValue can not be null.");
        }

        final Map<String, Object> fields = new HashMap<String, Object>();
        fields.put("sinkNodeId", sink.getLong("id"));
        fields.put("sinkNodeEntity", sink.getEntityName());
        fields.put("associationType", associationType);

        final List<GenericValue> results = getAssociations("UserAssociation", fields, useCache, useSequence);
        if (results.isEmpty())
        {
            return Collections.emptyList();
        }

        final List<String> outList = new ArrayList<String>(results.size());
        for (final GenericValue result : results)
        {
            outList.add(result.getString("sourceName"));
        }
        return outList;
    }

    public List<Long> getSinkIdsFromSource(GenericValue source, String sinkEntity, String associationType)
            throws GenericEntityException
    {
        List<GenericValue> sinks = getSinkIdsFromSource(source, sinkEntity, associationType, false, false);

        if (sinks != null && !sinks.isEmpty())
        {
            List<Long> sinkIds = new ArrayList<Long>();
            for (final GenericValue sink : sinks)
            {
                sinkIds.add(sink.getLong("sinkNodeId"));
            }
            return sinkIds;
        }

        return Collections.emptyList();
    }

    public List<Long> getSourceIdsFromSink(GenericValue sink, String sourceEntity, String associationType)
            throws GenericEntityException
    {
        List<GenericValue> sources = getSourceIdsFromSink(sink, associationType, sourceEntity, false, false);

        if (sources != null && !sources.isEmpty())
        {
            List<Long> sourceIds = new ArrayList<Long>();
            for (final GenericValue source : sources)
            {
                sourceIds.add(source.getLong("sourceNodeId"));
            }
            return sourceIds;
        }

        return Collections.emptyList();
    }

    private GenericValue getAssociation(Long sourceNodeId, String sourceNodeEntity, Long sinkNodeId, String sinkNodeEntity, String associationType)
            throws GenericEntityException
    {
        return EntityUtil.getOnly(delegator.findByAnd("NodeAssociation", UtilMisc.toMap("associationType", associationType, "sourceNodeId", sourceNodeId, "sourceNodeEntity", sourceNodeEntity, "sinkNodeId", sinkNodeId, "sinkNodeEntity", sinkNodeEntity)));
    }

    private GenericValue getAssociation(String sourceName, Long sinkNodeId, String sinkNodeEntity, String associationType)
            throws GenericEntityException
    {
        return EntityUtil.getOnly(delegator.findByAnd("UserAssociation", UtilMisc.toMap("associationType", associationType, "sourceName", sourceName, "sinkNodeId", sinkNodeId, "sinkNodeEntity", sinkNodeEntity)));
    }
}
