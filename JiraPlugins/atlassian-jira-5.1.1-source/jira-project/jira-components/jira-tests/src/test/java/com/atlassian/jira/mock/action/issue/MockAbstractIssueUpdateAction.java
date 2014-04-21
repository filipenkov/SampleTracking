/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.mock.action.issue;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.action.issue.AbstractIssueUpdateAction;
import com.atlassian.jira.issue.comments.Comment;

public class MockAbstractIssueUpdateAction extends AbstractIssueUpdateAction
{
    boolean shouldUpdate = true;
    boolean calledUpdate = false;

    public MockAbstractIssueUpdateAction()
    {
        super(ComponentManager.getInstance().getIssueUpdater());
    }

    public void validate()
    {
        doValidation();
    }

    protected void doUpdate(Long eventTypeId, Comment comment)
    {
        calledUpdate = true;
        if (shouldUpdate)
            super.doUpdate(eventTypeId, comment);
    }

    /**
     * @return Whether or not super.doUpdate() was called
     */
    public boolean isCalledUpdate()
    {
        return calledUpdate;
    }

    /**
     * Set this if you don't want any actual doUpdate method to be run. The call will still be logged though.
     */
    public void setShouldUpdate(boolean shouldUpdate)
    {
        this.shouldUpdate = shouldUpdate;
    }

    public void mockDoUpdate(Long eventTypeId) throws Exception
    {
        doUpdate(eventTypeId, null);
    }
}
