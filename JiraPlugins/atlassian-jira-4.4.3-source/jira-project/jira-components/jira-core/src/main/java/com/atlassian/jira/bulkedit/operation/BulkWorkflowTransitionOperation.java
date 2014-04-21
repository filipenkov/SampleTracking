/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.bulkedit.operation;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.IssueUtilsBean;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.fields.CommentSystemField;
import com.atlassian.jira.issue.fields.OrderableField;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.util.JiraUtils;
import com.atlassian.jira.web.action.issue.bulkedit.WorkflowTransitionKey;
import com.atlassian.jira.web.bean.BulkEditBean;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowManager;
import com.atlassian.jira.workflow.WorkflowProgressAware;
import com.atlassian.jira.workflow.WorkflowTransitionUtil;
import com.atlassian.jira.workflow.WorkflowTransitionUtilImpl;
import com.opensymphony.workflow.loader.ActionDescriptor;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class BulkWorkflowTransitionOperation extends AbstractBulkOperation implements BulkOperation
{
    protected static final Logger log = Logger.getLogger(BulkWorkflowTransitionOperation.class);

    public static final String NAME = "BulkWorkflowTransition";
    public static final String NAME_KEY = "bulk.workflowtransition.operation.name";
    private static final String DESCRIPTION_KEY = "bulk.workflowtransition.operation.description";
    private static final String CANNOT_PERFORM_MESSAGE_KEY = "bulk.workflowtransition.cannotperform";

    private final WorkflowManager workflowManager;
    private final IssueUtilsBean issueUtilsBean;
    private final FieldLayoutManager fieldLayoutManager;

    public BulkWorkflowTransitionOperation(WorkflowManager workflowManager, IssueUtilsBean issueUtilsBean, FieldLayoutManager fieldLayoutManager)
    {
        this.workflowManager = workflowManager;
        this.issueUtilsBean = issueUtilsBean;
        this.fieldLayoutManager = fieldLayoutManager;
    }

    // OPERATION METHODS -----------------------------------------------------------------------------------------------

    public boolean canPerform(final BulkEditBean bulkEditBean, final User remoteUser)
    {
        Collection selectedIssues = bulkEditBean.getSelectedIssues();

        for (Iterator iterator = selectedIssues.iterator(); iterator.hasNext();)
        {
            Issue issue = (Issue) iterator.next();

            Map availableActions = issueUtilsBean.loadAvailableActions(issue);
            if (!availableActions.values().isEmpty())
                return true;
        }

        return false;
    }

    public boolean canPerform(BulkEditBean bulkEditBean, com.opensymphony.user.User remoteUser)
    {
        return canPerform(bulkEditBean, (User) remoteUser);
    }

    public void perform(final BulkEditBean bulkEditBean, final User remoteUser) throws Exception
    {
        int actionDescriptorId = getActionDescriptor(bulkEditBean.getSelectedWFTransitionKey()).getId();
        WorkflowTransitionUtil workflowTransitionUtil = (WorkflowTransitionUtil) JiraUtils.loadComponent(WorkflowTransitionUtilImpl.class);
        workflowTransitionUtil.setAction(actionDescriptorId);

        for (Iterator iterator = bulkEditBean.getSelectedIssues().iterator(); iterator.hasNext();)
        {
            MutableIssue issue = (MutableIssue) iterator.next();
            Map additionalInputs = new HashMap();

            // Update the issue fields as required for each issue
            final Map selectedActions = bulkEditBean.getActions();
            if (selectedActions != null && selectedActions.values() != null)
            {
                for (Iterator iterator1 = bulkEditBean.getActions().values().iterator(); iterator1.hasNext();)
                {
                    BulkEditAction bulkEditAction = (BulkEditAction) iterator1.next();
                    OrderableField field = bulkEditAction.getField();
                    FieldLayoutItem fieldLayoutItem = fieldLayoutManager.getFieldLayout(issue.getProject(), issue.getIssueTypeObject().getId()).getFieldLayoutItem(field);
                    if (fieldLayoutItem != null)
                    {
                        // JRA-14178: need to specifically set comment params here so that they get picked up
                        // by the CreateCommentFunction during the workflow processing.
                        // Also, don't update the issue with the comment field, as this will cause two comments
                        // to be created.
                        if (IssueFieldConstants.COMMENT.equals(field.getId()))
                        {
                            ((CommentSystemField) field).populateAdditionalInputs(bulkEditBean.getFieldValuesHolder(), additionalInputs);
                        }
                        else
                        {
                            field.updateIssue(fieldLayoutItem, issue, bulkEditBean.getFieldValuesHolder());
                        }
                    }
                }
            }

            GenericValue projectGV = issue.getProject();
            BulkWorkflowProgressAware bulkWorkflowProgressAware = new BulkWorkflowProgressAware(remoteUser, actionDescriptorId, issue, projectGV);

            additionalInputs.put("sendBulkNotification", Boolean.valueOf(bulkEditBean.isSendBulkNotification()));
            bulkWorkflowProgressAware.setAdditionalInputs(additionalInputs);

            // Transition the issue
            workflowManager.doWorkflowAction(bulkWorkflowProgressAware);
        }
    }

    public void perform(BulkEditBean bulkEditBean, com.opensymphony.user.User remoteUser) throws Exception
    {
        perform(bulkEditBean, (User) remoteUser);
    }

    public String getNameKey()
    {
        return NAME_KEY;
    }

    public String getDescriptionKey()
    {
        return DESCRIPTION_KEY;
    }

    public String getOperationName()
    {
        return NAME;
    }

    public String getCannotPerformMessageKey()
    {
        return CANNOT_PERFORM_MESSAGE_KEY;
    }

    // OPERATION HELPER METHODS ----------------------------------------------------------------------------------------

    public ActionDescriptor getActionDescriptor(WorkflowTransitionKey workflowTransitionKey)
    {
        String workflowName = workflowTransitionKey.getWorkflowName();
        String actionDescriptorId = workflowTransitionKey.getActionDescriptorId();

        JiraWorkflow workflow = workflowManager.getWorkflow(workflowName);

        return workflow.getDescriptor().getAction(Integer.parseInt(actionDescriptorId));
    }

    // INNER CLASS -----------------------------------------------------------------------------------------------------

    // Used to perform the workflow transition
    static class BulkWorkflowProgressAware implements WorkflowProgressAware
    {
        private User remoteUser;
        private int actionId;
        private MutableIssue issue;
        private GenericValue projectGV;
        private Map additionalInputs;
        private boolean hasError;

        public BulkWorkflowProgressAware(User remoteUser, int actionId, MutableIssue issue, GenericValue projectGV)
        {
            this.remoteUser = remoteUser;
            this.actionId = actionId;
            this.issue = issue;
            this.projectGV = projectGV;
        }


        public User getRemoteUser()
        {
            return remoteUser;
        }

        public int getAction()
        {
            return actionId;
        }

        public void setAction(int action)
        {
            actionId = action;
        }

        public void addErrorMessage(String error)
        {
            hasError = true;
        }

        public void addError(String name, String error)
        {
            hasError = true;
        }

        public boolean hasError()
        {
            return hasError;
        }

        public Map getAdditionalInputs()
        {
            return additionalInputs;
        }

        public void setAdditionalInputs(Map additionalInputs)
        {
            this.additionalInputs = additionalInputs;

        }

        public MutableIssue getIssue() throws Exception
        {
            return issue;
        }

        public GenericValue getProject() throws Exception
        {
            return projectGV;
        }
    }
}
