/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.issue.managers;

import com.atlassian.core.ofbiz.CoreFactory;
import com.atlassian.core.ofbiz.util.EntityUtils;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.event.ClearCacheEvent;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.exception.RemoveException;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.RendererManager;
import com.atlassian.jira.issue.comparator.CustomFieldComparator;
import com.atlassian.jira.issue.context.persistence.FieldConfigContextPersister;
import com.atlassian.jira.issue.customfields.CustomFieldSearcher;
import com.atlassian.jira.issue.customfields.CustomFieldType;
import com.atlassian.jira.issue.customfields.CustomFieldUtils;
import com.atlassian.jira.issue.customfields.persistence.CustomFieldValuePersister;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.CustomFieldImpl;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.config.FieldConfigScheme;
import com.atlassian.jira.issue.fields.config.manager.FieldConfigSchemeManager;
import com.atlassian.jira.issue.fields.config.persistence.FieldConfigPersister;
import com.atlassian.jira.issue.fields.screen.FieldScreenManager;
import com.atlassian.jira.issue.index.managers.FieldIndexerManager;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.issue.search.managers.IssueSearcherManager;
import com.atlassian.jira.jql.context.FieldConfigSchemeClauseContextUtil;
import com.atlassian.jira.notification.NotificationSchemeManager;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.plugin.customfield.CustomFieldSearcherModuleDescriptor;
import com.atlassian.jira.plugin.customfield.CustomFieldTypeModuleDescriptor;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.util.JiraUtils;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.event.events.PluginModuleEnabledEvent;
import com.atlassian.util.concurrent.Assertions;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MultiHashMap;
import org.apache.commons.collections.MultiMap;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericDelegator;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericPK;
import org.ofbiz.core.entity.GenericValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@edu.umd.cs.findbugs.annotations.SuppressWarnings(value="MT_CORRECTNESS", justification="TODO Needs to be fixed.")
public class DefaultCustomFieldManager implements CustomFieldManager
{
    private static final Logger log = Logger.getLogger(DefaultCustomFieldManager.class);

    private ConcurrentHashMap<Long, CustomField> allCustomFieldObjectsMapById;
    private MultiMap allCustomFieldObjectsMapByName;
    private List<CustomField> allCustomFieldObjectsList;

    private final PluginAccessor pluginAccessor;
    private final OfBizDelegator delegator;
    private final FieldConfigSchemeManager fieldConfigSchemeManager;
    private final JiraAuthenticationContext authenticationContext;
    private final ConstantsManager constantsManager;
    private final ProjectManager projectManager;
    private final PermissionManager permissionManager;
    private final FieldConfigContextPersister contextPersister;
    private final FieldScreenManager fieldScreenManager;
    private final RendererManager rendererManager;
    private final CustomFieldValuePersister customFieldValuePersister;
    private final NotificationSchemeManager notificationSchemeManager;
    private final FieldManager fieldManager;
    private final FieldConfigSchemeClauseContextUtil fieldConfigSchemeClauseContextUtil;

    public DefaultCustomFieldManager(PluginAccessor pluginAccessor,
            OfBizDelegator delegator,
            FieldConfigSchemeManager fieldConfigSchemeManager,
            JiraAuthenticationContext authenticationContext,
            ConstantsManager constantsManager,
            ProjectManager projectManager,
            PermissionManager permissionManager,
            FieldConfigContextPersister contextPersister, FieldScreenManager fieldScreenManager,
            RendererManager rendererManager, CustomFieldValuePersister customFieldValuePersister,
            final NotificationSchemeManager notificationSchemeManager, final FieldManager fieldManager,
            final FieldConfigSchemeClauseContextUtil fieldConfigSchemeClauseContextUtil,
            final EventPublisher eventPublisher)
    {
        this.pluginAccessor = pluginAccessor;
        this.delegator = delegator;
        this.fieldConfigSchemeManager = fieldConfigSchemeManager;
        this.authenticationContext = authenticationContext;
        this.constantsManager = constantsManager;
        this.projectManager = projectManager;
        this.permissionManager = permissionManager;
        this.contextPersister = contextPersister;
        this.fieldScreenManager = fieldScreenManager;
        this.rendererManager = rendererManager;
        this.customFieldValuePersister = customFieldValuePersister;
        this.notificationSchemeManager = notificationSchemeManager;
        this.fieldManager = fieldManager;
        this.fieldConfigSchemeClauseContextUtil = fieldConfigSchemeClauseContextUtil;
        eventPublisher.register(this);
        // Pre v4.0 we used to preload the cache here, but we can no longer do this, as this relies on the plugins being available.
        // The plugins rely on system components being constructed and available for dependency injection.
        // To avoid the circular dependency we lazy load the cahce later, when plugins are up and running.
    }

    @EventListener
    public void onClearCache(final ClearCacheEvent event)
    {
        refresh();
    }

    public CustomField createCustomField(String fieldName, String description, CustomFieldType fieldType,
                                         CustomFieldSearcher customFieldSearcher, List contexts, List issueTypes) throws GenericEntityException
    {
        Map<String, String> createFields = new HashMap<String, String>();

        createFields.put(CustomFieldImpl.ENTITY_NAME, StringUtils.abbreviate(fieldName, FieldConfigPersister.ENTITY_LONG_TEXT_LENGTH));
        createFields.put(CustomFieldImpl.ENTITY_CF_TYPE_KEY, fieldType.getKey());

        if (StringUtils.isNotEmpty(description))
            createFields.put(CustomFieldImpl.ENTITY_DESCRIPTION, description);

        if (customFieldSearcher != null)
            createFields.put(CustomFieldImpl.ENTITY_CUSTOM_FIELD_SEARCHER, customFieldSearcher.getDescriptor().getCompleteKey());

        GenericValue customFieldGV = EntityUtils.createValue(CustomFieldImpl.ENTITY_TABLE_NAME, createFields);
        CustomField customField = new CustomFieldImpl(customFieldGV, this, authenticationContext, constantsManager, fieldConfigSchemeManager, permissionManager, rendererManager, fieldConfigSchemeClauseContextUtil);

        associateCustomFieldContext(customField, contexts, issueTypes);

        // clear the custom fields cache
        GenericDelegator genericDelegator = CoreFactory.getGenericDelegator();
        genericDelegator.clearCacheLineFlexible(new GenericPK(genericDelegator.getModelEntity(CustomFieldImpl.ENTITY_TABLE_NAME)));
        this.refresh();

        // refresh the IssueFieldManager
        fieldManager.refresh();

        return getCustomFieldObject(customFieldGV.getLong(CustomFieldImpl.ENTITY_ID));
    }

    public void associateCustomFieldContext(CustomField customField, List contexts, List issueTypes)
    {
        if (contexts != null && !contexts.isEmpty())
        {
            fieldConfigSchemeManager.createDefaultScheme(customField, contexts, issueTypes);
        }
    }

    public List getCustomFieldTypes()
    {
        final List<CustomFieldType> customFieldTypes = pluginAccessor.getEnabledModulesByClass(CustomFieldType.class);
        Collections.sort(customFieldTypes, new Comparator<CustomFieldType>()
        {
            public int compare(CustomFieldType o1, CustomFieldType o2)
            {
                final String name1 = o1.getName();
                final String name2 = o2.getName();
                if (name1 == null)
                {
                    return name2 == null ? 0 : -1;
                }
                else
                {
                    return name1.compareTo(name2);
                }
            }
        });
        return customFieldTypes;
    }

    public CustomFieldType getCustomFieldType(String key)
    {
        final ModuleDescriptor module = pluginAccessor.getEnabledPluginModule(key);

        if (module != null && module instanceof CustomFieldTypeModuleDescriptor)
        {
            return (CustomFieldType) module.getModule();
        }

        log.error("Could not load custom field type plugin with key '" + key + "'. Is the plugin present and enabled?");
        return null;
    }

    public List<CustomFieldSearcher> getCustomFieldSearchers(CustomFieldType customFieldType)
    {
        final List<CustomFieldSearcher> allFieldSearchers = pluginAccessor.getEnabledModulesByClass(CustomFieldSearcher.class);
        List<CustomFieldSearcher> customFieldSearchers = new ArrayList<CustomFieldSearcher>();
        for (final CustomFieldSearcher searcher : allFieldSearchers)
        {
            if (searcher.getDescriptor().getValidCustomFieldKeys().contains(customFieldType.getKey()))
            {
                customFieldSearchers.add(searcher);
            }
        }
        return customFieldSearchers;
    }

    public CustomFieldSearcher getCustomFieldSearcher(String key)
    {
        if (key == null)
            return null;

        final ModuleDescriptor module = pluginAccessor.getEnabledPluginModule(key);

        if (module != null && module instanceof CustomFieldSearcherModuleDescriptor)
        {
            Class<CustomFieldSearcher> searcherClazz = module.getModuleClass();
            CustomFieldSearcher cfSearcher = JiraUtils.loadComponent(searcherClazz);
            cfSearcher.init((CustomFieldSearcherModuleDescriptor)module);
            return cfSearcher;
        }
        else
        {
            log.warn("Custom field searcher module: " + key + " is invalid. Null being returned.");
            return null;
        }
    }

    public Class getCustomFieldSearcherClass(String key)
    {
        if (key == null)
        {
            return null;
        }

        final ModuleDescriptor module = pluginAccessor.getEnabledPluginModule(key);

        if (module != null && module instanceof CustomFieldSearcherModuleDescriptor)
        {
            return module.getModuleClass();
        }
        else
        {
            log.warn("Custom field searcher module: " + key + " is invalid. Null being returned.");
            return null;
        }
    }

    /** Get all {@link CustomField}s in scope for this issue's project/type.
     */
    public List<CustomField> getCustomFieldObjects(Issue issue)
    {
        return getCustomFieldObjects(issue.getProjectObject().getId(), issue.getIssueTypeObject().getId());
    }

    /** @deprecated Use {@link #getCustomFieldObjects(com.atlassian.jira.issue.Issue)} */
    public List<CustomField> getCustomFieldObjects(GenericValue issue)
    {
        return getCustomFieldObjects(issue.getLong("project"), issue.getString("type"));
    }


    public List<CustomField> getCustomFieldObjects(Long projectId, String issueTypeId)
    {
        List<String> issueTypes = issueTypeId == null ? null : com.atlassian.jira.util.EasyList.build(issueTypeId);
        return getCustomFieldObjects(projectId, issueTypes);
    }

    public List<CustomField> getCustomFieldObjects(Long projectId, List<String> issueTypeIds)
    {
        List<CustomField> customFieldsInContext = new ArrayList<CustomField>();

        // Convert 2 Objects
        Project project = projectManager.getProjectObj(projectId);
        issueTypeIds = constantsManager.expandIssueTypeIds(issueTypeIds);

        // Add fields in context
        for (final CustomField customField : getCustomFieldObjects())
        {
            if (customField.isInScope(project, issueTypeIds))
            {
                customFieldsInContext.add(customField);
            }
        }

        return customFieldsInContext;
    }

    public List<CustomField> getCustomFieldObjects(SearchContext searchContext)
    {
        List<CustomField> customFieldsInContext = new ArrayList<CustomField>();

        // Add fields in context
        for (final CustomField customField : getCustomFieldObjects())
        {
            if (customField.isInScope(authenticationContext.getUser(), searchContext))
            {
                customFieldsInContext.add(customField);
            }
        }

        return customFieldsInContext;
    }


    public CustomField getCustomFieldObject(Long id)
    {
        ensureCacheLoaded();
        return allCustomFieldObjectsMapById.get(id);
    }

    public CustomField getCustomFieldObject(String key)
    {
        final Long id = CustomFieldUtils.getCustomFieldId(key);
        if (id != null)
            return getCustomFieldObject(id);
        else
            return null;
    }

    public CustomField getCustomFieldObjectByName(final String customFieldName)
    {
        Collection values = getCustomFieldObjectsByName(customFieldName);
        if (values == null || values.size() == 0) return null;
        if (values.size() > 1)
        {
            // Should have called getCustomFieldObjectsByName instead?
            log.warn("Warning: returning 1 of "+values.size()+" custom fields named '"+customFieldName+"'");
            // Only dump the stack trace if debug logging is enabled - otherwise the log file can get full of rubbish.
            // Some 3rd party plugins are known to call this a lot.
            if (log.isDebugEnabled())
            {
                Thread.dumpStack();
            }
        }
        return getCustomFieldObjectsByName(customFieldName).iterator().next();
    }

    public Collection<CustomField> getCustomFieldObjectsByName(final String customFieldName)
    {
        ensureCacheLoaded();
        return (Collection<CustomField>) this.allCustomFieldObjectsMapByName.get(customFieldName);
    }

    public List<CustomField> getCustomFieldObjects()
    {
        ensureCacheLoaded();
        return Collections.unmodifiableList(allCustomFieldObjectsList);
    }

    public List<CustomField> getGlobalCustomFieldObjects()
    {
        ensureCacheLoaded();
        Collection<CustomField> global = CollectionUtils.select(allCustomFieldObjectsList, new Predicate()
        {
            public boolean evaluate(Object object)
            {
                CustomField cf = (CustomField) object;
                return cf.isGlobal();
            }
        });
        return new ArrayList<CustomField>(global);
    }

    private void ensureCacheLoaded()
    {
        // Check that the maps are populated:
        if (allCustomFieldObjectsMapById == null)
        {
            // Not loaded yet. Lets only load once - so synchronize and recheck for null
            synchronized(this)
            {
                if (allCustomFieldObjectsMapById == null)
                {
                    populateCache();
                }
            }
        }
    }

    public synchronized void refresh()
    {
        fieldConfigSchemeManager.init();
        populateCache();

        // Resets the issue search manager @todo This must be statically called since otherwise a cyclic dependency will occur. There really needs to be a CacheManager that handles all these dependent caches
        final IssueSearcherManager issueSearcherManager = ComponentManager.getComponentInstanceOfType(IssueSearcherManager.class);
        issueSearcherManager.refresh();

        final FieldIndexerManager fieldIndexerManager = ComponentManager.getComponentInstanceOfType(FieldIndexerManager.class);
        fieldIndexerManager.refresh();
    }

    public void clear()
    {
        synchronized (this)
        {
            allCustomFieldObjectsMapById = null;
            fieldManager.getFieldLayoutManager().refresh();
        }

    }

    private List<GenericValue> getCustomFieldsFromDB()
    {
        List<GenericValue> customFields = new ArrayList(delegator.findAll(CustomFieldImpl.ENTITY_TABLE_NAME));
        Collections.sort(customFields, new CustomFieldComparator());
        return customFields;
    }

    public void removeCustomFieldPossiblyLeavingOrphanedData(final Long customFieldId) throws RemoveException
    {
        Assertions.notNull("id", customFieldId);

        final CustomField originalCustomField = getCustomFieldObject(customFieldId);
        if (originalCustomField != null)
        {
            removeCustomField(originalCustomField);
        }
        else
        {
            log.debug("Couldn't load customfield object for id '" + customFieldId + "'.  Trying to lookup field directly via the db."
                    + "  Please note that deleting a custom field this way may leave some custom field data behind.");
            //couldn't find it via the manager.  Lets try to look it up via the db directly.  The customfield
            //type is no longer available via the pluginmanager.
            final GenericValue customFieldGv = delegator.findByPrimaryKey(CustomFieldImpl.ENTITY_TABLE_NAME,
                    EasyMap.build(CustomFieldImpl.ENTITY_ID, customFieldId));
            if (customFieldGv != null)
            {
                log.debug("Customfield with id '" + customFieldId + "' retrieved successfully via the db.");

                final String customFieldStringId = FieldManager.CUSTOM_FIELD_PREFIX + customFieldId;
                removeCustomFieldAssociations(customFieldStringId);

                customFieldValuePersister.removeAllValues(customFieldStringId);
                try
                {
                    customFieldGv.remove();
                }
                catch (GenericEntityException e)
                {
                    throw new DataAccessException("Error deleting custom field gv with id '" + customFieldId + "'", e);
                }
                this.refresh();
                fieldManager.refresh();
            }
            else
            {
                throw new IllegalArgumentException("Tried to remove custom field with id '" + customFieldId + "' that doesn't exist!");
            }
        }
    }

    public void removeCustomField(CustomField customField) throws RemoveException
    {
        removeCustomFieldAssociations(customField.getId());
        customField.remove();
        this.refresh();
        fieldManager.refresh();
    }

    private void removeCustomFieldAssociations(String customFieldId) throws RemoveException
    {
        // Remove and field screen layout items of this custom field
        fieldScreenManager.removeFieldScreenItems(customFieldId);

        delegator.removeByAnd("ColumnLayoutItem", EasyMap.build("fieldidentifier", customFieldId));
        // JRA-4423 Remove any references to the customfield in the field layouts.
        delegator.removeByAnd("FieldLayoutItem", EasyMap.build("fieldidentifier", customFieldId));

        fieldConfigSchemeManager.removeInvalidFieldConfigSchemesForCustomField(customFieldId);

        // This should be triggered via an event system but until then is done explicitly
        notificationSchemeManager.removeSchemeEntitiesForField(customFieldId);
    }

    public void removeCustomFieldValues(GenericValue issue) throws GenericEntityException
    {
        // Remove the rows in the customfieldValues
        delegator.removeByAnd("CustomFieldValue", EasyMap.build("issue", issue.getLong("id")));
    }

    void populateCache()
    {
        if (pluginAccessor.getPlugins().isEmpty())
        {
            log.error("Attempting to Populate the Custom Fields cache in DefaultCustomFieldManager, however no plugins are loaded in the Plugin Manager.", new IllegalStateException("Plugin Manager not initialized."));
            return;
        }

        Map<Long, CustomField> customFieldObjectsMapById = new HashMap<Long, CustomField>();
        MultiMap customFieldObjectsMapByName = new MultiHashMap();
        List<CustomField> customFieldObjectsList = new ArrayList<CustomField>();

        try
        {
            List<GenericValue> customFields = getCustomFieldsFromDB();
            if (customFields != null && !customFields.isEmpty())
            {
                for (GenericValue customFieldGv : customFields)
                {
                    CustomFieldImpl customFieldImpl = new CustomFieldImpl(customFieldGv, this, authenticationContext, constantsManager, fieldConfigSchemeManager, permissionManager, rendererManager, fieldConfigSchemeClauseContextUtil);
                    // Don't add if the customfield type is invalid
                    if (customFieldImpl.getCustomFieldType() != null)
                    {
                        // Attach configurations
                        final List<FieldConfigScheme> configForCustomField = fieldConfigSchemeManager.getConfigSchemesForField(customFieldImpl);
                        customFieldImpl.setConfigurationSchemes(configForCustomField);

                        customFieldObjectsList.add(customFieldImpl);
                        customFieldObjectsMapById.put(customFieldGv.getLong(CustomFieldImpl.ENTITY_ID), customFieldImpl);
                        customFieldObjectsMapByName.put(customFieldGv.getString(CustomFieldImpl.ENTITY_NAME), customFieldImpl);
                    }
                }
            }

            // Synchronize to ensure that the collections are assigned values initialised by one thread
            synchronized (this)
            {
                // These collection should never be modified once they are created. So they do not need to be synchronized in any way
                this.allCustomFieldObjectsMapById = new ConcurrentHashMap<Long, CustomField>(customFieldObjectsMapById);
                this.allCustomFieldObjectsMapByName = customFieldObjectsMapByName;
                this.allCustomFieldObjectsList = customFieldObjectsList;
            }

        }
        catch (Exception e)
        {
            log.error("Exception thrown in DefaultCustomFieldManager.populateCache()");
            log.error(e.getMessage(), e);
        }
    }

    public CustomField getCustomFieldInstance(GenericValue customFieldGv)
    {
        return new CustomFieldImpl(customFieldGv, this, authenticationContext, constantsManager, fieldConfigSchemeManager, permissionManager, rendererManager, fieldConfigSchemeClauseContextUtil);
    }

    public void removeProjectAssociations(GenericValue project)
    {
        contextPersister.removeContextsForProject(project);
        refresh();
    }

    public void removeProjectCategoryAssociations(GenericValue projectCategory)
    {
        contextPersister.removeContextsForProjectCategory(projectCategory);
        refresh();
    }

    @EventListener
    public void onPluginModuleEnabled(final PluginModuleEnabledEvent event)
    {
        // when a CustomFieldSearcher is enabled we need to flush the cache so that the searcher
        // can be associated with the correct CustomFieldType.  We could be selective, but for now
        // simply blow the entire cache away.
        if (event.getModule() instanceof CustomFieldSearcherModuleDescriptor)
        {
           clear();
        }
    }
}
