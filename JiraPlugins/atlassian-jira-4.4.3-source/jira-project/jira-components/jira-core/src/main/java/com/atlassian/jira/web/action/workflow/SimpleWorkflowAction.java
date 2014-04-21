/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.workflow;

import com.atlassian.jira.bc.issue.IssueService;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.web.action.issue.AbstractIssueSelectAction;

import java.util.Collections;

/**
 * @author <a href="mailto:plightbo@hotmail.com">Pat Lightbody</a>
 */
public class SimpleWorkflowAction extends AbstractIssueSelectAction
{
    private int action;
    private final IssueService issueService;

    public SimpleWorkflowAction(final IssueService issueService)
    {
        this.issueService = issueService;
    }

    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        final IssueService.IssueResult transitionResult = issueService.transition(getRemoteUser(), new IssueService.TransitionValidationResult(getIssueObject(), this, Collections.<String, Object>emptyMap(), Collections.emptyMap(), getAction()));

        if (!transitionResult.isValid())
        {
            addErrorCollection(transitionResult.getErrorCollection());
            return ERROR;
        }

        if (isInlineDialogMode())
        {
            return returnComplete();
        }

        return redirectToView();
    }

    private int getAction()
    {
        return action;
    }

    public void setAction(int action)
    {
        this.action = action;
    }
}
