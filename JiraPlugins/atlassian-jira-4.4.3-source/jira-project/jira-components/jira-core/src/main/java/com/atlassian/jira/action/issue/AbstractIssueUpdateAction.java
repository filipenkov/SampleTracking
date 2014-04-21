/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.action.issue;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.JiraException;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.event.issue.IssueEventSource;
import com.atlassian.jira.issue.comments.Comment;
import com.atlassian.jira.issue.util.IssueChangeHolder;
import com.atlassian.jira.issue.util.IssueUpdateBean;
import com.atlassian.jira.issue.util.IssueUpdater;
import com.atlassian.jira.issue.util.TextAnalyzer;
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
    protected final TextAnalyzer textAnalyzer;

    public AbstractIssueUpdateAction(IssueUpdater issueUpdater, TextAnalyzer textAnalyzer)
    {
        this.issueUpdater = issueUpdater;
        this.textAnalyzer = textAnalyzer;
    }

    //this is very very bad!
    protected AbstractIssueUpdateAction()
    {
        this(null, null);
    }

    public AbstractIssueUpdateAction(IssueUpdater issueUpdater)
    {
        this(issueUpdater, null);
    }

    /**
     * This method 'completes' the update of an issue entity.
     * <p/>
     * It sets the update timestamp, stores the issue, updated the cache if needed, creates the changelog and dispatches
     * the event (if desired).
     * <p/>
     * This method will ALWAYS generate an update - see also doUpdateIfNeeded.
     */
    protected void doUpdate(Long eventTypeId, Comment comment) throws Exception
    {
        IssueUpdateBean issueUpdateBean = new IssueUpdateBean(getIssue(), originalIssue, eventTypeId, getRemoteUser());
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
    protected void doUpdate(Long eventTypeId, Comment comment, Map params) throws Exception
    {
        IssueUpdateBean issueUpdateBean = new IssueUpdateBean(getIssue(), originalIssue, eventTypeId, getRemoteUser());
        issueUpdateBean.setComment(comment);
        issueUpdateBean.setParams(params);
        if (isDispatchEvent())
        {
            issueUpdateBean.setDispatchEvent(true);
        }

        issueUpdater.doUpdate(issueUpdateBean, true);

        //        doUpdate(eventType, commentGV, null, params);
    }

    protected void doUpdate(IssueUpdateBean issueUpdateBean, boolean generateChangeItems) throws JiraException
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
    protected void doUpdateWithChangelog(Long eventTypeId, List changeItems) throws JiraException
    {
        IssueUpdateBean issueUpdateBean = new IssueUpdateBean(getIssue(), originalIssue, eventTypeId, getRemoteUser());
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
            throws Exception
    {
        IssueUpdateBean issueUpdateBean = new IssueUpdateBean(getIssue(), getOriginalIssue(), eventTypeId, getRemoteUser(), sendMail, issueChangeHolder.isSubtasksUpdated());
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
            originalIssue = ManagerFactory.getIssueManager().getIssue(issue.getLong("id"));
        }

        super.setIssue(issue);
    }

    protected GenericValue getOriginalIssue()
    {
        return originalIssue;
    }
}
