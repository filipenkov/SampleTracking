/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.action.issue;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.JiraException;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.event.issue.IssueEventSource;
import com.atlassian.jira.issue.comments.Comment;
import com.atlassian.jira.issue.history.ChangeItemBean;
import com.atlassian.jira.issue.util.IssueChangeHolder;
import com.atlassian.jira.issue.util.IssueUpdateBean;
import com.atlassian.jira.issue.util.IssueUpdater;
import org.ofbiz.core.entity.GenericValue;

import java.util.List;
import java.util.Map;

/**
 * This action should be subclassed by any action which changes issues and might want to generate a changelog.
 * <p/>
 * It automatically stores a copy of the original issue, so that changelogs can be created easily using
 * createChangeLog()
 */
public abstract class AbstractIssueUpdateAction extends AbstractIssueAction
{
    private GenericValue originalIssue;
    private final IssueUpdater issueUpdater;

    public AbstractIssueUpdateAction(IssueUpdater issueUpdater)
    {
        this.issueUpdater = issueUpdater;
    }

    //this is very very bad!
    protected AbstractIssueUpdateAction()
    {
        this(null);
    }

    /**
     * This method 'completes' the update of an issue entity.
     * <p/>
     * It sets the update timestamp, stores the issue, updated the cache if needed, creates the changelog and dispatches
     * the event (if desired).
     * <p/>
     * This method will ALWAYS generate an update - see also doUpdateIfNeeded.
     */
    protected void doUpdate(Long eventTypeId, Comment comment)
    {
        IssueUpdateBean issueUpdateBean = new IssueUpdateBean(getIssue(), originalIssue, eventTypeId, getLoggedInUser());
        issueUpdateBean.setComment(comment);
        if (isDispatchEvent())
        {
            issueUpdateBean.setDispatchEvent(true);
        }
        issueUpdater.doUpdate(issueUpdateBean, true);

        //        doUpdate(eventType, commentGV, null, null);
    }

    /**
     * This method 'completes' the update of an issue entity.
     * <p/>
     * It sets the update timestamp, stores the issue, updated the cache if needed, creates the changelog and dispatches
     * the event (if desired).
     * <p/>
     * This method will ALWAYS generate an update - see also doUpdateIfNeeded.
     */
    protected void doUpdate(Long eventTypeId, Comment comment, Map params)
    {
        IssueUpdateBean issueUpdateBean = new IssueUpdateBean(getIssue(), originalIssue, eventTypeId, getLoggedInUser());
        issueUpdateBean.setComment(comment);
        issueUpdateBean.setParams(params);
        if (isDispatchEvent())
        {
            issueUpdateBean.setDispatchEvent(true);
        }

        issueUpdater.doUpdate(issueUpdateBean, true);

        //        doUpdate(eventType, commentGV, null, params);
    }

    protected void doUpdate(IssueUpdateBean issueUpdateBean, boolean generateChangeItems)
    {
        issueUpdater.doUpdate(issueUpdateBean, generateChangeItems);
    }

    /**
     * This method 'completes' the update of an issue entity.
     * <p/>
     * It sets the update timestamp, stores the issue, updated the cache if needed, creates the changelog and dispatches
     * the event (if desired).
     * <p/>
     * This method will ALWAYS generate an update - see also doUpdateIfNeeded.
     */
    protected void doUpdateWithChangelog(Long eventTypeId, List<ChangeItemBean> changeItems)
    {
        IssueUpdateBean issueUpdateBean = new IssueUpdateBean(getIssue(), originalIssue, eventTypeId, getLoggedInUser());
        issueUpdateBean.setChangeItems(changeItems);
        if (isDispatchEvent())
        {
            issueUpdateBean.setDispatchEvent(true);
        }

        issueUpdater.doUpdate(issueUpdateBean, true);

        //        doUpdate(eventType, null, changeItems);
    }

    /**
     * This method 'completes' the update of an issue entity, given part of the changelog being prewritten as a
     * StringBuffer.
     * <p/>
     * It sets the update timestamp, stores the issue, updated the cache if needed, creates the changelog and dispatches
     * the event (if desired).
     * <p/>
     * This method will ALWAYS generate an update - see also doUpdateIfNeeded.
     */
    protected void doUpdate(Long eventTypeId, IssueChangeHolder issueChangeHolder, boolean generateChangeItems, boolean sendMail)
    {
        IssueUpdateBean issueUpdateBean = new IssueUpdateBean(getIssue(), getOriginalIssue(), eventTypeId, getLoggedInUser(), sendMail, issueChangeHolder.isSubtasksUpdated());
        issueUpdateBean.setComment(issueChangeHolder.getComment());
        issueUpdateBean.setChangeItems(issueChangeHolder.getChangeItems());
        issueUpdateBean.setDispatchEvent(isDispatchEvent());
        issueUpdateBean.setParams(EasyMap.build("eventsource", IssueEventSource.ACTION));
        issueUpdater.doUpdate(issueUpdateBean, generateChangeItems);
    }

    /**
     * Here we override the AbstractGVIssueAction.setIssue() method and store a clone of the original issue.
     * <p/>
     * This means we can automatically generate changelogs - nice!
     */
    public void setIssue(GenericValue issue)
    {
        if (originalIssue == null)
        {
            originalIssue = ComponentAccessor.getIssueManager().getIssue(issue.getLong("id"));
        }

        super.setIssue(issue);
    }

    protected GenericValue getOriginalIssue()
    {
        return originalIssue;
    }
}
