package com.atlassian.jira.propertyset;

import com.opensymphony.module.propertyset.PropertySet;

/**
 * Provides a number of utility methods to get a handle on a property set in JIRA.  Please note that this class' sole
 * responsibility is to create a property set.  It does not cache this property set internal and repeated method
 * calls will create a new property set every time.  The returned PropertySet should be referenced by the caller to
 * avoid re-creating it every time.
 *
 * @since v3.12
 */
public interface JiraPropertySetFactory
{
    /**
     * Returns a {@link com.opensymphony.module.propertyset.PropertySet} for a particular entity.
     * Specifies a default entity id of 1.  Any access (read and write) to this PropertySet will result in a DB call.
     *
     * @param entityName The entity name to lookup your properties. E.g. jira.svn.plugin
     * @return a {@link com.opensymphony.module.propertyset.PropertySet} backed by a {@link com.opensymphony.module.propertyset.ofbiz.OFBizPropertySet}.
     */
    PropertySet buildNoncachingPropertySet(String entityName);

    /**
     * Returns a {@link com.opensymphony.module.propertyset.PropertySet} based on a ofbiz delegator.
     * Any access (read and write) to this PropertySet will result in a DB call.
     *
     * @param entityName The entity name to lookup your properties. E.g. jira.svn.plugin
     * @param entityId   The entity id if you multiple properties per entity name. (E.g. OSUser properties per user id)
     * @return a {@link com.opensymphony.module.propertyset.PropertySet} backed by a {@link com.opensymphony.module.propertyset.ofbiz.OFBizPropertySet}.
     */
    PropertySet buildNoncachingPropertySet(String entityName, Long entityId);

    /**
     * Returns a {@link com.opensymphony.module.propertyset.PropertySet} for a particular entity. Entries in this set are
     * cached in memory for better performance.Specifies a default entity id of 1.  Please note that this is a
     * write-through-cache, meaning that reads will be cached up, however any write will call through to the database
     * and invalidate the relevant cache entry.
     *
     * @param entityName The entity name to lookup your properties. E.g. jira.svn.plugin
     * @param bulkLoad   If true, all properties will be loaded during initialisation of the propertyset and cached
     * @return a {@link com.opensymphony.module.propertyset.PropertySet} backed by a caching property set
     */
    PropertySet buildCachingDefaultPropertySet(String entityName, boolean bulkLoad);

    /**
     * Returns a {@link com.opensymphony.module.propertyset.PropertySet}. Entries in this set are
     * cached in memory for better performance. Please note that this is a
     * write-through-cache, meaning that reads will be cached up, however any write will call through to the database
     * and invalidate the relevant cache entry.
     *
     * @param entityName The entity name to lookup your properties. E.g. jira.svn.plugin
     * @param entityId   The entity id if you multiple properties per entity name. (E.g. OSUser properties per user id)
     * @param bulkLoad   If true, all properties will be loaded during initialisation of the propertyset and cached
     * @return a {@link com.opensymphony.module.propertyset.PropertySet} backed by a caching property set
     */
    PropertySet buildCachingPropertySet(String entityName, Long entityId, boolean bulkLoad);

    /**
     * Returns a caching {@link com.opensymphony.module.propertyset.PropertySet} that wraps the provided propertyset.
     * Entries in this set are cached in memory for better performance. Please note that this is a
     * write-through-cache, meaning that reads will be cached up, however any write will call through to the database
     * and invalidate the relevant cache entry.
     *
     * @param propertySet A PropertySet to wrap by a caching property set
     * @param bulkLoad    If true, all properties will be loaded during initialisation of the propertyset and cached
     * @return a {@link com.opensymphony.module.propertyset.PropertySet} backed by a caching property set
     */
    PropertySet buildCachingPropertySet(PropertySet propertySet, boolean bulkLoad);

    /**
     * Returns an in memory copy of a property set from the database. This property set will not have its configuration
     * saved to the database on each change. It is up to the caller of this method to manually synchronize the returned
     * property set with the database.
     *
     * @param entityName The entity name to lookup your properties. E.g. jira.svn.plugin
     * @param entityId   The entity id if you multiple properties per entity name. (E.g. OSUser properties per user id)
     * @return a {@link com.opensymphony.module.propertyset.PropertySet} held completely in memory. Changes will not
     *         be written to the database.
     */
    PropertySet buildMemoryPropertySet(String entityName, Long entityId);
}
