package com.atlassian.jira.workflow;

import com.atlassian.core.ofbiz.association.AssociationManager;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.entity.EntityUtils;
import com.atlassian.jira.event.ClearCacheEvent;
import com.atlassian.jira.event.workflow.WorkflowSchemeAddedToProjectEvent;
import com.atlassian.jira.event.workflow.WorkflowSchemeCopiedEvent;
import com.atlassian.jira.event.workflow.WorkflowSchemeCreatedEvent;
import com.atlassian.jira.event.workflow.WorkflowSchemeDeletedEvent;
import com.atlassian.jira.event.workflow.WorkflowSchemeEntityAddedEvent;
import com.atlassian.jira.event.workflow.WorkflowSchemeEntityDeletedEvent;
import com.atlassian.jira.event.workflow.WorkflowSchemeUpdatedEvent;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.exception.RemoveException;
import com.atlassian.jira.extension.Startable;
import com.atlassian.jira.ofbiz.FieldMap;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.permission.PermissionContextFactory;
import com.atlassian.jira.permission.PermissionTypeManager;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.scheme.AbstractSchemeManager;
import com.atlassian.jira.scheme.Scheme;
import com.atlassian.jira.scheme.SchemeEntity;
import com.atlassian.jira.scheme.SchemeFactory;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.util.collect.MapBuilder;
import net.jcip.annotations.GuardedBy;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.EntityUtil;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class DefaultWorkflowSchemeManager extends AbstractSchemeManager implements WorkflowSchemeManager, Startable
{
    private static final Logger log = Logger.getLogger(DefaultWorkflowSchemeManager.class);
    private static final String ALL_ISSUE_TYPES = "0";
    private static final String SCHEME_ENTITY_NAME = "WorkflowScheme";
    private static final String WORKFLOW_ENTITY_NAME = "WorkflowSchemeEntity";
    private static final String SCHEME_DESC = "Workflow";
    private static final String DEFAULT_NAME_KEY = "admin.schemes.workflows.default";
    private static final String DEFAULT_DESC_KEY = "admin.schemes.workflows.default.desc";

    private static final String COLUMN_ISSUETYPE = "issuetype";
    private static final String COLUMN_WORKFLOW = "workflow";

    /**
     * Stores {WorkflowScheme} -> {{issuetype} -> {WorkflowSchemeEntity}}.
     */

    //JRADEV-239: This used to be an LRU map, but we converted it to a concurrent map because it was a point of contention.
    // We probably need to consider converting it to a Concurrent LRU map. Until such a thing exists its max size of
    // (#workflowscheme * #issuetype) should be acceptable.
    private final Map<Long, Map<String, GenericValue>> cache = new ConcurrentHashMap<Long, Map<String, GenericValue>>();
    private final ActiveWorkflowCache activeWorkflowCache = new ActiveWorkflowCache();
    private final WorkflowManager workflowManager;
    private final ConstantsManager constantsManager;
    private final OfBizDelegator ofBizDelegator;
    private final EventPublisher eventPublisher;

    public DefaultWorkflowSchemeManager(final ProjectManager projectManager, final PermissionTypeManager permissionTypeManager,
            final PermissionContextFactory permissionContextFactory, final SchemeFactory schemeFactory,
            final WorkflowManager workflowManager, final ConstantsManager constantsManager,
            final OfBizDelegator ofBizDelegator, final EventPublisher eventPublisher, final AssociationManager associationManager,
            final GroupManager groupManager)
    {
        super(projectManager, permissionTypeManager, permissionContextFactory, schemeFactory, associationManager, ofBizDelegator, groupManager);
        this.workflowManager = workflowManager;
        this.constantsManager = constantsManager;
        this.ofBizDelegator = ofBizDelegator;
        this.eventPublisher = eventPublisher;
    }

    public void start() throws Exception
    {
        eventPublisher.register(this);
    }

    @EventListener
    public void onClearCache(final ClearCacheEvent event)
    {
        super.onClearCache(event);
        clearWorkflowCache();
    }

    @Override
    public String getSchemeEntityName()
    {
        return SCHEME_ENTITY_NAME;
    }

    @Override
    public String getEntityName()
    {
        return WORKFLOW_ENTITY_NAME;
    }

    public void clearWorkflowCache()
    {
        activeWorkflowCache.clear();
        clearCache();
    }

    @Override
    public String getSchemeDesc()
    {
        return SCHEME_DESC;
    }

    @Override
    public String getDefaultNameKey()
    {
        return DEFAULT_NAME_KEY;
    }

    @Override
    public String getDefaultDescriptionKey()
    {
        return DEFAULT_DESC_KEY;
    }

    public GenericValue getWorkflowScheme(final GenericValue project) throws GenericEntityException
    {
        return EntityUtil.getOnly(getSchemes(project));
    }

    public GenericValue createSchemeEntity(final GenericValue scheme, final SchemeEntity schemeEntity)
            throws GenericEntityException
    {
        if (!(schemeEntity.getEntityTypeId() instanceof String))
        {
            throw new IllegalArgumentException("Workflow scheme IDs must be String values.");
        }

        try
        {
            final GenericValue result = EntityUtils.createValue(getEntityName(), FieldMap.build("scheme", scheme.getLong("id"), COLUMN_WORKFLOW, schemeEntity.getType(),
                    COLUMN_ISSUETYPE, schemeEntity.getEntityTypeId().toString()));

            eventPublisher.publish(new WorkflowSchemeEntityAddedEvent(scheme.getLong("id"), schemeEntity));

            return result;
        }
        finally
        {
            clearCache();
        }
    }

    private void clearCache()
    {
        if (log.isDebugEnabled())
        {
            log.debug("Clearing workflow scheme entity cache, had " + cache.size() + " entries");
        }

        cache.clear();
    }

    public List<GenericValue> getEntities(final GenericValue scheme, final String issuetype)
            throws GenericEntityException
    {
        final Map<String, GenericValue> genericValueMap = getSchemeMap(scheme);
        final GenericValue value = genericValueMap.get(issuetype);
        if (value == null)
        {
            return Collections.emptyList();
        }
        else
        {
            return Collections.singletonList(value);
        }
    }

    public Map<String, String> getWorkflowMap(Project project)
    {
        final GenericValue schemeForProject = getSchemeForProject(project);
        if (schemeForProject == null)
        {
            return MapBuilder.build(null, JiraWorkflow.DEFAULT_WORKFLOW_NAME);
        }
        else
        {
            Map<String, String> result = new HashMap<String, String>();
            final Map<String, GenericValue> schemeMap = getSchemeMap(schemeForProject);
            for (Map.Entry<String, GenericValue> entry : schemeMap.entrySet())
            {
                final String workflow = entry.getValue().getString(COLUMN_WORKFLOW);
                if (entry.getKey() != null && workflow != null)
                {
                    if (ALL_ISSUE_TYPES.equals(entry.getKey()))
                    {
                        result.put(null, workflow);
                    }
                    else
                    {
                        result.put(entry.getKey(), workflow);
                    }
                }
            }
            return result;
        }
    }

    public String getWorkflowName(Project project, String issueType)
    {
        return getWorkflowName(getSchemeForProject(project), issueType);
    }

    public String getWorkflowName(GenericValue scheme, String issueType)
    {
        if (scheme != null)
        {
            final Map<String, GenericValue> map = getSchemeMap(scheme);
            GenericValue value = map.get(issueType);
            if (value == null)
            {
                value = map.get(ALL_ISSUE_TYPES);
            }
            if (value != null)
            {
                return value.getString(COLUMN_WORKFLOW);
            }
        }

        // otherwise always return the default workflow
        return JiraWorkflow.DEFAULT_WORKFLOW_NAME;
    }

    @Override
    public boolean isUsingDefaultScheme(Project project)
    {
        return getSchemeForProject(project) == null;
    }

    private GenericValue getSchemeForProject(final Project project)
    {
        try
        {
            return EntityUtil.getOnly(getSchemes(project.getGenericValue()));
        }
        catch (GenericEntityException e)
        {
            throw new DataAccessException(e);
        }
    }

    private static Long getCacheKeyForScheme(final GenericValue scheme)
    {
        return scheme.getLong("id");
    }

    private Map<String, GenericValue> getSchemeMap(final GenericValue scheme)
    {
        if (scheme == null)
        {
            return Collections.emptyMap();
        }

        //If its in the cache then return directly.
        final Long cacheKeyForScheme = getCacheKeyForScheme(scheme);
        final Map<String, GenericValue> cachedEntry = cache.get(cacheKeyForScheme);
        if (cachedEntry != null)
        {
            return cachedEntry;
        }

        final List<GenericValue> valueList;
        try
        {
            valueList = getEntities(scheme);
        }
        catch (GenericEntityException e)
        {
            throw new DataAccessException(e);
        }

        Map<String, GenericValue> schemeMap = new HashMap<String, GenericValue>();
        for (GenericValue value : valueList)
        {
            final String issueType = value.getString(COLUMN_ISSUETYPE);
            if (issueType != null)
            {
                schemeMap.put(issueType, value);
            }
        }

        schemeMap = Collections.unmodifiableMap(schemeMap);
        cache.put(cacheKeyForScheme, schemeMap);
        return schemeMap;
    }

    @Override
    public GenericValue copySchemeEntity(final GenericValue scheme, final GenericValue entity) throws GenericEntityException
    {
        final SchemeEntity schemeEntity = new SchemeEntity(entity.getString(COLUMN_WORKFLOW), entity.getString(COLUMN_ISSUETYPE));
        return createSchemeEntity(scheme, schemeEntity);
    }

    public List<GenericValue> getEntities(final GenericValue scheme, final Long entityTypeId) throws GenericEntityException
    {
        throw new IllegalArgumentException("Workflow scheme IDs must be String values.");
    }

    public List<GenericValue> getEntities(final GenericValue scheme, final Long entityTypeId, final String parameter) throws GenericEntityException
    {
        throw new IllegalArgumentException("Workflow scheme IDs must be String values.");
    }

    public List<GenericValue> getEntities(final GenericValue scheme, final String type, final Long entityTypeId) throws GenericEntityException
    {
        throw new IllegalArgumentException("Workflow scheme IDs must be String values.");
    }

    public boolean hasSchemeAuthority(final Long entityType, final GenericValue entity)
    {
        return false;
    }

    public boolean hasSchemeAuthority(final Long entityType, final GenericValue entity, final com.atlassian.crowd.embedded.api.User user, final boolean issueCreation)
    {
        return false;
    }

    public GenericValue getDefaultEntity(final GenericValue scheme) throws GenericEntityException
    {
        return EntityUtil.getOnly(getEntities(scheme, ALL_ISSUE_TYPES));
    }

    public List<GenericValue> getNonDefaultEntities(final GenericValue scheme) throws GenericEntityException
    {
        final List<GenericValue> entities = getEntities(scheme);

        // remove the default entity
        for (final Iterator<GenericValue> iterator = entities.iterator(); iterator.hasNext();)
        {
            final GenericValue genericValue = iterator.next();
            if (ALL_ISSUE_TYPES.equals(genericValue.getString(COLUMN_ISSUETYPE)))
            {
                iterator.remove();
                break;
            }
        }
        return entities;
    }

    public Collection<String> getActiveWorkflowNames() throws GenericEntityException, WorkflowException
    {
        return activeWorkflowCache.get();
    }

    public void addWorkflowToScheme(final GenericValue scheme, final String workflowName, final String issueTypeId) throws GenericEntityException
    {
        try
        {
            final SchemeEntity schemeEntity = new SchemeEntity(workflowName, issueTypeId);

            // prevent adding the same workflow multiple times to one scheme
            if (getEntities(scheme, issueTypeId).isEmpty())
            {
                createSchemeEntity(scheme, schemeEntity);
            }
        }
        finally
        {
            clearWorkflowCache();
        }
    }

    public void updateSchemesForRenamedWorkflow(final String oldWorkflowName, final String newWorkflowName)
    {
        if (StringUtils.isBlank(oldWorkflowName))
        {
            throw new IllegalArgumentException("oldWorkflowName must not be null or empty string");
        }
        if (StringUtils.isBlank(newWorkflowName))
        {
            throw new IllegalArgumentException("newWorkflowName must not be null or empty string");
        }
        ofBizDelegator.bulkUpdateByAnd(getEntityName(), EasyMap.build(COLUMN_WORKFLOW, newWorkflowName), EasyMap.build(COLUMN_WORKFLOW, oldWorkflowName));
        clearWorkflowCache();
    }

    public Collection<GenericValue> getSchemesForWorkflow(final JiraWorkflow workflow)
    {
        // TODO This does not cater for default workflow which is used by schemes with no default for all issue types and
        // by projects that do not have a workflow shceme assigned
        final Collection<GenericValue> schemes = new LinkedList<GenericValue>();
        try
        {
            final Set<Long> schemeIds = new HashSet<Long>();
            // Find all scheme entities with the workflow name
            final List<GenericValue> schemeEntities = ofBizDelegator.findByAnd(getEntityName(),
                    MapBuilder.build(COLUMN_WORKFLOW, workflow.getName()));
            // Loop through all the entities and retrieve the scheme ids
            for (final GenericValue schemeEntity : schemeEntities)
            {
                final Long schemeId = schemeEntity.getLong("scheme");
                // Only retrieve schemes that we have not retrieved already
                if (!schemeIds.contains(schemeId))
                {
                    schemes.add(getScheme(schemeId));
                    schemeIds.add(schemeId);
                }
            }
        }
        catch (final GenericEntityException e)
        {
            throw new DataAccessException("Error retrieving scheme entities for workflow '" + workflow.getName() + "'.", e);
        }

        return schemes;
    }

    @Override
    public void deleteEntity(final Long id) throws DataAccessException
    {
        try
        {
            super.deleteEntity(id);

            eventPublisher.publish(new WorkflowSchemeEntityDeletedEvent(id));
        }
        finally
        {
            clearCache();
        }
    }

    @Override
    public boolean removeEntities(final GenericValue scheme, final Long entityTypeId) throws RemoveException
    {
        try
        {
            return super.removeEntities(scheme, entityTypeId);
        }
        finally
        {
            clearCache();
        }
    }

    @Override
    public GenericValue createScheme(final String name, final String description) throws GenericEntityException
    {
        try
        {
            return super.createScheme(name, description);
        }
        finally
        {
            clearCache();
        }
    }

    @Override
    public Scheme createSchemeObject(String name, String description)
    {
        try
        {
            final Scheme scheme = super.createSchemeObject(name, description);
            
            if (scheme != null)
            {
                // Only publish the event in this method, and not createScheme() as it is deprecated
                eventPublisher.publish(new WorkflowSchemeCreatedEvent(scheme));
            }
            
            return scheme;
        }
        finally
        {
            clearCache();
        }
    }

    @Override
    public Scheme copyScheme(Scheme scheme)
    {
        final Scheme result = super.copyScheme(scheme);

        if (result != null)
        {
            // Only publish the event in this method, and not copyScheme(GenericValue) as it is deprecated
            eventPublisher.publish(new WorkflowSchemeCopiedEvent(scheme, result));
        }

        return result;
    }

    @Override
    public void updateScheme(Scheme scheme) throws DataAccessException
    {
        super.updateScheme(scheme);

        // Only publish the event in this method, and not updateScheme(GenericValue) as it is deprecated
        eventPublisher.publish(new WorkflowSchemeUpdatedEvent(scheme));
    }

    @Override
    public void deleteScheme(Long id) throws GenericEntityException
    {
        super.deleteScheme(id);

        eventPublisher.publish(new WorkflowSchemeDeletedEvent(id));
    }

    @Override
    public void addSchemeToProject(GenericValue project, GenericValue scheme) throws GenericEntityException
    {
        super.addSchemeToProject(project, scheme);

        final Long projectId = (project == null) ? null : project.getLong("id");
        final Long schemeId = (scheme == null) ? null : scheme.getLong("id");
        eventPublisher.publish(new WorkflowSchemeAddedToProjectEvent(projectId, schemeId));
    }

    @Override
    public void addSchemeToProject(Project project, Scheme scheme) throws DataAccessException
    {
        super.addSchemeToProject(project, scheme);

        final Long projectId = (project == null) ? null : project.getId();
        final Long schemeId = (scheme == null) ? null : scheme.getId();
        eventPublisher.publish(new WorkflowSchemeAddedToProjectEvent(projectId, schemeId));
    }

    @Override
    protected void flushProjectSchemes()
    {
        try
        {
            super.flushProjectSchemes();
        }
        finally
        {
            clearCache();
        }
    }

    /*
     * protected for unit tests
     */
    protected List<GenericValue> getAllIssueTypes()
    {
        return constantsManager.getAllIssueTypes();
    }

    /*
     * protected for unit tests
     */
    protected JiraWorkflow getWorkflowFromScheme(final GenericValue workflowScheme, final String issueTypeId)
    {
        return workflowManager.getWorkflowFromScheme(workflowScheme, issueTypeId);
    }

    /**
     * Encapsulate the caching policy implementation
     */
    class ActiveWorkflowCache
    {
        private final ReadWriteLock lock = new ReentrantReadWriteLock();

        @GuardedBy("lock")
        private Set<String> cache = null;

        Set<String> get() throws GenericEntityException, WorkflowException
        {
            lock.readLock().lock();
            try
            {
                load();
                return cache;
            }
            finally
            {
                lock.readLock().unlock();
            }
        }

        void clear()
        {
            lock.writeLock().lock();
            try
            {
                cache = null;
            }
            finally
            {
                lock.writeLock().unlock();
            }
        }

        @GuardedBy("lock")
        private void load() throws GenericEntityException, WorkflowException
        {
            if (cache != null)
            {
                return;
            }

            final Set<String> set = new HashSet<String>();

            final Collection<GenericValue> schemes = getSchemes();
            for (final GenericValue scheme : schemes)
            {

                // Only interested in the schemes that are associated with a project
                if (!getProjects(scheme).isEmpty())
                {
                    final Collection<GenericValue> entities = getEntities(scheme);
                    for (final GenericValue schemeEntity : entities)
                    {
                        set.add((String) schemeEntity.get(COLUMN_WORKFLOW));
                    }
                }
            }

            // Check if default workflow is active i.e. a project with no associated scheme or scheme with an unassigned issue type
            final Collection<GenericValue> projects = projectManager.getProjects();
            // Stop searching as soon as use of default workflow is detected
            boolean checkComplete = false;

            for (final GenericValue project : projects)
            {
                final GenericValue workflowScheme = getWorkflowScheme(project);
                if (workflowScheme == null)
                {
                    // Default workflow in use
                    set.add(JiraWorkflow.DEFAULT_WORKFLOW_NAME);
                    checkComplete = true;
                }
                else
                {
                    // Check if an unassigned issue type exists within this scheme
                    final Collection<GenericValue> issueTypes = getAllIssueTypes();

                    for (final GenericValue issueType : issueTypes)
                    {
                        final String issueTypeId = issueType.getString("id");
                        final JiraWorkflow workflow = getWorkflowFromScheme(workflowScheme, issueTypeId);
                        if (workflow.getName().equals(JiraWorkflow.DEFAULT_WORKFLOW_NAME))
                        {
                            // Default workflow in use
                            set.add(JiraWorkflow.DEFAULT_WORKFLOW_NAME);
                            checkComplete = true;
                        }
                        if (checkComplete)
                        {
                            break;
                        }
                    }
                }
                if (checkComplete)
                {
                    break;
                }
            }
            cache = Collections.unmodifiableSet(set);
        }
    }
}
