/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.issue.bulkedit;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.bulkedit.operation.BulkMoveOperation;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFactory;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.customfields.impl.VersionCFType;
import com.atlassian.jira.issue.customfields.view.CustomFieldParams;
import com.atlassian.jira.issue.customfields.view.CustomFieldParamsImpl;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.Field;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.IssueTypeSystemField;
import com.atlassian.jira.issue.fields.OrderableField;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.web.bean.BulkEditBean;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowException;
import com.atlassian.jira.workflow.WorkflowManager;
import org.ofbiz.core.entity.GenericValue;
import webwork.action.ActionContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class BulkMove extends AbstractBulkOperationDetailsAction
{
    public static final String RADIO_ERROR_MSG = "buik.edit.must.select.one.action.to.perform";
    private static final String PARENT_SELECTION = "parent";

    private boolean subTaskPhase = false;

    protected BulkMoveOperation bulkMoveOperation;
    protected final FieldManager fieldManager;
    protected final WorkflowManager workflowManager;
    protected final ConstantsManager constantsManager;
    protected final IssueFactory issueFactory;
    protected final PermissionManager permissionManager;

    public BulkMove(SearchService searchService, BulkMoveOperation bulkMoveOperation, FieldManager fieldManager, WorkflowManager workflowManager, ConstantsManager constantsManager, IssueFactory issueFactory, PermissionManager permissionManager)
    {
        super(searchService);
        this.fieldManager = fieldManager;
        this.workflowManager = workflowManager;
        this.bulkMoveOperation = bulkMoveOperation;
        this.constantsManager = constantsManager;
        this.issueFactory = issueFactory;
        this.permissionManager = permissionManager;
    }

    ///// ------------------------ Move Action Methods ------------------------ ///// ------------------------
    // TODO many of the doXxx() methods in this Action are no longer called since we moved to BulkMigrate.
    
    public String doDefault() throws Exception
    {
        if (getBulkEditBean() == null)
        {
            return redirectToStart();
        }

        // set BulkEditBean to use the issues that have now been selected rather than the issues from the search request
        getBulkEditBean().setIssuesInUse(getBulkEditBean().getSelectedIssues());
        getBulkEditBean().setOperationName(BulkMoveOperation.NAME);

        return super.doDefault();
    }

    // Verify selection of issues - parents or subtasks
    public String doDetails()
    {
        BulkEditBean bulkEditBean = getRootBulkEditBean();

        // Check that we have a BulkEditBean - i.e. the user got here by following the wizard - not by
        // clicking the "back" button of the browser (or something like that)
        if (bulkEditBean == null)
        {
            // If we do not have BulkEditBean, send the user to the first step of the wizard
            return redirectToStart();
        }

        // Reset the collection to contain only parent issues (subtasks can't be "moved" only migrated)
        resetIssueCollection(PARENT_SELECTION);

        bulkEditBean.resetMoveData();
        bulkEditBean.clearAvailablePreviousSteps();
        bulkEditBean.addAvailablePreviousStep(1);
        bulkEditBean.addAvailablePreviousStep(2);
        bulkEditBean.setCurrentStep(3);
        return INPUT;
    }


    // Verify and perform the move operation
    public String doPerform() throws Exception
    {
        if (getBulkEditBean() == null)
        {
            return redirectToStart();
        }

        try
        {
            // Ensure the user has the global BULK CHANGE permission
            if (!permissionManager.hasPermission(Permissions.BULK_CHANGE, getRemoteUser()))
            {
                addErrorMessage(getText("bulk.change.no.permission", String.valueOf(getBulkEditBean().getSelectedIssues().size())));
            }

            // Ensure the user can perform the operation
            if (!getBulkMoveOperation().canPerform(getRootBulkEditBean(), getRemoteUser()))
            {
                addErrorMessage(getText("bulk.edit.cannotperform.error", String.valueOf(getBulkEditBean().getSelectedIssues().size())));
            }
        }
        catch (Exception e1)
        {
            log.error("Error occurred while testing operation.", e1);
            addErrorMessage(getText("bulk.canperform.error"));
        }

        if (invalidInput())
        {
            return ERROR;
        }

        try
        {
            getBulkMoveOperation().perform(getRootBulkEditBean(), getRemoteUser());
        }
        catch (Exception e)
        {
            log.error("Error while performing Bulk Edit operation.", e);
            addErrorMessage(getText("bulk.edit.perform.error"));
            return ERROR;
        }

        return finishWizard();
    }

    public String doDetailsValidation() throws Exception
    {
//        // Check that we have a BulkEditBean - i.e. the user got here by following the wizard - not by
//        // clicking the "back" button of the browser (or something like that)
//        if (getBulkEditBean() == null)
//        {
//            // If we do not have BulkEditBean, send the user to the first step of the wizard
//            return redirectToStart();
//        }
//
//        if (invalidInput())
//        {
//            return ERROR;
//        }
//        else
//        {
//            progressToLastStep();
//        }
//        return getResult();

        throw new IllegalArgumentException("This should never be called.");

    }

    public boolean isHasAvailableActions() throws Exception
    {
        return getBulkMoveOperation().canPerform(getBulkEditBean(), getRemoteUser());
    }


    ///// ------------------------ Validation Methods ------------------------ ///// ------------------------
    // Validate the Project and Issue Type selected
    public String doContextValidation() throws Exception
    {
        // Check that we have a BulkEditBean - i.e. the user got here by following the wizard - not by
        // clicking the "back" button of the browser (or something like that)
        if (getBulkEditBean() == null)
        {
            // If we do not have BulkEditBean, send the user to the first step of the wizard
            return redirectToStart();
        }

        getBulkEditBean().resetMoveData();

        // Validate & commit context
        getBulkMoveOperation().chooseContext(getBulkEditBean(), getRemoteUser(), this, this);

        if (invalidInput())
        {
            return ERROR;
        }

        // Check if status change is required for any issues
        if (!getBulkMoveOperation().isStatusValid(getBulkEditBean()))
        {
            return "statuserror";
        }
        else
        {
            return SUCCESS;
        }
    }


    // Populate status mappings for parents and subtasks
    public String doStatusValidation() throws Exception
    {
        // Check that we have a BulkEditBean - i.e. the user got here by following the wizard - not by
        // clicking the "back" button of the browser (or something like that)
        if (getBulkEditBean() == null)
        {
            // If we do not have BulkEditBean, send the user to the first step of the wizard
            return redirectToStart();
        }

        getBulkMoveOperation().setStatusFields(getBulkEditBean());

        return getResult();
    }

    public String doFieldsValidation() throws Exception
    {
        // Check that we have a BulkEditBean - i.e. the user got here by following the wizard - not by
        // clicking the "back" button of the browser (or something like that)
        if (getBulkEditBean() == null)
        {
            // If we do not have BulkEditBean, send the user to the first step of the wizard
            return redirectToStart();
        }

        getBulkMoveOperation().validatePopulateFields(getBulkEditBean(), this, this);

        if (invalidInput())
        {
            return ERROR;
        }

        // If there's another layer of sub-tasking
        if (getBulkEditBean().getSubTaskBulkEditBean() != null)
        {
            setSubTaskPhase(true);
            return "subtaskphase";
        }
        else
        {
            setSubTaskPhase(false);
            // Progress to the final level
            progressToLastStep();

            return getResult();
        }
    }

    ///// ------------------------ Field Edit Methods ------------------------ ///// ------------------------

    /**
     * Only invoked when displaying the Project and Issue type as part of the bulk move operation
     * @return Field HTML
     */
    public String getFieldHtml(String fieldId, BulkEditBean bulkEditBean)
    {
        OrderableField orderableField = (OrderableField) fieldManager.getField(fieldId);
        return orderableField.getBulkEditHtml(bulkEditBean, this, bulkEditBean,
                EasyMap.build(OrderableField.NO_HEADER_PARAM_KEY, Boolean.TRUE,
                        "fieldNamePrefix", bulkEditBean.getKey()));
    }

    public String getFieldHtml(String fieldId)
    {
        return getFieldHtml(fieldId, getBulkEditBean());
    }

    /**
     * Used when displaying the fields to be edited during the bulk move operation
     * @return Field HTML
     */
    public String getFieldHtml(FieldLayoutItem fieldLayoutItem)
    {
        OrderableField orderableField = fieldLayoutItem.getOrderableField();
        if (orderableField.isShown(getBulkEditBean().getFirstTargetIssueObject()))
        {
            // Need to display edit template with target fieldlayout item
            return orderableField.getBulkEditHtml(getBulkEditBean(), this, getBulkEditBean(), EasyMap.build(OrderableField.NO_HEADER_PARAM_KEY, Boolean.TRUE));
        }
        else
        {
            return "";
        }
    }

    public boolean isIssueTypesAvailable()
    {
        IssueTypeSystemField issueTypeField = (IssueTypeSystemField) fieldManager.getField(IssueFieldConstants.ISSUE_TYPE);
        return !issueTypeField.isHasCommonIssueTypes(getBulkEditBean().getSelectedIssues());
    }

    // Determine whether there are available target subtasks
    public boolean isSubTaskTypesAvailable()
    {
        IssueTypeSystemField issueTypeField = (IssueTypeSystemField) fieldManager.getField(IssueFieldConstants.ISSUE_TYPE);
        Collection selectedIssues = getBulkEditBean().getSelectedIssues();
        Collection selectedSubTasks = new ArrayList();

        for (Iterator iterator = selectedIssues.iterator(); iterator.hasNext();)
        {
            Issue issue = (Issue) iterator.next();
            if (issue.isSubTask())
            {
                selectedSubTasks.add(issue);
            }
        }
        return !issueTypeField.isHasCommonIssueTypes(selectedSubTasks);
    }

    public String getFieldViewHtml(OrderableField orderableField)
    {
        final Map<String, Object> displayParams = MapBuilder.<String, Object>newBuilder("readonly", Boolean.TRUE)
                .add("nolink", Boolean.TRUE)
                .add("bulkoperation", getBulkEditBean().getOperationName()).toMutableMap();

        // Use the layout item of where we are going since we are moving to that space
        FieldLayoutItem layoutItem = getBulkEditBean().getTargetFieldLayout().getFieldLayoutItem(orderableField);
        return orderableField.getViewHtml(layoutItem, this, getBulkEditBean().getFirstTargetIssueObject(), getBulkEditBean().getFieldValues().get(orderableField.getId()), displayParams);
    }


    public Collection getMoveFieldLayoutItems()
    {
        return getBulkEditBean().getMoveFieldLayoutItems();
    }

    public String getFieldName(Field field)
    {
        if (field instanceof CustomField)
        {
            return field.getName();
        }
        else
        {
            return getText(field.getNameKey());
        }
    }

    public String getNewViewHtml(OrderableField field)
    {
        final Map displayParameters = MapBuilder.newBuilder("readonly", Boolean.TRUE).add("nolink", Boolean.TRUE).toMap();
        return field.getViewHtml(getBulkEditBean().getTargetFieldLayout().getFieldLayoutItem(field), this, getBulkEditBean().getFirstTargetIssueObject(), displayParameters);
    }

    public String getNewViewHtml(BulkEditBean bulkEditBean, OrderableField field)
    {
        final Map<String, Object> displayParameters = MapBuilder.<String, Object>newBuilder("readonly", Boolean.TRUE)
                .add("nolink", Boolean.TRUE)
                .add("prefix", bulkEditBean.getProject().getString("id") + "_" + bulkEditBean.getIssueType().getString("id") + "_")
                .toMap();
        return field.getViewHtml(bulkEditBean.getTargetFieldLayout().getFieldLayoutItem(field), this, bulkEditBean.getFirstTargetIssueObject(), displayParameters);
    }

    public boolean isFieldUsingSubstitutions(BulkEditBean bulkEditBean, OrderableField field)
    {
        final Map<Long, Long> substitutions = bulkEditBean.getFieldSubstitutionMap().get(field.getId());
        return substitutions != null;
    }

    public Map<Long, Long> getSubstitutionsForField(BulkEditBean bulkEditBean, OrderableField field)
    {
        return bulkEditBean.getFieldSubstitutionMap().get(field.getId());
    }

    public String getMappingViewHtml(BulkEditBean bulkEditBean, OrderableField field, Long id, final boolean showProject)
    {
        final FieldLayoutItem fieldLayoutItem = bulkEditBean.getTargetFieldLayout().getFieldLayoutItem(field);
        final Issue baseIssue = bulkEditBean.getFirstTargetIssueObject();
        return getViewHtmlForId(baseIssue, fieldLayoutItem, field, id, showProject);
    }

    private String getViewHtmlForId(Issue baseIssue, final FieldLayoutItem fieldLayoutItem, OrderableField field, Long id, final boolean showProject)
    {
        // -1 means no value 
        if (id == null || id == -1L)
        {
            return getText("common.words.unknown");
        }

        // dummy up the value for an issue
        Object value;
        if (field instanceof CustomField)
        {
            value = getValueForId((CustomField) field, id);
        }
        else
        {
            value = getValueForId(field, id);
        }

        // set the value in a field values holder
        final Map<String, Object> fieldValuesHolder = new LinkedHashMap<String, Object>();
        fieldValuesHolder.put(field.getId(), value);

        // update an issue with these values
        final MutableIssue dummyIssue = issueFactory.cloneIssue(baseIssue);
        field.updateIssue(fieldLayoutItem, dummyIssue, fieldValuesHolder);

        // now render the field
        if (showProject)
        {
            return field.getViewHtml(fieldLayoutItem, this, dummyIssue, MapBuilder.singletonMap("showProject", true));
        }
        else
        {
            return field.getViewHtml(fieldLayoutItem, this, dummyIssue);
        }
    }

    // custom field values are expected to be as a list of strings inside a CustomFieldParams
    private CustomFieldParams getValueForId(CustomField customField, Long id)
    {
        return new CustomFieldParamsImpl(customField, Collections.singletonList(id.toString()));
    }

    // system field values are simply a collection of Longs
    private Collection<Long> getValueForId(OrderableField orderableField, Long id)
    {
        return Collections.singletonList(id);
    }

    public boolean isAvailable(String action) throws Exception
    {
        return true;
    }

    public boolean isAllowProjectEdit()
    {
        return isAllowProjectEdit(getBulkEditBean());
    }

    public boolean isAllowProjectEdit(BulkEditBean bulkEditBean)
    {
        return !bulkEditBean.isSubTaskCollection();
    }

    public String getOperationDetailsActionName()
    {
        return getBulkMoveOperation().getOperationName() + "Details.jspa";
    }

    // This is taken out as a protected method such that it can be overridden, and the doValidation() method reused by subclass actions
    protected void populateFromParams(OrderableField orderableField)
    {
        orderableField.populateFromParams(getBulkEditBean().getFieldValuesHolder(), ActionContext.getParameters());
    }

///// ------------------------ Workflow / Status Methods ------------------------ ///// ------------------------

    // Retrieve collection of target workflow statuses from the workflow associated with the specified issue type id
    public Collection getTargetWorkflowStatuses(String issueTypeId) throws WorkflowException
    {
        JiraWorkflow workflow = getWorkflowForType(getBulkEditBean().getTargetPid(), issueTypeId);
        return workflow.getLinkedStatuses();
    }

    public JiraWorkflow getWorkflowForType(Long projectId, String issueTypeId) throws WorkflowException
    {
        return workflowManager.getWorkflow(projectId, issueTypeId);
    }

    public String getStatusName(String id)
    {
        return constantsManager.getStatus(id).getString("name");
    }

    public String getCurrentTargetPid()
    {
        return String.valueOf(getBulkEditBean().getTargetPid());
    }

    public GenericValue getCurrentTargetProject()
    {
        return getBulkEditBean().getTargetProjectGV();
    }

///// ------------------------ Helper Methods ------------------------ ///// ------------------------

    // Reset the selected issues to contain only parent or sub-task issues

    private void resetIssueCollection(String collectionType)
    {
        Collection selectedIssues = getBulkEditBean().getSelectedIssues();
        Collection modifiedSelection = new ArrayList();

        if (PARENT_SELECTION.equals(collectionType))
        {
            for (Iterator iterator = selectedIssues.iterator(); iterator.hasNext();)
            {
                Issue issue = (Issue) iterator.next();
                if (!issue.isSubTask())
                {
                    modifiedSelection.add(issue);
                }
            }
        }
        else
        {
            for (Iterator iterator = selectedIssues.iterator(); iterator.hasNext();)
            {
                Issue issue = (Issue) iterator.next();
                if (issue.isSubTask())
                {
                    modifiedSelection.add(issue);
                }
            }
        }
        getBulkEditBean().initSelectedIssues(modifiedSelection);

    }

    protected void progressToLastStep()
    {
        if (getRootBulkEditBean() != null)
        {
            getRootBulkEditBean().clearAvailablePreviousSteps();
            getRootBulkEditBean().addAvailablePreviousStep(1);
            getRootBulkEditBean().addAvailablePreviousStep(2);
            getRootBulkEditBean().addAvailablePreviousStep(3);
            getRootBulkEditBean().setCurrentStep(4);
        }
    }

    private BulkMoveOperation getBulkMoveOperation()
    {
        return bulkMoveOperation;
    }

    protected MutableIssue getIssueObject(GenericValue issueGV)
    {
        return issueFactory.getIssue(issueGV);
    }

    public ConstantsManager getConstantsManager()
    {
        return ComponentAccessor.getConstantsManager();
    }

    public String getCurrentIssueType()
    {
        return ((Issue) getBulkEditBean().getSelectedIssues().get(0)).getIssueType().getString("id");
    }

    protected String redirectToStart()
    {
        return super.redirectToStart("bulk.move.session.timeout.message");
    }

    public boolean isSubTaskPhase()
    {
        return subTaskPhase;
    }

    public void setSubTaskPhase(boolean subTaskPhase)
    {
        this.subTaskPhase = subTaskPhase;
    }


    /**
     * Method to determine if a field must try to retain the values already set in issues. In the case of Components, Versions
     * and Version custom fields, we must retain where possible since if we select issues that don't need moving, then
     * no mapping options will be presented, but we don't want other values to be chosen for those issues. Hence, their
     * values must be retained.
     *
     * @param field the field to check for
     * @return true if retaining should be mandatory; false otherwise.
     */
    public boolean isRetainMandatory(OrderableField field)
    {
        if (field instanceof CustomField)
        {
            final CustomField customField = (CustomField) field;
            return customField.getCustomFieldType() instanceof VersionCFType;
        }
        else
        {
            final String id = field.getId();
            return (IssueFieldConstants.FIX_FOR_VERSIONS.equals(id) || IssueFieldConstants.AFFECTED_VERSIONS.equals(id) || IssueFieldConstants.COMPONENTS.equals(id));
        }
    }

    public BulkEditBean getBulkEditBean()
    {
        final BulkEditBean bulkEditBean = getRootBulkEditBean();
        if (!isSubTaskPhase())
        {
            return bulkEditBean;
        }
        else
        {
            return bulkEditBean != null ? bulkEditBean.getSubTaskBulkEditBean() : null;
        }
    }
}

