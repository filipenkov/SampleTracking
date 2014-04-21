package com.atlassian.jira.propertyset;

import com.atlassian.jira.ofbiz.DefaultOfBizConnectionFactory;
import com.atlassian.jira.ofbiz.OfBizConnectionFactory;
import com.atlassian.jira.util.collect.MapBuilder;
import com.google.common.collect.Maps;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.module.propertyset.PropertySetManager;

import java.util.HashMap;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Default implementation of the JiraPropertySetFactory.  It relies heavily on the
 * {@link com.opensymphony.module.propertyset.ofbiz.OFBizPropertySet} and
 * {@link com.atlassian.jira.propertyset.JiraCachingPropertySet}.
 *
 * @since v3.12
 */
public class DefaultJiraPropertySetFactory implements JiraPropertySetFactory
{
    private static final Long DEFAULT_ENTITY_ID = 1L;
    private final OfBizConnectionFactory ofBizConnectionFactory = new DefaultOfBizConnectionFactory();

    public PropertySet buildNoncachingPropertySet(final String entityName)
    {
        return buildNoncachingPropertySet(entityName, DEFAULT_ENTITY_ID);
    }

    public PropertySet buildNoncachingPropertySet(final String entityName, final Long entityId)
    {
        final HashMap<String, Object> ofbizArgs =
                MapBuilder.<String, Object>newBuilder()
                        .add("delegator.name", ofBizConnectionFactory.getDelegatorName())
                        .add("entityName", entityName)
                        .add("entityId", entityId)
                        .toHashMap();

        return createPropertySet("ofbiz", ofbizArgs);
    }

    public PropertySet buildCachingDefaultPropertySet(final String entityName, final boolean bulkLoad)
    {
        return buildCachingPropertySet(buildNoncachingPropertySet(entityName), bulkLoad);
    }

    public PropertySet buildCachingPropertySet(final String entityName, final Long entityId, final boolean bulkLoad)
    {
        return buildCachingPropertySet(buildNoncachingPropertySet(entityName, entityId), bulkLoad);
    }

    public PropertySet buildCachingPropertySet(final PropertySet propertySet, final boolean bulkLoad)
    {
        checkNotNull(propertySet, "propertySet is a required parameter");

        final HashMap<String, Object> arguments =
                MapBuilder.<String, Object>newBuilder()
                        .add("PropertySet", propertySet)
                        .add("bulkload", bulkLoad)
                        .toHashMap();

        return createPropertySet("cached", arguments);
    }

    public PropertySet buildMemoryPropertySet(final String entityName, final Long entityId)
    {
        final PropertySet dbPropertySet = buildNoncachingPropertySet(entityName, entityId);
        final PropertySet memoryPropertySet = createPropertySet("memory", Maps.<String, Object>newHashMap());

        // Clone the property set.
        PropertySetManager.clone(dbPropertySet, memoryPropertySet);

        return memoryPropertySet;
    }

    PropertySet createPropertySet(final String propertySetDelegator, final HashMap<String, Object> ofbizArgs)
    {
        return PropertySetManager.getInstance(propertySetDelegator, ofbizArgs);
    }
}
