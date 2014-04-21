/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.issue;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.jira.bc.issue.comment.CommentService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.config.util.AttachmentPathManager;
import com.atlassian.jira.event.issue.IssueEventDispatcher;
import com.atlassian.jira.event.type.EventType;
import com.atlassian.jira.issue.AttachmentManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFactory;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.IssueVerifier;
import com.atlassian.jira.issue.ModifiedValue;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.attachment.Attachment;
import com.atlassian.jira.issue.customfields.impl.FieldValidationException;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.Field;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.OrderableField;
import com.atlassian.jira.issue.fields.layout.field.FieldLayout;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.fields.screen.FieldScreenRendererFactory;
import com.atlassian.jira.issue.history.ChangeItemBean;
import com.atlassian.jira.issue.history.ChangeLogUtils;
import com.atlassian.jira.issue.link.IssueLinkManager;
import com.atlassian.jira.issue.security.IssueSecurityHelper;
import com.atlassian.jira.issue.util.DefaultIssueChangeHolder;
import com.atlassian.jira.issue.util.IssueChangeHolder;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.transaction.Transaction;
import com.atlassian.jira.transaction.Txn;
import com.atlassian.jira.util.AttachmentUtils;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowException;
import com.atlassian.jira.workflow.WorkflowManager;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;
import org.ofbiz.core.util.UtilDateTime;

import java.io.File;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class MoveIssueConfirm extends MoveIssueUpdateFields
{
    boolean confirm = false;
    private final AttachmentManager attachmentManager;
    private final IssueManager issueManager;
    private final AttachmentPathManager attachmentPathManager;

    public MoveIssueConfirm(IssueLinkManager issueLinkManager, SubTaskManager subTaskManager,
            AttachmentManager attachmentManager, ConstantsManager constantsManager,
            WorkflowManager workflowManager, FieldManager fieldManager,
            FieldLayoutManager fieldLayoutmanager, IssueFactory issueFactory,
            FieldScreenRendererFactory fieldScreenRendererFactory, CommentService commentService,
            IssueSecurityHelper issueSecurityHelper, IssueManager issueManager, final AttachmentPathManager attachmentPathManager)
    {
        super(issueLinkManager, subTaskManager, constantsManager, workflowManager, fieldManager, fieldLayoutmanager,
                issueFactory, fieldScreenRendererFactory, commentService, issueSecurityHelper);
        this.attachmentManager = attachmentManager;
        this.issueManager = issueManager;
        this.attachmentPathManager = attachmentPathManager;
    }

    public String doDefault()
    {
        if (getMoveIssueBean() == null)
        {
            return redirectToSessionTimeoutPage();
        }

        // Sub task move does not have status selection step
        if (isSubTask())
        {
            getMoveIssueBean().setCurrentStep(3);
            getMoveIssueBean().addAvailablePreviousStep(2);
        }
        else
        {
            getMoveIssueBean().setCurrentStep(4);
            getMoveIssueBean().addAvailablePreviousStep(3);
        }

        return INPUT;
    }

    public Collection getConfimationFieldLayoutItems()
    {
        return getMoveIssueBean().getMoveFieldLayoutItems();
    }

    public Collection getRemoveFields()
    {
        return getMoveIssueBean().getRemovedFields();
    }

    public String getOldViewHtml(OrderableField field)
    {
        FieldLayout fieldLayout = fieldLayoutManager.getFieldLayout(getIssue());
        FieldLayoutItem fieldLayoutItem = fieldLayout.getFieldLayoutItem(field);

        final Map<String, Object> displayParams = MapBuilder.<String, Object>newBuilder("nolink", Boolean.TRUE)
                .add("readonly", Boolean.TRUE)
                .add("prefix", "old_").toMutableMap();

        return field.getViewHtml(fieldLayoutItem, this, getIssueObject(getIssue()), displayParams);
    }

    public String getNewViewHtml(OrderableField field)
    {
        MutableIssue updatedIssue = getMoveIssueBean().getUpdatedIssue();
        FieldLayout fieldLayout = fieldLayoutManager.getFieldLayout(updatedIssue.getProject(), updatedIssue.getIssueTypeObject().getId());
        FieldLayoutItem fieldLayoutItem = fieldLayout.getFieldLayoutItem(field);

        final Map<String, Object> displayParams = MapBuilder.<String, Object>newBuilder("nolink", Boolean.TRUE)
                .add("readonly", Boolean.TRUE)
                .add("prefix", "new_").toMutableMap();

        return field.getViewHtml(fieldLayoutItem, this, updatedIssue, displayParams);
    }

    protected void doValidation()
    {

        if (getMoveIssueBean() != null)
        {
            //JRA-11605: Do not call super.doValidation() here.  This will cause the Issue security level to
            //be set to null, which will possibly cause problems when detecting the field has been modified.
            // Validation is not necessary in this step as it has been carried out by the previous MoveIssueUpdateFields
            // action already.

            try
            {
                validateAttachmentMove();
                validateCreateIssue();
            }
            catch (GenericEntityException e)
            {
                log.error("Error occurred while moving issue.", e);
                addErrorMessage(getText("moveissue.error.attachment"));
            }

            if (!isConfirm())
            {
                addErrorMessage(getText("admin.errors.error.occured.moving.issue"));
            }
        }
    }

    protected void popluateDefault(OrderableField orderableField)
    {
        // Override the parent method to do nothing - the field values holder should be already populated
    }

    protected void populateFromParams(OrderableField orderableField)
    {
        // Override the parent method to do nothing - the field values holder should be already populated
    }

    protected MutableIssue getTargetIssueObject()
    {
        return getMoveIssueBean().getUpdatedIssue();
    }

    /**
     * Actually does the moving of the issue from one Project/Issue Type to another
     */
    @RequiresXsrfCheck
    public String doExecute() throws Exception
    {
        if (getMoveIssueBean() == null)
        {
            return redirectToSessionTimeoutPage();
        }

        MutableIssue originalIssueObject =
                getIssueManager().getIssueObject(getMoveIssueBean().getUpdatedIssue().getId());
        MutableIssue updatedIssueObject = getMoveIssueBean().getUpdatedIssue();
        // The Updated Issue object will contain the new project ID, but it will still contain the old Issue Key.
        // We check if anyone else has moved the Issue in the meantime by comparing the original issue Key to the current Issue Key
        if (!originalIssueObject.getKey().equals(updatedIssueObject.getKey()))
        {
            addErrorMessage(getText("moveissue.error.already.moved", updatedIssueObject.getKey(), originalIssueObject.getKey()));
            return ERROR;
        }

        String newKey = null;
        String originalWfId = null;

        if (updatedIssueObject.getWorkflowId() != null)
        {
            originalWfId = updatedIssueObject.getWorkflowId().toString();
        }

        // Verify integrity of issue
        IssueVerifier issueVerifier = new IssueVerifier();
        Map workflowMigrationMapping = new HashMap();
        workflowMigrationMapping.put(updatedIssueObject.getStatusObject().getId(), getTargetStatusId());
        // Validate the current workflow state to ensure that the issue can be moven properly
        ErrorCollection errorCollection = issueVerifier.verifyIssue(originalIssueObject.getGenericValue(), workflowMigrationMapping, true);

        // Verify integrity of subtasks
        if (!updatedIssueObject.isSubTask() && !updatedIssueObject.getSubTaskObjects().isEmpty())
        {
            Collection subTasks = updatedIssueObject.getSubTasks();
            for (Iterator iterator = subTasks.iterator(); iterator.hasNext(); )
            {
                GenericValue subtask = (GenericValue) iterator.next();
                workflowMigrationMapping.clear();

                // Task Target Status will remain the same if it exists in the target workflow
                if (isTaskStatusValid(subtask.getString("type"), subtask.getString("status")))
                {
                    workflowMigrationMapping.put(subtask.getString("status"), subtask.getString("status"));
                }
                else
                {
                    // Retrieve target status of subtask in new workflow from moveissuebean
                    final String key = getPrefixIssueTypeId(subtask.getString("type"));
                    String newIssueType = (String) getMoveIssueBean().getFieldValuesHolder().get(key);
                    // newIssueType will be null if they didn't migrate to a new issue type
                    if (newIssueType == null)
                    {
                        newIssueType = subtask.getString("type");
                    }
                    String subTaskTypeKey = getPrefixTaskStatusId(newIssueType, subtask.getString("status"));
                    Map taskTargetStatusMap = getMoveIssueBean().getTaskTargetStatusHolder();
                    workflowMigrationMapping.put(subtask.getString("status"), taskTargetStatusMap.get(subTaskTypeKey));
                }

                // Validate the current workflow state to ensure that the issue can be moven properly
                errorCollection.addErrorCollection(issueVerifier.verifyIssue(subtask, workflowMigrationMapping, true));
            }
        }

        if (errorCollection != null && errorCollection.hasAnyErrors())
        {
            // Do not complete the migration
            addErrorCollection(errorCollection);
            return "workflowmigrationerror";
        }


        Transaction txn = Txn.begin();

        // Move attachments if we are moving to a new project
        if (!(getProject().equals(getTargetProject())))
        {
            newKey = getNewKey();
            moveAttachments(updatedIssueObject.getId(), newKey);
        }

        try
        {
            moveIssueInTxn(txn, originalIssueObject, updatedIssueObject, newKey, originalWfId);
        }
        finally
        {
            txn.finallyRollbackIfNotCommitted();
        }


        // Move subtasks to target and create changelog
        if (!updatedIssueObject.isSubTask() && !updatedIssueObject.getSubTaskObjects().isEmpty())
        {
            moveSubTasks(updatedIssueObject);
        }

        return getRedirect("/browse/" + getKey());
    }

    private void moveIssueInTxn(Transaction txn, MutableIssue originalIssueObject, MutableIssue updatedIssueObject, String newKey, String originalWfId)
            throws GenericEntityException
    {
        // Only migrate the issue if the target workflow is different from the current workflow
        if (!isWorkflowMatch(getCurrentIssueType(), getTargetIssueType()))
        {
            migrateIssueToWorkflow(updatedIssueObject.getGenericValue(), getIssue().getString("type"), getWorkflowForType(getTargetPid(), getTargetIssueType()), getTargetStatusGV());
        }

        // Log and set new details for target
        updatedIssueObject.setUpdated(new Timestamp(System.currentTimeMillis()));
        IssueChangeHolder issueChangeHolder = moveIssueDetails(updatedIssueObject, newKey, originalWfId);

        //create and store the changelog for this whole process
        GenericValue updateLog = ChangeLogUtils.createChangeGroup(getRemoteUser(), originalIssueObject.getGenericValue(),
                updatedIssueObject.getGenericValue(), issueChangeHolder.getChangeItems(), false);

        txn.commit();

        issueUpdate(updatedIssueObject, updateLog, issueChangeHolder);
    }

    // ---- Move issue details methods ----
    // Store issue, update statistics, flush caches and generate relevant event
    private void issueUpdate(Issue newIssue, GenericValue updateLog, IssueChangeHolder issueChangeHolder)
            throws GenericEntityException
    {
        if (updateLog != null && !issueChangeHolder.getChangeItems().isEmpty())
        {
            //dispatch the event - moving a subtask is actually an update event rather than a move event
            Long eventTypeId;
            if (isSubTask() || getProject().equals(getTargetProject()))
            {
                eventTypeId = EventType.ISSUE_UPDATED_ID;
            }
            else
            {
                eventTypeId = EventType.ISSUE_MOVED_ID;
            }
            IssueEventDispatcher.dispatchEvent(eventTypeId, newIssue, getRemoteUser(), updateLog, true,
                    issueChangeHolder.isSubtasksUpdated());
        }
    }

    // Create change log items for all new details - set details of "moved" issue also
    private IssueChangeHolder moveIssueDetails(MutableIssue newIssue, String newKey, String originalWfId)
            throws GenericEntityException, WorkflowException
    {
        IssueChangeHolder changeHolder = new DefaultIssueChangeHolder();

        GenericValue currentIssueTypeGV = getConstantsManager().getIssueType(getCurrentIssueType());
        GenericValue targetIssueTypeGV = getConstantsManager().getIssueType(getTargetIssueType());

        // Set new project and issue key - issue key only changes if issue is moving to new project
        if (!(getProject().equals(getTargetProject())))
        {
            changeHolder.addChangeItem(new ChangeItemBean(ChangeItemBean.STATIC_FIELD, "Project", getProject().getLong("id").toString(), getProject().getString("name"), getTargetProject().getLong("id").toString(), getTargetProject().getString("name")));
            newIssue.setProject(getTargetProject());

            if (newKey == null)
            {
                throw new IllegalArgumentException("New issue key should not be null when moving between projects!");
            }

            changeHolder.addChangeItem(new ChangeItemBean(ChangeItemBean.STATIC_FIELD, "Key", null, newIssue.getString("key"), null, newKey));
            newIssue.setKey(newKey);
        }

        // Check if issue type is changing
        if (isSubTask() || !getCurrentIssueType().equals(getTargetIssueType()))
        {
            changeHolder.addChangeItem(new ChangeItemBean(ChangeItemBean.STATIC_FIELD, IssueFieldConstants.ISSUE_TYPE, currentIssueTypeGV.getString("id"), currentIssueTypeGV.getString("name"), targetIssueTypeGV.getString("id"), targetIssueTypeGV.getString("name")));
            newIssue.setIssueType(getTargetIssueTypeGV());
        }

        // Only log a workflow/status change if the target workflow is different from the current workflow
        if (!isWorkflowMatch(getCurrentIssueType(), getTargetIssueType()))
        {
            changeHolder.addChangeItem(new ChangeItemBean(ChangeItemBean.STATIC_FIELD, "Workflow", originalWfId, getWorkflowManager().getWorkflow(getIssue()).getName(), newIssue.getLong("workflowId").toString(), getWorkflowForType(getTargetPid(), getTargetIssueType()).getName()));

            if (!isStatusMatch())
            {
                changeHolder.addChangeItem(new ChangeItemBean(ChangeItemBean.STATIC_FIELD, IssueFieldConstants.STATUS, getCurrentStatusGV().getString("id"), getCurrentStatusGV().getString("name"), getTargetStatusGV().getString("id"), getTargetStatusGV().getString("name")));
            }
        }

        // Store the issue
        newIssue.store();

        // Maybe move this code to the issue.store() method
        Map<String, ModifiedValue> modifiedFields = newIssue.getModifiedFields();
        for (final String fieldId : modifiedFields.keySet())
        {
            if (getFieldManager().isOrderableField(fieldId))
            {
                OrderableField field = getFieldManager().getOrderableField(fieldId);
                FieldLayoutItem fieldLayoutItem = ComponentAccessor.getFieldLayoutManager().getFieldLayout(newIssue.getGenericValue()).getFieldLayoutItem(field);
                field.updateValue(fieldLayoutItem, newIssue, modifiedFields.get(fieldId), changeHolder);

                // This code will only become necessary if the following fields become editable during a move operation.
                // Further work is required in order to trackback analyse these fields
                /*if (IssueFieldConstants.SUMMARY.equals(fieldId) || IssueFieldConstants.DESCRIPTION.equals(fieldId) ||
                        IssueFieldConstants.ENVIRONMENT.equals(fieldId))
                {
                    modifiedText.append(modifiedFields.get(fieldId)).append(" ");
                }*/
            }
        }
        // Reset the fields as they all have been persisted to the db. Maybe move this code to the "createValue"
        // method of the issue, so that the fiels removes itself from the modified list as soon as it is persisted.
        newIssue.resetModifiedFields();

        return changeHolder;
    }

    // ---- Move subtask details methods ----

    /**
     * Move subtasks to new project - setting status, target custom fields, removing non-applicable current custom
     * fields and creating a changelog.
     *
     * @param parentIssue Parent issue.
     * @throws GenericEntityException GenericEntityException
     * @throws FieldValidationException FieldValidationException
     * @throws WorkflowException WorkflowException
     */
    private void moveSubTasks(Issue parentIssue)
            throws GenericEntityException, FieldValidationException, WorkflowException
    {
        Collection subTasks = parentIssue.getSubTaskObjects();

        for (Iterator iterator = subTasks.iterator(); iterator.hasNext(); )
        {
            Issue originalSubTask = (Issue) iterator.next();
            MutableIssue targetSubTask = getIssueManager().getIssueObject(originalSubTask.getId());

            List subTaskChangeItems = new ArrayList();
            String newKey = null;

            // first update to a new issue type if necessary
            final String issueTypeKey = getPrefixIssueTypeId(originalSubTask.getIssueTypeObject().getId());
            if (getMoveIssueBean().getFieldValuesHolder().containsKey(issueTypeKey))
            {
                final String newIssueType = (String) getMoveIssueBean().getFieldValuesHolder().get(issueTypeKey);
                targetSubTask.setIssueTypeId(newIssueType);
            }

            Transaction txn = Txn.begin();

            // Move attachments if we are moving to a new project
            if (!(getProject().equals(getTargetProject())))
            {
                newKey = getNewKey();
                moveAttachments(originalSubTask.getId(), newKey);
            }

            GenericValue subTaskUpdateLog = null;
            try
            {
                // Migrate the subtask to the new workflow/status if necessary
                if (!isWorkflowMatch(originalSubTask.getIssueTypeObject().getId(), targetSubTask.getIssueTypeObject().getId()))
                {
                    List subTaskMigrationItems = migrateSubTask(originalSubTask, targetSubTask);
                    subTaskChangeItems.addAll(subTaskMigrationItems);
                }
                // Set and log subtask details.
                // Note that we can ignore the "subTasks changed" flag here, as we are only dealing with subtasks, not parents.
                List subTaskDetails = moveSubTaskDetails(originalSubTask, targetSubTask, newKey);
                subTaskChangeItems.addAll(subTaskDetails);

                //create and store the changelog for this whole process
                subTaskUpdateLog = ChangeLogUtils.createChangeGroup(getLoggedInUser(), originalSubTask, targetSubTask, subTaskChangeItems, false);

                if (subTaskUpdateLog != null && !subTaskChangeItems.isEmpty())
                {
                    targetSubTask.setUpdated(UtilDateTime.nowTimestamp());
                    //update the issue in the database
                    targetSubTask.store();
                }

                txn.commit();
            }
            finally
            {
                txn.finallyRollbackIfNotCommitted();
            }

            subTaskUpdate(targetSubTask, subTaskUpdateLog, subTaskChangeItems);
        }
    }

    // Store subtask, update statistics, flush caches and generate relevant event
    private void subTaskUpdate(MutableIssue subTask, GenericValue subTaskUpdateLog, List subTaskChangeItems)
            throws GenericEntityException
    {
        if (subTaskUpdateLog != null && !subTaskChangeItems.isEmpty())
        {
            if (!(getProject().equals(getTargetProject())))
            {
                IssueEventDispatcher.dispatchEvent(EventType.ISSUE_MOVED_ID, subTask, getRemoteUser(), null, null, subTaskUpdateLog);
            }
            else
            {
                IssueEventDispatcher.dispatchEvent(EventType.ISSUE_UPDATED_ID, subTask, getRemoteUser(), null, null, subTaskUpdateLog);
            }
        }
    }

    // Move sub task details to target
    private List moveSubTaskDetails(Issue originalSubTask, MutableIssue targetSubTask, String newKey)
    {
        IssueChangeHolder subTaskChangeHolder = new DefaultIssueChangeHolder();

        // Change only if target project is different from current project
        if (!(getProject().equals(getTargetProject())))
        {
            if (newKey == null)
            {
                throw new IllegalArgumentException("New issue key should not be null when moving between projects!");
            }

            subTaskChangeHolder.addChangeItem(new ChangeItemBean(ChangeItemBean.STATIC_FIELD, "Project", getProject().getLong("id").toString(), getProject().getString("name"), getTargetProject().getLong("id").toString(), getTargetProject().getString("name")));
            subTaskChangeHolder.addChangeItem(new ChangeItemBean(ChangeItemBean.STATIC_FIELD, "Key", null, originalSubTask.getKey(), null, newKey));

            targetSubTask.setProject(getTargetProject());
            targetSubTask.setKey(newKey);

            FieldLayout targetFieldLayout = getTargetFieldLayout(targetSubTask);
            Map fieldValuesHolder = new HashMap();
            List targetIssueTypeIdList = EasyList.build(targetSubTask.getIssueType().getString("id"));
            for (Iterator iterator = targetFieldLayout.getVisibleLayoutItems(getRemoteUser(), getTargetProjectObj(), targetIssueTypeIdList).iterator(); iterator.hasNext(); )
            {
                FieldLayoutItem fieldLayoutItem = (FieldLayoutItem) iterator.next();
                OrderableField orderableField = fieldLayoutItem.getOrderableField();
                // Security is always set to the same value as the parent's issue - so no need to process it here
                if (!IssueFieldConstants.SECURITY.equals(orderableField.getId()) && !IssueFieldConstants.ISSUE_TYPE.equals(orderableField.getId()))
                {
                    if (orderableField.needsMove(EasyList.build(originalSubTask), targetSubTask, fieldLayoutItem).getResult())
                    {
                        // For every field that needs to be updated for the move, populate the default value and save the issue
                        orderableField.populateDefaults(fieldValuesHolder, targetSubTask);
                        orderableField.updateIssue(fieldLayoutItem, targetSubTask, fieldValuesHolder);
                    }
                }
            }

            // Remove all the hidden fields
            for (Iterator iterator = targetFieldLayout.getHiddenFields(getRemoteUser(), getTargetProject(), targetIssueTypeIdList).iterator(); iterator.hasNext(); )
            {
                Field field = (Field) iterator.next();
                if (getFieldManager().isOrderableField(field))
                {
                    OrderableField orderableField = (OrderableField) field;
                    // Remove values of all the fields that have a value but are hidden in the target project
                    if (orderableField.hasValue(targetSubTask)
                            && orderableField.canRemoveValueFromIssueObject(targetSubTask))
                    {
                        orderableField.removeValueFromIssueObject(targetSubTask);
                    }
                }
            }

            // Store the issue
            targetSubTask.store();

            // Maybe move this code to the issue.store() method
            Map<String, ModifiedValue> modifiedFields = targetSubTask.getModifiedFields();
            for (final String fieldId : modifiedFields.keySet())
            {
                if (getFieldManager().isOrderableField(fieldId))
                {
                    OrderableField field = getFieldManager().getOrderableField(fieldId);
                    FieldLayoutItem fieldLayoutItem = ComponentAccessor.getFieldLayoutManager().getFieldLayout(targetSubTask.getGenericValue()).getFieldLayoutItem(field);
                    field.updateValue(fieldLayoutItem, targetSubTask, modifiedFields.get(fieldId), subTaskChangeHolder);

                    // This code will only become necessary if the following fields become editable during a move operation.
                    // Further work is required in order to trackback analyse these fields
                    /*if (IssueFieldConstants.SUMMARY.equals(fieldId) || IssueFieldConstants.DESCRIPTION.equals(fieldId) ||
                            IssueFieldConstants.ENVIRONMENT.equals(fieldId))
                    {
                      modifiedText.append(modifiedFields.get(fieldId)).append(" ");
                    }*/
                }
            }
            // Reset the fields as they all have been persisted to the db. Maybe move this code to the "createValue"
            // method of the issue, so that the fiels removes itself from the modified list as soon as it is persisted.
            targetSubTask.resetModifiedFields();
        }

        return subTaskChangeHolder.getChangeItems();
    }

    private FieldLayout getTargetFieldLayout(Issue targetSubTask)
    {
        return getFieldLayoutManager().getFieldLayout(getTargetProject(), targetSubTask.getIssueType().getString("id"));
    }

    // Migrate the subtasks associated with the issue that is moving
    private List migrateSubTask(final Issue originalSubtask, final MutableIssue targetSubtask)
            throws GenericEntityException
    {
        ArrayList subTaskChangeItems = new ArrayList();

        // update the target status if the original status is not valid in the target workflow
        if (!isTaskStatusValid(targetSubtask.getIssueTypeObject().getId(), targetSubtask.getStatusObject().getId()))
        {
            // Retrieve target status of subtask in new workflow from moveissuebean
            String subTaskTypeKey = getPrefixTaskStatusId(targetSubtask.getIssueTypeObject().getId(), targetSubtask.getStatusObject().getId());
            Map taskTargetStatusMap = getMoveIssueBean().getTaskTargetStatusHolder();
            targetSubtask.setStatusId((String) taskTargetStatusMap.get(subTaskTypeKey));
            subTaskChangeItems.add(new ChangeItemBean(ChangeItemBean.STATIC_FIELD, IssueFieldConstants.STATUS, originalSubtask.getStatusObject().getId(), originalSubtask.getStatusObject().getName(),
                    targetSubtask.getStatusObject().getId(), targetSubtask.getStatusObject().getName()));
        }

        // Migrate the subtask to the new status in the target workflow and create a changelog
        if (!isWorkflowMatch(originalSubtask.getIssueTypeObject().getId(), targetSubtask.getIssueTypeObject().getId()))
        {
            migrateIssueToWorkflow(targetSubtask.getGenericValue(), originalSubtask.getIssueTypeObject().getId(), getWorkflowForType(getTargetPid(), targetSubtask.getIssueTypeObject().getId()), targetSubtask.getStatusObject().getGenericValue());
            subTaskChangeItems.add(new ChangeItemBean(ChangeItemBean.STATIC_FIELD, "Workflow",
                    originalSubtask.getWorkflowId().toString(), getWorkflowManager().getWorkflow(originalSubtask.getGenericValue()).getName(),
                    targetSubtask.getWorkflowId().toString(), getWorkflowForType(getTargetPid(), targetSubtask.getIssueTypeObject().getId()).getName()));
        }
        return subTaskChangeItems;
    }

    /**
     * Migrate the specified issue to the specified workflow, specified status and target type.
     *
     * @param issue - the issue to migrate - should be the issue that will be changed
     * @param oldIssueType - the old issue type of the issue
     * @param targetWorkflow - the destination workflow
     * @param targetStatus - the destination status
     */
    protected void migrateIssueToWorkflow(GenericValue issue, String oldIssueType, JiraWorkflow targetWorkflow,
            GenericValue targetStatus)
            throws GenericEntityException
    {
        // Do not move if current worklfow is the same as the target workflow
        if (!isWorkflowMatch(oldIssueType, issue.getString("type")))
        {
            getWorkflowManager().migrateIssueToWorkflow(issue, targetWorkflow, targetStatus);
        }
    }

    /**
     * Create a new directory for this issue, and move all the attachments from the old directory to the new directory.
     * <p/>
     * NB - this will fail if the old directory and new directory are on different filesystems as {@link File#renameTo}
     * fails across filesystems.
     *
     * @param newIssueKey The key of the new issue.
     */
    private void moveAttachments(Long issueId, String newIssueKey)
    {
        // JRA-15475: reload the issue here to ensure we get the latest state before trying to move attachments.  When
        // moving attachments it's important to get the source location right otherwise the attachment may 'disappear'.
        // The source location could be wrong if another move happens moving the same issue as this one
        // which changes the source issue's key and thus the source file path.
        // Note: This isn't really a 100% fix since another move could still enter after the issue has been loaded from the DB here and before
        // we move the attachments, but it's a lot less likely to happen.
        // Also note that there is now validation at the beginning of the doExecute() method to fail the move operation
        // if the Issue was already moved by then.
        final Issue issue = issueManager.getIssueObject(issueId);
        final String fSep = System.getProperty("file.separator");
        List<Attachment> attachments = attachmentManager.getAttachments(issue);
        for (final Attachment attachment : attachments)
        {
            final File attachmentFile = AttachmentUtils.getAttachmentFile(attachment);
            final String fileName = attachmentFile.getName();
            final String originalFilePath = attachmentFile.getAbsolutePath();
            log.debug("Attachment: " + originalFilePath);

            String targetDirectoryPath = attachmentPathManager.getAttachmentPath() + fSep +
                    getTargetProject().getString("key") + fSep + newIssueKey;
            log.debug("Attachment: " + targetDirectoryPath + fSep + fileName);

            if (attachmentFile.exists())
            {
                File destDirectory = new File(targetDirectoryPath);
                destDirectory.mkdirs();
                attachmentFile.renameTo(new File(destDirectory, fileName));
            }
            else
            {
                log.warn("Could not move the attachment '" + attachment.getFilename() + "' because it does not exist at the expected location '" + originalFilePath + "'.");
            }
        }
    }

    // ---- Helper Methods ----
    // Return next available issue key in target project
    private String getNewKey()
    {
        long incCount = getProjectManager().getNextId(getTargetProject());
        return getTargetProject().getString("key") + "-" + incCount;
    }

    // Return the custom field id with the custom field prefix
    public String getPrefixCustomFieldId(String key) throws GenericEntityException
    {
        Collection targetCustomFields = getTargetCustomFieldObjects(getTargetIssueType());

        for (Iterator iterator = targetCustomFields.iterator(); iterator.hasNext(); )
        {
            CustomField targetCustomField = (CustomField) iterator.next();

            if (key.equals(targetCustomField.getName()))
            {
                return targetCustomField.getId();
            }
        }
        return null;
    }

    public boolean isConfirm()
    {
        return confirm;
    }

    public void setConfirm(boolean confirm)
    {
        this.confirm = confirm;
    }

    public GenericValue getTargetStatusGV()
    {
        return getConstantsManager().getStatus(getTargetStatusId());
    }

    // ---- Checks for changes made ----
    public boolean isIssueTypeMatch() throws GenericEntityException
    {
        return getCurrentIssueType().equals(getTargetIssueType());
    }

    public boolean isProjectMatch() throws GenericEntityException
    {
        return getProject().equals(getTargetProject());
    }

    public boolean isStatusMatch()
    {
        return getCurrentStatusGV().equals(getTargetStatusGV());
    }
}
