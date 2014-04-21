package com.atlassian.jira.issue.fields;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.bulkedit.operation.BulkMigrateOperation;
import com.atlassian.jira.bulkedit.operation.BulkMoveOperation;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.ModifiedValue;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.context.IssueContext;
import com.atlassian.jira.issue.customfields.OperationContext;
import com.atlassian.jira.issue.customfields.impl.FieldValidationException;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.config.FieldConfigScheme;
import com.atlassian.jira.issue.fields.config.manager.FieldConfigSchemeManager;
import com.atlassian.jira.issue.fields.config.manager.IssueTypeSchemeManager;
import com.atlassian.jira.issue.fields.layout.field.FieldLayout;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.fields.option.FieldConfigSchemeOption;
import com.atlassian.jira.issue.fields.option.IssueConstantOption;
import com.atlassian.jira.issue.fields.option.Option;
import com.atlassian.jira.issue.fields.option.OptionSetManager;
import com.atlassian.jira.issue.fields.option.TextOption;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderLayoutItem;
import com.atlassian.jira.issue.fields.util.MessagedResult;
import com.atlassian.jira.issue.history.ChangeItemBean;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.search.LuceneFieldSorter;
import com.atlassian.jira.issue.search.handlers.IssueTypeSearchHandlerFactory;
import com.atlassian.jira.issue.statistics.IssueTypeStatisticsMapper;
import com.atlassian.jira.issue.util.IssueChangeHolder;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.ofbiz.GenericValueUtils;
import com.atlassian.jira.web.bean.BulkEditBean;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowException;
import com.atlassian.jira.workflow.WorkflowManager;
import com.atlassian.velocity.VelocityManager;
import com.opensymphony.util.TextUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericValue;
import webwork.action.Action;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
public class IssueTypeSystemField extends AbstractOrderableNavigableFieldImpl implements IssueTypeField
{
    private static final Logger log = Logger.getLogger(IssueTypeSystemField.class);

    private static final String ISSUE_TYPE_NAME_KEY = "issue.field.issuetype";

    private final ConstantsManager constantsManager;
    private final WorkflowManager workflowManager;
    private final IssueTypeStatisticsMapper issueTypeStatisticsMapper;
    private final FieldConfigSchemeManager fieldConfigSchemeManager;
    private final OptionSetManager optionSetManager;
    private final IssueTypeSchemeManager issueTypeSchemeManager;

    public IssueTypeSystemField(VelocityManager velocityManager, ApplicationProperties applicationProperties, JiraAuthenticationContext authenticationContext,
            ConstantsManager constantsManager, WorkflowManager workflowManager, PermissionManager permissionManager, IssueTypeStatisticsMapper issueTypeStatisticsMapper,
            FieldConfigSchemeManager fieldConfigSchemeManager, OptionSetManager optionSetManager, IssueTypeSchemeManager issueTypeSchemeManager,
            IssueTypeSearchHandlerFactory searchHandlerFactory)
    {
        super(IssueFieldConstants.ISSUE_TYPE, ISSUE_TYPE_NAME_KEY, velocityManager, applicationProperties, authenticationContext, permissionManager, searchHandlerFactory);
        this.constantsManager = constantsManager;
        this.workflowManager = workflowManager;
        this.issueTypeStatisticsMapper = issueTypeStatisticsMapper;
        this.fieldConfigSchemeManager = fieldConfigSchemeManager;
        this.optionSetManager = optionSetManager;
        this.issueTypeSchemeManager = issueTypeSchemeManager;
    }

    public String getCreateHtml(FieldLayoutItem fieldLayoutItem, OperationContext operationContext, Action action, Issue issue, Map displayParameters)
    {
        Map velocityParams = prepareVelocityParams(fieldLayoutItem, action, issue, displayParameters, operationContext);

        populateOptionsForProjects(velocityParams, issue, displayParameters, issue.isSubTask());

        return renderTemplate("issuetype-edit.vm", velocityParams);
    }

    private List populateOptionsForProjects(Map velocityParams, Issue issue, Map displayParameters, boolean isSubTask)
    {
        // Get the available projects
        ProjectSystemField projectField = (ProjectSystemField) getParentField();
        Collection allowedProjects;
        if (isSubTask && issue != null)
        {
            // For sub tasks, only allow the current project
            allowedProjects = EasyList.build(issue.getProject());
        }
        else
        {
            allowedProjects = projectField.getAllowedProjects();
        }

        Map projectToConfig = new ListOrderedMap();
        Set releventConfigs = new HashSet();
        for (Iterator iterator = allowedProjects.iterator(); iterator.hasNext();)
        {
            GenericValue project = (GenericValue) iterator.next();
            FieldConfigScheme configScheme = issueTypeSchemeManager.getConfigScheme(project);
            FieldConfig relevantConfig = configScheme.getOneAndOnlyConfig();

            releventConfigs.add(configScheme);
            projectToConfig.put(project.getLong("id"), relevantConfig.getId());
        }
        velocityParams.put("projectToConfig", projectToConfig);


        // Get all options for the config
        List configOptions = new ArrayList();
        for (Iterator iterator = releventConfigs.iterator(); iterator.hasNext();)
        {
            FieldConfigScheme configScheme = (FieldConfigScheme) iterator.next();
            FieldConfig config = configScheme.getOneAndOnlyConfig();
            List options = new ArrayList(getOptionsForConfig(config, issue, displayParameters, isSubTask));

            if (!options.isEmpty())
            {
                if (!isSubTask && issueTypeSchemeManager.getDefaultValue(config) == null)
                {
                    // If no default then add a please select
                    TextOption pleaseSelect = new TextOption("", authenticationContext.getI18nHelper().getText("common.words.pleaseselect"));
                    options.add(0, pleaseSelect);
                }

                configOptions.add(new FieldConfigSchemeOption(configScheme, options));
            }
        }
        velocityParams.put("configOptions", configOptions);


        // Set all the different defaults
        if (!isSubTask)
        {
            Map configToDefaultOption = new ListOrderedMap();
            for (Iterator iterator = releventConfigs.iterator(); iterator.hasNext();)
            {
                FieldConfigScheme configScheme = (FieldConfigScheme) iterator.next();
                FieldConfig config = configScheme.getOneAndOnlyConfig();
                IssueType defaultValue = issueTypeSchemeManager.getDefaultValue(config);
                configToDefaultOption.put(config.getId(), defaultValue != null ? defaultValue.getId() : "");
            }

            if (configToDefaultOption.size() > 1)
            {
                velocityParams.put("configToDefaultOption", configToDefaultOption);
            }
            else
            {
                // There's only one config, so set the value as an issue type, if nothing's been selected before
                if (velocityParams.get(getId()) == null)
                {
                    velocityParams.put(getId(), configToDefaultOption.values().iterator().next());
                }
            }
        }
        return configOptions;
    }


    private boolean isMoveIssue(Map displayParameters, Issue issue)
    {
        return displayParameters.containsKey(MOVE_ISSUE_PARAM_KEY) && Boolean.TRUE.equals(displayParameters.get(MOVE_ISSUE_PARAM_KEY)) && issue != null;
    }

    public String getEditHtml(FieldLayoutItem fieldLayoutItem, OperationContext operationContext, Action action, Issue issue, Map displayParameters)
    {
        Map velocityParams = prepareVelocityParams(fieldLayoutItem, action, issue, displayParameters, operationContext);

        // Get all options for the config
        List configOptions = new ArrayList();
        FieldConfigScheme configScheme = issueTypeSchemeManager.getConfigScheme(issue.getProject());
        FieldConfig relevantConfig = configScheme.getOneAndOnlyConfig();
        Collection options = getOptionsForConfig(relevantConfig, issue, displayParameters);
        options = CollectionUtils.select(options, new ValidForEditIssueTypes(issue, displayParameters));

        if (options.size() > 1)
        {
            configOptions.add(new FieldConfigSchemeOption(configScheme, options));
            velocityParams.put("configOptions", configOptions);
            return renderTemplate("issuetype-edit.vm", velocityParams);

        }
        else
        {
            velocityParams.put("noAllowedIssueTypes", Boolean.TRUE);
            velocityParams.put("hasMovePermission", Boolean.valueOf(getPermissionManager().hasPermission(Permissions.MOVE_ISSUE, issue.getProject(), getAuthenticationContext().getUser())));
            velocityParams.put("issue", issue);
            return renderTemplate("issuetype-edit-not-allowed.vm", velocityParams);
        }
    }


    public String getBulkEditHtml(OperationContext operationContext, Action action, BulkEditBean bulkEditBean, Map displayParameters)
    {
        Map velocityParams = prepareVelocityParams(null, action, null, displayParameters, operationContext);

        // Bulk Move - determine collection of possible target issue types
        if (BulkMoveOperation.NAME.equals(bulkEditBean.getOperationName()) ||
            BulkMigrateOperation.OPERATION_NAME.equals(bulkEditBean.getOperationName()))
        {
            populateOptionsForProjects(velocityParams, null, displayParameters, bulkEditBean.isSubTaskCollection());
        }
        else
        {
            // Bulk editing
            Collection options = getAllowedIssueTypeOptionsForEdit(bulkEditBean.getSelectedIssues(), displayParameters);
            velocityParams.put("configOptions", EasyList.build(new FieldConfigSchemeOption(null, options)));
        }

        return renderTemplate("issuetype-edit.vm", velocityParams);
    }

    public String getEditHtml(OperationContext operationContext, Action action, List options)
    {
        Map velocityParams = prepareVelocityParams(null, action, null, EasyMap.build(), operationContext);
        velocityParams.put("configOptions", EasyList.build(new FieldConfigSchemeOption(null, options)));
        return renderTemplate("issuetype-edit.vm", velocityParams);
    }


    public String getViewHtml(FieldLayoutItem fieldLayoutItem, Action action, Issue issue, Map displayParameters)
    {
        Map velocityParams = getVelocityParams(fieldLayoutItem, action, issue, displayParameters);
        // Create issue type object
        IssueType issueType = issue.getIssueTypeObject();
        velocityParams.put("issueTypeObject", issueType);
        return getViewHtml(velocityParams);
    }

    public String getViewHtml(FieldLayoutItem fieldLayoutItem, Action action, Issue issue, Object value, Map displayParameters)
    {
        Map velocityParams = getVelocityParams(fieldLayoutItem, action, null, displayParameters);
        // Create issue type object
        GenericValue issueTypeGV = (GenericValue) value;
        IssueType issueType = constantsManager.getIssueTypeObject(issueTypeGV.getString("id"));
        velocityParams.put("issueTypeObject", issueType);
        return getViewHtml(velocityParams);
    }

    private String getViewHtml(Map velocityParams)
    {
        return renderTemplate("issuetype-view.vm", velocityParams);
    }

    public boolean isShown(Issue issue)
    {
        return true;
    }

    public void populateDefaults(Map fieldValuesHolder, Issue issue)
    {
        IssueType issueType = (IssueType) getDefaultValue(issue);
        if (issueType != null)
        {
            fieldValuesHolder.put(getId(), issueType.getId());
        }
    }

    public Object getDefaultValue(Issue issue)
    {
        return issueTypeSchemeManager.getDefaultValue(issue);
    }

    public void updateIssue(FieldLayoutItem fieldLayoutItem, MutableIssue issue, Map fieldValueHolder)
    {
        issue.setIssueType((GenericValue) getValueFromParams(fieldValueHolder));
    }

    public MessagedResult needsMove(Collection originalIssues, Issue targetIssue, FieldLayoutItem targetFieldLayoutItem)
    {
        return new MessagedResult(true);
    }

    public void populateForMove(Map fieldValuesHolder, Issue originalIssue, Issue targetIssue)
    {
        // Issue type should be updated on the first screen of the Move Issue wizard
        throw new UnsupportedOperationException("Not implemented.");
    }

    public void removeValueFromIssueObject(MutableIssue issue)
    {
        throw new UnsupportedOperationException("This method should never be called.");
    }

    public boolean canRemoveValueFromIssueObject(Issue issue)
    {
        return false;
    }

    public boolean hasValue(Issue issue)
    {
        return (issue.getIssueType() != null);
    }

    /**
     * validate the field value
     *
     * @param operationContext
     * @param errorCollectionToAddTo
     * @param fieldScreenRenderLayoutItem
     */
    public void validateParams(OperationContext operationContext, ErrorCollection errorCollectionToAddTo, I18nHelper i18n, Issue issue, FieldScreenRenderLayoutItem fieldScreenRenderLayoutItem)
    {
        Map fieldValuesHolder = operationContext.getFieldValuesHolder();
        String issueTypeId = (String) fieldValuesHolder.get(getId());

        // Check that the issue type with the given id exists.
        if (issueTypeId == null)
        {
            errorCollectionToAddTo.addError(getId(), i18n.getText("createissue.noissuetype"));
        }
        else if (getValueFromParams(fieldValuesHolder) == null)
        {
            errorCollectionToAddTo.addError(getId(), i18n.getText("createissue.invalidissuetype"));
        }
        else
        {
            IssueType issueTypeObject = constantsManager.getIssueTypeObject(issueTypeId);

            // Validate that the issue type is a valid option for the project that we are in.
            Collection allowedIssueTypeOptions = getOptionsForIssue(issue);
            if (!allowedIssueTypeOptions.contains(new IssueConstantOption(issueTypeObject)))
            {
                errorCollectionToAddTo.addError(getId(), i18n.getText("createissue.invalidissuetype"));
            }
        }
    }


    public Object getValueFromParams(Map params)
    {
        String issueTypeId = (String) params.get(getId());

        if (TextUtils.stringSet(issueTypeId))
        {
            return constantsManager.getIssueType(issueTypeId);
        }
        else
        {
            return null;
        }
    }

    public void populateParamsFromString(Map fieldValuesHolder, String stringValue, Issue issue) throws FieldValidationException
    {
        Long issuetypeId = null;
        try
        {
            // Check if the issue type is a number
            issuetypeId = Long.valueOf(stringValue);
        }
        catch (NumberFormatException e)
        {
            // If not, try to convert to a number
            issuetypeId = getIssueTypeIdByName(stringValue);
        }

        // Yes, issue type id is a String, even though it is actually a number.
        // Ahh, the joy of backwards compatibility
        fieldValuesHolder.put(getId(), issuetypeId.toString());
    }

    private Long getIssueTypeIdByName(String stringValue) throws FieldValidationException
    {
        for (Iterator iterator = constantsManager.getAllIssueTypes().iterator(); iterator.hasNext();)
        {
            GenericValue issueTypeGV = (GenericValue) iterator.next();
            if (stringValue.equalsIgnoreCase(issueTypeGV.getString("name")))
            {
                return Long.valueOf(issueTypeGV.getString("id"));
            }
        }

        throw new FieldValidationException("Invalid issue type name '" + stringValue + "'.");
    }

    public void createValue(Issue issue, Object value)
    {
        // The field is recorded on the issue itself so there is nothing to do
    }

    public void updateValue(FieldLayoutItem fieldLayoutItem, Issue issue, ModifiedValue modifiedValue, IssueChangeHolder issueChangeHolder)
    {
        Object currentValue = modifiedValue.getOldValue();
        Object value = modifiedValue.getNewValue();
        ChangeItemBean cib = null;

        if (currentValue == null)
        {
            if (value != null)
            {
                GenericValue issueType = (GenericValue) value;
                cib = new ChangeItemBean(ChangeItemBean.STATIC_FIELD, getId(), null, null, issueType.getString("id"), issueType.getString("name"));
            }
        }
        else
        {
            if (!valuesEqual(value, currentValue))
            {
                GenericValue currentIssueType = (GenericValue) currentValue;
                if (value != null)
                {
                    GenericValue issueType = (GenericValue) value;
                    cib = new ChangeItemBean(ChangeItemBean.STATIC_FIELD, getId(), currentIssueType.getString("id"), currentIssueType.getString("name"), issueType.getString("id"), issueType.getString("name"));
                }
                else
                {
                    cib = new ChangeItemBean(ChangeItemBean.STATIC_FIELD, getId(), currentIssueType.getString("id"), currentIssueType.getString("name"), null, null);
                }
            }
        }

        if (cib != null)
        {
            issueChangeHolder.addChangeItem(cib);
        }
    }

    public String availableForBulkEdit(BulkEditBean bulkEditBean)
    {
        // Issue type is not possible to hide as it is always required. So no need to check the field layouts

        // Have to look through all the issues in case permission has been given to current assignee/reporter (i.e. role based)
        List selectedIssues = bulkEditBean.getSelectedIssues();
        for (Iterator iterator = selectedIssues.iterator(); iterator.hasNext();)
        {
            Issue issue = (Issue) iterator.next();
            // If we got here then the field is visible in all field layouts
            // So check for permission
            if (!hasBulkUpdatePermission(bulkEditBean, issue) || !isShown(issue))
            {
                return "bulk.edit.multiproject.unavailable.permission";
            }
        }

        // Need to ensure that the list of available issue is not empty
        if (isHasCommonIssueTypes(selectedIssues))
        {
            return "bulk.edit.issuetype.noissuetypes";
        }

        return null;
    }

    /**
     * This method will determine if there are ANY issue type that the selectedIssues
     * have in common. This takes into account the possible difference in workflow or
     * field configuration for each issue type.
     * @param selectedIssues
     * @return true if there are issue types in common, false otherwise
     */
    public boolean isHasCommonIssueTypes(Collection selectedIssues)
    {
        return getAllowedIssueTypeOptionsForEdit(selectedIssues, new HashMap()).isEmpty();
    }

    private Collection getAllowedIssueTypeOptionsForEdit(Collection issues, Map displayParameters)
    {
        Iterator iterator = issues.iterator();
        Issue issue = (Issue) iterator.next();
        Collection availableIssueTypes = CollectionUtils.select(new ArrayList(getOptionsForIssue(issue)),
                new ValidForEditIssueTypes(issue, displayParameters));

        while (!availableIssueTypes.isEmpty() && iterator.hasNext())
        {
            issue = (Issue) iterator.next();
            Collection newOptions = getOptionsForIssue(issue);

            // Cull the list by removing any options that do not match the original workflow or field config
            newOptions = CollectionUtils.select(newOptions,
                    new ValidForEditIssueTypes(issue, displayParameters));

            // Work out the intersection between the two collections
            availableIssueTypes.retainAll(newOptions);
        }
        return availableIssueTypes;
    }

    protected Object getRelevantParams(Map params)
    {
        String[] value = (String[]) params.get(getId());
        if (value != null && value.length > 0)
        {
            return value[0];
        }
        else
        {
            return null;
        }
    }

    public void populateFromIssue(Map fieldValuesHolder, Issue issue)
    {
        fieldValuesHolder.put(getId(), issue.getIssueType().getString("id"));
    }

    private FieldLayoutManager getFieldLayoutManager()
    {
        return ManagerFactory.getFieldManager().getFieldLayoutManager();
    }

    public String getColumnHeadingKey()
    {
        return "issue.column.heading.issuetype";
    }

    public String getDefaultSortOrder()
    {
        return ORDER_DESCENDING;
    }

    public LuceneFieldSorter getSorter()
    {
        return issueTypeStatisticsMapper;
    }

    public String getColumnViewHtml(FieldLayoutItem fieldLayoutItem, Map displayParams, Issue issue)
    {
        Map velocityParams = getVelocityParams(getAuthenticationContext().getI18nHelper(), displayParams, issue);
        velocityParams.put("issue", issue);
        // Create issue type object
        IssueType issueType = issue.getIssueTypeObject();
        velocityParams.put(getId(), issueType);
        return renderTemplate("issuetype-columnview.vm", velocityParams);
    }

    public Collection<Option> getOptionsForIssue(Issue issue, boolean isSubTask)
    {
        FieldConfig relevantConfig = getRelevantConfig(issue);
        return getOptionsForConfig(relevantConfig, issue, EasyMap.build(), isSubTask);
    }
    // -------------------------------------------------------------------------------------------------- Config methods


    public List getConfigurationItemTypes()
    {
        return Collections.EMPTY_LIST;
    }

    public List getAssociatedProjects()
    {
        return fieldConfigSchemeManager.getAssociatedProjects(this);
    }

    public FieldConfig getRelevantConfig(IssueContext issueContext)
    {
        return fieldConfigSchemeManager.getRelevantConfig(issueContext, this);
    }

    public Field getParentField()
    {
        return ManagerFactory.getFieldManager().getField(IssueFieldConstants.PROJECT);
    }

    private Collection getOptionsForIssue(Issue issue)
    {
        FieldConfig relevantConfig = getRelevantConfig(issue);
        return getOptionsForConfig(relevantConfig, issue, EasyMap.build());
    }

    private Collection getOptionsForConfig(FieldConfig fieldConfig, Issue issue, Map displayParameters)
    {
        return getOptionsForConfig(fieldConfig, issue, displayParameters, issue.isSubTask());
    }

    private Collection getOptionsForConfig(FieldConfig fieldConfig, Issue issue, Map displayParameters, boolean isSubTask)
    {
        Collection options = optionSetManager.getOptionsForConfig(fieldConfig).getOptions();
        options = CollectionUtils.select(options, new ValidIssueTypePredicate(issue, displayParameters, isSubTask));
        return options;
    }

    private Map prepareVelocityParams(FieldLayoutItem fieldLayoutItem, Action action, Issue issue, Map displayParameters, OperationContext operationContext)
    {
        Map velocityParams = getVelocityParams(fieldLayoutItem, action, issue, displayParameters);
        String issueTypeId = (String) operationContext.getFieldValuesHolder().get(getId());
        velocityParams.put(getId(), issueTypeId);
        if (issueTypeId != null && constantsManager.getIssueType(issueTypeId) != null)
        {
            velocityParams.put("issueTypeObject", constantsManager.getIssueTypeObject(issueTypeId));
        }
        return velocityParams;
    }

    public Collection getIssueConstants()
    {
        return constantsManager.getAllIssueTypeObjects();
    }


    private class ValidIssueTypePredicate implements Predicate
    {
        private final IssueConstantOption currentIssuesOption;
        private final Issue issue;
        private final Map displayParameters;
        private final Collection subTaskIds = GenericValueUtils.transformToStringIdsList(constantsManager.getSubTaskIssueTypes());
        private boolean subTaskOnly = false;

        public ValidIssueTypePredicate(Issue issue, Map displayParameters)
        {
            this.issue = issue;
            this.displayParameters = displayParameters;
            currentIssuesOption = issue != null ? new IssueConstantOption(issue.getIssueTypeObject()) : null;
        }

        public ValidIssueTypePredicate(Issue issue, Map displayParameters, boolean isSubTask)
        {
            this(issue, displayParameters);
            this.subTaskOnly = isSubTask;
        }


        public boolean evaluate(Object object)
        {
            Option option = (Option) object;
            if (!isCorrectType(option))
            {
                return false;
            }
            if (isMoveIssue(displayParameters, issue) && currentIssuesOption.equals(option))
            {
                return false;
            }
            else
            {
                // If issue has not been created or we are not using the enterprise edition then simply return the possible issue types
                return true;
            }
        }

        private boolean isCorrectType(Option option)
        {
            if (subTaskOnly)
            {
                return subTaskIds.contains(option.getId());
            }
            else
            {
                return !subTaskIds.contains(option.getId());
            }
        }
    }

    /**
     * This predicate is used as a filter on a list of com.atlassian.jira.issue.fields.option.Option objects.
     * The issue provided in the constructor is used as the base value (the value before the change). We use
     * the project and issue type from the original issue and then we find the field layout and the workflow
     * for the project from the issue and the issue type specified by the option. If the field layout or
     * the workflow are different from the original then we will not include that issue type option in the
     * list (we filter it out).
     */
    private class ValidForEditIssueTypes implements Predicate
    {
        FieldLayout currentFieldLayout;
        JiraWorkflow currentWorkflow;
        private final Issue issue;
        private final Map displayParameters;

        public ValidForEditIssueTypes(Issue issue, Map displayParameters)
        {
            this.issue = issue;
            this.displayParameters = displayParameters;
        }


        public boolean evaluate(Object object)
        {
            Option option = (Option) object;

            try
            {
                if (currentFieldLayout == null || currentWorkflow == null)
                {
                    currentFieldLayout = getFieldLayoutManager().getFieldLayout(issue.getProject(),
                                                                                issue.getIssueTypeObject() != null ? issue.getIssueTypeObject().getId() : null);
                    currentWorkflow = workflowManager.getWorkflow(issue.getLong("project"),
                                                                  issue.getIssueTypeObject() != null ? issue.getIssueTypeObject().getId() : null);

                }

                // Otherwise, return all issue types that an issue can change to without causing 'problems', that is all issue types that have the same:
                // 1. Field Layout
                // 2. Workflow
                // as the original issue type.

                FieldLayout fieldLayoutScheme = getFieldLayoutManager().getFieldLayout(issue.getProject(), option.getId());
                if (!currentFieldLayout.equals(fieldLayoutScheme))
                {
                    // This parameter is used by the view - issuetype-edit.vm which will display a message to the user
                    // that an incompatable issue type has been discarded.
                    displayParameters.put("restrictedSelection", Boolean.TRUE);
                    return false;
                }

                // No need to check current issue type workflow
                if (!issue.getIssueTypeObject().getId().equals(option.getId()))
                {
                    JiraWorkflow workflow = workflowManager.getWorkflow(issue.getLong("project"), option.getId());
                    if (!currentWorkflow.equals(workflow))
                    {
                        // This parameter is used by the view - issuetype-edit.vm which will display a message to the user
                        // that an incompatable issue type has been discarded.
                        displayParameters.put("restrictedSelection", Boolean.TRUE);
                        return false;
                    }
                }

                return true;
            }
            catch (WorkflowException e)
            {
                throw new DataAccessException(e);
            }
        }
    }

    /**
     * Return an internationalized value for the changeHistory item - an issue type name in this case.
     *
     * @param changeHistory     name of issue type
     * @param i18nHelper        used to translate the issue type name
     * @return String
     */
    public String prettyPrintChangeHistory(String changeHistory, I18nHelper i18nHelper)
    {
        if (TextUtils.stringSet(changeHistory))
        {
            Long issueTypeId = getIssueTypeIdByName(changeHistory);

            if (issueTypeId != null)
            {
                IssueType issueType = constantsManager.getIssueTypeObject(issueTypeId.toString());
                if (issueType != null)
                {
                    return issueType.getNameTranslation(i18nHelper);
                }
            }
        }
        // Otherwise - return the original string
        return changeHistory;
    }
}
