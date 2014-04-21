/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.bean;

import com.atlassian.jira.bulkedit.operation.BulkEditAction;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.customfields.OperationContext;
import com.atlassian.jira.issue.customfields.impl.FieldValidationException;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.layout.field.FieldLayout;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderer;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.operation.IssueOperation;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.web.SessionKeys;
import com.atlassian.jira.web.action.issue.bulkedit.WorkflowTransitionKey;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowException;
import org.apache.commons.collections.MultiMap;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;
import webwork.action.ActionContext;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Used in the BulkEdit Wizard
 * Stores in session:
 * currentStep
 * action selected and values associated with that action
 * issues selected
 */
public abstract class BulkEditBean implements OperationContext
{
    public static final String SUBTASK_STATUS_INFO = "subtaskstatusinfo_";
    public static final String BULK_MOVE_OP = "bulk_move_op";
    public static final String BULK_DELETE_OP = "bulk_delete_op";
    public static final String BULK_EDIT_OP = "bulk_edit_op";
    public static final String BULK_DELETE = "delete";
    public static final String BULKEDIT_PREFIX = "bulkedit_";


    public abstract BulkEditBean getParentBulkEditBean();

    /**
     * Initialises the {@link BulkEditBean} with the selected issues
     *
     * @param selectedIssues Required selected Issues.
     */
    public abstract void initSelectedIssues(final Collection<Issue> selectedIssues);

    public abstract void addIssues(final Collection<Issue> issues);

    /**
     * Returns a list of "selected" issues.
     * For the "top level" BulkEditBean this should be the actual issues chosen by the user for the bulk operation.
     * <p>
     * The Bulk Move operation will then break this list up in groups of project and issue type and store each of these
     * types in its own BulkEditBean, so for these nested BulkEditBeans this list may be a subset of the original
     * selected issues. Furthermore when moving parent issues to a new Project, we will have to move any subtasks as
     * well. In this case a third level of BulkEditBean is created and these ones will have subtasks that may not have
     * been explicitly selected by the user at all.
     * </p>
     *
     * @return List of the Selected Issues
     */
    public abstract List<Issue> getSelectedIssues();

    public abstract List<Issue> getSelectedIssuesIncludingSubTasks();

    // ------------------------------------------------------------------------------------------- Informational Methods
    public abstract boolean isChecked(final Issue issue);

    public abstract boolean isMultipleProjects();

    public abstract boolean isMutipleIssueTypes();

    public abstract GenericValue getProject();

    public abstract GenericValue getIssueType();

    /**
     * Returns all the unique field layouts of the selected issues
     *
     * @return Collection of the FieldLayouts.
     */
    public abstract Collection<FieldLayout> getFieldLayouts();

    /**
     * Returns a string that represents a "unique" identifier for this bulke edit bean
     *
     * @return unique key formed from projects, issue types, target project if a subtask only bulkeditbean and size of the bean
     */
    public abstract String getKey();

    /**
     * returns a list of project ids for projects which the currently selected issues belong to.
     *
     * @return A list of project ids for projects which the currently selected issues belong to.
     */
    public abstract Collection<Long> getProjectIds();

    public abstract Collection<GenericValue> getProjects();

    public abstract Collection<String> getIssueTypes();

    public abstract Collection<IssueType> getIssueTypeObjects();

    public abstract String getCheckboxName(final Issue issue);

    public abstract CustomField getCustomField(final String customFieldKey) throws GenericEntityException;

    public abstract String getCustomFieldView(final CustomField customField) throws FieldValidationException;

    public abstract void setParams(final Map<String, ?> params);

    public abstract Map<String, ?> getParams();

    public abstract void setIssuesInUse(final Collection<?> issuesInUse);

    public abstract void addAvailablePreviousStep(final int stepId);

    public abstract void clearAvailablePreviousSteps();

    public abstract boolean isAvailablePreviousStep(final int stepId);

    /**
     * Check if a mail server has been specified.
     *
     * @return boolean  true if a mail server has been specified
     */
    public abstract boolean isHasMailServer();

    ///// ------------------------ Move Issues ------------------------ /////
    public abstract Collection<?> getMoveFieldLayoutItems();

    public abstract void setMoveFieldLayoutItems(final Collection<?> moveFieldLayoutItems);

    public abstract Long getTargetPid();

    public abstract void setTargetProject(final GenericValue project);

    public abstract Project getTargetProject();

    public abstract GenericValue getTargetProjectGV();

    public abstract void setTargetIssueTypeId(final String id);

    public abstract String getTargetIssueTypeId();

    public abstract GenericValue getTargetIssueTypeGV();

    public abstract IssueType getTargetIssueTypeObject();

    // Retrieve the target statuses from the params
    public abstract void populateStatusHolder() throws WorkflowException;

    /**
     * Gets a set of invalid statuses that are not valid in the destination workflow
     *
     * @return Set of {@link GenericValue} objects
     * @throws WorkflowException
     */
    public abstract Collection<GenericValue> getInvalidStatuses() throws WorkflowException;

    /**
     * Gets issues whose status is null
     *
     * @return Set of {@link Issue} objects. Emoty Set if no invalid issues
     * @throws WorkflowException
     */
    public abstract Set<Issue> getInvalidIssues() throws WorkflowException;

    // Retrieve a collection of sub-task types that are associated with an invalid status in the target context
    public abstract Set<String> getInvalidSubTaskTypes() throws WorkflowException;

    // Retrieve collection of invalid statuses associated with the specified subtask type.
    // Collection retireved from Map: (Key); SubTask Type ID -> (Value); Collection of Invalid SubTask Status IDs
    public abstract Set<String> getInvalidSubTaskStatusesByType(final String subTaskTypeId) throws WorkflowException;

    /**
     * Sets the targetFieldLayout to the appropriate FieldLayout (aka "Field Configuration"); for the target Project and
     * Issue Type.
     */
    public abstract void setTargetFieldLayout();

    public abstract FieldLayout getTargetFieldLayout();

    public abstract FieldLayout getTargetFieldLayoutForType(final String targetTypeId);

    public abstract JiraWorkflow getTargetWorkflow() throws WorkflowException;

    /**
     * This method is used to get a target issue that will provide the correct context (i.e. project and issue type);,
     * for where you are moving to. The object returned is not mapped to a specific selected issue.
     *
     * @return an issue whose project and issue type are of where the you are moving to.
     */
    public abstract Issue getFirstTargetIssueObject();

    public abstract Map<Issue, Issue> getTargetIssueObjects();

    public abstract void setTargetIssueObjects(final Map<Issue, Issue> targetIssueObjects);

    /**
     * This is a convinience method for converting the list of objects to a list of GenericValues
     *
     * @return list of GenericValue issue objects
     */
    public abstract List<GenericValue> getTargetIssueGVs();

    public abstract GenericValue getTargetStatus(final Issue issue);

    public abstract Map<String, String> getStatusMapHolder();

    public abstract Collection<?> getRemovedFields();

    public abstract void setRemovedFields(final Set<?> removedFields);

    public abstract void resetMoveData();

    // Retrieve the sub task target statuses from the params
    // The subtaskStatusHolder contains a collection of strings constructd as follows:
    // subtaskstatusinfo_subtasktype_originalstatusid_targetstatusid
    public abstract void populateSubTaskStatusHolder() throws WorkflowException;

    // Retrieve the workflow associated with the sub-task in the target project
    public abstract JiraWorkflow getTargetWorkflowByType(final String issueTypeId) throws WorkflowException;

    // -------------------------------------------------------------------------------------- Basic accessors & mutators
    public abstract Collection<String> getSubTaskStatusHolder();

    public abstract boolean isRetainChecked(final String fieldId);

    public abstract boolean isSubTaskCollection();

    public abstract boolean isSubTaskOnly();

    public abstract String getOperationName();

    public abstract void setOperationName(final String operationName);

    public abstract void setInvalidSubTaskStatusesByType(final Map<String, Set<String>> invalidSubTaskStatusesByType);

    public abstract void setInvalidSubTaskTypes(final Set<String> invalidSubTaskTypes);

    public abstract int getInvalidSubTaskCount();

    public abstract Set<String> getRetainValues();

    public abstract void setRetainValues(final Set<String> retainValues);

    public abstract void addRetainValue(final String fieldId);

    public abstract List<Issue> getSubTaskOfSelectedIssues();

    public abstract void setSubTaskOfSelectedIssues(final List<Issue> subTaskOfSelectedIssues);

    public abstract List<Issue> getIssuesFromSearchRequest();

    public abstract void setIssuesFromSearchRequest(final List<Issue> issuesFromSearchRequest);

    public abstract int getCurrentStep();

    public abstract void setCurrentStep(final int currentStep);

    public abstract Map<String, BulkEditAction> getActions();

    public abstract void setActions(final Map<String, BulkEditAction> actions);

    public abstract Map<String, Object> getFieldValues();

    public abstract Map<String, Object> getFieldValuesHolder();

    public abstract IssueOperation getIssueOperation();

    public abstract Collection<?> getIssuesInUse();

    public abstract BulkEditBean getSubTaskBulkEditBean();

    public abstract void setSubTaskBulkEditBean(final BulkEditBean subTaskBulkEditBean);

    public abstract MultiBulkMoveBean getRelatedMultiBulkMoveBean();

    public abstract void setRelatedMultiBulkMoveBean(final MultiBulkMoveBean relatedMultiBulkMoveBean);

    public abstract boolean isSendBulkNotification();

    public abstract void setSendBulkNotification(final boolean sendBulkNotification);

    // -------------------------------------------------------------------------------- Bulk Workflow Tranistion Methods

    public abstract MultiMap getWorkflowTransitionMap();

    public abstract void setWorkflowTransitionMap(final MultiMap workflowTransitionMap);

    public abstract Set<String> getWorkflowsInUse();

    public abstract List<WorkflowTransitionKey> getTransitionIdsForWorkflow(final String workflowName);

    public abstract String getTransitionName(final String workflowName, final String actionDescriptorId);

    public abstract List<String> getTransitionIssueKeys(final WorkflowTransitionKey workflowTransitionKey);

    public abstract void setSelectedWFTransitionKey(final WorkflowTransitionKey workflowTransitionKey);

    public abstract WorkflowTransitionKey getSelectedWFTransitionKey();

    public abstract void resetWorkflowTransitionSelection();

    public abstract boolean isTransitionChecked(final WorkflowTransitionKey workflowTransitionKey);

    public abstract String getSelectedTransitionName();

    public abstract void setFieldScreenRenderer(final FieldScreenRenderer fieldScreenRenderer);

    public abstract FieldScreenRenderer getFieldScreenRenderer();

    public abstract Map<?, ?> getMessagedFieldLayoutItems();

    public abstract void setMessagedFieldLayoutItems(final Map<?, ?> messagedFieldLayoutItems);

    public abstract void initMultiBulkBean();

    public abstract void initMultiBulkBeanWithSubTasks();

    public abstract boolean isOnlyContainsSubTasks();

    // ------------------------------------------------------------------------------------------ Static Session Storage

    @SuppressWarnings("unchecked")
    public static void storeToSession(final BulkEditBean bulkEditBean)
    {
        ActionContext.getSession().put(SessionKeys.BULKEDITBEAN, bulkEditBean);
    }

    public static BulkEditBean getFromSession()
    {
        return (BulkEditBean) ActionContext.getSession().get(SessionKeys.BULKEDITBEAN);
    }

    public static void removeFromSession()
    {
        ActionContext.getSession().remove(SessionKeys.BULKEDITBEAN);
    }

    /**
     * If this BulkEditBean contains subtasks of another BulkEditBean, then we can set a pointer back to
     * the BulkEditBean containing the parent issues.
     * This is used so that the subtask issues have access to the <em>new</em> values in their parent issues.
     * See JRA-13937 where we had to ensure that the subtasks in a Bulk Move could get to the new Security Level of
     * their parents.
     *
     * @param parentBulkEditBean The BulkEditBean that contains parent issues of the issues (subtasks); in this BulkEditBean.
     */
    public abstract void setParentBulkEditBean(final BulkEditBean parentBulkEditBean);

    /**
     * If there is a limit on the number of issues that can be bulk edited, this will return that number,
     * otherwise -1.
     *
     * @return -1 to indicate no limit on bulk editing issues, otherwise the number of the limit.
     */
    public abstract int getMaxIssues();

    /**
     * Sets the maximum number of issues allowed to be bulk edited at once. Use -1 to indicate no limit.
     *
     * @param maxIssues either -1 or a positive integer representing the maximum number of issues allowed for bulk edit.
     */
    public abstract void setMaxIssues(final int maxIssues);

    public abstract Map<String, Map<Long, Long>> getFieldSubstitutionMap();
}
