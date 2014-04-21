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

    @Override
    public ActionDescriptor getActionDescriptor()
    {
        return actionDescriptor;
    }

    @Override
    public String getNameKey()
    {
        return operation.getNameKey();
    }

    @Override
    public String getDescriptionKey()
    {
        return operation.getDescriptionKey();
    }

    @Override
    public boolean canPerform(final BulkEditBean bulkEditBean, final User remoteUser)
    {
        return operation.canPerform(bulkEditBean, remoteUser);
    }

    @Override
    public void perform(final BulkEditBean bulkEditBean, final User remoteUser) throws Exception
    {
        operation.perform(bulkEditBean, remoteUser);
    }

    @Override
    public String getOperationName()
    {
        return operation.getOperationName();
    }

    @Override
    public String getCannotPerformMessageKey()
    {
        return operation.getCannotPerformMessageKey();
    }
}
