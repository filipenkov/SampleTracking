/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.event.issue;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.comments.Comment;
import com.atlassian.jira.issue.worklog.Worklog;
import com.atlassian.jira.util.ImportUtils;
import org.ofbiz.core.entity.GenericValue;

import java.util.HashMap;
import java.util.Map;

/**
 * Static utility to dispatch issue events.
 *
 * TODO migrate not-yet-deprecated methods to {@link com.atlassian.jira.event.issue.IssueEventManager} (and deprecate).
 */
public class IssueEventDispatcher
{
    public static void dispatchEvent(Long eventTypeId, Issue issue, User remoteUser)
    {
        dispatchEvent(eventTypeId, issue, new HashMap(), remoteUser);
    }

    /**
     *
     * @deprecated Since v4.3
     */
    public static void dispatchEvent(Long eventTypeId, Issue issue, com.opensymphony.user.User remoteUser)
    {
        dispatchEvent(eventTypeId, issue, new HashMap(), (User) remoteUser);
    }

    /**
     * Use {@link com.atlassian.jira.event.issue.IssueEventManager}.
     *
     * @param eventTypeId type of event
     * @param issue affected issue
     * @param remoteUser user initiating the event
     * @param sendMail whether or not a mail notification should be sent
     *
     * @deprecated use {@link com.atlassian.jira.event.issue.IssueEventManager#dispatchEvent(Long,
     * com.atlassian.jira.issue.Issue, com.atlassian.crowd.embedded.api.User, boolean)} instead.
     */
    public static void dispatchEvent(Long eventTypeId, Issue issue, User remoteUser, boolean sendMail)
    {
        dispatchEvent(eventTypeId, issue, new HashMap(), remoteUser, sendMail);
    }

    /**
     * Dispatch event allowing sendMail configuration
     *
     * Use sendMail to disable mail delivery for an event notification - e.g. bulk operations
     *
     * @param eventTypeId   eventTypeId
     * @param issue         Issue
     * @param remoteUser    User
     * @param sendMail configure if mail is sent or not
     *
     * @deprecated Since v4.3
     */
    public static void dispatchEvent(Long eventTypeId, Issue issue, com.opensymphony.user.User remoteUser, boolean sendMail)
    {
        dispatchEvent(eventTypeId, issue, new HashMap(), (User) remoteUser, sendMail);
    }


    public static void dispatchEvent(Long eventTypeId, Issue issue, Map params, User remoteUser)
    {
        Map<String, Object> copyOfParams = copyParams(params);
        putBaseUrlIntoPlay(copyOfParams);

        dispatchEvent(new IssueEvent(issue, copyOfParams, remoteUser, eventTypeId));
    }

    /**
     * @deprecated Since v4.3
     */
    public static void dispatchEvent(Long eventTypeId, Issue issue, Map params, com.opensymphony.user.User remoteUser)
    {
        Map<String, Object> copyOfParams = copyParams(params);
        putBaseUrlIntoPlay(copyOfParams);

        dispatchEvent(new IssueEvent(issue, copyOfParams, (User) remoteUser, eventTypeId));
    }

    /**
     * Use {@link com.atlassian.jira.event.issue.IssueEventManager}.
     *
     * @param eventTypeId type of event
     * @param issue affected issue
     * @param params custom event parameters
     * @param remoteUser user initiating the event
     * @param sendMail whether or not a mail notification should be sent
     *
     * @deprecated use {@link com.atlassian.jira.event.issue.IssueEventManager#dispatchEvent(Long,
     * com.atlassian.jira.issue.Issue, java.util.Map, com.atlassian.crowd.embedded.api.User, boolean)} instead.
     */
    public static void dispatchEvent(Long eventTypeId, Issue issue, Map params, User remoteUser, boolean sendMail)
    {
        Map<String, Object> copyOfParams = copyParams(params);
        putBaseUrlIntoPlay(copyOfParams);

        dispatchEvent(new IssueEvent(issue, copyOfParams, remoteUser, eventTypeId, sendMail));
    }

    /**
     *
     * @deprecated Since v4.3
     */
    public static void dispatchEvent(Long eventTypeId, Issue issue, Map params, com.opensymphony.user.User remoteUser, boolean sendMail)
    {
        Map<String, Object> copyOfParams = copyParams(params);
        putBaseUrlIntoPlay(copyOfParams);

        dispatchEvent(new IssueEvent(issue, copyOfParams, (User) remoteUser, eventTypeId, sendMail));
    }

    public static void dispatchEvent(Long eventTypeId, Issue issue, User remoteUser, Comment comment, Worklog worklog, GenericValue changelog)
    {
        Map<String, Object> copyOfParams = new HashMap<String, Object>();
        putBaseUrlIntoPlay(copyOfParams);
        dispatchEvent(eventTypeId, issue, remoteUser, comment, worklog, changelog, copyOfParams);
    }

    /**
     * @deprecated Since v4.3
     */
    public static void dispatchEvent(Long eventTypeId, Issue issue, com.opensymphony.user.User remoteUser, Comment comment, Worklog worklog, GenericValue changelog)
    {
        Map<String, Object> copyOfParams = new HashMap<String, Object>();
        putBaseUrlIntoPlay(copyOfParams);
        dispatchEvent(eventTypeId, issue, (User) remoteUser, comment, worklog, changelog, copyOfParams);
    }

    public static void dispatchEvent(Long eventTypeId, Issue issue, User remoteUser, Comment comment, Worklog worklog, GenericValue changelog, boolean sendMail)
    {
        Map<String, Object> copyOfParams = new HashMap<String, Object>();
        putBaseUrlIntoPlay(copyOfParams);
        dispatchEvent(eventTypeId, issue, remoteUser, comment, worklog, changelog, copyOfParams, sendMail);
    }

    /**
     * @deprecated Since v4.3
     */
    public static void dispatchEvent(Long eventTypeId, Issue issue, com.opensymphony.user.User remoteUser, Comment comment, Worklog worklog, GenericValue changelog, boolean sendMail)
    {
        Map<String, Object> copyOfParams = new HashMap<String, Object>();
        putBaseUrlIntoPlay(copyOfParams);
        dispatchEvent(eventTypeId, issue, (User) remoteUser, comment, worklog, changelog, copyOfParams, sendMail);
    }

    public static void dispatchEvent(Long eventTypeId, Issue issue, User remoteUser, Comment comment, Worklog worklog, GenericValue changelog, Map params)
    {
        Map<String, Object> copyOfParams = copyParams(params);
        putBaseUrlIntoPlay(copyOfParams);
        dispatchEvent(new IssueEvent(issue, remoteUser, comment, worklog, changelog, copyOfParams, eventTypeId));
    }

    /**
     * @deprecated Since v4.3
     */
    public static void dispatchEvent(Long eventTypeId, Issue issue, com.opensymphony.user.User remoteUser, Comment comment, Worklog worklog, GenericValue changelog, Map params)
    {
        Map<String, Object> copyOfParams = copyParams(params);
        putBaseUrlIntoPlay(copyOfParams);
        dispatchEvent(new IssueEvent(issue, remoteUser, comment, worklog, changelog, copyOfParams, eventTypeId));
    }

    public static void dispatchEvent(Long eventTypeId, Issue issue, User remoteUser, Comment comment, Worklog worklog, GenericValue changelog, Map params, boolean sendMail)
    {
        Map<String, Object> copyOfParams = copyParams(params);
        putBaseUrlIntoPlay(copyOfParams);
        dispatchEvent(new IssueEvent(issue, remoteUser, comment, worklog, changelog, copyOfParams, eventTypeId, sendMail));
    }

    /**
     * @deprecated Since v4.3
     */
    public static void dispatchEvent(Long eventTypeId, Issue issue, com.opensymphony.user.User remoteUser, Comment comment, Worklog worklog, GenericValue changelog, Map params, boolean sendMail)
    {
        Map<String, Object> copyOfParams = copyParams(params);
        putBaseUrlIntoPlay(copyOfParams);
        dispatchEvent(new IssueEvent(issue, (User) remoteUser, comment, worklog, changelog, copyOfParams, eventTypeId, sendMail));
    }

    public static void dispatchEvent(Long eventTypeId, Issue issue, User remoteUser, GenericValue changelog,
                                        boolean sendMail, boolean subtasksUpdated)
    {
        dispatchEvent(eventTypeId, issue, remoteUser, null, null, changelog, null, sendMail, subtasksUpdated);
    }

    /**
     * @deprecated Since v4.3
     */
    public static void dispatchEvent(Long eventTypeId, Issue issue, com.opensymphony.user.User remoteUser, GenericValue changelog,
                                        boolean sendMail, boolean subtasksUpdated)
    {
        dispatchEvent(eventTypeId, issue, (User) remoteUser, null, null, changelog, null, sendMail, subtasksUpdated);
    }


    public static void dispatchEvent(Long eventTypeId, Issue issue, User remoteUser, Comment comment, Worklog worklog,
                                          GenericValue changelog, Map params, boolean sendMail, boolean subtasksUpdated)
    {
        Map<String, Object> copyOfParams = copyParams(params);
        putBaseUrlIntoPlay(copyOfParams);
        dispatchEvent(new IssueEvent(issue, remoteUser, comment, worklog, changelog, copyOfParams, eventTypeId, sendMail, subtasksUpdated));
    }

    /**
     * @deprecated Since v4.3
     */
    public static void dispatchEvent(Long eventTypeId, Issue issue, com.opensymphony.user.User remoteUser, Comment comment, Worklog worklog,
                                          GenericValue changelog, Map params, boolean sendMail, boolean subtasksUpdated)
    {
        Map<String, Object> copyOfParams = copyParams(params);
        putBaseUrlIntoPlay(copyOfParams);
        dispatchEvent(new IssueEvent(issue, remoteUser, comment, worklog, changelog, copyOfParams, eventTypeId, sendMail, subtasksUpdated));
    }

    private static Map<String, Object> copyParams(final Map params)
    {
        Map<String,Object> copyOfParams = new HashMap<String,Object>();
        if (params != null)
        {
            copyOfParams.putAll(params);
        }
        return copyOfParams;
    }

    private static void putBaseUrlIntoPlay(final Map<String, Object> params)
    {
        params.put("baseurl", ManagerFactory.getApplicationProperties().getString(APKeys.JIRA_BASEURL));
    }

    /**
     * Notifies registered IssueEventListeners of the given event.
     * @param event the event.
     */
    public static void dispatchEvent(IssueEvent event)
    {
        if (ImportUtils.isEnableNotifications())
        {
            ComponentManager.getComponentInstanceOfType(EventPublisher.class).publish(event);
        }
    }
}
