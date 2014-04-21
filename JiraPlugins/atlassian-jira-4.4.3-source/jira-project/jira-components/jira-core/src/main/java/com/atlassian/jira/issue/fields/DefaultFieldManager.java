/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.issue.fields;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.event.ClearCacheEvent;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.extension.Startable;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.customfields.CustomFieldUtils;
import com.atlassian.jira.issue.fields.layout.column.ColumnLayoutManager;
import com.atlassian.jira.issue.fields.layout.field.FieldLayout;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.fields.renderer.RenderableField;
import com.atlassian.jira.jql.context.QueryContext;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.util.JiraUtils;
import com.atlassian.jira.util.collect.CollectionBuilder;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.functors.InstanceofPredicate;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DefaultFieldManager implements FieldManager, Startable
{
    private static final Logger log = Logger.getLogger(DefaultFieldManager.class);

    private FieldLayoutManager fieldLayoutManager;
    private Map<String, Field> fields;
    private List<OrderableField> orderableFields;
    private List<NavigableField> navigableFields;
    private ProjectSystemField projectSystemField;
    private final EventPublisher eventPublisher;

    public DefaultFieldManager(final EventPublisher eventPublisher)
    {
        this.eventPublisher = eventPublisher;
        init();
    }

    public void start() throws Exception
    {
        eventPublisher.register(this);
    }

    @EventListener
    public void onClearCache(final ClearCacheEvent event)
    {
        // TODO: NOT_THREAD_SAFE
        init();
    }

    private void init()
    {
        // add all static fields to the map
        fields = new HashMap<String, Field>();
        orderableFields = new ArrayList<OrderableField>();
        navigableFields = new ArrayList<NavigableField>();

        // If you add a column here - some tests will fail. Make sure you run tests before you check in!

        projectSystemField = createAndRegister(ProjectSystemField.class);
        navigableFields.add(projectSystemField);

        navigableFields.add(createAndRegister(KeySystemField.class));

        SummarySystemField summarySystemField = createAndRegister(SummarySystemField.class);
        orderableFields.add(summarySystemField);
        navigableFields.add(summarySystemField);

        IssueTypeSystemField issueTypeSystemField = createAndRegister(IssueTypeSystemField.class);
        orderableFields.add(issueTypeSystemField);
        navigableFields.add(issueTypeSystemField);

        navigableFields.add(createAndRegister(StatusSystemField.class));

        PrioritySystemField prioritySystemField = createAndRegister(PrioritySystemField.class);
        orderableFields.add(prioritySystemField);
        navigableFields.add(prioritySystemField);

        ResolutionSystemField resolutionSystemField = createAndRegister(ResolutionSystemField.class);
        orderableFields.add(resolutionSystemField);
        navigableFields.add(resolutionSystemField);

        AssigneeSystemField assigneeSystemField = createAndRegister(AssigneeSystemField.class);
        orderableFields.add(assigneeSystemField);
        navigableFields.add(assigneeSystemField);

        ReporterSystemField reporterSystemField = createAndRegister(ReporterSystemField.class);
        orderableFields.add(reporterSystemField);
        navigableFields.add(reporterSystemField);

        navigableFields.add(createAndRegister(CreatedSystemField.class));

        navigableFields.add(createAndRegister(UpdatedSystemField.class));

        navigableFields.add(createAndRegister(ResolutionDateSystemField.class));

        AffectedVersionsSystemField affectedVersionsSystemField = createAndRegister(AffectedVersionsSystemField.class);
        orderableFields.add(affectedVersionsSystemField);
        navigableFields.add(affectedVersionsSystemField);

        FixVersionsSystemField fixVersionsSystemField = createAndRegister(FixVersionsSystemField.class);
        orderableFields.add(fixVersionsSystemField);
        navigableFields.add(fixVersionsSystemField);

        ComponentsSystemField componentsSystemField = createAndRegister(ComponentsSystemField.class);
        orderableFields.add(componentsSystemField);
        navigableFields.add(componentsSystemField);

        DueDateSystemField dueDateSystemField = createAndRegister(DueDateSystemField.class);
        orderableFields.add(dueDateSystemField);
        navigableFields.add(dueDateSystemField);

        navigableFields.add(createAndRegister(VotesSystemField.class));
        navigableFields.add(createAndRegister(WatchesSystemField.class));

        navigableFields.add(createAndRegister(ThumbnailSystemField.class));

        navigableFields.add(createAndRegister(OriginalEstimateSystemField.class));

        navigableFields.add(createAndRegister(TimeEstimateSystemField.class));

        navigableFields.add(createAndRegister(TimeSpentSystemField.class));

        navigableFields.add(createAndRegister(WorkRatioSystemField.class));

        navigableFields.add(createAndRegister(SubTaskSystemField.class));

        IssueLinksSystemField issueLinksSystemField = createAndRegister(IssueLinksSystemField.class);
        orderableFields.add(issueLinksSystemField);
        navigableFields.add(issueLinksSystemField);

        // JRA-3663 - 'Attachment' field is added to the issue fields - but must ensure that it is always optional.
        orderableFields.add(createAndRegister(AttachmentSystemField.class));

        EnvironmentSystemField environmentSystemField = createAndRegister(EnvironmentSystemField.class);
        orderableFields.add(environmentSystemField);
        navigableFields.add(environmentSystemField);

        DescriptionSystemField descriptionSystemField = createAndRegister(DescriptionSystemField.class);
        orderableFields.add(descriptionSystemField);
        navigableFields.add(descriptionSystemField);

        orderableFields.add(createAndRegister(TimeTrackingSystemField.class));

        SecurityLevelSystemField securityLevelSystemField = createAndRegister(SecurityLevelSystemField.class);
        orderableFields.add(securityLevelSystemField);
        navigableFields.add(securityLevelSystemField);

        orderableFields.add(createAndRegister(CommentSystemField.class));

        navigableFields.add(createAndRegister(ProgressBarSystemField.class));

        navigableFields.add(createAndRegister(AggregateProgressBarSystemField.class));

        navigableFields.add(createAndRegister(AggregateTimeSpentSystemField.class));

        navigableFields.add(createAndRegister(AggregateEstimateSystemField.class));

        navigableFields.add(createAndRegister(AggregateOriginalEstimateSystemField.class));

        LabelsSystemField labelsSystemField = createAndRegister(LabelsSystemField.class);
        orderableFields.add(labelsSystemField);
        navigableFields.add(labelsSystemField);

        orderableFields.add(createAndRegister(WorklogSystemField.class));
    }

    /**
     * Instantiates the given class and registers it by id in #fields.
     *
     * @param clazz the class of the field.
     * @return the instance of the field, already registered.
     */
    private <T extends Field> T createAndRegister(Class<T> clazz)
    {
        T field = JiraUtils.loadComponent(clazz);
        fields.put(field.getId(), field);
        return field;
    }

    public Field getField(String id)
    {
        if (isCustomField(id))
        {
            return getCustomField(id);
        }
        else
        {
            return fields.get(id);
        }
    }

    /**
     * Returns a set of {@link Field}s that are NOT hidden in AT LEAST ONE project in the system.
     * <p/>
     * NOTE: This method is used in the Admin interface, as admins should be able to configure the default ColumnLayouts
     * irrespective of their permissions. They should be able to see all fields that are not hidden in at least one
     * FieldLayout in the system
     *
     * @throws FieldException if cannot retrieve the projects the user can see, or if cannot retrieve the field layouts
     * for the viewable projects
     */
    public Set<NavigableField> getAllAvailableNavigableFields() throws FieldException
    {
        try
        {
            // Include custom fields (irrespective of scope) and exclude fields that should not be available (e.g. timetracking if it is turned off)
            Set<NavigableField> allAvailableFields = getAvailableNavigableFields();

            // Retrieve all unique FieldLayouts in the system
            Set<FieldLayout> uniqueSchemes = getAllFieldLayouts();

            // Go through the list of available fields and see of the field is NOT hidden in at least one scheme
            return getAvailableFields(allAvailableFields, uniqueSchemes);
        }
        catch (DataAccessException e)
        {
            String message = "Error retrieving field layout.";
            log.error(message, e);
            throw new FieldException(message, e);
        }
    }

    /**
     * Returns a set of {@link Field}s that are NOT hidden in AT LEAST ONE project that the remote user can see (has
     * {@link com.atlassian.jira.security.Permissions#BROWSE} permission for).
     * <p/>
     * The returned set of fields contains all custom fields that are not hidden in AT LEAST one FieldLayout that the
     * user can see.
     * <p/>
     * NOTE: This method is primarily used for configuring user's ColumnLayout, as the user should be able to add any
     * field (including custom field) to it that they can see in the system. THe scope of custom fields is ignored here
     * as the user configures the ColumnLayout outside of scope.
     *
     * @param remoteUser the remote user
     * @throws FieldException if cannot retrieve the projects the user can see, or if cannot retrieve the field layouts
     * for the viewable projects
     */
    public Set<NavigableField> getAvailableNavigableFields(User remoteUser) throws FieldException
    {
        Set<NavigableField> availableFields = new HashSet<NavigableField>();
        try
        {
            Set<FieldLayout> uniqueSchemes = getUniqueSchemes(Collections.<Long>emptyList(), Collections.<String>emptyList(), remoteUser);

            // Include custom fields and exclude fields that should not be available (e.g. timetracking it is turned off)
            Set<NavigableField> allAvailableFields = getAvailableNavigableFields();

            // Go through the list of available fields and see of the field is NOT hidden in at least one scheme
            for (final NavigableField field : allAvailableFields)
            {
                if (!isFieldHidden(uniqueSchemes, field))
                {
                    // if the field is a project custom field ensure that the user can see the project
                    if (isCustomField(field))
                    {
                        // Check if the user has permission to the associated projects
                        CustomField customField = getCustomField(field.getId());
                        if (CustomFieldUtils.isUserHasPermissionToProjects(customField, remoteUser))
                        {
                            availableFields.add(field);
                        }
                    }
                    else
                    {
                        // The field is not a custom field and is is not hidden, add it to the list
                        availableFields.add(field);
                    }
                }
            }
            return availableFields;
        }
        catch (DataAccessException e)
        {
            String message = "Error retrieving field layout.";
            log.error(message, e);
            throw new FieldException(message, e);
        }
    }

    public final Set<NavigableField> getAvailableNavigableFields(com.opensymphony.user.User remoteUser)
            throws FieldException
    {
        return getAvailableNavigableFields((User) remoteUser);
    }

    public Set<SearchableField> getAllSearchableFields()
    {
        final Set<SearchableField> allFields = new LinkedHashSet<SearchableField>();
        addAllSystemSearchableFields(allFields);
        //All custom fields are SearchableFields so we don't have to filter them.
        allFields.addAll(getCustomFieldManager().getCustomFieldObjects());
        return allFields;
    }

    public Set<SearchableField> getSystemSearchableFields()
    {
        final Set<SearchableField> allFields = new LinkedHashSet<SearchableField>();
        addAllSystemSearchableFields(allFields);
        return allFields;
    }

    // --------------------------------------------------------------------------------------------- Convenience Methods
    @Override
    public IssueTypeField getIssueTypeField()
    {
        return (IssueTypeField) getField(IssueFieldConstants.ISSUE_TYPE);
    }

    @Override
    public IssueTypeSystemField getIssueTypeSystemField()
    {
        return (IssueTypeSystemField) getField(IssueFieldConstants.ISSUE_TYPE);
    }

    @Override
    public ProjectField getProjectField()
    {
        return (ProjectField) getField(IssueFieldConstants.PROJECT);
    }

    @Override
    public ProjectSystemField getProjectSystemField()
    {
        return (ProjectSystemField) getField(IssueFieldConstants.PROJECT);
    }

    public Set<NavigableField> getAvailableNavigableFieldsWithScope(final User user) throws FieldException
    {
        return getAvailableNavigableFieldsWithScope(user, Collections.<Long>emptyList(), Collections.<String>emptyList());
    }

    public final Set<NavigableField> getAvailableNavigableFieldsWithScope(final com.opensymphony.user.User user)
            throws FieldException
    {
        return getAvailableNavigableFieldsWithScope(user, Collections.<Long>emptyList(), Collections.<String>emptyList());
    }

    public Set<NavigableField> getAvailableNavigableFieldsWithScope(User remoteUser, QueryContext queryContext)
            throws FieldException
    {
        Set<NavigableField> allFields = new LinkedHashSet<NavigableField>();
        for (QueryContext.ProjectIssueTypeContexts context : queryContext.getProjectIssueTypeContexts())
        {
            Set<NavigableField> availableFields = getAvailableNavigableFieldsWithScope(remoteUser, context.getProjectIdInList(), context.getIssueTypeIds());

            // Union all the visible fields for each project/issue type context
            allFields.addAll(availableFields);
        }
        return allFields;
    }

    public final Set<NavigableField> getAvailableNavigableFieldsWithScope(com.opensymphony.user.User remoteUser, QueryContext queryContext)
            throws FieldException
    {
        return getAvailableNavigableFieldsWithScope((User) remoteUser, queryContext);
    }

    /**
     * Returns a set of {@link Field}s that are NOT hidden in AT LEAST ONE project that the remote user can see (has
     * {@link com.atlassian.jira.security.Permissions#BROWSE} permission for).
     * <p/>
     * NOTE: This method is used when actually showing the results (e.g. in Issue Navigator) to determine if the field
     * (column) should be actually shown.
     *
     * @param remoteUser the remote user.
     * @param projectIds a List of Longs.
     * @param issueTypes Issue types
     * @return a set of {@link Field}s that are NOT hidden in AT LEAST ONE project that the remote user can see.
     * @throws FieldException if cannot retrieve the projects the user can see, or if cannot retrieve the field layouts
     * for the viewable projects
     */
    private Set<NavigableField> getAvailableNavigableFieldsWithScope(User remoteUser, List<Long> projectIds, List<String> issueTypes)
            throws FieldException
    {
        Set<NavigableField> availableFields = new LinkedHashSet<NavigableField>();
        try
        {
            // Get the projects Field Layout
            Set<FieldLayout> schemes = getUniqueSchemes(projectIds, issueTypes, remoteUser);

            Set<NavigableField> allAvailableFields = new LinkedHashSet<NavigableField>();

            allAvailableFields.addAll(navigableFields);

            // Exclude fields that should not be available (e.g. timetracking is turned off)
            allAvailableFields.removeAll(getUnavailableFields());

            // Add all standard (non-custom) available fields
            availableFields.addAll(getAvailableFields(allAvailableFields, schemes));

            // Add all the custom fields that are in scope
            availableFields.addAll(getAvailableCustomFieldsWithScope(remoteUser, projectIds, issueTypes));

            return availableFields;
        }
        catch (DataAccessException e)
        {
            String message = "Error retrieving field layout.";
            log.error(message, e);
            throw new FieldException(message, e);
        }
    }

    /**
     * Checks that the fields in the fieldsToCheck collection are actually available in AT LEAST ONE FieldLayout present
     * in the given schemes set.
     *
     * @param fieldsToCheck Superset of all Fields. We will return a subset of these.
     * @param schemes Schemes used to find Visible fields.
     * @return the fields that are available in AT LEAST ONE FieldLayout
     */
    private <F extends Field> Set<F> getAvailableFields(Collection<F> fieldsToCheck, Set<FieldLayout> schemes)
    {
        Set<F> availableFields = new LinkedHashSet<F>();

        // Go through the list of available fields and see of the field is NOT hidden in at least one scheme
        for (final F field : fieldsToCheck)
        {
            if (!isFieldHidden(schemes, field))
            {
                availableFields.add(field);
            }
        }

        return availableFields;
    }

    public Set<CustomField> getAvailableCustomFields(com.opensymphony.user.User remoteUser, GenericValue issue)
            throws FieldException
    {
        GenericValue project = ManagerFactory.getProjectManager().getProject(issue);
        return getAvailableCustomFieldsWithScope(remoteUser, (project == null) ? Collections.<Long>emptyList() : CollectionBuilder.list(project.getLong("id")), CollectionBuilder.list(issue.getString("type")));
    }

    public Set<CustomField> getAvailableCustomFields(User remoteUser, Issue issue) throws FieldException
    {
        Project project = issue.getProjectObject();
        // TODO: Do we really want to do this null check? It was just copied from the old GenericValue version of this method.
        List<Long> projectList = (project == null) ? Collections.<Long>emptyList() : CollectionBuilder.list(project.getId());
        return getAvailableCustomFieldsWithScope(remoteUser, projectList, CollectionBuilder.list(issue.getIssueTypeObject().getId()));
    }

    public final Set<CustomField> getAvailableCustomFields(com.opensymphony.user.User remoteUser, Issue issue)
            throws FieldException
    {
        return getAvailableCustomFields((User) remoteUser, issue);
    }

    /**
     * Returns a set of {@link CustomField}s that are in scope.
     *
     * @param remoteUser Remote User
     * @param projectIds List of Project IDs
     * @param issueTypes List of Issue Types
     * @return a set of {@link CustomField}s that are in scope.
     * @throws FieldException if cannot retrieve the projects the user can see, or if cannot retrieve the field layouts
     * for the viewable projects
     */
    private Set<CustomField> getAvailableCustomFieldsWithScope(User remoteUser, List<Long> projectIds, List<String> issueTypes)
            throws FieldException
    {
        try
        {
            // Retrieve all the unique FieldLayout schemes
            Set<FieldLayout> schemes = getUniqueSchemes(projectIds, issueTypes, remoteUser);

            Collection<CustomField> existingCustomFields = new HashSet<CustomField>();
            // Only get custom fields and exclude fields that should not be available (e.g. timetracking it is turned off)
            for (final Long projectId : projectIds)
            {
                List<CustomField> existingCustomFieldsForProject = getCustomFieldManager().getCustomFieldObjects(projectId, issueTypes);
                if (existingCustomFieldsForProject != null)
                {
                    existingCustomFields.addAll(existingCustomFieldsForProject);
                }
            }

            if (projectIds.isEmpty())
            {
                existingCustomFields = getCustomFieldManager().getCustomFieldObjects(null, issueTypes);
            }

            // Go through the list of existing custom fields and see if the field is NOT hidden in at least one scheme
            return getAvailableFields(existingCustomFields, schemes);
        }
        catch (DataAccessException e)
        {
            String message = "Error retrieving field layout for " + (projectIds != null && !projectIds.isEmpty() ? "projects '" + projectIds + "'." : "null project.");
            log.error(message, e);
            throw new FieldException(message, e);
        }
    }

    /**
     * Retrieves all the unique FieldLayouts that the user should be able to see for project/issuetype pairs.
     *
     * @param projectIds a List of Longs.
     * @param issueTypes Issue types
     * @param remoteUser the remote user.
     * @return all the unique FieldLayouts that the user should be able to see for project/issuetype pairs.
     */
    private Set<FieldLayout> getUniqueSchemes(List<Long> projectIds, List<String> issueTypes, User remoteUser)
    {
        if (projectIds.isEmpty() && issueTypes.isEmpty())
        {
            // JRA-19426 - we need to be more efficient about how we get all unique field layouts for a project
            // If no project and no specific issue types have been specified retrieve all schemes
            final Collection<Project> projects = getBrowsableProjectObjects(remoteUser);
            final Set<FieldLayout> fieldLayoutSet = new HashSet<FieldLayout>();
            for (Project project : projects)
            {
                fieldLayoutSet.addAll(getFieldLayoutManager().getUniqueFieldLayouts(project));
            }
            return fieldLayoutSet;
        }
        else if (projectIds.isEmpty() && !issueTypes.isEmpty())
        {
            // If the project has NOT been specified, but issue types have been, we need to retrieve
            // unique schemes for all projects that the user can see, but only for the specified issue types
            return findVisibleFieldLayouts(getBrowsableProjectObjects(remoteUser), issueTypes);
        }
        else if (!projectIds.isEmpty() && issueTypes.isEmpty())
        {
            // If we have a project specified but no issue types, we need to retrieve all unique schemes
            // for all issue types but only the ones for that project
            return getVisibleFieldLayouts(ManagerFactory.getProjectManager().convertToProjects(projectIds), getAllIssueTypes());
        }
        else
        {
            // We have a project as well as issue types. Retrieve unique schemes only for those project/issuetype pairs
            return getVisibleFieldLayouts(ManagerFactory.getProjectManager().convertToProjects(projectIds), issueTypes);
        }
    }

    public boolean isFieldHidden(User remoteUser, String fieldId)
    {
        return isFieldHidden(remoteUser, getField(fieldId));
    }

    public final boolean isFieldHidden(com.opensymphony.user.User remoteUser, String fieldId)
    {
        return isFieldHidden(remoteUser, getField(fieldId));
    }

    public boolean isFieldHidden(User remoteUser, Field field)
    {
        Set<FieldLayout> uniqueSchemes = getUniqueSchemes(Collections.EMPTY_LIST, Collections.EMPTY_LIST, remoteUser);
        return isFieldHidden(uniqueSchemes, field);
    }

    public final boolean isFieldHidden(com.opensymphony.user.User remoteUser, Field field)
    {
        Set<FieldLayout> uniqueSchemes = getUniqueSchemes(Collections.EMPTY_LIST, Collections.EMPTY_LIST, remoteUser);
        return isFieldHidden(uniqueSchemes, field);
    }

    private Set<FieldLayout> getAllFieldLayouts()
    {
        // Retrieve the list of all projects
        return getVisibleFieldLayouts(ManagerFactory.getProjectManager().getProjects(), getAllIssueTypes());
    }

    /**
     * Retrive a list of all issue types in the system.
     *
     * @return all issue types in the system.
     */
    private List<String> getAllIssueTypes()
    {
        ConstantsManager constantsManager = ComponentAccessor.getConstantsManager();
        return constantsManager.expandIssueTypeIds(EasyList.build(ConstantsManager.ALL_ISSUE_TYPES));
    }

    private Collection<Project> getBrowsableProjectObjects(User remoteUser)
    {
        return ManagerFactory.getPermissionManager().getProjectObjects(Permissions.BROWSE, remoteUser);
    }

    /**
     * Retrives unique FieldLayouts for the given projects and issuetype pairs.
     */
    private Set<FieldLayout> findVisibleFieldLayouts(Collection<Project> projects, List<String> issueTypes)
    {
        // Get the field layout schemes of each project/issuetype pair and work out all the unique schemes
        // remember that more than one project can have the same field layout (scheme)
        Set<FieldLayout> uniqueSchemes = new HashSet<FieldLayout>();
        for (final Project project : projects)
        {

            for (final String issueTypeId : issueTypes)
            {
                FieldLayout fieldLayout = getFieldLayoutManager().getFieldLayout(project, issueTypeId);
                uniqueSchemes.add(fieldLayout);
            }
        }
        return uniqueSchemes;
    }

    /**
     * Retrives unique FieldLayouts for the given projects and issuetype pairs.
     */
    private Set<FieldLayout> getVisibleFieldLayouts(Collection<GenericValue> projects, List<String> issueTypes)
    {
        // Get the field layout schemes of each project/issuetype pair and work out all the unique schemes
        // remember that more than one project can have the same field layout (scheme)
        Set<FieldLayout> uniqueSchemes = new HashSet<FieldLayout>();
        for (final GenericValue project : projects)
        {

            for (final String issueTypeId : issueTypes)
            {
                FieldLayout fieldLayout = getFieldLayoutManager().getFieldLayout(project, issueTypeId);
                uniqueSchemes.add(fieldLayout);
            }
        }
        return uniqueSchemes;
    }

    /**
     * Determines whether the field is NOT hidden in AT LEAST one {@link FieldLayout} that is in fieldLayouts
     */
    private boolean isFieldHidden(Set<FieldLayout> fieldLayouts, Field field)
    {
        // All hideable fields must be orderable
        if (!isOrderableField(field))
        {
            // If the field cannot be hidden then it's not hidden :)
            return false;
        }

        final OrderableField orderableField = getOrderableField(field.getId());
        for (final FieldLayout fieldLayout : fieldLayouts)
        {
            FieldLayoutItem fieldLayoutItem = fieldLayout.getFieldLayoutItem(orderableField);
            if (!fieldLayoutItem.isHidden())
            {
                return fieldLayoutItem.isHidden();
            }
        }

        // The field is hidden in all field layouts
        return true;
    }

    public boolean isCustomField(String key)
    {
        CustomField customField = getCustomFieldManager().getCustomFieldObject(key);
        return (customField != null);
    }

    public boolean isCustomField(Field field)
    {
        return (field instanceof CustomField);
    }

    public CustomField getCustomField(String key)
    {
        final CustomField customFieldObject = getCustomFieldManager().getCustomFieldObject(key);
        if (customFieldObject == null)
        {
            throw new IllegalArgumentException("Custom field with id '" + key + "' does not exist.");
        }
        else
        {
            return customFieldObject;
        }
    }

    public boolean isHideableField(String id)
    {
        if (isCustomField(id))
        {
            return true;
        }
        else
        {
            Field field = fields.get(id);
            return isHideableField(field);
        }
    }

    public boolean isHideableField(Field field)
    {
        if (isCustomField(field))
        {
            return true;
        }
        else
        {
            return (field instanceof HideableField);
        }
    }

    public HideableField getHideableField(String id)
    {
        if (isCustomField(id))
        {
            return getCustomField(id);
        }
        else
        {
            if (isHideableField(id))
            {
                return (HideableField) fields.get(id);
            }
            else
            {
                throw new IllegalArgumentException("The field with id '" + id + "' is not a HideableField.");
            }
        }
    }

    public boolean isOrderableField(String id)
    {
        if (isCustomField(id))
        {
            return true;
        }
        else
        {
            Field field = fields.get(id);
            return isOrderableField(field);
        }
    }

    public boolean isOrderableField(Field field)
    {
        if (isCustomField(field))
        {
            return true;
        }
        else
        {
            return (field instanceof OrderableField) && orderableFields.contains(field);
        }
    }

    public OrderableField getOrderableField(String id)
    {
        if (isCustomField(id))
        {
            return getCustomField(id);
        }
        else
        {
            if (isOrderableField(id))
            {
                return (OrderableField) fields.get(id);
            }
            else
            {
                return null;
            }
        }
    }

    public ConfigurableField getConfigurableField(String id)
    {
        OrderableField field = getOrderableField(id);
        if (field != null && field instanceof ConfigurableField)
        {
            return (ConfigurableField) field;
        }
        else
        {
            if (log.isInfoEnabled() && field != null)
            {
                log.info("Field found for " + id + " but was not a ConfigurableField. Type is " + field.getClass().getName() + " : " + field);
            }

            return null;
        }
    }

    public Set<OrderableField> getOrderableFields()
    {
        return Collections.unmodifiableSet(getAvailableOrderableFields());
    }

    public boolean isNavigableField(String id)
    {
        if (isCustomField(id))
        {
            return true;
        }
        else
        {
            Field field = fields.get(id);
            return isNavigableField(field);
        }
    }

    public boolean isNavigableField(Field field)
    {
        if (isCustomField(field))
        {
            return true;
        }
        else
        {
            return (field instanceof NavigableField) && navigableFields.contains(field);
        }
    }

    public NavigableField getNavigableField(String id)
    {
        if (isCustomField(id))
        {
            return getCustomField(id);
        }
        else
        {
            if (isNavigableField(id))
            {
                return (NavigableField) fields.get(id);
            }
            else
            {
                throw new IllegalArgumentException("The field with id '" + id + "' is not a NavigableField.");
            }
        }
    }

    public boolean isRequirableField(String id)
    {
        if (isCustomField(id))
        {
            return true;
        }
        else
        {
            return isRequirableField(fields.get(id));
        }
    }

    public boolean isRequirableField(Field field)
    {
        if (isCustomField(field))
        {
            return true;
        }
        else
        {
            return (field instanceof RequirableField);
        }
    }

    public boolean isMandatoryField(String id)
    {
        if (isCustomField(id))
        {
            return false;
        }
        else
        {
            Field field = fields.get(id);
            return isMandatoryField(field);
        }
    }

    public boolean isMandatoryField(Field field)
    {
        if (isCustomField(field))
        {
            return false;
        }
        else
        {
            return (field instanceof MandatoryField);
        }
    }

    public boolean isRenderableField(String id)
    {
        if (isCustomField(id))
        {
            CustomField field = getCustomField(id);
            return field.isRenderable();
        }
        else
        {
            Field field = fields.get(id);
            return isRenderableField(field);
        }
    }

    public boolean isRenderableField(Field field)
    {
        if (isCustomField(field))
        {
            return ((CustomField) field).isRenderable();
        }
        else
        {
            return (field instanceof RenderableField);
        }
    }

    public boolean isUnscreenableField(String id)
    {
        if (isCustomField(id))
        {
            return false;
        }
        else
        {
            Field field = fields.get(id);
            return isUnscreenableField(field);
        }
    }

    public boolean isUnscreenableField(Field field)
    {
        if (isCustomField(field))
        {
            return false;
        }
        else
        {
            return (field instanceof UnscreenableField);
        }
    }

    public RequirableField getRequiredField(String id)
    {
        if (isCustomField(id))
        {
            return getCustomField(id);
        }
        else
        {
            if (isRequirableField(id))
            {
                return (RequirableField) fields.get(id);
            }
            else
            {
                throw new IllegalArgumentException("The field with id '" + id + "' is not a RequirableField.");
            }
        }
    }

    public CustomFieldManager getCustomFieldManager()
    {
        return ManagerFactory.getCustomFieldManager();
    }

    /**
     * @deprecated Declare your dependency and let PicoContainer resolve it instead
     */
    @Deprecated
    public FieldLayoutManager getFieldLayoutManager()
    {
        //this is really a circular design.
        //all clients of this method should declare their dep. on fieldlayoutmanager
        //and let pico resolve them
        if (fieldLayoutManager == null)
        {
            fieldLayoutManager = ComponentAccessor.getFieldLayoutManager();
        }
        return fieldLayoutManager;
    }

    /**
     * @deprecated Declare your dependency and let PicoContainer resolve it instead
     */
    @Deprecated
    public ColumnLayoutManager getColumnLayoutManager()
    {
        return ComponentAccessor.getColumnLayoutManager();
    }

    public void refresh()
    {
        // refresh customfield manager
        getCustomFieldManager().refresh();

        // Refresh the FieldLayoutManager (due to its caches of field layouts)
        getFieldLayoutManager().refresh();

        // Refresh the ColumnLayoutManager (due to its caches of column layouts)
        getColumnLayoutManager().refresh();
    }

    public boolean isTimeTrackingOn()
    {
        return ManagerFactory.getApplicationProperties().getOption(APKeys.JIRA_OPTION_TIMETRACKING);
    }

    protected boolean isVotingOn()
    {
        return ManagerFactory.getApplicationProperties().getOption(APKeys.JIRA_OPTION_VOTING);
    }

    protected boolean isWatchingOn()
    {
        return ManagerFactory.getApplicationProperties().getOption(APKeys.JIRA_OPTION_WATCHING);
    }

    protected boolean isSubTasksOn()
    {
        return ComponentManager.getInstance().getSubTaskManager().isSubTasksEnabled();
    }

    /**
     * Returns all navigable fields (including Custom Fields) minus the unavailable fields.
     *
     * @return All navigable fields (including Custom Fields) minus the unavailable fields.
     */
    @SuppressWarnings ("unchecked")
    private Set<NavigableField> getAvailableNavigableFields()
    {
        // Use getAvailableFields(), then cast it appropriately
        return (Set<NavigableField>) getAvailableFields(navigableFields);
    }

    /**
     * Returns all Orderable fields (including Custom Fields) minus the unavailable fields.
     *
     * @return All Orderable fields (including Custom Fields) minus the unavailable fields.
     */
    @SuppressWarnings ("unchecked")
    private Set<OrderableField> getAvailableOrderableFields()
    {
        // Use getAvailableFields(), then cast it appropriately
        return (Set<OrderableField>) getAvailableFields(orderableFields);
    }

    /**
     * Returns the list of available fields with all custom fields irrespective of scope. Takes the given list of
     * fields, adds custom fields, then removes Unavailable fields.
     *
     * @param allFields all Fields
     * @return the list of available fields with all custom fields irrespective of scope.
     */
    @SuppressWarnings ("unchecked")
    private Set getAvailableFields(List<? extends Field> allFields)
    {
        // this method is not genericised as ti allows us to build either a list of NavigableField or OrderableField
        // see the calling methods getAvailableNavigableFields(), and getAvailableOrderableFields()
        Set availableFields = new HashSet(allFields);

        // Add custom fields that are relevant to the project and issue types
        availableFields.addAll(getCustomFieldManager().getCustomFieldObjects());

        availableFields.removeAll(getUnavailableFields());

        return availableFields;
    }

    public Set<Field> getUnavailableFields()
    {
        Set<Field> unavailableFields = new HashSet<Field>();
        if (!isTimeTrackingOn())
        {
            unavailableFields.add(fields.get(IssueFieldConstants.TIMETRACKING));
            unavailableFields.add(fields.get(IssueFieldConstants.TIME_ORIGINAL_ESTIMATE));
            unavailableFields.add(fields.get(IssueFieldConstants.TIME_ESTIMATE));
            unavailableFields.add(fields.get(IssueFieldConstants.TIME_SPENT));
            unavailableFields.add(fields.get(IssueFieldConstants.PROGRESS));
            unavailableFields.add(fields.get(IssueFieldConstants.WORKLOG));
        }
        if (!isSubTasksOn() || !isTimeTrackingOn())
        {
            unavailableFields.add(fields.get(IssueFieldConstants.AGGREGATE_TIME_SPENT));
            unavailableFields.add(fields.get(IssueFieldConstants.AGGREGATE_TIME_ORIGINAL_ESTIMATE));
            unavailableFields.add(fields.get(IssueFieldConstants.AGGREGATE_TIME_ESTIMATE));
            unavailableFields.add(fields.get(IssueFieldConstants.AGGREGATE_PROGRESS));
        }
        if (!isVotingOn())
        {
            unavailableFields.add(fields.get(IssueFieldConstants.VOTES));
        }
        if (!isWatchingOn())
        {
            unavailableFields.add(fields.get(IssueFieldConstants.WATCHES));
        }
        if (!isSubTasksOn())
        {
            unavailableFields.add(fields.get(IssueFieldConstants.SUBTASKS));
        }

        return unavailableFields;
    }

    /**
     * Add all the system searchable to the passed collection.
     *
     * @param searchableFields the collection to add the fields to.
     */
    private void addAllSystemSearchableFields(final Set<SearchableField> searchableFields)
    {
        searchableFields.add(projectSystemField);
        CollectionUtils.select(orderableFields, InstanceofPredicate.getInstance(SearchableField.class), searchableFields);
        CollectionUtils.select(navigableFields, InstanceofPredicate.getInstance(SearchableField.class), searchableFields);
    }
}
