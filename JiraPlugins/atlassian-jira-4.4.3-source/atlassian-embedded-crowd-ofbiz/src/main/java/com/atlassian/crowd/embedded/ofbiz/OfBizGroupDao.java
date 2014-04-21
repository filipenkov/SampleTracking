package com.atlassian.crowd.embedded.ofbiz;

import com.atlassian.crowd.embedded.api.Directory;
import com.atlassian.crowd.embedded.api.SearchRestriction;
import com.atlassian.crowd.embedded.impl.IdentifierUtils;
import com.atlassian.crowd.embedded.ofbiz.db.DataAccessException;
import com.atlassian.crowd.embedded.ofbiz.db.OfBizHelper;
import com.atlassian.crowd.embedded.spi.DirectoryDao;
import com.atlassian.crowd.embedded.spi.GroupDao;
import com.atlassian.crowd.event.migration.XMLRestoreFinishedEvent;
import com.atlassian.crowd.exception.DirectoryNotFoundException;
import com.atlassian.crowd.exception.GroupNotFoundException;
import com.atlassian.crowd.model.group.DelegatingGroupWithAttributes;
import com.atlassian.crowd.model.group.Group;
import com.atlassian.crowd.model.group.GroupWithAttributes;
import com.atlassian.crowd.search.query.entity.EntityQuery;
import com.atlassian.crowd.util.BatchResult;
import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.google.common.base.Function;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.ofbiz.core.entity.DelegatorInterface;
import org.ofbiz.core.entity.EntityCondition;
import org.ofbiz.core.entity.EntityConditionList;
import org.ofbiz.core.entity.EntityExpr;
import org.ofbiz.core.entity.EntityOperator;
import org.ofbiz.core.entity.EntityUtil;
import org.ofbiz.core.entity.GenericValue;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static com.atlassian.crowd.embedded.ofbiz.GroupEntity.ENTITY;
import static com.atlassian.crowd.embedded.ofbiz.PrimitiveMap.of;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Collections.singletonList;

public class OfBizGroupDao implements GroupDao
{
    private final OfBizHelper ofBiz;
    private final DirectoryDao directoryDao;
    private final InternalMembershipDao membershipDao;
    private final ConcurrentMap<DirectoryEntityKey, OfBizGroup> groupCache = new ConcurrentHashMap<DirectoryEntityKey, OfBizGroup>();

    public OfBizGroupDao(final DelegatorInterface genericDelegator, final DirectoryDao directoryDao, final InternalMembershipDao membershipDao, final EventPublisher eventPublisher)
    {
        this.ofBiz = new OfBizHelper(genericDelegator);
        this.membershipDao = membershipDao;
        this.directoryDao = directoryDao;
        eventPublisher.register(this);
        buildCache();
    }

    public void removeAllGroups(long directoryId, Set<String> groupNames)
    {
        for (String groupName : groupNames)
        {
            try
            {
                remove(findByName(directoryId, groupName));
            }
            catch (GroupNotFoundException e)
            {
                // do nothing
            }
        }
    }

    /**
     * Listen for XMLRestoreFinishedEvents, which mean we need to clear any caches.
     * @param event XMLRestoreFinishedEvent
     */
    @SuppressWarnings ({ "UnusedDeclaration" })
    @EventListener
    public void onEvent(final XMLRestoreFinishedEvent event)
    {
        flushCache();
    }

    public OfBizGroup findByName(final long directoryId, final String name) throws GroupNotFoundException
    {
        // Try with the case we have been given.
        // This is an optimisation that should work in most JIRA instances where group and user names are really all lower case.
        OfBizGroup group = groupCache.get(DirectoryEntityKey.getKeyPreserveCase(directoryId, name));
        if (group != null)
        {
            return group;
        }
        group = groupCache.get(DirectoryEntityKey.getKey(directoryId, name));
        if (group == null)
        {
            // Because the SPI says we should do this.
            throw new GroupNotFoundException(name);
        }
        return group;
    }

    private GenericValue findGroupGenericValue(final Group group) throws GroupNotFoundException
    {
        return findGroup(checkNotNull(group).getDirectoryId(), group.getName());
    }

    private GenericValue findGroup(final long directoryId, final String name) throws GroupNotFoundException
    {
        final GenericValue groupGenericValue = EntityUtil.getOnly(findGroups(of(GroupEntity.DIRECTORY_ID, directoryId, GroupEntity.LOWER_NAME, toLowerCase(
            name))));
        if (groupGenericValue != null)
        {
            return groupGenericValue;
        }
        throw new GroupNotFoundException(name);
    }

    @SuppressWarnings("unchecked")
    private List<GenericValue> findGroups(final Map<String, Object> filter)
    {
        return ofBiz.findByAnd(GroupEntity.ENTITY, filter);
    }

    public GroupWithAttributes findByNameWithAttributes(final long directoryId, final String name)
            throws GroupNotFoundException
    {
        final GenericValue groupGenericValue = findGroup(directoryId, name);
        final List<GenericValue> attributesGenericValue = findAttributes(directoryId, groupGenericValue.getLong(GroupEntity.ID));
        return new DelegatingGroupWithAttributes(OfBizGroup.from(groupGenericValue), OfBizAttributesBuilder.toAttributes(attributesGenericValue));
    }

    public BatchResult<Group> addAll(Set<? extends Group> groups) throws DirectoryNotFoundException
    {
        BatchResult<Group> results = new BatchResult<Group>(groups.size());
        for (Group group : groups)
        {
            try
            {
                final Group addedGroup = add(group);
                results.addSuccess(addedGroup);
            }
            catch (DataAccessException e)
            {
                // Try to catch problems so that at least the *other* groups will be added
                results.addFailure(group);
            }
        }
        return results;
    }

    public synchronized Group add(final Group group)
    {
        return add(group, false);
    }

    public Group addLocal(final Group group)
    {
        return add(group, true);
    }

    private synchronized Group add(final Group group, boolean local)
    {
        GenericValue gvGroup;
        // Create the new Group in the DB
        final Timestamp currentTimestamp = getCurrentTimestamp();
        final Map<String, Object> groupEntity = GroupEntity.getData(group, currentTimestamp, currentTimestamp, local);
        gvGroup = ofBiz.createValue(GroupEntity.ENTITY, groupEntity);
        // Convert GenericValue to an object
        OfBizGroup ofBizGroup = OfBizGroup.from(gvGroup);
        // Shove it in the cache
        putGroupInCache(ofBizGroup);
        return ofBizGroup;
    }

    public synchronized Group update(final Group group) throws GroupNotFoundException
    {
        // Get the latest GenericValue from the DB
        final GenericValue groupGenericValue = findGroupGenericValue(group);
        // Update the relevant values
        groupGenericValue.set(GroupEntity.ACTIVE, BooleanUtils.toInteger(group.isActive()));
        groupGenericValue.set(GroupEntity.UPDATED_DATE, getCurrentTimestamp());
        groupGenericValue.set(GroupEntity.DESCRIPTION, group.getDescription());
        groupGenericValue.set(GroupEntity.LOWER_DESCRIPTION, toLowerCase(group.getDescription()));
        // Save to DB
        storeGroup(groupGenericValue);
        // Convert GenericValue to an object
        OfBizGroup ofBizGroup = OfBizGroup.from(groupGenericValue);
        // Shove it in the cache
        putGroupInCache(ofBizGroup);
        return ofBizGroup;
    }

    private Timestamp getCurrentTimestamp()
    {
        return new Timestamp(System.currentTimeMillis());
    }

    private String toLowerCase(final String value)
    {
        if (value == null)
        {
            return null;
        }
        return IdentifierUtils.toLowerCase(value);
    }

    private void storeGroup(final GenericValue groupGenericValue)
    {
        ofBiz.store(groupGenericValue);
    }

    public Group rename(final Group group, final String newName)
    {
        throw new UnsupportedOperationException("Renaming groups is not supported!");
    }

    public void storeAttributes(final Group group, final Map<String, Set<String>> attributes)
            throws GroupNotFoundException
    {
        for (final Map.Entry<String, Set<String>> attribute : checkNotNull(attributes).entrySet())
        {
            // remove attributes before adding new ones.
            // Duplicate key values are allowed, but we always add as a complete set under the key.
            removeAttribute(group, attribute.getKey());
            if ((attribute.getValue() != null) && !attribute.getValue().isEmpty())
            {
                storeAttributeValues(group, attribute.getKey(), attribute.getValue());
            }
        }
    }

    private void storeAttributeValues(final Group group, final String name, final Set<String> values)
            throws GroupNotFoundException
    {
        for (final String value : values)
        {
            if (StringUtils.isNotEmpty(value))
            {
                storeAttributeValue(group, name, value);
            }
        }
    }

    private void storeAttributeValue(final Group group, final String name, final String value)
            throws GroupNotFoundException
    {
        final GenericValue groupGenericValue = findGroupGenericValue(group);
        ofBiz.createValue(GroupAttributeEntity.ENTITY, GroupAttributeEntity.getData(group.getDirectoryId(),
            groupGenericValue.getLong(GroupEntity.ID), name, value));
    }

    @SuppressWarnings("unchecked")
    private List<GenericValue> findAttributes(final long directoryId, final long groupId)
    {
        return ofBiz.findByAnd(GroupAttributeEntity.ENTITY, of(GroupAttributeEntity.DIRECTORY_ID, directoryId,
            GroupAttributeEntity.GROUP_ID, groupId));
    }

    public void removeAttribute(final Group group, final String attributeName) throws GroupNotFoundException
    {
        checkNotNull(attributeName);
        final GenericValue gv = findGroupGenericValue(group);
        ofBiz.removeByAnd(GroupAttributeEntity.ENTITY, of(GroupAttributeEntity.GROUP_ID, gv.getLong(GroupEntity.ID),
            GroupAttributeEntity.NAME, attributeName));
    }

    public synchronized void remove(final Group group) throws GroupNotFoundException
    {
        final GenericValue groupGenericValue = findGroupGenericValue(group);
        // remove memberships
        membershipDao.removeAllMembersFromGroup(group);
        membershipDao.removeAllGroupMemberships(group);

        ofBiz.removeByAnd(GroupAttributeEntity.ENTITY, of(GroupAttributeEntity.GROUP_ID, groupGenericValue.getLong(GroupEntity.ID)));
        ofBiz.removeValue(groupGenericValue);
        groupCache.remove(DirectoryEntityKey.getKey(group));
    }

    public <T> List<T> search(final long directoryId, final EntityQuery<T> query)
    {
        final SearchRestriction searchRestriction = query.getSearchRestriction();
        final EntityCondition baseCondition = new GroupEntityConditionFactory().getEntityConditionFor(searchRestriction);
        if (baseCondition == null)
        {
            // The search restriction is a NullRestriction (or null) then we can just throw back the whole of the groups cache.
            return (getAllGroupsFromCache(directoryId, query.getReturnType()));
        }
        final EntityExpr directoryCondition = new EntityExpr(GroupEntity.DIRECTORY_ID, EntityOperator.EQUALS, directoryId);
        final EntityCondition entityCondition;
        final List<EntityCondition> entityConditions = new ArrayList<EntityCondition>(2);
        entityConditions.add(baseCondition);
        entityConditions.add(directoryCondition);
        entityCondition = new EntityConditionList(entityConditions, EntityOperator.AND);

        List<GenericValue> results;
        results = ofBiz.findByCondition(ENTITY, entityCondition, null, singletonList(GroupEntity.NAME));

        ArrayList<T> typedResults = new ArrayList<T>(results.size());
        final Function<GenericValue, T> valueFunction = getTransformer(query.getReturnType());
        for (GenericValue result : results)
        {
            typedResults.add(valueFunction.apply(result));
        }

        return typedResults;
    }

    @SuppressWarnings ({ "unchecked" })
    private <T> List<T> getAllGroupsFromCache(final long directoryId, final Class<T> returnType)
    {
        final List<OfBizGroup> allGroups = new ArrayList<OfBizGroup>();
        for (OfBizGroup group : groupCache.values())
        {
            if (group.getDirectoryId() == directoryId)
            {
                allGroups.add(group);
            }
        }
        if (returnType.isAssignableFrom(String.class))
        {
            // Transform from the lower case name to the case preserving name
            Function<OfBizGroup, String> valueFunction = new Function<OfBizGroup, String>()
            {
                public String apply(final OfBizGroup from)
                {
                    return from.getName();
                }
            };
            ArrayList<T> typedResults = new ArrayList<T>(allGroups.size());
            for (OfBizGroup group : allGroups)
            {
                typedResults.add((T) valueFunction.apply(group));
            }
            return typedResults;
        }
        // If the required return type is anything that OfBizGroup can be cast to, then return OfBizGroup Objects
        if (returnType.isAssignableFrom(OfBizGroup.class))
        {
            return (List<T>) allGroups;
        }
        throw new IllegalArgumentException("Class type for return values ('" + returnType + "') is not 'String' or 'Group'");
    }

    private <T> Function<GenericValue, T> getTransformer(final Class<T> returnType)
    {
        //noinspection unchecked
        return (Function<GenericValue, T>) (returnType.equals(String.class) ? TO_GROUPNAME_FUNCTION : TO_GROUP_FUNCTION);
    }

    private static final Function<GenericValue, String> TO_GROUPNAME_FUNCTION = new Function<GenericValue, String>()
    {
        public String apply(final GenericValue gv)
        {
            return gv.getString(GroupEntity.NAME);
        }
    };

    private static final Function<GenericValue, OfBizGroup> TO_GROUP_FUNCTION = new Function<GenericValue, OfBizGroup>()
    {
        public OfBizGroup apply(final GenericValue gv)
        {
            return OfBizGroup.from(gv);
        }
    };

    public synchronized void flushCache()
    {
        groupCache.clear();
        buildCache();
    }

    private void buildCache()
    {
        // Load all Groups
        for (Directory directory : directoryDao.findAll())
        {
            List<GenericValue> groupGenericValues = findGroups(of(UserEntity.DIRECTORY_ID, directory.getId()));
            for (GenericValue groupGenericValue : groupGenericValues)
            {
                putGroupInCache(TO_GROUP_FUNCTION.apply(groupGenericValue));
            }
        }
    }

    private void putGroupInCache(OfBizGroup group)
    {
        DirectoryEntityKey key = DirectoryEntityKey.getKey(group.getDirectoryId(), group.getName());
        groupCache.put(key, group);
    }
}
