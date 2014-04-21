/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.event.listeners.search;

import com.atlassian.core.action.ActionUtils;
import com.atlassian.core.ofbiz.CoreFactory;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.action.ActionNames;
import com.atlassian.jira.action.admin.ListenerCreate;
import com.atlassian.jira.event.issue.AbstractIssueEventListener;
import com.atlassian.jira.event.issue.IssueEvent;
import com.atlassian.jira.issue.Issue;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericValue;
import webwork.dispatcher.ActionResult;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * This listener updates the search index within JIRA.
 * <p>
 * Do not delete ;)
 */
public class IssueIndexListener extends AbstractIssueEventListener
{
    private static final Logger log = org.apache.log4j.Logger.getLogger(IssueIndexListener.class);

    @Override
    public void init(final Map params)
    {}

    @Override
    public String[] getAcceptedParams()
    {
        return new String[0];
    }

    /**
     * Should not be deleted - required for JIRA operation
     */
    @Override
    public boolean isInternal()
    {
        return true;
    }

    @Override
    public void issueCreated(final IssueEvent event)
    {
        reIndex(event);
    }

    @Override
    public void issueUpdated(final IssueEvent event)
    {
        reIndex(event);
    }

    @Override
    public void issueAssigned(final IssueEvent event)
    {
        reIndex(event);
    }

    @Override
    public void issueResolved(final IssueEvent event)
    {
        reIndex(event);
    }

    @Override
    public void issueClosed(final IssueEvent event)
    {
        reIndex(event);
    }

    @Override
    public void issueCommented(final IssueEvent event)
    {
        reIndex(event);
    }

    @Override
    public void issueWorkLogged(final IssueEvent event)
    {
        reIndex(event);
    }

    @Override
    public void issueReopened(final IssueEvent event)
    {
        reIndex(event);
    }

    @Override
    public void issueGenericEvent(final IssueEvent event)
    {
        reIndex(event);
    }

    @Override
    public void issueCommentEdited(final IssueEvent event)
    {
        reIndex(event);
    }

    @Override
    public void issueWorklogUpdated(final IssueEvent event)
    {
        reIndex(event);
    }

    @Override
    public void issueWorklogDeleted(final IssueEvent event)
    {
        reIndex(event);
    }

    @Override
    public void issueDeleted(final IssueEvent event)
    {}

    @Override
    public void issueMoved(final IssueEvent event)
    {
        reIndex(event);
    }

    @Override
    public void customEvent(final IssueEvent event)
    {}

    private static void reIndex(final IssueEvent issueEvent)
    {
        final Set issuesToReindex = new HashSet();
        final Issue issue = issueEvent.getIssue();
        issuesToReindex.add(issue);

        //if there are any subtasks that were modified as part of an issue operation (e.g. editing a parent issue's
        //security level) ensure that they are also re-indexed.
        if (issueEvent.isSubtasksUpdated())
        {
            issuesToReindex.addAll(issue.getSubTaskObjects());
        }
        try
        {
            ManagerFactory.getIndexManager().reIndexIssueObjects(issuesToReindex);
        }
        catch (final Throwable e) //catch everything here to make sure it doesn't bring server down.
        {
            log.error("Error re-indexing changes for issue '" + issue.getKey() + "'", e);
        }
    }

    /**
     * A utility method to create this listener
     *
     * @see ListenerCreate
     */
    public static GenericValue create() throws Exception
    {
        final ActionResult aResult = CoreFactory.getActionDispatcher().execute(ActionNames.LISTENER_CREATE,
            EasyMap.build("name", "Issue Index Listener", "clazz", "com.atlassian.jira.event.listeners.search.IssueIndexListener"));
        ActionUtils.checkForErrors(aResult);

        return ((ListenerCreate) aResult.getFirstAction()).getListenerConfig();
    }

    /**
     * A utility method to remove this listener
     *
     * @throws Exception
     */
    public static void remove()
    {
        try
        {
            final ActionResult aResult = CoreFactory.getActionDispatcher().execute(ActionNames.LISTENER_DELETE,
                EasyMap.build("clazz", IssueIndexListener.class.getName()));
            ActionUtils.checkForErrors(aResult);
        }
        catch (final Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * As we wish to only have one IssueIndexListener at a time - enforce uniqueness
     */
    @Override
    public boolean isUnique()
    {
        return true;
    }

    @Override
    public String getDescription()
    {
        return getI18NBean().getText("admin.listener.issue.index.desc");
    }
}
