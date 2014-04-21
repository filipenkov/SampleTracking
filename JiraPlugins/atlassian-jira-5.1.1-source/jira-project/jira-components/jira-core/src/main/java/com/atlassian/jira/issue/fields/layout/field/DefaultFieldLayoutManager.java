package com.atlassian.jira.issue.fields.layout.field;

import com.atlassian.core.ofbiz.association.AssociationManager;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.event.api.EventListener;
import com.atlassian.jira.EventComponent;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.entity.EntityUtils;
import com.atlassian.jira.event.ClearCacheEvent;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.layout.field.enterprise.ImmutableFieldConfigurationScheme;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.ofbiz.FieldMap;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.scheme.SchemeManager;
import com.atlassian.jira.util.Function;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.JiraEntityUtils;
import com.atlassian.jira.util.collect.CollectionUtil;
import com.atlassian.jira.util.dbc.Assertions;
import com.atlassian.jira.util.map.CacheObject;
import com.opensymphony.util.TextUtils;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.EntityUtil;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

@EventComponent
public class DefaultFieldLayoutManager extends AbstractFieldLayoutManager implements FieldLayoutManager
{
    private static final Logger log = Logger.getLogger(DefaultFieldLayoutManager.class);
    private final ConstantsManager constantsManager;
    private final SubTaskManager subTaskManager;

    /**
     * Cache of Project ID to Field Configuration (FieldLayout) Scheme ID.
     */
    private final ConcurrentMap<Long, CacheObject<Long>> fieldSchemeCache = new ConcurrentHashMap<Long, CacheObject<Long>>();
    /**
     * Cache of Immutables objects that give us the FieldConfigurationScheme mapping of IssueType to FieldLayoutId.
     *
     * See JRA-16870. This replaced the old schemeCache of FieldLayoutScheme objects.
     * FieldLayoutScheme is mutable, and therefore had to lock on certain operations, causing major lock contention issues.
     */
    private final ConcurrentMap<Long, ImmutableFieldConfigurationScheme> fieldConfigurationSchemeCache = new ConcurrentHashMap<Long, ImmutableFieldConfigurationScheme>();
    private static final String FIELD_LAYOUT_SCHEME_ASSOCIATION = "FieldLayoutScheme";
    private ProjectManager projectManager;
    private final AssociationManager associationManager;

    public DefaultFieldLayoutManager(FieldManager fieldManager, OfBizDelegator ofBizDelegator, final ConstantsManager constantsManager,
            final SubTaskManager subTaskManager, final ProjectManager projectManager, I18nHelper.BeanFactory i18n, AssociationManager associationManager)
    {
        super(fieldManager, ofBizDelegator, i18n);
        this.projectManager = projectManager;
        this.constantsManager = notNull("constantsManager", constantsManager);
        this.subTaskManager = notNull("subTaskManager", subTaskManager);
        this.associationManager = associationManager;
    }

    @SuppressWarnings ({ "UnusedDeclaration" })
    @EventListener
    public void onClearCache(final ClearCacheEvent event)
    {
        refresh();
    }

    public FieldLayout getFieldLayout(GenericValue issue)
    {
        if (issue == null)
        {
            throw new IllegalArgumentException("Issue cannot be null.");
        }
        if (!"Issue".equals(issue.getEntityName()))
        {
            throw new IllegalArgumentException("GenericValue must be an issue. It is a(n) " + issue.getEntityName() + ".");
        }

        return getFieldLayout(JiraEntityUtils.getProject(issue), issue.getString("type"));
    }

    public FieldLayout getFieldLayout(Project project, String issueTypeId)
    {
        if (project == null)
        {
            throw new IllegalArgumentException("Project cannot be null.");
        }

        // Retrieve the scheme from the project
        FieldConfigurationScheme fieldConfigurationScheme = getFieldConfigurationScheme(project);

        if (fieldConfigurationScheme != null)
        {
            // Lookup the Field Layout id for the issue type
            Long fieldLayoutId = fieldConfigurationScheme.getFieldLayoutId(issueTypeId);
            // Retrieve the field layout for the given id
            return getRelevantFieldLayout(fieldLayoutId);
        }
        else
        {
            // If the project is not associated with any field layout schemes use the system default
            return getFieldLayout();
        }
    }

    public FieldLayout getFieldLayout(GenericValue project, String issueTypeId)
    {
        if (project == null)
        {
            throw new IllegalArgumentException("Generic Value cannot be null.");
        }
        if (!"Project".equals(project.getEntityName()))
        {
            throw new IllegalArgumentException("Generic Value must be a Project - '" + project.getEntityName() + "' is not.");
        }

        // Retrieve the scheme from the project
        FieldConfigurationScheme fieldConfigurationScheme = getFieldConfigurationScheme(project);

        if (fieldConfigurationScheme != null)
        {
            // Lookup the Field Layout id for the issue type
            Long fieldLayoutId = fieldConfigurationScheme.getFieldLayoutId(issueTypeId);
            // Retrieve the field layout for the given id
            return getRelevantFieldLayout(fieldLayoutId);
        }
        else
        {
            // If the project is not associated with any field layout schemes use the system default
            return getFieldLayout();
        }
    }

    private ImmutableFieldConfigurationScheme buildFieldConfigurationScheme(final GenericValue fieldLayoutSchemeGV)
    {
        Assertions.notNull("fieldLayoutSchemeGV", fieldLayoutSchemeGV);
        // Get the Scheme entities (these are the mappings from IssueTypeId -> FieldLayoutId)
        final Collection<GenericValue> schemeEntities = getFieldLayoutSchemeEntityGVs(fieldLayoutSchemeGV.getLong("id"));
        return new ImmutableFieldConfigurationScheme(fieldLayoutSchemeGV, schemeEntities);
    }

    private Collection<GenericValue> getFieldLayoutSchemeEntityGVs(Long fieldLayoutSchemeId)
    {
        try
        {
            return ofBizDelegator.findByField("FieldLayoutSchemeEntity", "scheme", fieldLayoutSchemeId);
        }
        catch (DataAccessException e)
        {
            throw new DataAccessException("Error occurred while retrieving field layout scheme entities from the database.", e);
        }
    }

    public List<FieldLayoutScheme> getFieldLayoutSchemes()
    {
        List<FieldLayoutScheme> fieldLayoutSchemes = new LinkedList<FieldLayoutScheme>();
        List<GenericValue> fieldLayoutSchemeGVs = ofBizDelegator.findAll(SCHEME, Collections.singletonList("name ASC"));
        for (final GenericValue fieldLayoutSchemeGV : fieldLayoutSchemeGVs)
        {
            fieldLayoutSchemes.add(buildFieldLayoutScheme(fieldLayoutSchemeGV));
        }

        return fieldLayoutSchemes;
    }

    public Collection<GenericValue> getRelatedProjects(FieldLayout fieldLayout)
    {
        Collection<GenericValue> relatedProjects = new ArrayList<GenericValue>();
        // Find all the custom schemes that use this fieldLayout
        for (final FieldConfigurationScheme fieldConfigurationScheme : getFieldConfigurationSchemes(fieldLayout))
        {
            // For each scheme, we add all projects that use that scheme
            relatedProjects.addAll(getProjects(fieldConfigurationScheme));
        }
        // If the fieldLayout is the Default one, then we need to consider Projects that use the Default FieldConfigurationScheme
        if (fieldLayout.isDefault())
        {
            relatedProjects.addAll(getProjects((FieldConfigurationScheme) null));
        }
        
        return relatedProjects;
    }

    public List<EditableFieldLayout> getEditableFieldLayouts()
    {
        List<EditableFieldLayout> fieldLayouts = new LinkedList<EditableFieldLayout>();
        // Retrieve the default field layout
        fieldLayouts.add(getEditableDefaultFieldLayout());

        // Get all non-default field layouts
        List<GenericValue> fieldLayoutGVs = ofBizDelegator.findByField("FieldLayout", "type", null, "name");
        for (final GenericValue editableFieldLayoutGV : fieldLayoutGVs)
        {
            FieldLayout fieldLayout = getRelevantFieldLayout(editableFieldLayoutGV.getLong("id"));
            fieldLayouts.add(new EditableFieldLayoutImpl(fieldLayout.getGenericValue(), fieldLayout.getFieldLayoutItems()));
        }

        return fieldLayouts;
    }

    public EditableFieldLayout getEditableFieldLayout(Long id)
    {
        FieldLayout fieldLayout = getRelevantFieldLayout(id);
        return new EditableFieldLayoutImpl(fieldLayout.getGenericValue(), fieldLayout.getFieldLayoutItems());
    }

    public void updateFieldLayoutScheme(FieldLayoutScheme fieldLayoutScheme)
    {
        if (!TextUtils.stringSet(fieldLayoutScheme.getName()))
        {
            throw new IllegalArgumentException("Name passed must not be null.");
        }

        try
        {
            // The field layout scheme might have been cached a few times. So take the conservative approach
            // and clear the whole cache.
            clearCaches();

            // Now update the scheme
            fieldLayoutScheme.getGenericValue().store();
        }
        catch (GenericEntityException e)
        {
            throw new DataAccessException(e);
        }
    }

    public void deleteFieldLayoutScheme(FieldLayoutScheme fieldLayoutScheme)
    {
        try
        {
            // Remove any project assoications to the this scheme
            associationManager.removeAssociationsFromSink(fieldLayoutScheme.getGenericValue());

            // Remove the scheme
            fieldLayoutScheme.getGenericValue().remove();

            // Reset the caches
            refresh();
        }
        catch (GenericEntityException e)
        {
            throw new DataAccessException(e);
        }
    }

    public void deleteFieldLayout(FieldLayout fieldLayout)
    {
        try
        {
            GenericValue genericValue = fieldLayout.getGenericValue();
            if (genericValue != null)
            {
                genericValue.removeRelated("ChildFieldLayoutItem");
                genericValue.remove();
            }

            clearCaches();
        }
        catch (GenericEntityException e)
        {
            throw new DataAccessException(e);
        }
    }

    public Collection<FieldLayoutSchemeEntity> getFieldLayoutSchemeEntities(FieldLayoutScheme fieldLayoutScheme)
    {
        try
        {
            List<FieldLayoutSchemeEntity> fieldLayoutSchemeEntities = new LinkedList<FieldLayoutSchemeEntity>();

            List<GenericValue> fieldLayoutSchemeEntityGVs = ofBizDelegator.findByField("FieldLayoutSchemeEntity", "scheme", fieldLayoutScheme.getId());
            for (final GenericValue fieldLayoutSchemeEntityGV : fieldLayoutSchemeEntityGVs)
            {
                FieldLayoutSchemeEntity fieldLayoutSchemeEntity = new FieldLayoutSchemeEntityImpl(this, fieldLayoutSchemeEntityGV, ComponentAccessor.getConstantsManager());
                fieldLayoutSchemeEntity.setFieldLayoutScheme(fieldLayoutScheme);
                fieldLayoutSchemeEntities.add(fieldLayoutSchemeEntity);
            }

            return fieldLayoutSchemeEntities;

        }
        catch (DataAccessException e)
        {
            throw new DataAccessException("Error occurred while retrieving field layout scheme entities from the database.", e);
        }
    }

    public void createFieldLayoutSchemeEntity(FieldLayoutSchemeEntity fieldLayoutSchemeEntity)
    {
        String issueTypeId = fieldLayoutSchemeEntity.getIssueTypeId();
        GenericValue fieldLayoutSchemeEntityGV = EntityUtils.createValue("FieldLayoutSchemeEntity", EasyMap.build("scheme", fieldLayoutSchemeEntity.getFieldLayoutScheme().getId(), "issuetype", issueTypeId, "fieldlayout", fieldLayoutSchemeEntity.getFieldLayoutId()));
        fieldLayoutSchemeEntity.setGenericValue(fieldLayoutSchemeEntityGV);
        clearCaches();
    }

    public void updateFieldLayoutSchemeEntity(FieldLayoutSchemeEntity fieldLayoutSchemeEntity)
    {
        try
        {
            fieldLayoutSchemeEntity.getGenericValue().store();
            clearCaches();
        }
        catch (GenericEntityException e)
        {
            throw new DataAccessException("Error occurred while persisting field layout scheme entity.", e);
        }
    }

    public void removeFieldLayoutSchemeEntity(FieldLayoutSchemeEntity fieldLayoutSchemeEntity)
    {
        try
        {
            fieldLayoutSchemeEntity.getGenericValue().remove();
            clearCaches();
        }
        catch (GenericEntityException e)
        {
            throw new DataAccessException("Error occurred while removing field layout scheme entity.", e);
        }
    }

    public void removeFieldLayoutScheme(FieldLayoutScheme fieldLayoutScheme)
    {
        try
        {
            fieldLayoutScheme.getGenericValue().remove();
            clearCaches();
        }
        catch (GenericEntityException e)
        {
            throw new DataAccessException(e);
        }
    }

    public Collection<FieldConfigurationScheme> getFieldConfigurationSchemes(FieldLayout fieldLayout)
    {
        // The Default FieldLayout will have a real ID if it has been customized, however the FieldLayoutSchemeEntity
        // will continue to use null to represent this FieldLayout
        Long id = fieldLayout.isDefault() ? null : fieldLayout.getId();

        // Build up a set of unique scheme ID's.
        Set<Long> fieldLayoutSchemeIds = new HashSet<Long>();
        // Find the FieldLayoutSchemeEntity's that include this fieldlayout
        List<GenericValue> fieldLayoutSchemeEntitiyGVs = ofBizDelegator.findByField("FieldLayoutSchemeEntity", "fieldlayout", id);
        for (final GenericValue fieldLayoutSchemeEntitiyGV : fieldLayoutSchemeEntitiyGVs)
        {
            fieldLayoutSchemeIds.add(fieldLayoutSchemeEntitiyGV.getLong("scheme"));
        }

        // Now turn our set of ID's into a Collection of scheme objects.
        Set<FieldConfigurationScheme> fieldConfigurationSchemes = new HashSet<FieldConfigurationScheme>(fieldLayoutSchemeIds.size());
        for (final Long schemeId : fieldLayoutSchemeIds)
        {
            fieldConfigurationSchemes.add(getFieldConfigurationScheme(schemeId));
        }

        return fieldConfigurationSchemes;
    }

    public void restoreSchemeFieldLayout(GenericValue scheme)
    {
        if (scheme == null)
        {
            throw new IllegalArgumentException("Scheme passed must not be null.");
        }
        restoreFieldLayout(scheme.getLong("id"));
    }

    public Collection<GenericValue> getProjects(FieldConfigurationScheme fieldConfigurationScheme)
    {
        if (fieldConfigurationScheme == null)
        {
            return getProjectsWithDefaultFieldConfigurationScheme();
        }
        GenericValue fieldConfigurationSchemeGV = makeFieldLayoutSchemeGenericValue(fieldConfigurationScheme.getId());
        try
        {
            return associationManager.getSourceFromSink(fieldConfigurationSchemeGV, "Project", SchemeManager.PROJECT_ASSOCIATION, false);
        }
        catch (GenericEntityException ex)
        {
            throw new DataAccessException(ex);
        }
    }

    private Collection<GenericValue> getProjectsWithDefaultFieldConfigurationScheme()
    {

        final Collection<GenericValue> projects = new ArrayList<GenericValue>();
        for (final GenericValue project : projectManager.getProjects())
        {
            if (getFieldConfigurationScheme(project) == null)
            {
                projects.add(project);
            }
        }
        return projects;
    }

    public Collection<GenericValue> getProjects(FieldLayoutScheme fieldLayoutScheme)
    {
        try
        {
            return associationManager.getSourceFromSink(fieldLayoutScheme.getGenericValue(), "Project", SchemeManager.PROJECT_ASSOCIATION, false);
        }
        catch (GenericEntityException e)
        {
            throw new DataAccessException(e);
        }
    }

    public FieldLayoutScheme createFieldLayoutScheme(FieldLayoutScheme fieldLayoutScheme)
    {
        GenericValue genericValue = EntityUtils.createValue(SCHEME, FieldMap.build("name", fieldLayoutScheme.getName(), "description", fieldLayoutScheme.getDescription()));
        fieldLayoutScheme.setGenericValue(genericValue);
        return fieldLayoutScheme;
    }

    private FieldLayoutScheme buildFieldLayoutScheme(GenericValue genericValue)
    {
        if (genericValue != null)
        {
            return new FieldLayoutSchemeImpl(this, genericValue);
        }
        else
        {
            return null;
        }
    }

    public FieldConfigurationScheme getFieldConfigurationScheme(final Long schemeId)
    {
        // Try to get our Immutable object from the cache
        ImmutableFieldConfigurationScheme fieldConfigurationScheme = fieldConfigurationSchemeCache.get(schemeId);
        if (fieldConfigurationScheme != null)
        {
            return fieldConfigurationScheme;
        }

        // Cache miss - we build the Immutable scheme object using the FieldLayoutScheme GenericValue
        final GenericValue fieldLayoutSchemeGV = ofBizDelegator.findById(SCHEME, schemeId);
        if (fieldLayoutSchemeGV == null)
        {
            throw new DataAccessException("No " + SCHEME + " found for id " + schemeId);
        }
        fieldConfigurationScheme = buildFieldConfigurationScheme(fieldLayoutSchemeGV);
        // Cache it for next time
        final ImmutableFieldConfigurationScheme result = fieldConfigurationSchemeCache.putIfAbsent(schemeId, fieldConfigurationScheme);
        return (result == null) ? fieldConfigurationScheme : result;
    }

    public FieldLayoutScheme getMutableFieldLayoutScheme(Long schemeId)
    {
        return buildFieldLayoutScheme(ofBizDelegator.findById(SCHEME, schemeId));
    }

    public boolean fieldConfigurationSchemeExists(String schemeName)
    {
        return !ofBizDelegator.findByField(SCHEME, "name", schemeName).isEmpty();
    }

    public Set<FieldLayout> getUniqueFieldLayouts(Project project)
    {
        final Set<FieldLayout> uniqueLayouts = new HashSet<FieldLayout>();
        final FieldConfigurationScheme scheme = getFieldConfigurationScheme(project.getGenericValue());
        if (scheme != null)
        {
            // Run through all the layouts id's for the scheme and resolve them
            for (Long layoutId : scheme.getAllFieldLayoutIds(constantsManager.getAllIssueTypeIds()))
            {
                final FieldLayout fieldLayout = getFieldLayout(layoutId);
                if (fieldLayout != null)
                {
                    uniqueLayouts.add(fieldLayout);
                }
            }
        }
        else
        {
            // If the project is not associated with any field layout schemes use the system default
            uniqueLayouts.add(getFieldLayout());
        }

        return uniqueLayouts;
    }

    public FieldConfigurationScheme getFieldConfigurationScheme(Project project)
    {
        if (project == null)
        {
            throw new IllegalArgumentException("Project passed must not be null.");
        }
        return getFieldConfigurationScheme(project.getGenericValue());
    }

    public FieldConfigurationScheme getFieldConfigurationScheme(GenericValue project)
    {
        if (project == null)
        {
            log.error("Project passed must not be null.");
            throw new IllegalArgumentException("Project passed must not be null.");
        }

        final Long projectId = project.getLong("id");
        // Use the fieldSchemeCache to try get the cached SchemeId for this project
        CacheObject<Long> cacheObject = fieldSchemeCache.get(projectId);
        if (cacheObject != null)
        {
            // We have a cache hit
            if (cacheObject.getValue() != null)
            {
                // Get the FieldConfigurationScheme object for this ID
                return getFieldConfigurationScheme(cacheObject.getValue());
            }
            else
            {
                return null;
            }
        }

        try
        {
            // Cache miss on ProjectId -> Scheme ID. Get the FieldLayoutScheme from AssociationManager
            GenericValue fieldLayoutSchemeGV = EntityUtil.getOnly(associationManager.getSinkFromSource(project, FIELD_LAYOUT_SCHEME_ASSOCIATION, SchemeManager.PROJECT_ASSOCIATION, false));
            if (fieldLayoutSchemeGV != null)
            {
                // We have a non-default scheme - build the FieldConfigurationScheme object from the GenericValue.
                ImmutableFieldConfigurationScheme scheme = buildFieldConfigurationScheme(fieldLayoutSchemeGV);
                // Cache the ProjectId -> Scheme ID
                fieldSchemeCache.putIfAbsent(projectId, new CacheObject<Long>(scheme.getId()));
                return scheme;
            }
            else
            {
                // Cache null value to indicate a field layout scheme is not assigned to this project
                fieldSchemeCache.putIfAbsent(projectId, new CacheObject<Long>(null));
                return null;
            }
        }
        catch (GenericEntityException e)
        {
            throw new DataAccessException("Error occurred while retrieving a field layout scheme.", e);
        }
    }

    public void addSchemeAssociation(GenericValue project, Long fieldLayoutSchemeId)
    {
        if (project == null)
        {
            throw new IllegalArgumentException("Project passed must not be null.");
        }

        try
        {
            // Get old association
            FieldConfigurationScheme oldScheme = getFieldConfigurationScheme(project);
            if (oldScheme != null)
            {
                // Remove old association
                removeSchemeAssociation(project, oldScheme.getId());
            }

            if (fieldLayoutSchemeId != null)
            {
                // TODO: Update AssociationManager so it does not require GenericValues.
                GenericValue gvFieldLayoutScheme = makeFieldLayoutSchemeGenericValue(fieldLayoutSchemeId);
                associationManager.createAssociation(project, gvFieldLayoutScheme, SchemeManager.PROJECT_ASSOCIATION);
            }
            clearCaches();
        }
        catch (GenericEntityException ex)
        {
            throw new DataAccessException(ex);
        }
    }

    /**
     * Makes a simple FieldLayoutScheme GenericValue with just the "id" field populated.
     * <p>Note that this GenericValue exists in memory only - it is not persisted.
     * <p>This is used to pass to the AssociationManager, which requires GenericValue in its arguments.
     *
     * @param fieldLayoutSchemeId FieldLayoutScheme ID
     * @return a simple FieldLayoutScheme GenericValue with just the "id" field populated.
     */
    private GenericValue makeFieldLayoutSchemeGenericValue(final Long fieldLayoutSchemeId)
    {
        GenericValue gvFieldLayoutScheme = ofBizDelegator.makeValue(SCHEME);
        gvFieldLayoutScheme.set("id", fieldLayoutSchemeId);
        return gvFieldLayoutScheme;
    }

    public void removeSchemeAssociation(GenericValue project, Long fieldLayoutSchemeId)
    {
        // TODO: Update AssociationManager so it does not require GenericValues.
        GenericValue gvFieldLayoutScheme = ofBizDelegator.makeValue(SCHEME);
        gvFieldLayoutScheme.set("id", fieldLayoutSchemeId);

        try
        {
            associationManager.removeAssociation(project, gvFieldLayoutScheme, SchemeManager.PROJECT_ASSOCIATION);
        }
        catch (GenericEntityException ex)
        {
            throw new DataAccessException(ex);
        }

        // Clear the caches
        clearCaches();
    }

    public FieldLayout getFieldLayout(Long id)
    {
        return getRelevantFieldLayout(id);
    }

    public void refresh()
    {
        clearCaches();
        super.refresh();
    }

    protected void clearCaches()
    {
        fieldSchemeCache.clear();
        fieldConfigurationSchemeCache.clear();
    }

    public boolean isFieldLayoutSchemesVisiblyEquivalent(Long fieldConfigurationSchemeId1, Long fieldConfigurationSchemeId2)
    {
        // short circuit for comparing the system default with itself.
        if (fieldConfigurationSchemeId1 == null && fieldConfigurationSchemeId2 == null)
        {
            return true;
        }
        FieldConfigurationScheme scheme1 = getNotNullFieldConfigurationScheme(fieldConfigurationSchemeId1);
        FieldConfigurationScheme scheme2 = getNotNullFieldConfigurationScheme(fieldConfigurationSchemeId2);

        // Check the mapped FieldConfiguration for each Issue Type
        for (String issueType : getAllRelevantIssueTypeIds())
        {
            if (!isFieldLayoutsVisiblyEquivalent(scheme1.getFieldLayoutId(issueType), scheme2.getFieldLayoutId(issueType)))
            {
                return false;
            }
        }
        // All checks OK
        return true;
    }

    private FieldConfigurationScheme getNotNullFieldConfigurationScheme(final Long fieldConfigurationSchemeId)
    {
        if (fieldConfigurationSchemeId == null)
        {
            return new DefaultFieldConfigurationScheme();
        }
        else
        {
            return getFieldConfigurationScheme(fieldConfigurationSchemeId);
        }
    }

    public boolean isFieldLayoutsVisiblyEquivalent(final Long fieldLayoutId1, final Long fieldLayoutId2)
    {
        final Map<String, Boolean> map1 = createFieldIdToVisibilityMap(fieldLayoutId1);
        final Map<String, Boolean> map2 = createFieldIdToVisibilityMap(fieldLayoutId2);
        return map1.equals(map2);
    }

    private Map<String, Boolean> createFieldIdToVisibilityMap(final Long fieldLayoutId)
    {
        FieldLayout fieldLayout = getFieldLayout(fieldLayoutId);
        final List<FieldLayoutItem> list = fieldLayout.getFieldLayoutItems();
        final Map<String, Boolean> map = new HashMap<String, Boolean>();
        for (FieldLayoutItem item : list)
        {
            map.put(item.getOrderableField().getId(), item.isHidden());
        }
        return map;
    }

    ///CLOVER:OFF
    protected List<String> getAllRelevantIssueTypeIds()
    {
        if (subTaskManager.isSubTasksEnabled())
        {
            return constantsManager.getAllIssueTypeIds();
        }
        else
        {
            return CollectionUtil.transform(constantsManager.getRegularIssueTypeObjects().iterator(), new Function<IssueType, String>()
            {
                public String get(final IssueType input)
                {
                    return input.getId();
                }
            });
        }
    }
    ///CLOVER:ON

    /**
     * A FieldConfigurationScheme representing the default system scheme which usually is null.
     */
    private class DefaultFieldConfigurationScheme implements FieldConfigurationScheme
    {
        public Long getId()
        {
            return null;
        }

        public String getName()
        {
            return "Default Field Configuration Scheme";
        }

        public String getDescription()
        {
            return "";
        }

        public Long getFieldLayoutId(final String issueTypeId)
        {
            return null;
        }

        public Set<Long> getAllFieldLayoutIds(final Collection<String> allIssueTypeIds)
        {
            return Collections.singleton(null);
        }
    }
}
