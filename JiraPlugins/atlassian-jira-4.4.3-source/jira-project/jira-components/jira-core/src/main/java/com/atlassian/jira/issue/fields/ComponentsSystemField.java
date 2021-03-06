package com.atlassian.jira.issue.fields;

import com.atlassian.jira.bc.EntityNotFoundException;
import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.bc.project.component.ProjectComponentManager;
import com.atlassian.jira.bulkedit.operation.BulkMoveOperation;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.IssueRelationConstants;
import com.atlassian.jira.issue.ModifiedValue;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.customfields.OperationContext;
import com.atlassian.jira.issue.customfields.impl.FieldValidationException;
import com.atlassian.jira.issue.fields.layout.field.FieldLayout;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.renderer.HackyRendererType;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderLayoutItem;
import com.atlassian.jira.issue.fields.util.MessagedResult;
import com.atlassian.jira.issue.search.LuceneFieldSorter;
import com.atlassian.jira.issue.search.handlers.ComponentSearchHandlerFactory;
import com.atlassian.jira.issue.statistics.ComponentStatisticsMapper;
import com.atlassian.jira.issue.util.IssueChangeHolder;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.Function;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.JiraEntityUtils;
import com.atlassian.jira.web.bean.BulkEditBean;
import com.atlassian.jira.web.bean.BulkMoveHelper;
import com.atlassian.jira.web.bean.DefaultBulkMoveHelper;
import com.atlassian.velocity.VelocityManager;
import com.opensymphony.util.TextUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;
import webwork.action.Action;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.atlassian.jira.util.dbc.Assertions.notEmpty;
import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * A field implementation to render {@link com.atlassian.jira.bc.project.component.ProjectComponent} values.
 */
public class ComponentsSystemField extends AbstractOrderableNavigableFieldImpl implements HideableField, RequirableField
{
    // TODO need to migrate to use ProjectComponent instead of GenericValue

    private static final Logger log = Logger.getLogger(ComponentsSystemField.class);
    private static final String COMPONENTS_NAME_KEY = "issue.field.components";
    private static final Long UNKNOWN_COMPONENTS_ID = -1L;

    private final ProjectComponentManager projectComponentManager;
    private final ComponentStatisticsMapper componentStatisticsMapper;
    private final ProjectManager projectManager;

    public ComponentsSystemField(VelocityManager velocityManager, ProjectComponentManager projectComponentManager, ApplicationProperties applicationProperties,
            PermissionManager permissionManager, JiraAuthenticationContext authenticationContext, ComponentStatisticsMapper componentStatisticsMapper,
            ComponentSearchHandlerFactory componentSearchHandlerFactory, final ProjectManager projectManager)
    {
        super(IssueFieldConstants.COMPONENTS, COMPONENTS_NAME_KEY, velocityManager, applicationProperties, authenticationContext, permissionManager, componentSearchHandlerFactory);
        this.projectComponentManager = projectComponentManager;
        this.componentStatisticsMapper = componentStatisticsMapper;
        this.projectManager = projectManager;
    }

    public String getCreateHtml(FieldLayoutItem fieldLayoutItem, OperationContext operationContext, Action action, Issue issue, Map dispayParameters)
    {
        return getEditHtml(fieldLayoutItem, operationContext, action, issue, dispayParameters);
    }

    public String getEditHtml(FieldLayoutItem fieldLayoutItem, OperationContext operationContext, Action action, Issue issue, Map displayParameters)
    {
        Map<String, Object> velocityParams = getVelocityParams(fieldLayoutItem, action, issue, displayParameters);
        // Currently selected Components for this issue.
        velocityParams.put("currentComponents", operationContext.getFieldValuesHolder().get(getId()));
        // List of possible Components for the project
        velocityParams.put("components", projectComponentManager.convertToGenericValues(getComponents(issue.getProjectObject())));
        velocityParams.put("unknownComponentId", UNKNOWN_COMPONENTS_ID);
        if (fieldLayoutItem != null)
        {
            velocityParams.put("isFrotherControl", HackyRendererType.fromKey(fieldLayoutItem.getRendererType()) == HackyRendererType.FROTHER_CONTROL);
        }
        return renderTemplate("components-edit.vm", velocityParams);
    }

    /**
     * Returns HTML that should be shown when components are being bulk edited.
     *
     * The HTML displayed for Bulk Move of Components needs to allow the user to specify mappings for each old component
     * present in the currently selected issues.
     */
    public String getBulkEditHtml(OperationContext operationContext, Action action, BulkEditBean bulkEditBean, Map displayParameters)
    {
        notNull("bulkEditBean", bulkEditBean);
        notEmpty("selectedIssues", bulkEditBean.getSelectedIssues());

        if (BulkMoveOperation.NAME.equals(bulkEditBean.getOperationName()))
        {
            FieldLayoutItem fieldLayoutItem = bulkEditBean.getTargetFieldLayout().getFieldLayoutItem(this);
            final BulkMoveHelper bulkMoveHelper = new DefaultBulkMoveHelper();
            final Function<Object, String> componentNameResolver = new Function<Object, String>()
            {
                public String get(final Object input)
                {
                    try
                    {
                        return projectComponentManager.find((Long) input).getName();
                    }
                    catch (EntityNotFoundException e)
                    {
                        throw new RuntimeException(e);
                    }
                }
            };
            final Function<Issue, Collection<Object>> issueValueResolver = new Function<Issue, Collection<Object>>()
            {
                public Collection<Object> get(final Issue input)
                {
                    final Map fieldValuesHolder = new LinkedHashMap();
                    populateFromIssue(fieldValuesHolder, input);
                    return (Collection<Object>) fieldValuesHolder.get(getId());
                }
            };
            final Map<Long, BulkMoveHelper.DistinctValueResult> distinctComponentValues = bulkMoveHelper.getDistinctValuesForMove(bulkEditBean, this, issueValueResolver, componentNameResolver);

            final Issue issue = bulkEditBean.getFirstTargetIssueObject();
            final Map velocityParams = getVelocityParams(fieldLayoutItem, action, issue, displayParameters);

            // the distinct values which need to be mapped
            velocityParams.put("valuesToMap", distinctComponentValues);
            velocityParams.put("bulkMoveHelper", bulkMoveHelper);

            // List of possible Components for the project
            velocityParams.put("components", projectComponentManager.convertToGenericValues(getComponents(issue.getProjectObject())));
            velocityParams.put("unknownComponentId", UNKNOWN_COMPONENTS_ID);
            if (fieldLayoutItem != null)
            {
                velocityParams.put("isFrotherControl", HackyRendererType.fromKey(fieldLayoutItem.getRendererType()) == HackyRendererType.FROTHER_CONTROL);
            }
            return renderTemplate("components-bulkmove.vm", velocityParams);
        }
        else
        {
            return super.getBulkEditHtml(operationContext, action, bulkEditBean, displayParameters);
        }
    }

    public String getViewHtml(FieldLayoutItem fieldLayoutItem, Action action, Issue issue, Map displayParameters)
    {
        Map velocityParams = getVelocityParams(fieldLayoutItem, action, issue, displayParameters);
        velocityParams.put("components", issue.getComponents());
        velocityParams.put("projectManager", projectManager);
        return getViewHtml(velocityParams);
    }

    public String getViewHtml(FieldLayoutItem fieldLayoutItem, Action action, Issue issue, Object value, Map displayParameters)
    {
        Map velocityParams = getVelocityParams(fieldLayoutItem, action, null, displayParameters);
        velocityParams.put("components", value);
        velocityParams.put("projectManager", projectManager);
        return getViewHtml(velocityParams);
    }

    private String getViewHtml(Map velocityParams)
    {
        return renderTemplate("components-view.vm", velocityParams);
    }

    private Collection<ProjectComponent> getComponents(Project project)
    {
        return getComponents(project.getId());
    }

    private Collection<ProjectComponent> getComponents(Long id)
    {
        return projectComponentManager.findAllForProject(id);
    }


    public void validateParams(OperationContext operationContext, ErrorCollection errorCollectionToAddTo, I18nHelper i18n, Issue issue, FieldScreenRenderLayoutItem fieldScreenRenderLayoutItem)
    {
        Map fieldValuesHolder = operationContext.getFieldValuesHolder();
        final Collection componentIds = (Collection) fieldValuesHolder.get(getId());
        final Project project = issue.getProjectObject();
        if (componentIds != null && componentIds.size() > 1)
        {
            for (Object componentId : componentIds)
            {
                Long l = (Long) componentId;

                if (UNKNOWN_COMPONENTS_ID.equals(l))
                {
                    errorCollectionToAddTo.addError(getId(), i18n.getText("issue.field.components.noneselectedwithother"));
                    return;
                }
            }
        }

        if (validateForRequiredField(errorCollectionToAddTo, i18n, fieldScreenRenderLayoutItem, componentIds, project))
        {
            // only do this validation if they are valid Ids            
            validateComponentForProject(errorCollectionToAddTo, i18n, componentIds, project);
        }

    }

    private boolean validateForRequiredField(ErrorCollection errorCollectionToAddTo, I18nHelper i18n, FieldScreenRenderLayoutItem fieldScreenRenderLayoutItem, Collection componentIds, Project project)
    {
        final Collection<ProjectComponent> components = getComponents(project);
        // The check for 'Unknown' id is needed for bulk-edit
        if (fieldScreenRenderLayoutItem.isRequired() && (componentIds == null || componentIds.isEmpty() || componentIds.contains(UNKNOWN_COMPONENTS_ID)))
        {
            // Check if we have components configured in the Project:
            if (components.isEmpty())
            {
                errorCollectionToAddTo.addErrorMessage(i18n.getText("createissue.error.components.required", i18n.getText(getNameKey()), project.getName()));
            }
            else
            {
                errorCollectionToAddTo.addError(getId(), i18n.getText("issue.field.required", i18n.getText(getNameKey())));
            }
            return false;
        }
        return true;
    }

    private void validateComponentForProject(ErrorCollection errorCollectionToAddTo, I18nHelper i18n, Collection componentIds, Project project)
    {
        if (componentIds != null)
        {
            StringBuilder sb = null;
            for (Object componentIdObj : componentIds)
            {
                final Long componentId = getComponentIdAsLong(componentIdObj);
                if (componentId == -1)
                {
                    // Unkown should have already been validated
                    return;
                }
                try
                {
                    final ProjectComponent component = projectComponentManager.find(componentId);
                    if (!component.getProjectId().equals(project.getId()))
                    {
                        if (sb == null)
                        {
                            sb = new StringBuilder(component.getName()).append("(").append(component.getId()).append(")");
                        }
                        else
                        {
                            sb.append(", ").append(component.getName()).append("(").append(component.getId()).append(")");
                        }

                    }
                }
                catch (EntityNotFoundException e)
                {
                    errorCollectionToAddTo.addError(getId(), i18n.getText("issue.field.components.components.does.not.exist", componentId));
                    return;
                }
            }
            if (sb != null)
            {
                errorCollectionToAddTo.addError(getId(), i18n.getText("issue.field.components.components.not.valid.for.project", sb.toString(), project.getName()));
            }
        }
    }

    private Long getComponentIdAsLong(Object o)
    {
        Long l;
        if (o instanceof String)
        {
            l = new Long((String) o);
        }
        else
        {
            l = (Long) o;
        }
        return l;
    }

    protected Object getRelevantParams(Map params)
    {
        String[] value = (String[]) params.get(getId());
        if (value != null && value.length > 0)
        {
            List<Long> componentIds = new LinkedList<Long>();
            for (String aValue : value)
            {
                componentIds.add(new Long(aValue));
            }
            return componentIds;
        }
        else
        {
            return Collections.EMPTY_LIST;
        }
    }

    public Object getValueFromParams(Map params)
    {
        List<ProjectComponent> components = getComponentsFromParams(params);
        // existing behaviour for no value:
        if (components == null)
        {
            return Collections.emptyList();
        }
        // Convert component objects to GenericValues for backward compatibility:
        return projectComponentManager.convertToGenericValues(components);
    }

    /**
     * Returns the list of components contained in the given parameters Map, or null if not contained.
     * <p>
     * This is used by the DefaultAssigneeResolver to find components about to be set to an issue.
     *
     * @param params the map of parameters.
     * @return the list of components contained in the given parameters Map, or null if not contained.
     */
    public List<ProjectComponent> getComponentsFromParams(Map params)
    {
        List<Long> componentIds = (List<Long>) params.get(getId());
        if (componentIds == null)
        {
            // by contract this returns null because DefaultAssigneeResolver needs to know if a value is in the map or not.
            return null;
        }
        if (componentIds.isEmpty() || componentIds.contains(UNKNOWN_COMPONENTS_ID))
        {
            return Collections.emptyList();
        }
        else
        {
            try
            {
                return projectComponentManager.getComponents(componentIds);
            }
            catch (EntityNotFoundException e)
            {
                throw new FieldValidationException("Trying to retrieve non existant component");
            }
        }
    }

    public void populateParamsFromString(Map fieldValuesHolder, String stringValue, Issue issue) throws FieldValidationException
    {
        fieldValuesHolder.put(getId(), getComponentIds(issue, stringValue));
    }

    private List<Long> getComponentIds(Issue issue, String stringValue) throws FieldValidationException
    {
        // Use a set to ensure that there are no duplicate component ids.
        final Set<Long> components = new HashSet<Long>();
        final Project project = issue.getProjectObject();

        // Check if the components were provided
        if (TextUtils.stringSet(stringValue))
        {
            // If so set the values
            String[] componentParams = StringUtils.split(stringValue, ",");
            for (String componentParam : componentParams)
            {
                try
                {
                    components.add(Long.valueOf(componentParam));
                }
                catch (NumberFormatException e)
                {
                    // Try getting the version by name
                    final ProjectComponent component = projectComponentManager.findByComponentName(project.getId(), componentParam);
                    if (component != null)
                    {
                        components.add(component.getId());
                    }
                    else
                    {
                        throw new FieldValidationException("Invalid component name '" + componentParam + "'.");
                    }
                }
            }
        }

        return new ArrayList<Long>(components);
    }

    /**
     */
    public void updateValue(FieldLayoutItem fieldLayoutItem, Issue issue, ModifiedValue modifiedValue, IssueChangeHolder issueChangeHolder)
    {
        Collection currentComponents = (Collection) modifiedValue.getOldValue();
        Collection newComponents = (Collection) modifiedValue.getNewValue();
        if (currentComponents == null || currentComponents.isEmpty())
        {
            if (newComponents != null)
            {
                issueChangeHolder.addChangeItems(updateIssueValue(issue, newComponents));
            }
        }
        else
        {
            if (!compareIdSets(newComponents, currentComponents))
            {
                issueChangeHolder.addChangeItems(updateIssueValue(issue, newComponents));
            }
        }
    }

    /**
     * Compare the two genericValue collections and make sure they both contain the same set of ids
     * @param newComponentGVs collection of old {@link org.ofbiz.core.entity.GenericValue GenericValues}
     * @param currentComponentGVs collection of new {@link org.ofbiz.core.entity.GenericValue GenericValues}
     * @return true if they have the same set of ids or if they are both null, false otherwise
     */
    protected boolean compareIdSets(Collection /*<GenericValue>*/ newComponentGVs, Collection /*<GenericValue>*/ currentComponentGVs)
    {
        if (newComponentGVs != null && currentComponentGVs != null)
        {
            Collection /*<Long>*/ newComponentIds = CollectionUtils.collect(newComponentGVs, JiraEntityUtils.GV_TO_ID_TRANSFORMER);
            Collection /*<Long>*/ currentComponentIds = CollectionUtils.collect(currentComponentGVs, JiraEntityUtils.GV_TO_ID_TRANSFORMER);
            return valuesEqual(new HashSet(newComponentIds), new HashSet(currentComponentIds));
        }
        return newComponentGVs == null && currentComponentGVs == null;
    }

    private List updateIssueValue(Issue issue, Object value)
    {
        try
        {
            return JiraEntityUtils.updateDependentEntitiesCheckId(issue.getGenericValue(), (Collection) value, IssueRelationConstants.COMPONENT, "Component");
        }
        catch (GenericEntityException e)
        {
            log.error("Error while saving components '" + value + "' for issue with id '" + issue.getLong("id") + "'.");
        }

        return Collections.EMPTY_LIST;
    }

    public void createValue(Issue issue, Object value)
    {
        updateIssueValue(issue, value);
    }

    public boolean isShown(Issue issue)
    {
        return true;
    }

    public void populateDefaults(Map fieldValuesHolder, Issue issue)
    {
        //JRA-13011: The default for this field needs to be the empty list. We need this to ensure so that invalid
        //components are not kept on the issue when it moves between projects. This is really only a problem when moving an issue's
        //sub-tasks during a move operation since there is no GUI to configure new components for sub-taks.
        fieldValuesHolder.put(getId(), Collections.emptyList());
    }

    public void populateFromIssue(Map fieldValuesHolder, Issue issue)
    {
        Collection components = issue.getComponents();
        if (components != null)
        {
            Collection componentIds = new LinkedList();
            for (Iterator iterator = components.iterator(); iterator.hasNext();)
            {
                GenericValue componentGV = (GenericValue) iterator.next();
                componentIds.add(componentGV.getLong("id"));
            }

            fieldValuesHolder.put(getId(), componentIds);
        }
        else
        {
            fieldValuesHolder.put(getId(), null);
        }
    }

    public Object getDefaultValue(Issue issue)
    {
        return Collections.EMPTY_LIST;
    }

    public void updateIssue(FieldLayoutItem fieldLayoutItem, MutableIssue issue, Map fieldValueHolder)
    {
        if (fieldValueHolder.containsKey(getId()))
        {
            issue.setComponents((Collection) getValueFromParams(fieldValueHolder));
        }
    }

    public MessagedResult needsMove(Collection originalIssues, Issue targetIssue, FieldLayoutItem targetFieldLayoutItem)
    {
        for (Issue originalIssue : (Collection<Issue>)originalIssues)
        {
            // If the projects are different then need to ask user to specify new component
            if (projectMoved(originalIssue, targetIssue))
            {
                if (originalIssue.getComponents().isEmpty())
                {
                    // If no components are set only need to ask the user if the target field layout has components as required
                    if (targetFieldLayoutItem.isRequired())
                    {
                        return new MessagedResult(true);
                    }
                }
                else
                {
                    return new MessagedResult(true);
                }
            }
            else
            {
                // Same project (different issue type) - need to see if the field is required in the target field layout
                if (originalIssue.getComponents().isEmpty() && targetFieldLayoutItem.isRequired())
                {
                    return new MessagedResult(true);
                }
            }
        }
        return new MessagedResult(false);
    }

    private boolean projectMoved(final Issue originalIssue, final Issue targetIssue)
    {
        // JRA-20184: Should only check the ID, the other fields can change.
        // Don't do any null checks - a null Project or ID is an unrecoverable error.
        final Long originalProjectId = originalIssue.getProjectObject().getId();
        final Long targetProjectId = targetIssue.getProjectObject().getId();
        return !originalProjectId.equals(targetProjectId);
    }

    public void populateForMove(Map fieldValuesHolder, Issue originalIssue, Issue targetIssue)
    {

        // Preselect components with the same name
        final Collection<String> currentComponentNames = getComponentNames(originalIssue.getComponents());
        final Collection<ProjectComponent> possibleComponents = getComponents(targetIssue.getProjectObject());
        final Collection<Long> componentIds = new LinkedList<Long>();

        for (ProjectComponent possibleComponent : possibleComponents)
        {
            if (currentComponentNames.contains(possibleComponent.getName()))
            {
                componentIds.add(possibleComponent.getId());
            }
        }

        fieldValuesHolder.put(getId(), componentIds);
    }

    public void removeValueFromIssueObject(MutableIssue issue)
    {
        issue.setComponents(Collections.EMPTY_LIST);
    }

    public boolean canRemoveValueFromIssueObject(Issue issue)
    {
        return true;
    }

    public boolean hasValue(Issue issue)
    {
        Collection components = issue.getComponents();
        return (components != null && !components.isEmpty());
    }

    private Collection getComponentNames(Collection components)
    {
        Collection componentNames = new HashSet();
        for (Iterator iterator = components.iterator(); iterator.hasNext();)
        {
            GenericValue componentGV = (GenericValue) iterator.next();
            componentNames.add(componentGV.getString("name"));
        }

        return componentNames;
    }

    /////////////////////////////////////////// Bulk Edit //////////////////////////////////////////////////////////
    public String availableForBulkEdit(BulkEditBean bulkEditBean)
    {
        // Can bulk-edit this field only if all selected issue belong to one project
        final GenericValue project = bulkEditBean.getProject();
        if (!bulkEditBean.isMultipleProjects() && project != null)
        {
            // Ensure that the project has components
            if (getComponents(project.getLong("id")).isEmpty())
            {
                return "bulk.edit.unavailable.nocomponents";
            }

            // Ensure that this field is not hidden in any Field Layouts the selected issues belong to
            for (FieldLayout fieldLayout : (Collection<FieldLayout>)bulkEditBean.getFieldLayouts())
            {
                if (fieldLayout.isFieldHidden(getId()))
                {
                    return "bulk.edit.unavailable.hidden";
                }
            }

            // If we got here then the field is visible in all field layouts
            // So check for permissions
            // Need to check for EDIT permission here rather than in the BulkEdit itself, as a user does not need the EDIT permission to edit the ASSIGNEE field,
            // just the ASSIGNEE permission, so the permissions to check depend on the field
            // hAv eto loop through all the issues incase the permission has been granted to current assignee/reporter (i.e. assigned ot a role)
            for (Issue issue : (List<Issue>)bulkEditBean.getSelectedIssues())
            {
                if (!hasBulkUpdatePermission(bulkEditBean, issue) || !isShown(issue))
                {
                    return "bulk.edit.unavailable.permission";
                }
            }

            // This field is available for bulk-editing, return null (i.e no unavailble message)
            return null;
        }
        else
        {
            // Let the user know that selected issues belong to more than one project so the action is not available
            return "bulk.edit.unavailable.multipleprojects";
        }
    }

    //////////////////////////////////////////// NavigableField implementation ////////////////////////////////////
    public String getColumnHeadingKey()
    {
        return "issue.column.heading.components";
    }

    public String getDefaultSortOrder()
    {
        return NavigableField.ORDER_ASCENDING;
    }

    public LuceneFieldSorter getSorter()
    {
        return componentStatisticsMapper;
    }

    public String getColumnViewHtml(FieldLayoutItem fieldLayoutItem, Map displayParams, Issue issue)
    {
        Map velocityParams = getVelocityParams(getAuthenticationContext().getI18nHelper(), displayParams, issue);
        velocityParams.put("components", issue.getComponents());
        return renderTemplate("components-columnview.vm", velocityParams);
    }

    // this is a method that allows the AssigneeSystemField to find out about modified components before they
    // have been committed to the issue object
    public Collection getComponents(Issue issue, Map fieldValuesHolder)
    {
        Collection valuesFromMap = (Collection) getValueFromParams(fieldValuesHolder);
        if(valuesFromMap == null || valuesFromMap.isEmpty())
        {
            return issue.getComponents();
        }
        return valuesFromMap;
    }
}
