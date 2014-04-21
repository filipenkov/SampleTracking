package com.atlassian.jira.issue.operation;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bulkedit.operation.BulkOperation;
import com.atlassian.jira.web.bean.BulkEditBean;
import com.opensymphony.workflow.loader.ActionDescriptor;

/**
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
public class BulkTransitionIssueOperation implements WorkflowIssueOperation, BulkOperation
{
    private final BulkOperation operation;
    private final ActionDescriptor actionDescriptor;

    public BulkTransitionIssueOperation(BulkOperation operation, ActionDescriptor actionDescriptor)
    {
        this.operation = operation;
        this.actionDescriptor = actionDescriptor;
    }

    public ActionDescriptor getActionDescriptor()
    {
        return actionDescriptor;
    }

    public String getNameKey()
    {
        return operation.getNameKey();
    }

    public String getDescriptionKey()
    {
        return operation.getDescriptionKey();
    }

    public boolean canPerform(final BulkEditBean bulkEditBean, final User remoteUser)
    {
        return operation.canPerform(bulkEditBean, remoteUser);
    }

    public boolean canPerform(BulkEditBean bulkEditBean, com.opensymphony.user.User remoteUser)
    {
        return operation.canPerform(bulkEditBean, remoteUser);
    }

    public void perform(final BulkEditBean bulkEditBean, final User remoteUser) throws Exception
    {
        operation.perform(bulkEditBean, remoteUser);
    }

    public void perform(BulkEditBean bulkEditBean, com.opensymphony.user.User remoteUser) throws Exception
    {
        operation.perform(bulkEditBean, remoteUser);
    }

    public String getOperationName()
    {
        return operation.getOperationName();
    }

    public String getCannotPerformMessageKey()
    {
        return operation.getCannotPerformMessageKey();
    }
}
