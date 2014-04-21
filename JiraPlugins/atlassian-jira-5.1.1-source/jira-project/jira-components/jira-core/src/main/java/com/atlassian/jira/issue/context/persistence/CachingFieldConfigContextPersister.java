package com.atlassian.jira.issue.context.persistence;

import com.atlassian.annotations.Internal;
import com.atlassian.bandana.BandanaContext;
import com.atlassian.event.api.EventListener;
import com.atlassian.jira.EventComponent;
import com.atlassian.jira.cache.GoogleCacheInstruments;
import com.atlassian.jira.event.ClearCacheEvent;
import com.atlassian.jira.extension.Startable;
import com.atlassian.jira.issue.context.JiraContextNode;
import com.atlassian.jira.issue.context.manager.JiraContextTreeManager;
import com.atlassian.jira.issue.fields.config.FieldConfigScheme;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectCategory;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.util.map.CacheObject;
import com.google.common.base.Predicate;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import org.ofbiz.core.entity.GenericValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Caching decorator for {@link FieldConfigContextPersister}. This corresponds to the <code>configurationcontext</code>
 * table, which is essentially an association table between {@link com.atlassian.jira.issue.fields.CustomField
 * CustomField} and either a {@link ProjectCategory} or a {@link Project}, but not both (in practice it is always a
 * Project). Each association also has {@link FieldConfigScheme} as a property of the association, and this is where
 * things like default values for custom fields are ultimately stored. When both the project and projectCategory are
 * null, then that database row is in fact a special row holding the FieldConfigScheme for the "Global Context".
 * <p/>
 * See <a href="https://extranet.atlassian.com/x/koEPJg">CustomField Configuration - DB Entity Model</a> for a more
 * in-depth explanation of how this all works.
 *
 * @since v5.1
 */
@Internal
@EventComponent
public class CachingFieldConfigContextPersister implements FieldConfigContextPersister, Startable
{
    private static final Logger log = LoggerFactory.getLogger(CachingFieldConfigContextPersister.class);

    /**
     * The Guava cache where we store all query results. The {@link CacheKey#contextParams} is essentially the WHERE
     * clause that gets passed down to entity-engine, so we needs to invalidate cache entries based on that.
     */
    private final Cache<CacheKey, CacheObject> cache = CacheBuilder.newBuilder().build(new CacheLoader<CacheKey, CacheObject>()
    {
        @Override
        public CacheObject load(CacheKey key) throws Exception
        {
            return CacheObject.wrap(delegate.retrieve(key.contextNode, key.customField));
        }
    });

    /**
     * The real FieldConfigContextPersisterImpl.
     */
    private final FieldConfigContextPersister delegate;

    /**
     * Creates a new CachingFieldConfigContextPersister that wraps a new FieldConfigContextPersisterImpl instance.
     *
     * @param delegator the OfBizDelegator
     * @param projectManager the ProjectManager
     * @param treeManager the JiraContextTreeManager
     */
    @SuppressWarnings ("UnusedDeclaration")
    public CachingFieldConfigContextPersister(OfBizDelegator delegator, ProjectManager projectManager, JiraContextTreeManager treeManager)
    {
        this.delegate = new FieldConfigContextPersisterImpl(delegator, projectManager, treeManager);
    }

    /**
     * Registers this CachingFieldConfigContextPersister's cache in the JIRA instrumentation.
     *
     * @throws Exception
     */
    @Override
    public void start() throws Exception
    {
        new GoogleCacheInstruments(CachingFieldConfigContextPersister.class.getSimpleName()).addCache(cache).install();
    }

    /**
     * Clears this CachingFieldConfigContextPersister's cache upon receiving a ClearCacheEvent.
     *
     * @param clearCacheEvent a ClearCacheEvent
     */
    @EventListener
    @SuppressWarnings ("UnusedParameters")
    public void onClearCache(ClearCacheEvent clearCacheEvent)
    {
        invalidateAll();
    }

    //<editor-fold desc="FieldConfigContextPersister methods">
    @Override
    public List<JiraContextNode> getAllContextsForCustomField(String key)
    {
        return delegate.getAllContextsForCustomField(key);
    }

    @Override
    public List<JiraContextNode> getAllContextsForConfigScheme(FieldConfigScheme fieldConfigScheme)
    {
        return delegate.getAllContextsForConfigScheme(fieldConfigScheme);
    }

    @Override
    public void removeContextsForConfigScheme(Long fieldConfigSchemeId)
    {
        delegate.removeContextsForConfigScheme(fieldConfigSchemeId);
        invalidateMatchingValues(fieldConfigSchemeId);
    }

    @Override
    public void removeContextsForProject(final GenericValue project)
    {
        delegate.removeContextsForProject(project);
        invalidateMatchingKeys(new ProjectIdMatcher(project.getLong("id")));
    }

    @Override
    public void removeContextsForProject(final Project project)
    {
        delegate.removeContextsForProject(project);
        invalidateMatchingKeys(new ProjectIdMatcher(project.getId()));
    }

    @Override
    public void removeContextsForProjectCategory(ProjectCategory projectCategory)
    {
        delegate.removeContextsForProjectCategory(projectCategory);
        invalidateMatchingKeys(new ProjectCategoryMatcher(projectCategory));
    }
    //</editor-fold>

    //<editor-fold desc="BandanaPersister methods">
    @Override
    public Object retrieve(BandanaContext context, String key)
    {
        return cache.getUnchecked(new CacheKey((JiraContextNode) context, key)).getValue();
    }

    @Override
    public void store(BandanaContext context, final String customField, Object fieldConfigScheme)
    {
        delegate.store(context, customField, fieldConfigScheme);
        invalidateMatchingKeys(new CustomFieldMatcher(customField));
    }

    @Override
    public void flushCaches()
    {
        delegate.flushCaches();
        invalidateAll();
    }

    @Override
    public void remove(final BandanaContext context)
    {
        delegate.remove(context);
        invalidateMatchingKeys(new ContextMatcher(context));
    }

    @Override
    public void remove(BandanaContext context, String customField)
    {
        delegate.remove(context, customField);
        invalidateMatchingKeys(new CustomFieldMatcher(customField));
    }
    //</editor-fold>

    /**
     * Clears this instance's cache.
     */
    private void invalidateAll()
    {
        cache.invalidateAll();
        if (log.isTraceEnabled())
        {
            log.trace("called invalidateAll()", new Throwable());
        }
    }

    /**
     * Invalidates cache entries where the cache key matches the given predicate.
     *
     * @param predicate a Predicate
     */
    private void invalidateMatchingKeys(@Nonnull Predicate<CacheKey> predicate)
    {
        for (CacheKey key : cache.asMap().keySet())
        {
            if (predicate.apply(key))
            {
                cache.invalidate(key);
            }
        }
    }

    /**
     * Invalidates cache entries where the value matches the given <code>fieldConfigSchemeId</code>.
     *
     * @param fieldConfigSchemeId a Long that contains a field config scheme ID
     */
    private void invalidateMatchingValues(@Nonnull Long fieldConfigSchemeId)
    {
        for (Map.Entry<CacheKey, CacheObject> entry : cache.asMap().entrySet())
        {
            if (fieldConfigSchemeId.equals(entry.getValue().getValue()))
            {
                cache.invalidate(entry.getKey());
            }
        }
    }

    /**
     * This cache key is messed up because there are in fact three different types of JiraContextNode, but the database
     * equality is not the same as object equality in Java. At the database level, JIRA uses the output of {@link
     * JiraContextNode#appendToParamsMap(java.util.Map)} to determine equality, which is in contrast to the
     * implementation of the JiraContextNode equals/hashCode methods.
     */
    static final class CacheKey
    {
        /**
         * The context parameters, as given by {@link com.atlassian.jira.issue.context.JiraContextNode#appendToParamsMap(java.util.Map)},
         */
        private final Map<String, Object> contextParams;

        /**
         * The custom field id (as a string).
         */
        private final String customField;

        /**
         * This is not really used as a cache key. It's only kept around so it can be passed to the real
         * FieldConfigContextPersister when we need to load a row from the database.
         */
        private final JiraContextNode contextNode;

        CacheKey(JiraContextNode contextNode, String customField)
        {
            this.contextNode = contextNode;
            this.customField = customField;
            this.contextParams = Collections.unmodifiableMap(contextNode.appendToParamsMap(null));
        }

        @Override
        @SuppressWarnings ("RedundantIfStatement")
        public boolean equals(Object o)
        {
            if (this == o) { return true; }
            if (o == null || getClass() != o.getClass()) { return false; }

            CacheKey cacheKey = (CacheKey) o;

            if (contextParams != null ? !contextParams.equals(cacheKey.contextParams) : cacheKey.contextParams != null)
            {
                return false;
            }
            if (customField != null ? !customField.equals(cacheKey.customField) : cacheKey.customField != null)
            {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode()
        {
            int result = contextParams != null ? contextParams.hashCode() : 0;
            result = 31 * result + (customField != null ? customField.hashCode() : 0);
            return result;
        }

        @Override
        public String toString()
        {
            return "CacheKey{" + contextParams + "/" + customField + '}';
        }
    }

    /**
     * Matches cache keys by project id.
     */
    private static class ProjectIdMatcher implements Predicate<CacheKey>
    {
        private final Long projectId;

        public ProjectIdMatcher(Long projectId)
        {
            this.projectId = projectId;
        }

        @Override
        public boolean apply(CacheKey key)
        {
            Project project = key.contextNode.getProjectObject();

            return project != null && projectId.equals(project.getId());
        }
    }

    /**
     * Matches cache keys by context.
     */
    private static class ContextMatcher implements Predicate<CacheKey>
    {
        private final BandanaContext context;

        public ContextMatcher(BandanaContext context)
        {
            this.context = context;
        }

        @Override
        public boolean apply(CacheKey key)
        {
            return context.equals(key.contextNode);
        }
    }

    /**
     * Matches cache keys by custom field.
     */
    private static class CustomFieldMatcher implements Predicate<CacheKey>
    {
        private final String customField;

        public CustomFieldMatcher(String customField)
        {
            this.customField = customField;
        }

        @Override
        public boolean apply(CacheKey input)
        {
            return input.customField.equals(customField);
        }
    }

    /**
     * Matches cache keys by project category.
     */
    private class ProjectCategoryMatcher implements Predicate<CacheKey>
    {
        private final ProjectCategory projectCategory;

        public ProjectCategoryMatcher(ProjectCategory projectCategory)
        {
            this.projectCategory = projectCategory;
        }

        @Override
        public boolean apply(CacheKey key)
        {
            Project project = key.contextNode.getProjectObject();

            return project != null && projectCategory.equals(project.getProjectCategoryObject());
        }
    }
}
